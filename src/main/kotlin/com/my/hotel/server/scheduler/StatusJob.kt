package com.my.hotel.server.scheduler

import com.my.hotel.server.service.status.StatusService
import lombok.extern.slf4j.Slf4j
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
@Slf4j
class StatusJob : Job {
    @Autowired
    private val statusService: StatusService? = null
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun execute(context: JobExecutionContext?) {
        logger.info(context?.jobDetail?.key.toString()+" is executing...")
        statusService?.executeStatusJob()
    }
}