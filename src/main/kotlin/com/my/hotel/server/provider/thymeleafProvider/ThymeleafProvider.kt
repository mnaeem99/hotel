package com.my.hotel.server.provider.thymeleafProvider

import com.my.hotel.server.provider.engine.templateEngine.ITemplateEngine
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.thymeleaf.context.Context


@Component
class ThymeleafProvider @Autowired constructor(
    private var thymeleafTemplateEngine: ITemplateEngine,
    ) : IThymeleafProvider {
    override fun getHtmlEmailBody(templateModel: Map<String, String>): String {
        val thymeleafContext = Context()
        thymeleafContext.setVariables(templateModel)
        return thymeleafTemplateEngine.process("email-thymeleaf.html", thymeleafContext)
    }
}