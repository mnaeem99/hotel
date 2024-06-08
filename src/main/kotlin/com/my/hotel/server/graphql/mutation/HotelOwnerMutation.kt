package com.my.hotel.server.graphql.mutation

import com.my.hotel.server.service.clients.ClientService
import com.my.hotel.server.service.gift.GiftService
import com.my.hotel.server.service.promotion.PromotionService
import com.my.hotel.server.service.hotelProfile.HotelProfileService
import graphql.kickstart.tools.GraphQLMutationResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class HotelOwnerMutation @Autowired constructor(
    private val giftService: GiftService,
    private val promotionService: PromotionService,
    private val clientService: ClientService,
    private val hotelProfileService: HotelProfileService
): GraphQLMutationResolver {

    fun addGift(input: com.my.hotel.server.graphql.dto.request.GiftInput): com.my.hotel.server.graphql.dto.response.GiftDto? {
        return giftService.addGift(input)
    }
    fun updateGift(input: com.my.hotel.server.graphql.dto.request.UpdateGift): com.my.hotel.server.graphql.dto.response.GiftDto? {
        return giftService.updateGift(input)
    }
    fun deleteGift(id: Long): Boolean {
        return giftService.deleteGift(id)
    }
    fun addPromotion(input: com.my.hotel.server.graphql.dto.request.PromotionInput): com.my.hotel.server.graphql.dto.response.PromotionDto?{
        return promotionService.addPromotion(input)
    }
    fun updatePromotion(input: com.my.hotel.server.graphql.dto.request.UpdatePromotion): com.my.hotel.server.graphql.dto.response.PromotionDto?{
        return promotionService.updatePromotion(input)
    }
    fun deletePromotion(id: Long): Boolean{
        return promotionService.deletePromotion(id)
    }
    fun addLoyaltyPoints(input: com.my.hotel.server.graphql.dto.request.LoyaltyInput): com.my.hotel.server.graphql.dto.response.LoyaltyPointsDto {
        return clientService.addLoyaltyPoints(input)
    }
    fun addHotel(input: com.my.hotel.server.graphql.dto.request.HotelInput): com.my.hotel.server.graphql.dto.response.MyHotelDto?{
        return hotelProfileService.addHotel(input)
    }
    fun updateHotel(input: com.my.hotel.server.graphql.dto.request.UpdateHotel): com.my.hotel.server.graphql.dto.response.MyHotelDto?{
        return hotelProfileService.updateHotel(input)
    }
    fun hotelVerificationAppointment(input: com.my.hotel.server.graphql.dto.request.HotelVerificationAppointmentInput): com.my.hotel.server.graphql.dto.response.HotelVerificationAppointmentDto?{
        return hotelProfileService.hotelVerificationAppointment(input)
    }
}