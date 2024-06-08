package com.my.hotel.server.graphql.dto.request

data class LocationFilter(
    val latitude: Double,
    val longitude: Double,
    val language: String? = null
)
