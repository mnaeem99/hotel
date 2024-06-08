package com.my.hotel.server.provider.messageProvider

import com.google.firebase.messaging.*
import com.my.hotel.server.data.model.Notification
import com.my.hotel.server.data.model.NotificationType
import com.my.hotel.server.service.event.dto.Event
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component

@Component
class MessageProvider : IMessageProvider {
    override fun buildMessage(event: Event): Message<Event> {
        return MessageBuilder.withPayload(event).build()
    }
    override fun buildMessage(notification: Notification, deviceToken: String): com.google.firebase.messaging.Message? {

        val photoMap = mutableMapOf<String, String?>()
        photoMap["id"] = notification.user?.photo?.id.toString()
        photoMap["imageUrl"] = notification.user?.photo?.imageUrl.toString()
        photoMap["thumbnailUrl"] = notification.user?.photo?.thumbnailUrl.toString()

        val userMap = mutableMapOf<String, String>()
        userMap["id"] = notification.user?.id.toString()
        userMap["firstName"] = notification.user?.firstName.toString()
        userMap["lastName"] = notification.user?.lastName.toString()
        userMap["nickName"] = notification.user?.nickName.toString()
        userMap["bio"] = notification.user?.bio.toString()
        userMap["photo"] = photoMap.toString()

        val notificationMap = mutableMapOf<String, String>()
        notificationMap["type"] = notification.title.toString()
        notificationMap["text"] = notification.text.toString()
        notificationMap["id"] = notification.id.toString()
        notificationMap["content"] = userMap.toString()

        return com.google.firebase.messaging.Message.builder()
            .setNotification(
                com.google.firebase.messaging.Notification.builder()
                    .setTitle(createNotificationTitle(notification.title))
                    .setBody(notification.text)
                    .build()
            )
            .putAllData(notificationMap)
            .setAndroidConfig(
                AndroidConfig.builder()
                    .setNotification(
                        AndroidNotification.builder()
                            .setSound("default")
                            .setPriority(AndroidNotification.Priority.HIGH)
                            .build()
                    )
                    .build()
            )
            .setApnsConfig(
                ApnsConfig.builder()
                    .setAps(
                        Aps.builder()
                            .setSound("default")
                            .setAlert(
                                ApsAlert.builder()
                                    .setTitle(createNotificationTitle(notification.title))
                                    .setBody(notification.text)
                                    .build()
                            )
                            .setMutableContent(true)
                            .setContentAvailable(true)
                            .build()
                    )
                    .build()
            )
            .setToken(deviceToken)
            .build()
    }
    private fun createNotificationTitle(title: NotificationType): String{
        return when (title) {
            NotificationType.NEW_FOLLOWER -> "You have a new follower"
            NotificationType.REQUEST_FOLLOWING -> "You have a new follower request"
            NotificationType.NEWSFEED_ALERT -> "You have a new feeds"
            NotificationType.FRIEND_FAVORITE -> "Your friends added a hotel"
            NotificationType.FRIEND_STATUS -> "Your friends got a new status"
            NotificationType.MY_STATUS -> "You have a new status"
            NotificationType.FRIEND_JOINED -> "New friends joined my"
            NotificationType.FRIEND_ADDING_WISHLIST -> "Your friends added your suggestion"
            NotificationType.PROMOTION_FROM_hotel -> "You have a new Promotion"
            else -> title.toString()
        }
    }
}