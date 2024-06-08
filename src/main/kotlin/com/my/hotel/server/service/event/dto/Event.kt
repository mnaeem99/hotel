package com.my.hotel.server.service.event.dto

import com.my.hotel.server.data.model.NotificationType
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class Event(
    @Enumerated(EnumType.STRING)
    val type: NotificationType?,
    val sentUser: Long?,
    val receivedUser: Long?,
    val otherInfo: Long?=null,
    val hotelIds: List<Long>? =null,
    val facebookIds: List<String>? =null,
    val placeIds: List<String>? =null,
)