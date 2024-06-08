package com.my.hotel.server.graphql.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

data class PointsHistoryDto(
    @JsonProperty("user")
    var user: UserDto,
    @JsonProperty("hotel")
    var hotel: MyHotelDto,
    @JsonProperty("loyaltyPoints")
    var pointsSpent: Int?,
    @JsonProperty("id")
    var id: Long? = null,
)