package com.my.hotel.server.service.notification.notificationType

import com.my.hotel.server.data.model.NotificationSetting
import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class NotificationSettingService @Autowired constructor(
    val userRepository: UserRepository
){

    fun createNotificationSettings(user: User): User {
        if (user.notificationSetting==null) {
            val setting = NotificationSetting(id = user.id!!, user = user)
            user.notificationSetting = setting
            return userRepository.save(user)
        }
        return user
    }
}