package com.my.hotel.server.service.user

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.commons.SpatialUtils
import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.Location
import com.my.hotel.server.graphql.dto.response.UserProfileAdmin
import com.my.hotel.server.graphql.error.AlreadyExistCustomException
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.graphql.error.ValidationErrorCustomException
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.provider.thymeleafProvider.ThymeleafProvider
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.security.SecurityConstants
import com.my.hotel.server.security.SecurityUtils
import com.my.hotel.server.service.aws.AWSService
import com.my.hotel.server.service.user.dto.UpdateProfile
import com.my.hotel.server.service.user.saveUser.SaveUserService
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated


@Service
@Slf4j
@Validated
class UserService @Autowired constructor(val confirmationCodeRepository: ConfirmationCodeRepository,
                                         val userAuthenticationRepository: UserAuthenticationRepository,
                                         val userRepository: UserRepository,
                                         val awsService: AWSService,
                                         val securityUtils: SecurityUtils,
                                         var passwordEncoder: PasswordEncoder,
                                         var imageRepository: ImageRepository,
                                         val saveUserService: SaveUserService,
                                         val cityRepository: CityRepository,
                                         val countryRepository: CountryRepository,
                                         val translationService: TranslationService,
                                         val thymeleafProvider: ThymeleafProvider,
                                         val dateProvider: DateProvider,
                                         val deviceLocationRepository: DeviceLocationRepository,
                                         val userDeviceTokenRepository: UserDeviceTokenRepository,
): IUserService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    /**
     * Creates user using email and sends out confirmation code.
     * @param email email of the new user
     */
    override fun requestEmailVerificationCode(email: String): Boolean {
        if(userRepository.findByEmail(email) != null) {
            throw AlreadyExistCustomException(Constants.EMAIL_ALREADY_IN_USE)
        }
        logger.info("requestEmailVerificationCode called for email: $email")
        val confirmationCode = saveUserService.saveUser(UserAuthentication.Type.EMAIL, email, null)
        val templateModel = HashMap<String, String>()
        templateModel["confirmationCode"] = confirmationCode.code.toString()
        awsService.sendEmail(email, thymeleafProvider.getHtmlEmailBody(templateModel), Constants.CONFIRMATION_EMAIL_SUBJECT)
        return true
    }

    /**
     * Creates user using phone number and sends out confirmation code.
     * @param phone email of the new user
     */
    override fun requestPhoneVerificationCode(phone: String): Boolean {
        if(userRepository.findByPhone(phone) != null) {
            throw AlreadyExistCustomException(Constants.PHONE_ALREADY_IN_USE)
        }
        logger.info("requestPhoneVerificationCode called for phone: $phone")
        val confirmationCode = saveUserService.saveUser(UserAuthentication.Type.PHONE, null, phone)
//        awsService.sendSms(phone, confirmationCode.code.toString() + Constants.VERIFICATION_PHONE)
        return true
    }

    /**
     * Verifies user using email and verification code.
     * @param email email of the user
     * @param code verification code sent over email
     */
    override fun verifyEmailVerificationCode(email: String, code: Int, deviceID: String?): HashMap<String, String>? {
        val verificationCode = confirmationCodeRepository.findByEmail(email)
            ?: throw ValidationErrorCustomException(Constants.INVALID_VERIFICATION_CODE)
        if(saveUserService.verifyCode(verificationCode, code)) {
            logger.info("verifyEmailVerificationCode called for email: $email with code: $code")
            val user = userRepository.findByEmail(email)
            val tokens: HashMap<String,String> = HashMap()
            tokens["accessToken"] = SecurityConstants.TOKEN_PREFIX + user?.let { securityUtils.generateAccessToken(it, deviceID) }
            tokens["refreshToken"] = SecurityConstants.TOKEN_PREFIX + user?.let { securityUtils.generateRefreshToken(it, deviceID) }
            return tokens
        }
        throw ValidationErrorCustomException(Constants.INVALID_VERIFICATION_CODE)
    }

    /**
     * Verifies user using phone number and verification code.
     * @param phone phone number of the user
     * @param code verification code sent over phone
     */
    override fun verifyPhoneVerificationCode(phone: String, code: Int, deviceID: String?): HashMap<String, String>? {
        val verificationCode = confirmationCodeRepository.findByPhone(phone)
            ?: throw ValidationErrorCustomException(Constants.INVALID_VERIFICATION_CODE)
        if(saveUserService.verifyCode(verificationCode, code)) {
            logger.info("verifyPhoneVerificationCode called for phone: $phone with code: $code")
            val user = userRepository.findByPhone(phone)
            val tokens: HashMap<String,String> = HashMap()
            tokens["accessToken"] = SecurityConstants.TOKEN_PREFIX + user?.let { securityUtils.generateAccessToken(it, deviceID) }
            tokens["refreshToken"] = SecurityConstants.TOKEN_PREFIX + user?.let { securityUtils.generateRefreshToken(it, deviceID ) }
            return tokens
        }
        throw ValidationErrorCustomException(Constants.INVALID_VERIFICATION_CODE)
    }


    /**
     * Updates profile of logged-in user.
     * @param fields profile of the user
     */
    override fun updateProfile(fields: UpdateProfile): com.my.hotel.server.graphql.dto.response.UserDto? {
        val user = getLoggedInUser() ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "user")
        if(fields.firstName != null) {
            user.firstName = fields.firstName
        }
        if(fields.lastName != null) {
            user.lastName = fields.lastName
        }
        if(fields.nickName != null) {
            if(userRepository.findByNickName(fields.nickName!!, user.id!!)!=null)
                throw AlreadyExistCustomException(Constants.NICKNAME_ALREADY_IN_USE)
            user.nickName = fields.nickName
        }
        if(fields.dob != null) {
            user.dob = fields.dob
        }
        if (user.nickName==null){
            user.nickName = saveUserService.createNickName(user.firstName + user.lastName)
        }
        if (fields.bio != null) {
            user.bio = fields.bio
        }
        if (fields.language != null) {
            user.language = fields.language
        }
        if (fields.countryCode != null) {
            val country = countryRepository.findByCode(fields.countryCode!!) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "countryCode")
            user.country = country
        }
        if(fields.profilePicture != null) {
            // upload to s3
            val newImage = saveUserService.updateImage(fields.profilePicture!!, user)
            if (newImage!=null) {
                user.photo = newImage
            }
        }
        logger.info("user:${user.id} update the Profile: $fields")
        return translationService.mapUserDto(userRepository.save(user), fields.language)
    }

    override fun updatePassword(password: String): Boolean {
        val user = getLoggedInUser() ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "user")
        logger.info("user:${user.id} update the password")
        user.auths?.forEach { userAuth ->
            userAuth.password = passwordEncoder.encode(password)
            userAuthenticationRepository.save(userAuth)
        }
        return true
    }
    override fun changePassword(currentPassword: String, newPassword: String): Boolean {
        val user = getLoggedInUser() ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "user")
        logger.info("user:${user.id} update the password")
        if (!passwordEncoder.matches(currentPassword, user.auths?.first()?.password)){
            throw NotFoundCustomException(Constants.INCORRECT_PASSWORD, "currentPassword")
        }
        user.auths?.forEach { userAuth ->
            userAuth.password = passwordEncoder.encode(newPassword)
            userAuthenticationRepository.save(userAuth)
        }
        return true
    }

    /**
     * Returns logged-in user
     */
    override fun getLoggedInUser(): User? {
        return SecurityUtils.getLoggedInUser()
    }

    override fun deleteUserProfile(): Boolean {
        return saveUserService.deleteUser(getLoggedInUser()?.id!!)
    }

    override fun updateAccount(private: Boolean): Boolean? {
        val user = SecurityUtils.getLoggedInUser()
        user.isPrivate = private
        val updateUser = userRepository.save(user)
        logger.info("user:${user.id} change to private account")
        return updateUser.isPrivate
    }

    override fun updateEmail(email: String): Boolean {
        val user = SecurityUtils.getLoggedInUser()
        val foundUserAuth = userAuthenticationRepository.findByEmail(email)
        if (foundUserAuth!=null){
            if (foundUserAuth.verified == true)
                throw AlreadyExistCustomException(Constants.EMAIL_ALREADY_IN_USE)
            saveUserService.deleteUserAuthentication(foundUserAuth)
        }
        val userAuth: UserAuthentication?
        val existingAuth = user.auths?.find { auth -> auth.type == UserAuthentication.Type.EMAIL && auth.verified == false }
        if (existingAuth!=null){
            existingAuth.email = email
            userAuth = userAuthenticationRepository.save(existingAuth)
            deleteOldCode(userAuth)
        } else {
            userAuth = userAuthenticationRepository.save(UserAuthentication(UserAuthentication.Type.EMAIL, email, password = user.auths?.first()?.password, user = user, verified = false))
        }
        val confirmationCode = saveUserService.generateConfirmationCode()
        confirmationCode.userAuth = userAuth
        confirmationCodeRepository.save(confirmationCode)
        val templateModel = HashMap<String, String>()
        templateModel["confirmationCode"] = confirmationCode.code.toString()
        awsService.sendEmail(email, thymeleafProvider.getHtmlEmailBody(templateModel), Constants.CONFIRMATION_EMAIL_SUBJECT)
        return true
    }
    override fun updatePhone(phone: String): Boolean {
        val user = SecurityUtils.getLoggedInUser()
        val foundUserAuth = userAuthenticationRepository.findByPhone(phone)
        if (foundUserAuth!=null){
            if (foundUserAuth.verified == true)
                throw AlreadyExistCustomException(Constants.PHONE_ALREADY_IN_USE)
            saveUserService.deleteUserAuthentication(foundUserAuth)
        }
        val userAuth: UserAuthentication?
        val existingAuth = user.auths?.find { auth -> auth.type == UserAuthentication.Type.PHONE && auth.verified == false }
        if (existingAuth!=null){
            existingAuth.phone = phone
            userAuth = userAuthenticationRepository.save(existingAuth)
            deleteOldCode(userAuth)
        } else {
            userAuth = userAuthenticationRepository.save(UserAuthentication(UserAuthentication.Type.PHONE, phone = phone, password = user.auths?.first()?.password, user = user, verified = false))
        }
        val confirmationCode = saveUserService.generateConfirmationCode()
        confirmationCode.userAuth = userAuth
        confirmationCodeRepository.save(confirmationCode)
//        awsService.sendSms(phone, confirmationCode.code.toString() + Constants.VERIFICATION_PHONE)
        return true
    }
    private fun deleteOldCode(auth: UserAuthentication) {
        val oldCodes = confirmationCodeRepository.findByUserAuth(auth.id!!)
        oldCodes?.forEach { oldCode ->
            oldCode.userAuth = null
            confirmationCodeRepository.save(oldCode)
            confirmationCodeRepository.delete(oldCode)
        }
    }

    override fun updateNotificationSettings(fields: com.my.hotel.server.graphql.dto.request.UpdateNotificationSetting): NotificationSetting? {
        val user = SecurityUtils.getLoggedInUser()
        logger.info("user:${user.id} updateNotificationSettings called: $fields")
        if (user.notificationSetting==null) {
            val setting = NotificationSetting(id = user.id!!, user = user)
            user.notificationSetting = setting
        }
        if (fields.pauseAll!=null){
            user.notificationSetting?.pauseAll = fields.pauseAll!!
        }
        if (fields.newFollower!=null){
            user.notificationSetting?.newFollower = fields.newFollower!!
        }
        if (fields.friendStatus!=null){
            user.notificationSetting?.friendStatus = fields.friendStatus!!
        }
        if (fields.friendFavorite!=null){
            user.notificationSetting?.friendFavorite = fields.friendFavorite!!
        }
        if (fields.friendJoined!=null){
            user.notificationSetting?.friendJoined = fields.friendJoined!!
        }
        if (fields.friendAddingWishlist!=null){
            user.notificationSetting?.friendAddingWishlist = fields.friendAddingWishlist!!
        }
        if (fields.promotionFromHotel!=null){
            user.notificationSetting?.promotionFromHotel = fields.promotionFromHotel!!
        }
        if (fields.newsfeedAlert!=null){
            user.notificationSetting?.newsfeedAlert = fields.newsfeedAlert!!
        }
        userRepository.save(user)
        return user.notificationSetting
    }
    override fun requestResetPasswordCodeByEmail(email: String): Boolean {
        val user = userRepository.findByEmail(email)
            ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "email")
        logger.info("user:${user.id} requestResetPasswordCode by email: $email")
        val oldCode = confirmationCodeRepository.findByEmail(email)
        if (oldCode!=null) {
            oldCode.userAuth = null
            confirmationCodeRepository.save(oldCode)
            confirmationCodeRepository.delete(oldCode)
        }
        val confirmationCode = saveUserService.generateConfirmationCode()
        confirmationCode.userAuth = user.auths?.find { auth -> auth.type == UserAuthentication.Type.EMAIL && auth.email == email }
        confirmationCodeRepository.save(confirmationCode)
        val templateModel = HashMap<String, String>()
        templateModel["confirmationCode"] = confirmationCode.code.toString()
        awsService.sendEmail(email, thymeleafProvider.getHtmlEmailBody(templateModel), Constants.CONFIRMATION_EMAIL_SUBJECT)
        return true
    }
    override fun requestResetPasswordCodeByPhone(phone: String): Boolean {
        val user = userRepository.findByPhone(phone)
            ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "phone")
        logger.info("user:${user.id} requestResetPasswordCode by phone: $phone")
        val oldCode = confirmationCodeRepository.findByPhone(phone)
        if (oldCode!=null) {
            oldCode.userAuth = null
            confirmationCodeRepository.save(oldCode)
            confirmationCodeRepository.delete(oldCode)
        }
        val confirmationCode = saveUserService.generateConfirmationCode()
        confirmationCode.userAuth = user.auths?.find { auth -> auth.type == UserAuthentication.Type.PHONE && auth.phone == phone }
        confirmationCodeRepository.save(confirmationCode)
//        awsService.sendSms(phone, confirmationCode.code.toString() + Constants.VERIFICATION_PHONE)
        return true
    }
    override fun requestChefVerification(): Boolean {
        val user = SecurityUtils.getLoggedInUser()
        if (user.isPendingChef == true){
            throw AlreadyExistCustomException(Constants.REQUEST_ALREADY_IN_USE)
        } else if (user.isChef == true){
            throw AlreadyExistCustomException(Constants.CHEF_ALREADY)
        }
        user.isPendingChef = true
        userRepository.save(user)
        return true
    }
    /**
     * get user profile.
     * @param language of the user language
     */
    override fun getProfile(language: String?): com.my.hotel.server.graphql.dto.response.UserProfile {
        val principal = SecurityUtils.getLoggedInUser()
        return com.my.hotel.server.graphql.dto.response.UserProfile(
            translationService.mapUserDto(
                principal,
                language
            )
        )
    }
    /**
     * get other user profile.
     * @param id of the user id
     * @param language of the user language
     */
    override fun getUser(id: Long, language: String?): com.my.hotel.server.graphql.dto.response.UserDto? {
        val user = userRepository.findByIdOrNull(id)
            ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "id")
        return translationService.mapUserDto(user, language)
    }
    /**
     * get all users.
     * @param input of the user filter
     * @param pageOptions for the pagination
     */
    override fun getUsers(input: com.my.hotel.server.graphql.dto.request.AdminUserFilter, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>? {
        return userRepository.findAll(input.language?: Constants.DEFAULT_LANGUAGE,
            input.countryId,input.userType,input.searchKeyword,
            pageOptions.toPageable())?.map { entity ->
            if (entity.countryId==null){
                com.my.hotel.server.graphql.dto.response.UserDto(
                    entity.id,
                    entity.firstName,
                    entity.lastName,
                    entity.nickName,
                    entity.bio,
                    entity.photo,
                    entity.language,
                    null,
                    entity.private,
                    entity.chef,
                    entity.blocked,
                    entity.userType,
                    entity.dob,
                    null
                )
            }else{
                com.my.hotel.server.graphql.dto.response.UserDto(
                    entity.id,
                    entity.firstName,
                    entity.lastName,
                    entity.nickName,
                    entity.bio,
                    entity.photo,
                    entity.language,
                    com.my.hotel.server.graphql.dto.response.CountryDto(
                        entity.countryName,
                        entity.countryCode,
                        entity.countryPicture,
                        entity.countryFlag,
                        entity.countryId
                    ),
                    entity.private,
                    entity.chef,
                    entity.blocked,
                    entity.userType,
                    entity.dob,
                    null
                )
            }
        }
    }
    /**
     * add new User.
     * @param input of the new user
     */
    override fun addUser(input: com.my.hotel.server.graphql.dto.request.UserInput): com.my.hotel.server.graphql.dto.response.UserDto? {
        return saveUserService.addUser(input)
    }

    /**
     * update User.
     * @param input of the update user
     */
    override fun updateUser(input: com.my.hotel.server.graphql.dto.request.UpdateUser): com.my.hotel.server.graphql.dto.response.UserDto?{
        return saveUserService.updateUser(input)
    }
    /**
     * delete User.
     * @param id of the user
     */
    override fun deleteUser(id: Long): Boolean {
        return saveUserService.deleteUser(id)
    }
    override fun getUserAdmin(id: Long, language: String?): UserProfileAdmin {
        val user = userRepository.findByIdOrNull(id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "id")
        return UserProfileAdmin(translationService.mapUserDto(user, language))
    }
    override fun searchUserByNickname(nickName: String, language: String?): com.my.hotel.server.graphql.dto.response.UserDto? {
        val principal = SecurityUtils.getLoggedInUser()
        val user = userRepository.findByNickName(nickName, principal.id!!) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "nickName")
        return translationService.mapUserDto(user, language)
    }
    override fun blockUserAdmin(userId: Long, block: Boolean): Boolean {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "userId")
        user.isBlocked = block
        userRepository.save(user)
        return true
    }
    override fun setDeviceToken(token: String): Boolean {
        val user = SecurityUtils.getLoggedInUser()
        val deviceTokenFound = userDeviceTokenRepository.findByDeviceToken(token)
        if (deviceTokenFound==null){
            val userDeviceToken = UserDeviceToken(user,token)
            userDeviceTokenRepository.save(userDeviceToken)
            return true
        }
        deviceTokenFound.user = user
        userDeviceTokenRepository.save(deviceTokenFound)
        return true
    }
    override fun deleteDeviceToken(token: String): Boolean {
        val user = SecurityUtils.getLoggedInUser()
        val deviceTokenFound = userDeviceTokenRepository.findByUserAndDeviceToken(user, token) ?: return false
        userDeviceTokenRepository.delete(deviceTokenFound)
        return true
    }
    override fun updateLocation(location: Location, timezoneId: String?): Boolean {
        val point = SpatialUtils.getPoint(location.latitude, location.longitude)
        val user = SecurityUtils.getPrincipalUser()
        if (user!=null){
            if(point!=null){
                user.point = point
            }
            if (timezoneId!=null) {
                user.timezoneId = timezoneId
            }
            if (user.language == null){
                user.language = Constants.DEFAULT_LANGUAGE
            }
            userRepository.save(user)
        }
        val deviceID = SecurityUtils.getLoggedInDevice() ?: return false
        val deviceLocation = deviceLocationRepository.findByIdOrNull(deviceID)
        if (deviceLocation == null){
            val newDeviceLocation = DeviceLocation(user,point,dateProvider.getCurrentDateTime(),dateProvider.getCurrentDateTime(),deviceID)
            deviceLocationRepository.save(newDeviceLocation)
            return true
        }
        deviceLocation.point = point
        if (user!=null){
            deviceLocation.user = user
        }
        deviceLocation.modifiedAt = dateProvider.getCurrentDateTime()
        deviceLocationRepository.save(deviceLocation)
        return true
    }

}
