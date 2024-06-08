package com.my.hotel.server.graphql.dto.request

data class WishListHotel(
    val hotelId: Long?,
    val placeId: String?,
    var removeHotel: Boolean = false
)