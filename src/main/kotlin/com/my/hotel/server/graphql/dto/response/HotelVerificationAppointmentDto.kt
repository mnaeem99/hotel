package com.my.hotel.server.graphql.dto.response

import java.time.LocalDateTime

data class HotelVerificationAppointmentDto(
    var appointmentDate: LocalDateTime?,
    var hotel: com.my.hotel.server.graphql.dto.response.MyHotelDto?,
    var id: Long? = null
)