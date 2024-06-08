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

@Component
class NewFollowerType @Autowired constructor(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
    private val notificationSettingService: NotificationSettingService,
    private val dateProvider: DateProvider
) : INotificationType {
    override val type: NotificationType = NotificationType.NEW_FOLLOWER
    override fun createNotification(event: Event): Notification {
        val user = userRepository.findByIdOrNull(event.sentUser!!)
        val notification = Notification(event.type!!, user?.firstName +" "+ user?.lastName + " started following you", user = user,createdAt = dateProvider.getCurrentDateTime())
        notificationRepository.save(notification)
        return notification
    }
    override fun findUsers(event: Event): List<User>? {
        val user = userRepository.findByIdOrNull(event.receivedUser) ?: return null
        val settingUser = notificationSettingService.createNotificationSettings(user)
        if (settingUser.notificationSetting?.pauseAll == false && settingUser.notificationSetting?.newFollower == false) {
            return listOf(settingUser)
        }
        return null
    }
}