package com.my.hotel.server.graphql.dto.request

import com.my.hotel.server.data.model.CountryAddressConfig

data class CountryAddressConfigInput (
    var countryId: Long,
    var type: String,
    var priority: Int,
    var level: CountryAddressConfig.AddressLevel,
)