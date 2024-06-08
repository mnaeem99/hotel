package com.my.hotel.server.service.notification.notificationType

import com.my.hotel.server.data.model.Notification
import com.my.hotel.server.data.model.NotificationType
import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.repository.FavoriteRepository
import com.my.hotel.server.data.repository.NotificationRepository
import com.my.hotel.server.data.repository.MyHotelRepository
import com.my.hotel.server.data.repository.UserRepository
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.service.event.dto.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class FriendAddingWishlistType @Autowired constructor(
    private val userRepository: UserRepository,
    private val myHotelRepository: MyHotelRepository,
    private val favoriteRepository: FavoriteRepository,
    private val notificationRepository: NotificationRepository,
    private val notificationSettingService: NotificationSettingService,
    private val translationService: TranslationService,
    private val dateProvider: DateProvider
) : INotificationType {
    override val type: NotificationType = NotificationType.FRIEND_ADDING_WISHLIST
    override fun createNotification(event: Event): Notification {
        val senderUser = userRepository.findByIdOrNull(event.sentUser!!)
        val hotel = myHotelRepository.findByIdOrNull(event.otherInfo)
        val hotelDto = translationService.mapmyHotelDto(hotel!!,senderUser?.language)
        val notification = Notification(event.type!!, "Your friend " + senderUser?.firstName +" "+ senderUser?.lastName + " added your suggestion "+ hotelDto.name + " in wishlist", user = senderUser, hotel = hotel,createdAt = dateProvider.getCurrentDateTime())
        notificationRepository.save(notification)
        return notification
    }
    override fun findUsers(event: Event): List<User>? {
        val user = userRepository.findByIdOrNull(event.receivedUser) ?: return null
        val hotel = myHotelRepository.findByIdOrNull(event.otherInfo)
        val settingUsers = user.followers.stream().map { entity -> notificationSettingService.createNotificationSettings(entity) }.collect(Collectors.toList())
        val isSuggested = settingUsers.filter { entity -> favoriteRepository.findByUserAndHotel(entity, hotel!!) != null }.stream().collect(Collectors.toList())
        return isSuggested?.filter { entity -> checkNotificationSettings(entity) }?.stream()?.collect(Collectors.toList())
    }
    private fun checkNotificationSettings(user: User): Boolean {
        val settingUser = notificationSettingService.createNotificationSettings(user)
        return settingUser.notificationSetting?.pauseAll == false && settingUser.notificationSetting?.friendAddingWishlist == false
    }
}