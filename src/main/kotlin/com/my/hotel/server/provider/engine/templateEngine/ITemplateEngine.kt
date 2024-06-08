package com.my.hotel.server.provider.engine.templateEngine
import org.thymeleaf.context.IContext


interface ITemplateEngine {
    fun process(template: String?, context: IContext?): String
}