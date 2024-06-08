package com.my.hotel.server.data.repository.customFavoriteRepository

import org.locationtech.jts.geom.Point
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface FavoriteRepositoryCustom {
    fun findMostAddedHotel(userId: Long?, countryId: Long?, point: Point?, language: String, pageable: Pageable) : Page<com.my.hotel.server.graphql.dto.response.NewsFeedDto>
    fun findFriendsAddedHotel(userId: Long, language: String, pageable: Pageable) : Page<com.my.hotel.server.graphql.dto.response.NewsFeedDto>
}