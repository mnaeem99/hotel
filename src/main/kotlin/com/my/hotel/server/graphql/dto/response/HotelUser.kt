package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.User

data class HotelUser (
    val hotelId: Long,
    val user: User
)