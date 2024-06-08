package com.my.hotel.server.graphql.dto.response

data class HotelUsers(
    val count: Int?,
    val users: List<com.my.hotel.server.graphql.dto.response.UserDto>?,
)