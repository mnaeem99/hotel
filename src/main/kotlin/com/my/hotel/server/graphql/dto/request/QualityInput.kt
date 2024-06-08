package com.my.hotel.server.graphql.dto.request

data class QualityInput(
    val name: String?,
    val qualityTypeId: Long?,
    val active: Boolean?
)