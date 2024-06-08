package com.my.hotel.server.provider.thymeleafProvider

interface IThymeleafProvider {
    fun getHtmlEmailBody(templateModel: Map<String, String>): String
}