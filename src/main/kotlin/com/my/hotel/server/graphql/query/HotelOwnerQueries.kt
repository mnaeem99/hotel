package com.my.hotel.server.graphql.query

import com.my.hotel.server.data.model.TargetAudience
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.service.clients.ClientService
import com.my.hotel.server.service.gift.GiftService
import com.my.hotel.server.service.promotion.PromotionService
import com.my.hotel.server.service.hotelProfile.HotelProfileService
import graphql.kickstart.tools.GraphQLQueryResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

@Component
class HotelOwnerQueries @Autowired constructor(
    val giftService: GiftService,
    val promotionService: PromotionService,
    val clientService: ClientService,
    val hotelProfileService: HotelProfileService
) : GraphQLQueryResolver {

    fun getGifts(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.GiftDto>? {
        return giftService.getGifts(hotelId, language, pageOptions)
    }

    fun getPromotions(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.PromotionDto>?{
        return promotionService.getPromotions(hotelId, language, pageOptions)
    }
    fun getTargetAudiences(): List<TargetAudience>? {
        return promotionService.getTargetAudiences()
    }
    fun getClients(hotelId: Long, keyword: String?, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>?{
        return clientService.getClients(hotelId,keyword,language,pageOptions)
    }
}