package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.Gift
import com.my.hotel.server.data.model.HotelTranslation

data class HotelGift(
    val gift: Gift,
    val hotelTranslation: HotelTranslation
)