package com.my.hotel.server.provider.engine.templateEngine

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.thymeleaf.context.IContext
import org.thymeleaf.spring5.SpringTemplateEngine


@Component
class TemplateEngineWrapper : ITemplateEngine {
    @Autowired
    private val templateEngine: SpringTemplateEngine? = null

    override fun process(template: String?, context: IContext?): String {
        return templateEngine!!.process(template, context)
    }
}