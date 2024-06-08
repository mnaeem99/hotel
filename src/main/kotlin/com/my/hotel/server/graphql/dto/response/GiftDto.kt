package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.Image

data class GiftDto(
    var name: String,
    var picture: Image?  = null,
    var points: Int?,
    var otherInfo: String?,
    var hotel: com.my.hotel.server.graphql.dto.response.MyHotelDto?,
    var id: Long? = null,
)