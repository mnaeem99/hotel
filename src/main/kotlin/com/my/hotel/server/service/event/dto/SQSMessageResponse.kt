package com.my.hotel.server.service.event.dto


data class SQSMessageResponse(
    val headers: Header,
    val payload: Event
)
data class Header(
    val id: String,
    val timestamp: Long
)