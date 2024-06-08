package com.my.hotel.server.graphql.dto.request

data class UpdateQuality(
    var name: String?=null,
    var qualityTypeId: Long?=null,
    var id: Long? = null,
    var active: Boolean?=null
)
