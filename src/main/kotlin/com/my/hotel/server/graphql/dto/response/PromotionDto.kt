package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.Image
import com.my.hotel.server.data.model.TargetAudience
import java.time.LocalDateTime

data class PromotionDto(
    var title: String?,
    var titleColor: String?,
    var subTitle: String?,
    var subTitleColor: String?,
    var buttonText: String?,
    var buttonColor: String?,
    var budget: Int?,
    var duration: Int?,
    var showLogo: Boolean?,
    var cover: Image?,
    var geolat: Float?,
    var geolong: Float?,
    var radius: Float?,
    var region: String?,
    var active: Boolean?,
    var hotel: com.my.hotel.server.graphql.dto.response.MyHotelDto,
    var targetAudience: List<TargetAudience>?,
    var createdAt: LocalDateTime?,
    var modifiedAt: LocalDateTime?,
    var id: Long? = null,
)
