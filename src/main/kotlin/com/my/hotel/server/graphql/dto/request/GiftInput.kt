package com.my.hotel.server.graphql.dto.request

import com.my.hotel.server.commons.FileUpload


data class GiftInput(
    val name: String,
    var picture: FileUpload? = null,
    val hotelId: Long,
    var points: Int,
    var otherInfo: String? = null,
    var language: String? = null
)