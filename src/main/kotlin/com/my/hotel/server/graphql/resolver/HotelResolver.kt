package com.my.hotel.server.graphql.resolver

import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.dto.response.*
import com.my.hotel.server.graphql.security.Unsecured
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.service.favorites.FavoriteService
import com.my.hotel.server.service.gift.GiftService
import com.my.hotel.server.service.hotelPrice.HotelPriceRangeService
import com.my.hotel.server.service.wishlist.WishListService
import graphql.kickstart.tools.GraphQLResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component


@Component
class HotelResolver @Autowired constructor(
    val wishListService: WishListService,
    val favoriteService: FavoriteService,
    val giftService: GiftService,
    val myHotelRepository: MyHotelRepository,
    val loyaltyPointRepository: LoyaltyPointRepository,
    val userRepository: UserRepository,
    val hotelPriceRangeService: HotelPriceRangeService,
    val translationService: TranslationService
) : GraphQLResolver<MyHotelDto> {
    fun getmyHotel(hotelDto: MyHotelDto): MyHotel? {
        if (hotelDto.id!= null){
            return myHotelRepository.findByIdOrNull(hotelDto.id)
        }else if (hotelDto.placeId!=null){
            return myHotelRepository.findByPlaceId(hotelDto.placeId)
        }
        return null
    }
    @Unsecured
    fun getCity(hotelDto: MyHotelDto, language: String?): CityDto? {
        val hotel = getmyHotel(hotelDto) ?: return null
        return translationService.mapCityDto(hotel.city,language)
    }
    @Unsecured
    fun getLocality(hotelDto: MyHotelDto, language: String?): LocalityDto? {
        val hotel = getmyHotel(hotelDto) ?: return null
        return translationService.mapLocalityDto(hotel.locality, hotel.city?.id,language)
    }
    fun getPriceRangeVotes(hotelDto: MyHotelDto): List<HotelPriceRangeDto>? {
        return hotelPriceRangeService.getPriceRangeVotes(hotelDto)
    }
    fun addPriceRange(hotelDto: MyHotelDto): Boolean? {
        if (hotelDto.googlePriceLevel != null)
            return false
        return hotelPriceRangeService.isConvergence(hotelDto)
    }
    fun isOnWishlist(hotelDto: MyHotelDto): Boolean {
        return wishListService.isOnWishlist(hotelDto)
    }
    fun isOnFavorite(hotelDto: MyHotelDto): Boolean {
        return favoriteService.isOnFavorite(hotelDto)
    }
    @Unsecured
    fun getGifts(hotelDto: MyHotelDto): List<GiftDto>? {
        return giftService.getGifts(hotelDto)
    }
    @Unsecured
    fun gethotelPriceLevel(hotelDto: MyHotelDto): HotelPriceLevel? {
        if (hotelDto.hotelPriceLevel==null)
            return HotelPriceLevel(HotelPriceLevel.HotelPrice.Inexpensive, 1)
        return hotelDto.hotelPriceLevel
    }
    fun usersLoyaltyPoints(hotelDto: MyHotelDto): Int? {
        return favoriteService.usersLoyaltyPoints(hotelDto)
    }
    fun favoriteHotelQuality(hotelDto: MyHotelDto): List<HotelQualityDto>? {
        return favoriteService.favoriteHotelQuality(hotelDto)
    }
    @Unsecured
    fun getQualityRanking(hotelDto: MyHotelDto, cityId: Long?): List<RankingQuality>? {
        return favoriteService.getQualityRanking(hotelDto, cityId)
    }
    @Unsecured
    fun getCityRanking(hotelDto: MyHotelDto, cityId: Long?): Int? {
        return favoriteService.getCityRanking(hotelDto, cityId)
    }
    @Unsecured
    fun getGainLossRank(hotelDto: MyHotelDto): Int? {
        return favoriteService.getGainLossRank(hotelDto)
    }
    @Unsecured
    fun usersWhoAddedHotel(hotelDto: MyHotelDto): HotelUsers? {
        return favoriteService.usersWhoAddedHotel(hotelDto)
    }
}
