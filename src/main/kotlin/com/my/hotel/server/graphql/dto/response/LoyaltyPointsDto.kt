package com.my.hotel.server.graphql.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.io.Serializable

data class LoyaltyPointsDto(
    @JsonProperty("user")
    var user: UserDto,
    @JsonProperty("hotel")
    var hotel: MyHotelDto,
    @JsonProperty("loyaltyPoints")
    var loyaltyPoints: Int?,
    @JsonProperty("id")
    var id: Long? = null,
): Serializable