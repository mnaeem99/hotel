package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.Country
import com.my.hotel.server.data.model.Image

data class RegionDto(
    var name: String?,
    var address: String?=null,
    var geolat: Float?=null,
    var geolong: Float?=null,
    var photo: Image?  = null,
    var country: Country?  = null,
    var placeId: String?=null,
)