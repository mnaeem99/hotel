package com.my.hotel.server.service.notification

import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.service.event.dto.Event
import org.springframework.data.domain.Page

interface INotificationService {
    fun getNewNotification(language: String?): Page<com.my.hotel.server.graphql.dto.response.NotificationDto>?
    fun getNotification(language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.NotificationDto>?
    fun getPromotion(language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.PromotionDto>?
    fun receivedNotification(message: String)
    fun processNotification(event: Event)
    fun readNotification(notificationId: Long): com.my.hotel.server.graphql.dto.response.NotificationDto
    fun updateFollowRequestNotification(senderUser: Long, receivedUser: Long)
}