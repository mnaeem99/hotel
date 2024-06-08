package com.my.hotel.server.graphql.dto.request

import java.time.LocalDateTime

data class HotelVerificationAppointmentInput(
    var hotelId: Long,
    var phone: String?=null,
    var date: LocalDateTime,
    var language: String
)