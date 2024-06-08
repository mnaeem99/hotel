package com.my.hotel.server.graphql.dto.request


data class CityAddressConfigInput (
    var cityId: Long,
    var type: String,
    var priority: Int,
)