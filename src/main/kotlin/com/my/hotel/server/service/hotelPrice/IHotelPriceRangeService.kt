package com.my.hotel.server.service.hotelPrice

import com.my.hotel.server.data.model.*
import com.my.hotel.server.graphql.dto.request.ConfigInput
import com.my.hotel.server.graphql.dto.response.MyHotelDto
import com.my.hotel.server.graphql.dto.response.HotelPriceRangeDto

interface IHotelPriceRangeService {
    fun getPriceLevel(hotelDto: MyHotelDto): HotelPriceLevel?
    fun getPriceLevel(hotelId: Long?): Long?
    fun getPriceRangeVotes(hotelDto: MyHotelDto): List<HotelPriceRangeDto>?
    fun addPriceRange(user: User, hotel: MyHotel, priceRange: HotelPriceRange): Boolean
    fun deletePriceRange(user: User, hotel: MyHotel)
    fun setPriceRangeConfig(input: ConfigInput): HotelPriceConfig
}