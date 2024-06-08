package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.Favorites
import com.my.hotel.server.data.model.HotelTranslation

data class NewsFeedDto(
    val favorites: Favorites?,
    val hotelTranslation: HotelTranslation?,
    val favCount: Long?,
)