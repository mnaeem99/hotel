package com.my.hotel.server.service.notification

import com.fasterxml.jackson.databind.ObjectMapper
import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.response.NotificationDto
import com.my.hotel.server.graphql.dto.response.PromotionDto
import com.my.hotel.server.graphql.dto.response.UserNotificationDto
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.security.SecurityUtils
import com.my.hotel.server.service.event.dto.Event
import com.my.hotel.server.service.event.dto.SQSMessageResponse
import com.my.hotel.server.service.explore.ExploreService
import com.my.hotel.server.service.firebase.FirebaseService
import com.my.hotel.server.service.notification.notificationType.NotificationTypeFactory
import com.my.hotel.server.service.notification.promotionType.PromotionFromhotelType
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.aws.messaging.config.annotation.EnableSqs
import org.springframework.cloud.aws.messaging.listener.SqsMessageDeletionPolicy
import org.springframework.cloud.aws.messaging.listener.annotation.SqsListener
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.stereotype.Service


@Service
@Slf4j
@EnableSqs
class NotificationService @Autowired constructor(
    private val userRepository: UserRepository,
    private val translationService: TranslationService,
    private val notificationRepository: NotificationRepository,
    private val notificationTypeFactory: NotificationTypeFactory,
    private val promotionFromhotelType: PromotionFromhotelType,
    private val promotionRepository: PromotionRepository,
    private val firebaseService: FirebaseService,
    private val userDeviceTokenRepository: UserDeviceTokenRepository,
    private val dateProvider: DateProvider,
    private val exploreService: ExploreService,
    private val userNotificationRepository: UserNotificationRepository,
    ): INotificationService {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun getNewNotification(language: String?): Page<NotificationDto>? {
        val principal = SecurityUtils.getLoggedInUser()
        val notifications = notificationRepository.findUnReadNotification(principal.id!!,dateProvider.getCurrentDateTime().minusDays(7), Pageable.unpaged())
        return notifications?.map { entity -> toNotificationDto(entity, language) }
    }

    override fun getNotification(language: String?, pageOptions: GraphQLPage): Page<NotificationDto>? {
        val principal = SecurityUtils.getLoggedInUser()
        val notifications = notificationRepository.findByUser(principal.id!!,dateProvider.getCurrentDateTime().minusDays(7), pageOptions.toPageable())
        return notifications?.map { entity -> toNotificationDto(entity, language) }
    }
    override fun getPromotion(language: String?, pageOptions: GraphQLPage): Page<PromotionDto>? {
        val principal = SecurityUtils.getLoggedInUser()
        val promotions = promotionRepository.findByUser(principal.id!!, pageOptions.toPageable())?.map { entity -> toPromotionDto(entity,language) }
        return promotions
    }
    fun toPromotionDto(promotion: Promotion, language: String?): PromotionDto {
        val hotelDto = translationService.mapmyHotelDto(promotion.hotel, language)
        return PromotionDto(
            promotion.title,
            promotion.titleColor,
            promotion.subTitle,
            promotion.subTitleColor,
            promotion.buttonText,
            promotion.buttonColor,
            promotion.budget,
            promotion.duration,
            promotion.showLogo,
            promotion.cover,
            promotion.geolat,
            promotion.geolong,
            promotion.radius,
            promotion.region,
            promotion.active,
            hotelDto,
            promotion.targetAudience,
            promotion.createdAt,
            promotion.modifiedAt,
            promotion.id
        )
    }
    fun toNotificationDto(userNotificationDto: UserNotificationDto, language: String?): NotificationDto {
        val notification = userNotificationDto.notification
        val notificationDto = NotificationDto(
            notification.title,
            notification.text,
            userNotificationDto.status,
            null,
            null,
            notification.createdAt,
            notification.id
        )
        if (notification.user != null){
            val user = translationService.mapUser(notification.user, null)
            user.status = notification.status
            notificationDto.user = user
        }
        if (notification.hotel != null){
            notificationDto.hotel = translationService.mapmyHotelDto(notification.hotel,language)
        }
        return notificationDto
    }
    @SqsListener(value = ["\${aws.sqs.queue}"],deletionPolicy = SqsMessageDeletionPolicy.ON_SUCCESS)
    override fun receivedNotification(message: String) {
        logger.info("Received message from SQS: $message")
        val objectManager = Jackson2ObjectMapperBuilder.json().build<ObjectMapper>()
        val response = objectManager.readValue(message, SQSMessageResponse::class.java)
        if (response.payload.type?.equals(NotificationType.PROMOTION_FROM_hotel) == true)
            processPromotion(response.payload)
        else if (response.payload.type?.equals(NotificationType.SUGGESTED_HISTORY) == true)
            exploreService.executeSuggestions(response.payload)
        else
            processNotification(response.payload)
    }

    override fun processNotification(event: Event) {
        val notification = notificationTypeFactory.generateNotification(event)
        if (notification != null) {
            val users = notificationTypeFactory.findUsers(event)
            if (!users.isNullOrEmpty()){
                //Push Notification to users
                users.forEach { entity -> addUserNotification(entity,notification) }
            }
        }
    }

    override fun readNotification(notificationId: Long): NotificationDto {
        val principal = SecurityUtils.getLoggedInUser()
        val notification = notificationRepository.findByIdOrNull(notificationId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,notificationId.toString())
        val userNotification = userNotificationRepository.findByUserAndNotification(principal,notification) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,notificationId.toString())
        userNotification.status = UserNotification.NotificationStatus.READ
        userNotificationRepository.save(userNotification)
        return toNotificationDto(
            UserNotificationDto(
                notification,
                userNotification.status
            ), null)
    }

    override fun updateFollowRequestNotification(senderUser: Long, receivedUser: Long) {
        val userNotification = userNotificationRepository.findFollowRequestNotification(senderUser, receivedUser)
        if (userNotification!=null){
            val updateNotification = userNotification.notification
            updateNotification.title = NotificationType.NEW_FOLLOWER
            updateNotification.text =  updateNotification.user?.firstName +" "+ updateNotification.user?.lastName + " started following you"
            notificationRepository.save(updateNotification)
        }
    }

    fun processPromotion(event: Event) {
        val promotion = promotionFromhotelType.getPromotion(event)
        if (promotion != null) {
            val users = promotionFromhotelType.findUsers(event)
            if (!users.isNullOrEmpty()){
                //Push Notification to users
                users.forEach { entity -> addUserPromotion(entity,promotion) }
            }
        }
    }
    private fun addUserNotification(user: User, notification: Notification) {
        val foundUserNotification = userNotificationRepository.findByUserAndNotification(user, notification)
        if (foundUserNotification==null) {
            userNotificationRepository.save(UserNotification(user, notification))
        }
        pushNotification(user, notification)
    }

    private fun pushNotification(user: User, notification: Notification) {
        val userDeviceTokens = userDeviceTokenRepository.findByUser(user)
        userDeviceTokens?.stream()?.forEach { userDeviceToken ->
            if (userDeviceToken?.deviceToken != null) {
                try {
                    firebaseService.pushNotification(notification, userDeviceToken.deviceToken)
                } catch (e: Exception) {
                    logger.error(e.message)
                }
            }
        }
    }

    private fun addUserPromotion(user: User, promotion: Promotion): User {
        user.promotion = user.promotion?.plus(promotion)
        val hotelDto = translationService.mapmyHotelDto(promotion.hotel, user.language)
        val notification = Notification(NotificationType.PROMOTION_FROM_hotel, "New promotion ${promotion.title} is added on a hotel ${hotelDto.name}", user = null, createdAt = dateProvider.getCurrentDateTime())
        pushNotification(user, notification)
        return userRepository.save(user)
    }

    fun newsFeedAlert(){
        logger.info("Background NewsFeed Alert Job started")
        val date = dateProvider.getCurrentDateTime().minusHours(4)
        val users = userRepository.findByNewsfeedAlert()
        for (user in users){
            val friends = userRepository.getFriendsWhoAddedHotel(user.id!!,date)
            if (friends.isNotEmpty())
                pushNotification(user, getNewsfeedNotification(friends))
        }
        logger.info("Background NewsFeed Alert Job Finished")
}

    private fun getNewsfeedNotification(friends: List<User>): Notification {
        var text: String? = null
        if (friends.size == 1) {
            text = "Your friend ${friends.first().firstName} ${friends.first().lastName} added new recommendation in their list"
        } else if (friends.size == 2) {
            text = "Your friend ${friends[0].firstName} ${friends[0].lastName} and ${friends[1].firstName} ${friends[1].lastName} added new recommendation in their list"
        } else if (friends.size > 2) {
            text = "Your friend ${friends[0].firstName} ${friends[0].lastName} , ${friends[1].firstName} ${friends[1].lastName} and ${friends.size-2} others added new recommendation in their list"
        }
        return Notification(NotificationType.NEWSFEED_ALERT, text, user = friends.first(), createdAt = dateProvider.getCurrentDateTime())
    }
}