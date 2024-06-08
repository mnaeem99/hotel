package com.my.hotel.server.graphql.dto.response

import java.time.LocalDateTime

data class SuggestionDto(
    val hotels: List<MyHotelDto>?,
    val message: String?,
    val nextSuggestionTime: LocalDateTime?,
)