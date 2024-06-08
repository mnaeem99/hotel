package com.my.hotel.server.service.hotelPrice

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.dto.request.ConfigInput
import com.my.hotel.server.graphql.dto.response.MyHotelDto
import com.my.hotel.server.graphql.dto.response.HotelPriceRangeDto
import com.my.hotel.server.provider.translation.TranslationService
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.stream.Collectors
import kotlin.math.roundToLong

@Service
@Slf4j
class HotelPriceRangeService @Autowired constructor(
    private val hotelPriceRangeRepository: HotelPriceRangeRepository,
    private val hotelPriceLevelRepository: HotelPriceLevelRepository,
    private val myHotelRepository: MyHotelRepository,
    private val hotelPriceConfigRepository: HotelPriceConfigRepository,
    private val translationService: TranslationService
): IHotelPriceRangeService{
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun getPriceLevel(hotelDto: MyHotelDto): HotelPriceLevel? {
        val ranges = hotelPriceRangeRepository.findByHotel(hotelDto.id, hotelDto.placeId)
        if (!ranges.isNullOrEmpty()){
            val average = hotelPriceRangeRepository.findAveragePriceRating(hotelDto.id, hotelDto.placeId) ?: 0F
            return hotelPriceLevelRepository.findByIdOrNull(average.roundToLong())
        }
        return null
    }
    override fun getPriceLevel(hotelId: Long?): Long? {
        val ranges = hotelPriceRangeRepository.findByHotel(hotelId, null)
        if (!ranges.isNullOrEmpty()){
            val average = hotelPriceRangeRepository.findAveragePriceRating(hotelId, null) ?: 0F
            return average.roundToLong()
        }
        return null
    }
    fun isConvergence(hotelDto: MyHotelDto): Boolean {
        val ranges = hotelPriceRangeRepository.findByHotel(hotelDto.id, hotelDto.placeId)
        val config = hotelPriceConfigRepository.findAll()
        val users = config.first().priceRangeUsers ?: 3
        if (ranges!=null && ranges.size >= users){
            val average = hotelPriceRangeRepository.findAveragePriceRating(hotelDto.id, hotelDto.placeId) ?: 0F
            val x = config.first().priceLevelThreshold ?: 0.3F
            for (y in 0..4) {
                if ((y - x) < average && average < (y + x))
                    return false
            }
        }
        return true
    }
    override fun getPriceRangeVotes(hotelDto: MyHotelDto): List<HotelPriceRangeDto>? {
        val ranges = hotelPriceRangeRepository.findByHotel(hotelDto.id, hotelDto.placeId)
        return ranges?.stream()?.map { entity ->
            HotelPriceRangeDto(
                entity.range,
                translationService.mapUserDto(entity.user, entity.user.language ?: Constants.DEFAULT_LANGUAGE),
                translationService.mapmyHotelDto(
                    entity.hotel,
                    entity.user.language ?: Constants.DEFAULT_LANGUAGE
                ),
                entity.id
            )
        }?.collect(Collectors.toList())
    }
    override fun addPriceRange(user: User, hotel: MyHotel, priceRange: HotelPriceRange): Boolean {
        if (hotelPriceRangeRepository.findByUserAndHotel(user, hotel) == null) {
            val hotelPriceRange = hotelPriceRangeRepository.save(priceRange)
            logger.info("New Price Range Added: ${hotelPriceRange.id}")
            logger.info("${user.firstName} ${user.lastName} given price range ${hotelPriceRange.range} on ${hotel.id} ")
            val average = hotelPriceRangeRepository.findAveragePriceRating(hotel.id, null)
            val priceLevel = hotelPriceLevelRepository.findByIdOrNull(average?.roundToLong())
            if (priceLevel != null) {
                hotel.hotelPriceLevel = priceLevel
                myHotelRepository.save(hotel)
            }
            return true
        }
        return false
    }
    override fun deletePriceRange(user: User, hotel: MyHotel) {
        val hotelPriceRange = hotelPriceRangeRepository.findByUserAndHotel(user,hotel)
        if (hotelPriceRange!=null){
            hotelPriceRangeRepository.delete(hotelPriceRange)
            val average = hotelPriceRangeRepository.findAveragePriceRating(hotel.id, null)
            val priceLevel = hotelPriceLevelRepository.findByIdOrNull(average?.roundToLong())
            if (priceLevel != null) {
                hotel.hotelPriceLevel = priceLevel
                myHotelRepository.save(hotel)
            }
        }
    }

    override fun setPriceRangeConfig(input: ConfigInput): HotelPriceConfig {
        val configs = hotelPriceConfigRepository.findAll()
        if (configs.isEmpty()){
            val config = HotelPriceConfig(input.priceLevelThreshold,input.priceRangeUsers)
            return hotelPriceConfigRepository.save(config)
        }
        val config = configs.first()
        if (input.priceRangeUsers!=null)
            config.priceRangeUsers = input.priceRangeUsers
        if (input.priceLevelThreshold!=null)
            config.priceLevelThreshold = input.priceLevelThreshold
        return hotelPriceConfigRepository.save(config)
    }
}