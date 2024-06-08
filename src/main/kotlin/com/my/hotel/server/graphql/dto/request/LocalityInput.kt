package com.my.hotel.server.graphql.dto.request

import com.my.hotel.server.commons.FileUpload

data class LocalityInput (
    var name: String,
    var picture: FileUpload?  = null,
    var language: String?=null,
    var placeId: String?=null,
    var cityId: Long?=null,
)