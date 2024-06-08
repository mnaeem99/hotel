package com.my.hotel.server.graphql.dto.request

import com.my.hotel.server.commons.FileUpload

data class UpdatePromotion(
    var title: String?=null,
    var titleColor: String?=null,
    var subTitle: String?=null,
    var subTitleColor: String?=null,
    var buttonText: String?=null,
    var buttonColor: String?=null,
    var budget: Int?=null,
    var duration: Int?=null,
    var showLogo: Boolean?=null,
    var cover: FileUpload?=null,
    var geolat: Float?=null,
    var geolong: Float?=null,
    var radius: Float?=null,
    var region: String?=null,
    var active: Boolean?=null,
    var id: Long,
    var targetAudienceId: List<Long>?=null,
    var language: String? = null
)
