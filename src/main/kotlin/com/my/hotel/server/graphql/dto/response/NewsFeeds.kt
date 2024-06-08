package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.Image
import com.my.hotel.server.data.model.Quality
import java.time.LocalDateTime

data class NewsFeeds(
    val postTime: LocalDateTime?,
    val user: com.my.hotel.server.graphql.dto.response.UserDto?,
    val placeName: String?,
    val users: List<com.my.hotel.server.graphql.dto.response.UserDto>?,
    val countUser: Int?,
    val placeDesc: String?,
    val placeImage: Image?,
    val qualities: List<Quality>?,
    val hotelId: Long?,
)
