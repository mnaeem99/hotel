package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.Quality

data class HotelQualityDto(
    val count: Int,
    val endorsedUsers: List<com.my.hotel.server.graphql.dto.response.UserDto>?,
    val quality: Quality
)