package com.my.hotel.server.graphql.dto.request

import com.my.hotel.server.commons.FileUpload

data class UpdateCountry (
    var name: String?,
    var code: String?,
    var picture: FileUpload?  = null,
    var flag: FileUpload?  = null,
    var id: Long,
    var language: String?=null,
)