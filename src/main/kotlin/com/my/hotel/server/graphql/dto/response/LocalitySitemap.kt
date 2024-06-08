package com.my.hotel.server.graphql.dto.response

import java.time.LocalDateTime

interface LocalitySitemap {
    val localityId: Long?
    val cityName: String?
    val countryName: String?
    val lastModified: LocalDateTime?
}