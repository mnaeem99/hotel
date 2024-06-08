package com.my.hotel.server.scheduler

import org.quartz.spi.TriggerFiredBundle
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.scheduling.quartz.SpringBeanJobFactory

class AutoWiringSpringBeanJobFactory : SpringBeanJobFactory(), ApplicationContextAware {
    private var beanFactory: AutowireCapableBeanFactory? = null
    override fun setApplicationContext(context: ApplicationContext) {
        beanFactory = context.autowireCapableBeanFactory
    }
    override fun createJobInstance(bundle: TriggerFiredBundle): Any {
        val job = super.createJobInstance(bundle)
        beanFactory!!.autowireBean(job)
        return job
    }
}