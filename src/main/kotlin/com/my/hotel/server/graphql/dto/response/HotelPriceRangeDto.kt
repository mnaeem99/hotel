package com.my.hotel.server.graphql.dto.response

data class HotelPriceRangeDto(
    var range: Int,
    var user: com.my.hotel.server.graphql.dto.response.UserDto,
    var hotel: com.my.hotel.server.graphql.dto.response.MyHotelDto,
    var id: Long? = null,
)