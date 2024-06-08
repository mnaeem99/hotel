package com.my.hotel.server.service.favorites

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.FavoriteHotel
import com.my.hotel.server.graphql.dto.request.UserHotelFilter
import com.my.hotel.server.graphql.dto.response.*
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.security.SecurityUtils
import com.my.hotel.server.service.event.EventService
import com.my.hotel.server.service.event.dto.Event
import com.my.hotel.server.service.hotel.HotelDetailService
import com.my.hotel.server.service.hotelPrice.HotelPriceRangeService
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import java.util.stream.Collectors

@Service
@Slf4j
class FavoriteService @Autowired constructor(
    private val favoriteRepository: FavoriteRepository,
    private val userRepository: UserRepository,
    private val myHotelRepository: MyHotelRepository,
    private val hotelDetailService: HotelDetailService,
    private val qualityRepository: QualityRepository,
    private val hotelPriceRangeService: HotelPriceRangeService,
    private val notificationService: EventService,
    private val dateProvider: DateProvider,
    private val translationService: TranslationService,
    private val loyaltyPointRepository: LoyaltyPointRepository,
    private val hotelRankRepository: HotelRankRepository
    ) : IFavoriteService {

    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun getFavoriteHotels(filter: UserHotelFilter, pageOptions: GraphQLPage): Page<UserAddedHotel>? {
        val user = userRepository.findByIdOrNull(filter.userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "id")
        if (user.isPrivate == true){
            val principal = SecurityUtils.getLoggedInUser()
            if (userRepository.isFollowing(principal.id!!, user.id!!) != null
                || userRepository.isFollowing(user.id!!, principal.id!!) != null
                || principal.id == user.id){
                return favoriteRepository.findByUserLocationKeyword(filter.userId, filter.countryId, filter.language ?: Constants.DEFAULT_LANGUAGE, filter.searchKeyword, pageOptions.toPageable())
            }
            return null
        }
        return favoriteRepository.findByUserLocationKeyword(filter.userId, filter.countryId, filter.language ?: Constants.DEFAULT_LANGUAGE, filter.searchKeyword, pageOptions.toPageable())
    }
    override fun getLocationsOfFavoritesHotel(userId: Long?, language: String?, pageOptions: GraphQLPage): Page<PlaceHotelDto>? {
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "id")
        if (user.isPrivate == true){
            val principal = SecurityUtils.getLoggedInUser()
            if (userRepository.isFollowing(principal.id!!, user.id!!) != null
                || userRepository.isFollowing(user.id!!, principal.id!!) != null
                || principal.id == user.id){
                return myHotelRepository.findFavoriteHotelLocation(userId, language?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())
            }
            return null
        }
        return myHotelRepository.findFavoriteHotelLocation(userId, language?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())
    }
    override fun addToFavorite(favoriteHotel: FavoriteHotel): Boolean {
        val principal = SecurityUtils.getLoggedInUser()
        val hotel = hotelDetailService.getmyHotel(favoriteHotel.hotelId,favoriteHotel.placeId, principal.language?: Constants.DEFAULT_LANGUAGE)
        val favorite = favoriteRepository.findByUserAndHotel(principal, hotel)
        if (!favoriteHotel.removeHotel && favorite == null) {
            val favorites = favoriteRepository.save(Favorites(principal, hotel,postTime = dateProvider.getCurrentDateTime()))
            if (!favoriteHotel.hotelQuality.isNullOrEmpty()){
                setFavoriteQualities(favorites,favoriteHotel.hotelQuality)
            }
            if (favoriteHotel.priceRange!=null) {
                hotelPriceRangeService.addPriceRange(principal, hotel, HotelPriceRange(favoriteHotel.priceRange, principal, hotel))
            }
            logger.info("New Favorites Added: ${favorites.id}")
            logger.info("${principal.firstName} ${principal.lastName} added: ${hotel.id} in favorites") // working on this line
            notificationService.createEvent(Event(NotificationType.FRIEND_FAVORITE,principal.id,principal.id,hotel.id))
            return true
        }
        else if(favoriteHotel.removeHotel && favorite != null){
            logger.info("Removed hotel ${hotel.id} from Favorites: ${favorite.id}")
            logger.info("${principal.firstName} ${principal.lastName} removed: ${hotel.id} from favorites")
            favoriteRepository.deleteById(favorite.id!!)
            hotelPriceRangeService.deletePriceRange(principal,hotel)
            return true
        }
        return false
    }

    override fun addhotelsToFavorite(favoriteHotels: List<FavoriteHotel>): Boolean{
        favoriteHotels.stream().map { entity -> addToFavorite(entity) }?.collect(Collectors.toList())
        return true
    }

    fun setFavoriteQualities(favorites: Favorites, qualityIds: List<Long>): Boolean {
        val qualities = qualityRepository.findAllById(qualityIds)
        if (favorites.quality.isNullOrEmpty()){
            favorites.quality = qualities
            favoriteRepository.save(favorites)
            return true
        }
        for (quality in qualities) {
            if (!favorites.quality?.contains(quality)!!) {
                favorites.quality = favorites.quality?.plus(quality)
                favoriteRepository.save(favorites)
            }
        }
        return true
    }
    override fun addPriceRange(input: com.my.hotel.server.graphql.dto.request.PriceRange): Boolean {
        val user = userRepository.findByIdOrNull(input.userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "userId")
        val hotel = hotelDetailService.getmyHotel(input.hotelId,input.placeId, user.language?: Constants.DEFAULT_LANGUAGE)
        return hotelPriceRangeService.addPriceRange(user, hotel, HotelPriceRange(input.range, user, hotel))
    }
    override fun getUsersWhoAddedhotelToFavorites(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<UserDto>? {
        val users = userRepository.findByFavoriteHotel(hotelId, null,pageOptions.toPageable()).map { entity -> translationService.mapUserDto(entity, language) }
        return users
    }
    override fun getUsersWhoAddedQuality(hotelId: Long, qualityId: Long, language: String?, pageOptions: GraphQLPage): Page<UserDto>? {
        val hotel = myHotelRepository.findByIdOrNull(hotelId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "hotelId")
        val principal = SecurityUtils.getLoggedInUser()
        if (favoriteRepository.findByUserAndHotel(principal, hotel) != null) {
            val users = userRepository.findByHotelQuality(hotelId, null, qualityId, pageOptions.toPageable()).map { entity -> translationService.mapUserDto(entity, language) }
            return users
        }
        return null
    }
    override fun isOnFavorite(hotelDto: MyHotelDto): Boolean {
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal !is User)
            return false
        return favoriteRepository.findByUserAndHotel(principal.id!!, hotelDto.id, hotelDto.placeId) != null
    }

    override fun usersLoyaltyPoints(hotelDto: MyHotelDto): Int? {
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal !is User)
            return null
        val loyaltyPoints = loyaltyPointRepository.findByUserAndHotel(principal.id!!,hotelDto.id, hotelDto.placeId) ?: return 0
        return loyaltyPoints.loyaltyPoints
    }
    override fun favoriteHotelQuality(hotelDto: MyHotelDto): List<HotelQualityDto>? {
        val qualities = favoriteRepository.findhotelQualities(hotelDto.id, hotelDto.placeId, GraphQLPage(0,3).toPageable()) ?: return null
        return qualities.content.map { quality -> tohotelQualities(quality,hotelDto) }
    }
    override fun getQualityRanking(hotelDto: MyHotelDto, cityId: Long?): List<RankingQuality>? {
        return favoriteRepository.findRankingQuality(hotelDto.id, cityId)
    }
    override fun getCityRanking(hotelDto: MyHotelDto, cityId: Long?): Int? {
        return favoriteRepository.findRankingByCity(hotelDto.id, cityId)
    }
    override fun getGainLossRank(hotelDto: MyHotelDto): Int? {
        if (hotelDto.localityRanking==null){
            return null
        }
        val currentTime = dateProvider.getCurrentDateTime()
        val hotelRanks = hotelRankRepository.findLatestRank(hotelDto.id, GraphQLPage(0,1).toPageable())
        if (hotelRanks.isEmpty){
            val hotelRank = HotelRank(MyHotel(id = hotelDto.id),hotelDto.localityRanking!!,11,currentTime)
            hotelRankRepository.save(hotelRank)
            return hotelRank.previousRank!! - hotelRank.currentRank
        }
        val hotelRank = hotelRanks.content[0]
        if (hotelRank.updatedAt.monthValue == currentTime.monthValue && hotelRank.updatedAt.year == currentTime.year){
            return hotelRank.previousRank!! - hotelRank.currentRank
        }
        hotelRank.previousRank = hotelRank.currentRank
        hotelRank.currentRank = hotelDto.localityRanking!!
        hotelRank.updatedAt = currentTime
        hotelRankRepository.save(hotelRank)
        return hotelRank.previousRank!! - hotelRank.currentRank
    }

    override fun usersWhoAddedHotel(hotelDto: MyHotelDto): HotelUsers? {
        val users = userRepository.findByFavoriteHotel(hotelDto.id, hotelDto.placeId, GraphQLPage(0,5).toPageable())
        val usersDto = users.content.stream().map { entity ->  translationService.mapUser(entity, null) }?.collect(Collectors.toList())
        return HotelUsers(users.totalElements.toInt(), usersDto)
    }
    private fun tohotelQualities(quality: Quality, hotel: MyHotelDto): HotelQualityDto {
        val users = userRepository.findByHotelQuality(hotel.id, hotel.placeId, quality.id!!, GraphQLPage(0, 5).toPageable())
        val usersDto = users.content.stream().map { entity ->  translationService.mapUser(entity, null) }?.collect(Collectors.toList())
        return HotelQualityDto(
            users.totalElements.toInt(),
            usersDto,
            quality
        )
    }


}