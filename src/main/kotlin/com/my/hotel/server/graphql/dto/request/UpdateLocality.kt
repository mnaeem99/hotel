package com.my.hotel.server.graphql.dto.request

import com.my.hotel.server.commons.FileUpload

data class UpdateLocality (
    var name: String?,
    var picture: FileUpload?  = null,
    var id: Long,
    var language: String?=null,
    var placeId: String?=null,
    var cityId: Long?=null,
)