package com.my.hotel.server.service.clients

import com.my.hotel.server.graphql.GraphQLPage
import org.springframework.data.domain.Page


interface IClientService {
    fun getClients(hotelId: Long, keyword: String?, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>?
    fun addLoyaltyPoints(input: com.my.hotel.server.graphql.dto.request.LoyaltyInput): com.my.hotel.server.graphql.dto.response.LoyaltyPointsDto
}