package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.Image
import java.time.LocalDateTime

data class UserAddedHotel (
    val id: Long?,
    val name: String?,
    val address: String?,
    val hotelPriceLevel: Long?,
    val photo: Image?,
    val isOnFavorite: Boolean?,
    val isOnWishList: Boolean?,
    val createdAt: LocalDateTime?
)