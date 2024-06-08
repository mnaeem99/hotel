package com.my.hotel.server.provider.messageProvider

import com.my.hotel.server.data.model.Notification
import com.my.hotel.server.service.event.dto.Event
import org.springframework.messaging.Message

interface IMessageProvider {
    fun buildMessage(event: Event): Message<Event>
    fun buildMessage(notification: Notification, deviceToken: String): com.google.firebase.messaging.Message?
}