package com.my.hotel.server.scheduler

import com.my.hotel.server.service.notification.NotificationService
import lombok.extern.slf4j.Slf4j
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
@Slf4j
class NewsfeedAlertJob : Job {
    @Autowired
    private val notificationService: NotificationService? = null
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun execute(context: JobExecutionContext?) {
        notificationService?.newsFeedAlert()
    }
}