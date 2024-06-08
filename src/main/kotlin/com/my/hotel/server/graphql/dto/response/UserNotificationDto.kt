package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.Notification
import com.my.hotel.server.data.model.UserNotification

data class UserNotificationDto(
    var notification: Notification,
    var status: UserNotification.NotificationStatus?
)
