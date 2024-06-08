package com.my.hotel.server.graphql.dto.request

import com.my.hotel.server.data.model.CountryAddressConfig

data class UpdateCountryAddressConfig (
    var configId: Long,
    var countryId: Long?=null,
    var type: String?=null,
    var priority: Int?=null,
    var level: CountryAddressConfig.AddressLevel?=null,
)