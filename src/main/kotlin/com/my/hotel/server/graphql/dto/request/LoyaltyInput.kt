package com.my.hotel.server.graphql.dto.request

data class LoyaltyInput (
    val hotelId: Long,
    val userId: Long,
    val points: Int,
    val language: String? = null
)