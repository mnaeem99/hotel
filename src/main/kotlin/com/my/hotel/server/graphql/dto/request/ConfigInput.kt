package com.my.hotel.server.graphql.dto.request

data class ConfigInput(
    var priceLevelThreshold: Float? = null,
    var priceRangeUsers: Int? = null,
)