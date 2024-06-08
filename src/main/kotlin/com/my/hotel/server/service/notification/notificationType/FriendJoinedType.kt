package com.my.hotel.server.service.notification.notificationType

import com.my.hotel.server.data.model.Notification
import com.my.hotel.server.data.model.NotificationType
import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.repository.NotificationRepository
import com.my.hotel.server.data.repository.UserRepository
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.service.event.dto.Event
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class FriendJoinedType @Autowired constructor(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val notificationSettingService: NotificationSettingService,
    private val dateProvider: DateProvider
) : INotificationType {
    override val type: NotificationType = NotificationType.FRIEND_JOINED
    override fun createNotification(event: Event): Notification {
        val senderUser = userRepository.findByIdOrNull(event.sentUser!!)
        val notification = Notification(event.type!!, "Your friend from facebook " + senderUser?.firstName +" "+ senderUser?.lastName + " created an account on my",user = senderUser,createdAt = dateProvider.getCurrentDateTime())
        notificationRepository.save(notification)
        return notification
    }
    override fun findUsers(event: Event): List<User>? {
        val facebookFriends = userRepository.findByFacebook(event.facebookIds)
        val settingUsers = facebookFriends.stream().map { entity -> notificationSettingService.createNotificationSettings(entity) }?.collect(Collectors.toList())
        return settingUsers?.filter { entity -> checkNotificationSettings(entity) }?.stream()?.collect(Collectors.toList())
    }
    private fun checkNotificationSettings(user: User): Boolean {
        val settingUser = notificationSettingService.createNotificationSettings(user)
        return settingUser.notificationSetting?.pauseAll == false && settingUser.notificationSetting?.friendJoined == false
    }
}