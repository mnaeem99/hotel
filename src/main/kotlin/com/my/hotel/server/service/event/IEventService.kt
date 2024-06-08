package com.my.hotel.server.service.event

import com.my.hotel.server.service.event.dto.Event


interface IEventService {
    fun createEvent(event: Event)
}