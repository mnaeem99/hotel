package com.my.hotel.server.service.gift

import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.response.GiftDto
import com.my.hotel.server.graphql.dto.response.MyHotelDto
import org.springframework.data.domain.Page

interface IGiftService {
    fun getGifts(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.GiftDto>?
    fun getAllGifts(language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.GiftDto>?
    fun addGift(input: com.my.hotel.server.graphql.dto.request.GiftInput): com.my.hotel.server.graphql.dto.response.GiftDto?
    fun updateGift(input: com.my.hotel.server.graphql.dto.request.UpdateGift): com.my.hotel.server.graphql.dto.response.GiftDto?
    fun deleteGift(id: Long): Boolean
    fun getGiftAdmin(id: Long, language: String?): com.my.hotel.server.graphql.dto.response.GiftDto?
    fun getGifts(hotelDto: MyHotelDto): List<GiftDto>?
}