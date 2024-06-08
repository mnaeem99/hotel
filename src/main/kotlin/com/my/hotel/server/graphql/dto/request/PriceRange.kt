package com.my.hotel.server.graphql.dto.request

data class PriceRange(
    val userId: Long,
    val hotelId: Long?,
    val placeId: String?,
    val range: Int
)