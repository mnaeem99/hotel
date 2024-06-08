package com.my.hotel.server.service.user.saveUser

import com.fasterxml.jackson.databind.JsonNode
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.my.hotel.server.commons.Constants
import com.my.hotel.server.commons.FileUpload
import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.dto.request.UpdateUser
import com.my.hotel.server.graphql.dto.request.UserAuthInput
import com.my.hotel.server.graphql.dto.request.UserInput
import com.my.hotel.server.graphql.dto.response.UserDto
import com.my.hotel.server.graphql.error.ExecutionAbortedCustomException
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.graphql.error.ValidationErrorCustomException
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.service.aws.AWSService
import com.my.hotel.server.service.event.EventService
import com.my.hotel.server.service.event.dto.Event
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter


@Service
@Validated
class SaveUserService @Autowired constructor(
    val confirmationCodeRepository: ConfirmationCodeRepository,
    val userRepository: UserRepository,
    val userAuthenticationRepository: UserAuthenticationRepository,
    val dateProvider: DateProvider,
    val awsService: AWSService,
    var passwordEncoder: PasswordEncoder,
    var imageRepository: ImageRepository,
    val favoriteRepository: FavoriteRepository,
    val wishListRepository: WishListRepository,
    val notificationRepository: NotificationRepository,
    val loyaltyPointRepository: LoyaltyPointRepository,
    val qrCodeRepository: QRCodeRepository,
    val orderRepository: OrderRepository,
    val pointsHistoryRepository: PointsHistoryRepository,
    val autoCompleteQueryRepository: AutoCompleteQueryRepository,
    val searchQueryRepository: SearchQueryRepository,
    val refreshTokenRepository: RefreshTokenRepository,
    val hotelPriceRangeRepository: HotelPriceRangeRepository,
    val statusHistoryRepository: StatusHistoryRepository,
    val countryRepository: CountryRepository,
    val translationService: TranslationService,
    val deviceLocationRepository: DeviceLocationRepository,
    val eventService: EventService,
    val deviceTokenRepository: UserDeviceTokenRepository, private val suggestionRepository: SuggestionRepository
): ISaveUserService {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun saveUser(type:UserAuthentication.Type, email:String?, phone:String?): ConfirmationCode {
        val user = userRepository.save(User())
        val userAuthentication = if (type == UserAuthentication.Type.EMAIL) {
            UserAuthentication(type, email, user = user, verified = false)
        } else {
            UserAuthentication(type, null, phone, user = user, verified = false)
        }
        val newUserAuth = userAuthenticationRepository.save(userAuthentication)
        val confirmationCode = generateConfirmationCode()
        confirmationCode.userAuth = newUserAuth
        logger.info("New user added in the database: ${user.id}")
        return confirmationCodeRepository.save(confirmationCode)
    }
    override fun verifyCode(verificationCode: ConfirmationCode, code: Int): Boolean{
        if(verificationCode.code == code && verificationCode.expiry.isAfter(dateProvider.getCurrentDateTime())) {
            val userAuth = verificationCode.userAuth
            val oldAuth = userAuth?.user?.auths?.find { auth -> auth.type == userAuth.type && ( auth.email != userAuth.email || auth.phone != userAuth.phone ) }
            if (oldAuth != null){
                deleteUserAuthentication(oldAuth)
            }
            if(userAuth != null) {
                userAuth.verified = true
                userAuthenticationRepository.save(userAuth)
                verificationCode.userAuth = null
                confirmationCodeRepository.save(verificationCode)
                confirmationCodeRepository.delete(verificationCode)
                return true
            }
            return false
        }
        throw ValidationErrorCustomException(Constants.INVALID_VERIFICATION_CODE)
    }

    override fun deleteUserAuthentication(auth: UserAuthentication) {
        val oldCodes = confirmationCodeRepository.findByUserAuth(auth.id!!)
        oldCodes?.forEach { oldCode ->
            oldCode.userAuth = null
            confirmationCodeRepository.save(oldCode)
            confirmationCodeRepository.delete(oldCode)
        }
        auth.id?.let { userAuthenticationRepository.deleteByAuthId(it) }
    }

    fun generateConfirmationCode(): ConfirmationCode {
//        val random = SecureRandom()
//        val bytes = ByteArray(20)
//        random.nextBytes(bytes)
//        val code = random.nextInt(1000000)
        val code = 1234
        val expiryDate = dateProvider.getCurrentDateTime().plusHours(1)
        return ConfirmationCode(code, expiryDate)
    }
    /**
     * add new User.
     * @param input of the new user
     */
    override fun addUser(input: UserInput): UserDto? {
        val user = User(input.firstName, input.lastName)
        if(input.nickName != null) {
            user.nickName = input.nickName
        }
        if(input.dob != null) {
            user.dob = input.dob
        }
        if (user.nickName == null){
            user.nickName = createNickName(user.firstName + user.lastName)
        }
        if (input.bio != null) {
            user.bio = input.bio
        }
        if (input.userType != null) {
            user.userType = input.userType
        }
        if (input.language != null) {
            user.language = input.language
        }
        if (input.isPrivate != null) {
            user.isPrivate = input.isPrivate
        }
        if (input.isChef != null) {
            user.isChef = input.isChef
        }
        if (input.countryId != null) {
            val country = countryRepository.findByIdOrNull(input.countryId!!) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "countryId")
            user.country = country
        }
        if(input.photo != null) {
            // upload to s3
            if(input.photo?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri = awsService.savePicture(
                input.photo!!.content,
                "${user.nickName}-profile",
                input.photo!!.contentType,
            )
            // save to db
            val newImage = Image(photoUri?.toURL(),null)
            imageRepository.save(newImage)
            user.photo = newImage
        }
        logger.info("New User: ${user.firstName}-${user.lastName} added")
        val newUser = userRepository.save(user)
        input.auths.stream().forEach { auth -> addUserAuth(auth, newUser) }
        return translationService.mapUserDto(newUser, input.language)
    }

    /**
     * update User.
     * @param input of the update user
     */
    override fun updateUser(input: UpdateUser): UserDto? {
        val user = userRepository.findByIdOrNull(input.userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "userId")
        if(!input.auths.isNullOrEmpty()) {
            input.auths.stream().forEach { auth -> updateUserAuth(auth, user) }
        }
        if(input.firstName != null) {
            user.firstName = input.firstName
        }
        if(input.lastName != null) {
            user.lastName = input.lastName
        }
        if(input.nickName != null) {
            user.nickName = input.nickName
        }
        if(input.dob != null) {
            user.dob = input.dob
        }
        if (user.nickName == null){
            user.nickName = createNickName(user.firstName + user.lastName)
        }
        if (input.bio != null) {
            user.bio = input.bio
        }
        if (input.userType != null) {
            user.userType = input.userType
        }
        if (input.language != null) {
            user.language = input.language
        }
        if (input.isPrivate != null) {
            user.isPrivate = input.isPrivate
        }
        if (input.isChef != null) {
            user.isChef = input.isChef
        }
        if (input.countryId != null) {
            val country = countryRepository.findByIdOrNull(input.countryId!!) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "countryId")
            user.country = country
        }
        if(input.photo != null) {
            // upload to s3
            val newImage = updateImage(input.photo!!, user)
            if (newImage!=null) {
                user.photo = newImage
            }
        }
        logger.info("New User: ${user.firstName}-${user.lastName} added")
        return translationService.mapUserDto(userRepository.save(user), input.language)
    }
    fun updateImage(profilePicture: FileUpload, user: User): Image? {
        if (profilePicture.content.size > (5 * 1000000)) {
            throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
        }
        val photoUri = awsService.savePicture(
            profilePicture.content,
            "${user.id}-profile",
            profilePicture.contentType,
        )
        // save to db
        if (user.photo?.id == null) {
            val newImage = Image(photoUri?.toURL(), null)
            return imageRepository.save(newImage)
        } else {
            val image = imageRepository.findByIdOrNull(user.photo!!.id) ?: return null
            image.imageUrl = photoUri?.toURL()
            return imageRepository.save(image)
        }
    }

    /**
     * delete User.
     * @param id of the user
     */
    override fun deleteUser(id: Long): Boolean {
        val user = userRepository.findByIdOrNull(id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "id")
        try {
            favoriteRepository.deleteByUser(user)
            wishListRepository.deleteByUser(user)
            loyaltyPointRepository.deleteByUser(user)
            qrCodeRepository.deleteByUser(user.id!!)
            orderRepository.deleteByUser(user)
            pointsHistoryRepository.deleteByUser(user)
            autoCompleteQueryRepository.updateByUser(user.id!!)
            searchQueryRepository.updateByUser(user.id!!)
            hotelPriceRangeRepository.deleteByUser(user)
            statusHistoryRepository.deleteByUser(user)
            notificationRepository.deleteByUser(user.id)
            notificationRepository.deleteByContent(user)
            refreshTokenRepository.deleteByUser(user)
            deviceLocationRepository.deleteByUser(user)
            deviceTokenRepository.deleteByUser(user)
            suggestionRepository.deleteByUser(user)
            userRepository.deleteBlockUser(user.id)
            userRepository.deleteById(user.id!!)
            logger.info("user:${user.id} - ${user.firstName+" "+user.lastName} deleted.")
            return true
        }catch (e: Exception){
            logger.error("Exception while delete user:${user.id} - ${user.firstName+" "+user.lastName} Profile")
            throw ExecutionAbortedCustomException("Error while delete user profile")
        }

    }


    private fun addUserAuth(input: UserAuthInput, user: User): UserAuthentication {
        val userAuth = UserAuthentication(input.type, user = user)
        if (input.email != null) {
            userAuth.email = input.email
        }
        if (input.phone != null) {
            userAuth.phone = input.phone
        }
        if (input.password != null) {
            userAuth.password = passwordEncoder.encode(input.password)
        }
        if (input.googleId != null) {
            userAuth.googleId = input.googleId
        }
        if (input.facebookId != null) {
            userAuth.facebookId = input.facebookId
        }
        if (input.appleId != null) {
            userAuth.appleId = input.appleId
        }
        if (input.verified != null) {
            userAuth.verified = input.verified
        }
        return userAuthenticationRepository.save(userAuth)
    }
    private fun updateUserAuth(input: UserAuthInput, user: User): UserAuthentication {
        val userAuth = user.auths?.find { auth -> auth.type == input.type } ?: return addUserAuth(input, user)
        if (input.email != null) {
            userAuth.email = input.email
        }
        if (input.phone != null) {
            userAuth.phone = input.phone
        }
        if (input.password != null) {
            userAuth.password = passwordEncoder.encode(input.password)
        }
        if (input.googleId != null) {
            userAuth.googleId = input.googleId
        }
        if (input.facebookId != null) {
            userAuth.facebookId = input.facebookId
        }
        if (input.appleId != null) {
            userAuth.appleId = input.appleId
        }
        if (input.verified != null) {
            userAuth.verified = input.verified
        }
        return userAuth
    }

    override fun createNickName(name: String): String {
        val matchedUsers = userRepository.searchByNickName(name)
        if (matchedUsers.isNullOrEmpty())
            return name
        var nickNameNumbers: List<Int> = emptyList()
        for (matchUser in matchedUsers) {
            val nickNameNumber = matchUser.nickName?.substring(name.length)?.filter { it.isDigit() }
            if (nickNameNumber?.isNotEmpty() == true) {
                nickNameNumbers = nickNameNumbers.plus(nickNameNumber.toInt())
            }
        }
        val newNickNameNumber = nickNameNumbers.maxOrNull()?.inc() ?: 1
        return name+newNickNameNumber
    }
    override fun createGoogleUser(payload: GoogleIdToken.Payload): User {
        if (payload.email != null) {
            val foundUserAuth = userAuthenticationRepository.findByEmail(payload.email)
            if (foundUserAuth != null) {
                if (foundUserAuth.verified == true) {
                    val foundUser = userRepository.findByEmail(payload.email)
                    val userAuth1 = UserAuthentication(UserAuthentication.Type.GOOGLE, googleId = payload.subject, user = foundUser!!, verified = true)
                    userAuthenticationRepository.save(userAuth1)
                    return foundUser
                }
                deleteUserAuthentication(foundUserAuth)
            }
        }
        val pictureUrl = payload["picture"] as String?
        val familyName = payload["family_name"] as String?
        val givenName = payload["given_name"] as String?
        val user = User()
        if (givenName!=null){
            user.firstName = givenName
        }
        if (familyName!=null){
            user.lastName = familyName
        }
        if (givenName!=null || familyName!=null){
            user.nickName = createNickName(givenName+familyName)
        }
        if (pictureUrl!=null){
            val image = imageRepository.save(Image(imageUrl = URL(pictureUrl)))
            user.photo = image
        }
        val newUser = userRepository.save(user)
        val userAuth1 = UserAuthentication(UserAuthentication.Type.GOOGLE, googleId = payload.subject, user = newUser, verified = true)
        userAuthenticationRepository.save(userAuth1)
        if (payload.email!=null) {
            val userAuth2 = UserAuthentication(UserAuthentication.Type.EMAIL, email = payload.email, user = newUser, verified = true)
            userAuthenticationRepository.save(userAuth2)
        }
        logger.info("New user using google signup added in the database: ${newUser.id}")
        return newUser
    }
    override fun createFacebookUser(response: JsonNode): User {
        val facebookId = response.findValue("id")?.asText()
        val email = response.findValue("email")?.asText()
        val firstName = response.findValue("first_name")?.asText()
        val lastName = response.findValue("last_name")?.asText()
        val birthday = response.findValue("birthday")?.asText()
        val picture = response.findValue("picture")
        val friends = response.findValue("friends")
        if (email != null) {
            val foundUserAuth = userAuthenticationRepository.findByEmail(email)
            if (foundUserAuth!=null){
                if (foundUserAuth.verified == true) {
                    val foundUser = userRepository.findByEmail(email)
                    val userAuth = UserAuthentication(UserAuthentication.Type.FACEBOOK, facebookId = facebookId, user = foundUser!!, verified = true)
                    userAuthenticationRepository.save(userAuth)
                    return foundUser
                }
                deleteUserAuthentication(foundUserAuth)
            }
        }
        val createUser = User(firstName, lastName)
        if (birthday != null) {
            createUser.dob = LocalDate.parse(birthday, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        }
        if (firstName!=null || lastName!=null){
            createUser.nickName = createNickName(firstName + lastName)
        }
        if (picture!=null){
            val data = picture.get("data")
            val isSilhouette = data?.get("is_silhouette")?.asBoolean()
            if (isSilhouette==false){
                val pictureUrl = data.get("url")?.asText()
                val image = imageRepository.save(Image(imageUrl = URL(pictureUrl)))
                createUser.photo = image
            }
        }
        val newUser = userRepository.save(createUser)
        val userAuth = UserAuthentication(UserAuthentication.Type.FACEBOOK, facebookId = facebookId, user = newUser, verified = true)
        userAuthenticationRepository.save(userAuth)
        if (email != null) {
            val userAuth2 = UserAuthentication(UserAuthentication.Type.EMAIL, email = email, user = newUser, verified = true)
            userAuthenticationRepository.save(userAuth2)
        }
        if (friends!=null){
            val facebookIds = getFacebookFriends(friends)
            if (facebookIds.isNotEmpty()){
                eventService.createEvent(Event(NotificationType.FRIEND_JOINED, newUser.id, newUser.id, null, facebookIds = facebookIds))
            }
        }
        logger.info("New user using facebook signup added in the database: ${newUser.id}")
        return newUser
    }

    private fun getFacebookFriends(friends: JsonNode): List<String> {
        val data = friends.get("data")
        var facebookIds = emptyList<String>()
        if (data != null && data.size() > 0 ) {
            for (user in data) {
                val id = user.findValue("id")?.asText()
                if (id != null)
                    facebookIds = facebookIds.plus(id)
            }
        }
        return facebookIds
    }

    override fun createAppleUser(email: String?, appleId: String?): User {
        if (email != null) {
            val foundUserAuth = userAuthenticationRepository.findByEmail(email)
            if (foundUserAuth!=null){
                if (foundUserAuth.verified == true) {
                    val foundUser = userRepository.findByEmail(email)
                    val userAuth = UserAuthentication(UserAuthentication.Type.APPLE, appleId = appleId, user = foundUser!!, verified = true)
                    userAuthenticationRepository.save(userAuth)
                    return foundUser
                }
                deleteUserAuthentication(foundUserAuth)
            }
        }
        val newUser = userRepository.save(User())
        val userAuth = UserAuthentication(UserAuthentication.Type.APPLE, appleId = appleId, user = newUser, verified = true)
        userAuthenticationRepository.save(userAuth)
        if (email != null) {
            val userAuth2 = UserAuthentication(UserAuthentication.Type.EMAIL, email = email, user = newUser, verified = true)
            userAuthenticationRepository.save(userAuth2)
        }
        logger.info("New user using apple signup added in the database: ${newUser.id}")
        return newUser
    }


}
