package com.my.hotel.server.graphql.dto.response

import java.time.LocalDateTime

interface CitySitemap {
    val cityName: String?
    val countryName: String?
    val lastModified: LocalDateTime?
}