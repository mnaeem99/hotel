package com.my.hotel.server.service.firebase

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.my.hotel.server.data.model.Notification
import com.my.hotel.server.provider.messageProvider.MessageProvider
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
@Slf4j
class FirebaseService @Autowired constructor(
    private val firebaseMessaging: FirebaseMessaging,
    private val messageProvider: MessageProvider
): IFirebaseService {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun pushNotification(notification: Notification, deviceToken: String) {
        val message = messageProvider.buildMessage(notification,deviceToken)
        try {
            val result = firebaseMessaging.send(message)
            logger.info("$result: Notification Successfully sent to $deviceToken")
        } catch (e: FirebaseMessagingException) {
            logger.error("Exception while sending notification: " + e.message)
        }
    }

}