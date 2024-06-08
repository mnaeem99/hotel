package com.my.hotel.server.graphql.dto.request

import com.my.hotel.server.commons.FileUpload

data class UpdateGift(
    val name: String? = null,
    var picture: FileUpload? = null,
    val id: Long,
    var points: Int? = null,
    var otherInfo: String? = null,
    var language: String? = null
)