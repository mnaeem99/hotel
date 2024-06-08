package com.my.hotel.server.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.thymeleaf.spring5.SpringTemplateEngine
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver
import org.thymeleaf.spring5.view.ThymeleafViewResolver
import org.thymeleaf.templatemode.TemplateMode
import java.nio.charset.StandardCharsets


@Configuration
@EnableWebMvc
class ThymeleafConfig {
    @Bean
    fun springTemplateEngine(): SpringTemplateEngine? {
        val templateEngine = SpringTemplateEngine()
        templateEngine.addTemplateResolver(thymeleafTemplateResolver())
        return templateEngine
    }

    @Bean
    fun thymeleafTemplateResolver(): SpringResourceTemplateResolver? {
        val emailTemplateResolver = SpringResourceTemplateResolver()
        emailTemplateResolver.prefix = "classpath:/templates/"
        emailTemplateResolver.suffix = ".html"
        emailTemplateResolver.templateMode = TemplateMode.HTML
        emailTemplateResolver.characterEncoding = StandardCharsets.UTF_8.name()
        return emailTemplateResolver
    }

    @Bean
    fun thymeleafViewResolver(): ThymeleafViewResolver? {
        val viewResolver = ThymeleafViewResolver()
        viewResolver.templateEngine = springTemplateEngine()
        return viewResolver
    }
}