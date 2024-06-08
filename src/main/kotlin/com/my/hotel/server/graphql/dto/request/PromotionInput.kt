package com.my.hotel.server.graphql.dto.request

import com.my.hotel.server.commons.FileUpload

data class PromotionInput(
    var title: String?,
    var titleColor: String?,
    var subTitle: String?,
    var subTitleColor: String?,
    var buttonText: String?,
    var buttonColor: String?,
    var budget: Int?,
    var duration: Int?,
    var showLogo: Boolean?,
    var cover: FileUpload?,
    var geolat: Float?,
    var geolong: Float?,
    var radius: Float?,
    var region: String?,
    var active: Boolean?,
    var hotelId: Long,
    var targetAudienceId: List<Long>,
    var language: String? = null
)
