package com.my.hotel.server.scheduler

import org.quartz.*
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.PropertiesFactoryBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.scheduling.quartz.SchedulerFactoryBean
import org.springframework.scheduling.quartz.SpringBeanJobFactory
import java.io.IOException
import java.util.*


@Configuration
class QuartzConfig (@Autowired private val applicationContext: ApplicationContext){

    @Bean
    fun springBeanJobFactory(): SpringBeanJobFactory? {
        val jobFactory = AutoWiringSpringBeanJobFactory()
        println("Configuring Job factory")
        jobFactory.setApplicationContext(applicationContext)
        return jobFactory
    }
    private fun getScheduleBuilder(): ScheduleBuilder<SimpleTrigger?>? {
        return simpleSchedule().repeatForever().withIntervalInHours(1).withRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY)
    }
    @Bean
    fun scheduler(factory: SchedulerFactoryBean): Scheduler {
        val job1 = JobBuilder.newJob(StatusJob::class.java).build()
        job1.jobDataMap["key"] = "StatusJob"
        val trigger1: Trigger? = TriggerBuilder.newTrigger().forJob(job1).withSchedule(getScheduleBuilder()).build()

        val job2 = JobBuilder.newJob(NewsfeedAlertJob::class.java).build()
        job2.jobDataMap["key"] = "NewsfeedAlertJob"
        val trigger2: Trigger? = TriggerBuilder.newTrigger().forJob(job2).withSchedule(getScheduleBuilder()).build()

        val scheduler: Scheduler = factory.scheduler
        scheduler.scheduleJob(job1, trigger1)
        scheduler.scheduleJob(job2, trigger2)
        scheduler.start()
        return scheduler
    }
    @Bean
    @Throws(IOException::class)
    fun schedulerFactoryBean(): SchedulerFactoryBean? {
        val factory = SchedulerFactoryBean()
        factory.setJobFactory(springBeanJobFactory()!!)
        factory.setQuartzProperties(quartzProperties()!!)
        return factory
    }

    @Throws(IOException::class)
    fun quartzProperties(): Properties? {
        val propertiesFactoryBean = PropertiesFactoryBean()
        propertiesFactoryBean.setLocation(ClassPathResource("quartz.properties"))
        propertiesFactoryBean.afterPropertiesSet()
        return propertiesFactoryBean.getObject()
    }

}
