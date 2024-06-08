package com.my.hotel.server.graphql.dto.request

data class UpdateCityAddressConfig (
    var configId: Long,
    var cityId: Long?=null,
    var type: String?=null,
    var priority: Int?=null,
)