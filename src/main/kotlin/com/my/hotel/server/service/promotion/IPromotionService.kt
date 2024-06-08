package com.my.hotel.server.service.promotion

import com.my.hotel.server.data.model.TargetAudience
import com.my.hotel.server.graphql.GraphQLPage
import org.springframework.data.domain.Page

interface IPromotionService {
    fun getPromotions(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.PromotionDto>?
    fun getTargetAudiences(): List<TargetAudience>?
    fun addPromotion(input: com.my.hotel.server.graphql.dto.request.PromotionInput): com.my.hotel.server.graphql.dto.response.PromotionDto?
    fun updatePromotion(input: com.my.hotel.server.graphql.dto.request.UpdatePromotion): com.my.hotel.server.graphql.dto.response.PromotionDto?
    fun deletePromotion(id: Long): Boolean
}