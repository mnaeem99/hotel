package com.my.hotel.server.service.clients

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.LoyaltyPoints
import com.my.hotel.server.data.repository.LoyaltyPointRepository
import com.my.hotel.server.data.repository.MyHotelRepository
import com.my.hotel.server.data.repository.UserRepository
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.provider.translation.TranslationService
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated


@Service
@Slf4j
@Validated
class ClientService @Autowired constructor(
    private val userRepository: UserRepository,
    private val myHotelRepository: MyHotelRepository,
    private val translationService: TranslationService,
    private val loyaltyPointRepository: LoyaltyPointRepository
): IClientService{
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun getClients(hotelId: Long, keyword: String?, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>? {
        return userRepository.searchClients(hotelId,keyword, pageOptions.toPageable())?.map { entity -> translationService.mapUserDto(entity, language) }
    }
    override fun addLoyaltyPoints(input: com.my.hotel.server.graphql.dto.request.LoyaltyInput): com.my.hotel.server.graphql.dto.response.LoyaltyPointsDto {
        val hotel = myHotelRepository.findByIdOrNull(input.hotelId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "hotelId")
        val user = userRepository.findByIdOrNull(input.userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "userId")
        val loyaltyPoints = loyaltyPointRepository.findByUserAndHotel(user, hotel)
        if (loyaltyPoints!=null){
            loyaltyPoints.loyaltyPoints = loyaltyPoints.loyaltyPoints?.plus(input.points)
            val newLoyaltyPoints = loyaltyPointRepository.save(loyaltyPoints)
            return com.my.hotel.server.graphql.dto.response.LoyaltyPointsDto(
                translationService.mapUserDto(
                    newLoyaltyPoints.user,
                    input.language ?: Constants.DEFAULT_LANGUAGE
                ),
                translationService.mapmyHotelDto(
                    newLoyaltyPoints.hotel,
                    input.language ?: Constants.DEFAULT_LANGUAGE
                ),
                newLoyaltyPoints.loyaltyPoints,
                newLoyaltyPoints.id
            )
        }
        logger.info("${input.points} Points are added to ${user.firstName + user.lastName} on ${hotel.id}")
        val newLoyaltyPoints = loyaltyPointRepository.save(LoyaltyPoints(user,hotel, input.points))
        return com.my.hotel.server.graphql.dto.response.LoyaltyPointsDto(
            translationService.mapUserDto(
                newLoyaltyPoints.user,
                input.language ?: Constants.DEFAULT_LANGUAGE
            ),
            translationService.mapmyHotelDto(
                newLoyaltyPoints.hotel,
                input.language ?: Constants.DEFAULT_LANGUAGE
            ),
            newLoyaltyPoints.loyaltyPoints,
            newLoyaltyPoints.id
        )
    }
}