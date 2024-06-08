package com.my.hotel.server.service.event

import com.my.hotel.server.provider.messageProvider.MessageProvider
import com.my.hotel.server.service.event.dto.Event
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.aws.messaging.core.QueueMessagingTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
@Slf4j
class EventService @Autowired constructor(
    val queueMessagingTemplate: QueueMessagingTemplate,
    val messageProvider: MessageProvider,
    @Value("\${aws.sqs.destination}")
    private var destinationName:String
): IEventService {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    @Async
    override fun createEvent(event: Event) {
        logger.info("Sending message to SQS: $event")
        val msg = messageProvider.buildMessage(event)
        queueMessagingTemplate.convertAndSend(destinationName,msg)
    }

}