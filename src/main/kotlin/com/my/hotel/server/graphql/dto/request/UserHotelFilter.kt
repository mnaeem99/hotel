package com.my.hotel.server.graphql.dto.request

data class UserHotelFilter(
    val userId: Long? = null,
    val countryId: Long? = null,
    val searchKeyword: String? = null,
    val language: String? = null
)