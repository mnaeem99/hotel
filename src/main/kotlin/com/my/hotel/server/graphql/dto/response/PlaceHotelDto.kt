package com.my.hotel.server.graphql.dto.response

interface PlaceHotelDto {
    val countryId: Long?
    val noOfHotel: Int?
    val countryName: String?
    val countryImageUrl: String?
}