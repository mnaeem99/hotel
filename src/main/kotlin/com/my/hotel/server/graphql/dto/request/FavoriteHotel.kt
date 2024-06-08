package com.my.hotel.server.graphql.dto.request

data class FavoriteHotel(
    val hotelId: Long?,
    val placeId: String?,
    val hotelQuality: List<Long>?,
    var removeHotel: Boolean = false,
    val priceRange: Int? = null
)