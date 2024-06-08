package com.my.hotel.server.service.notification.notificationType

import com.my.hotel.server.data.model.Notification
import com.my.hotel.server.data.model.NotificationType
import com.my.hotel.server.data.model.User
import com.my.hotel.server.service.event.dto.Event

interface INotificationType {
    fun createNotification(event: Event): Notification?
    fun findUsers(event: Event): List<User>?
    val type: NotificationType
}