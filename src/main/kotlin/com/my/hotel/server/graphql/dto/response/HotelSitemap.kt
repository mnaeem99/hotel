package com.my.hotel.server.graphql.dto.response

import java.time.LocalDateTime

interface HotelSitemap {
    val hotelId: Long?
    val hotelName: String?
    val localityId: Long?
    val cityName: String?
    val countryName: String?
    val lastModified: LocalDateTime?
}