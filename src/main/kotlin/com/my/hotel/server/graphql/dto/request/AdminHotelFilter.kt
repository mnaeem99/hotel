package com.my.hotel.server.graphql.dto.request

data class AdminHotelFilter(
    val language: String? = null,
    val countryId: Long? = null,
    val cityId: Long? = null,
    val localityId: Long? = null,
    val priceLevelId: Long? = null,
    val searchKeyword: String? = null,
)