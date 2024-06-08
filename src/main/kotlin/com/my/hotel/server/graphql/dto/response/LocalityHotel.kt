package com.my.hotel.server.graphql.dto.response

interface LocalityHotel {
    val localityId: Long?
    val localityName: String?
    val localityImageUrl: String?
    val noOfHotel: Int?
}