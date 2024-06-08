package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.NotificationType
import com.my.hotel.server.data.model.UserNotification
import java.time.LocalDateTime

data class NotificationDto(
    var title: NotificationType,
    var text: String?=null,
    var status: UserNotification.NotificationStatus?=null,
    var user: com.my.hotel.server.graphql.dto.response.UserDto?=null,
    var hotel: com.my.hotel.server.graphql.dto.response.MyHotelDto?=null,
    val createdAt: LocalDateTime?=null,
    var id: Long? = null,
)
