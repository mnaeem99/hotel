package com.my.hotel.server.service.favorites

import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.FavoriteHotel
import com.my.hotel.server.graphql.dto.request.UserHotelFilter
import com.my.hotel.server.graphql.dto.response.MyHotelDto
import com.my.hotel.server.graphql.dto.response.RankingQuality
import com.my.hotel.server.graphql.dto.response.HotelQualityDto
import com.my.hotel.server.graphql.dto.response.HotelUsers
import org.springframework.data.domain.Page

interface IFavoriteService {
    fun getFavoriteHotels(filter: UserHotelFilter, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserAddedHotel>?
    fun getLocationsOfFavoritesHotel(userId: Long?, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.PlaceHotelDto>?
    fun addToFavorite(favoriteHotel: FavoriteHotel): Boolean
    fun addhotelsToFavorite(favoriteHotels: List<FavoriteHotel>): Boolean
    fun addPriceRange(input: com.my.hotel.server.graphql.dto.request.PriceRange): Boolean
    fun getUsersWhoAddedhotelToFavorites(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>?
    fun getUsersWhoAddedQuality(hotelId: Long, qualityId: Long, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>?
    fun isOnFavorite(hotelDto: MyHotelDto): Boolean
    fun usersLoyaltyPoints(hotelDto: MyHotelDto): Int?
    fun favoriteHotelQuality(hotelDto: MyHotelDto): List<HotelQualityDto>?
    fun getQualityRanking(hotelDto: MyHotelDto, cityId: Long?): List<RankingQuality>?
    fun getCityRanking(hotelDto: MyHotelDto, cityId: Long?): Int?
    fun usersWhoAddedHotel(hotelDto: MyHotelDto): HotelUsers?
    fun getGainLossRank(hotelDto: MyHotelDto): Int?
}