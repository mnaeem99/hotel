package com.my.hotel.server.service.explore

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.commons.SpatialUtils
import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.LocationFilter
import com.my.hotel.server.graphql.dto.request.QueryFilter
import com.my.hotel.server.graphql.dto.response.*
import com.my.hotel.server.graphql.error.ValidationErrorCustomException
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.security.SecurityUtils
import com.my.hotel.server.service.event.EventService
import com.my.hotel.server.service.event.dto.Event
import com.my.hotel.server.service.hotel.RegionDetailService
import com.my.hotel.server.service.hotel.HotelSearchService
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Slf4j
@Service
class ExploreService @Autowired constructor(
    private val myHotelRepository: MyHotelRepository,
    private val translationService: TranslationService,
    private val userRepository: UserRepository,
    private val dateProvider: DateProvider,
    private val regionDetailService: RegionDetailService,
    private val suggestionRepository: SuggestionRepository,
    private val hotelPriceLevelRepository: HotelPriceLevelRepository,
    private val eventService: EventService,
    private val googleHotelRepository: GoogleHotelRepository,
    private val hotelSearchService: HotelSearchService,
    private val searchQueryRepository: SearchQueryRepository,
) : IExploreService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun getSuggestions(location: LocationFilter): SuggestionDto {
        if(!SpatialUtils.isValidLatLang(location.latitude, location.longitude)) {
            throw ValidationErrorCustomException(Constants.INVALID_LOCATION)
        }
        val user = SecurityUtils.getPrincipalUser()
        val userTime = dateProvider.getDateTimeZone(user?.timezoneId)
        val nextSuggestionTime = userTime.toLocalDate().atStartOfDay().plusDays(1)
        val lastRequestTime = suggestionRepository.getLastRequestTime(user?.id)
        if (lastRequestTime!=null && lastRequestTime.toLocalDate().isEqual(dateProvider.getCurrentDate())){
            return getPreviousSuggestion(user?.id, location, nextSuggestionTime)
        }
        val hotels = calculateSuggestion(user?.id, lastRequestTime ?: dateProvider.getCurrentDateTime().minusDays(7), location)
        if (hotels.isNotEmpty() && user?.id!=null) {
            val hotelIds = hotels.filter { it.id != null }.map { it.id!! }
            val placeIds = hotels.filter { it.id == null && it.placeId != null }.map { it.placeId!! }
            eventService.createEvent(Event(NotificationType.SUGGESTED_HISTORY, user.id, null, null, hotelIds,null, placeIds))
        }
        return SuggestionDto(hotels, null, nextSuggestionTime)
    }
    override fun newSuggestionsAvailable(): Boolean {
        val userId = SecurityUtils.getLoggedInUserId()
        val lastRequestTime = suggestionRepository.getLastRequestTime(userId)
        return !(lastRequestTime!=null && lastRequestTime.toLocalDate().isEqual(dateProvider.getCurrentDate()))
    }

    private fun getPreviousSuggestion(userId: Long?, location: LocationFilter, nextSuggestionTime: LocalDateTime): SuggestionDto {
        var hotels = emptyList<MyHotelDto>()
        val startOfDay: LocalDateTime = dateProvider.getCurrentDate().atStartOfDay()
        val endOfDay: LocalDateTime = dateProvider.getCurrentDate().plusDays(1).atStartOfDay()
        val myHotels = suggestionRepository.getTodaySuggestion(userId, location.language ?: Constants.DEFAULT_LANGUAGE, startOfDay, endOfDay)
        val googlehotels = suggestionRepository.getTodayGoogleSuggestion(userId, startOfDay, endOfDay)
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        if (myHotels.isNotEmpty()) {
            hotels = hotels.plus(myHotels.map { entity -> entity.tohotelDto() })
        }
        if (googlehotels.isNotEmpty()) {
            hotels = hotels.plus(googlehotels.map { entity -> entity.tomyHotelDto() })
        }
        hotels = hotels.sortedBy { it.hotelPriceLevel?.id }
        return SuggestionDto(hotels, Constants.ALREADY_GET_SUGGESTIONS + nextSuggestionTime.format(formatter), nextSuggestionTime)
    }

    private fun calculateSuggestion(userId: Long?, dateTime: LocalDateTime, location: LocationFilter): List<MyHotelDto> {
        var hotels: List<MyHotelDto> = emptyList()
        val suggestionHistory = suggestionRepository.findUserSuggestion(userId, dateTime, dateProvider.getCurrentDateTime().minusDays(7))
        val googleSuggestionHistory = suggestionRepository.findUserGoogleSuggestion(userId, dateTime, dateProvider.getCurrentDateTime().minusDays(7))
        val priceLevels = hotelPriceLevelRepository.findAll()
        var isFetchedGoogle = false
        for (priceLevel in priceLevels) {
            val priceLevelhotels: List<MyHotelDto> = myHotelSuggestions(suggestionHistory, userId, location, dateTime, priceLevel)
            hotels = hotels.plus(priceLevelhotels)
            if (priceLevelhotels.isEmpty() || priceLevelhotels.size < 2){
                if (!isFetchedGoogle){
                    googleSearch(location)
                    isFetchedGoogle = true
                }
                val googlehotels = googlehotelSuggestions(googleSuggestionHistory, location, userId, priceLevelhotels.getOrNull(0)?.placeId, priceLevel, GraphQLPage(0, 2 - priceLevelhotels.size).toPageable())
                hotels = hotels.plus(googlehotels)
            }
        }
        val noOfOnePriceLevel = hotels.count { it.hotelPriceLevel?.id?.toInt() == 1 }
        val remaininghotel = 2 - noOfOnePriceLevel
        if (remaininghotel > 0){
            val googlehotels = googleHotelRepository.findSuggestionhotelForNoPriceLevel(SpatialUtils.createCircle(location.latitude, location.longitude, 0.5), location.language ?: Constants.DEFAULT_LANGUAGE, userId, GraphQLPage(0,remaininghotel).toPageable()).content
            hotels = hotels.plus(googlehotels.map {  googlehotel -> googlehotel.tomyHotelDto() })
        }
        hotels = hotels.sortedBy { it.hotelPriceLevel?.id }
        return hotels
    }

    private fun googleSearch(location: LocationFilter) {
        val foundQuery = searchQueryRepository.findByInput(SpatialUtils.createCircle(location.latitude, location.longitude, 0.1), null, location.language ?: Constants.DEFAULT_LANGUAGE, SpatialUtils.getPoint(location.latitude.toFloat(), location.longitude.toFloat()))
        if (foundQuery.isNullOrEmpty())
            hotelSearchService.getAllhotelsFromGoogle(QueryFilter(null, location.latitude, location.longitude, location.language))
    }
    private fun myHotelSuggestions(suggestionHistory: List<Long>?, userId: Long?, location: LocationFilter, dateTime: LocalDateTime, priceLevel: HotelPriceLevel): List<MyHotelDto> {
        val priceLevelhotels: List<MyHotelDto> = if (suggestionHistory.isNullOrEmpty()) {
            myHotelRepository.findSuggestionHotel(userId, SpatialUtils.createCircle(location.latitude, location.longitude, 0.5), location.language ?: Constants.DEFAULT_LANGUAGE, dateTime, priceLevel.id, GraphQLPage(0, 2).toPageable())
                .content.map { dto -> dto.tohotelDto() }
        } else {
            myHotelRepository.findSuggestionHotel(userId, SpatialUtils.createCircle(location.latitude, location.longitude, 0.5), location.language ?: Constants.DEFAULT_LANGUAGE, dateTime, priceLevel.id, suggestionHistory, GraphQLPage(0, 2).toPageable())
                .content.map { dto -> dto.tohotelDto() }
        }
        return priceLevelhotels
    }
    private fun googlehotelSuggestions(suggestionHistory: List<String>?, location: LocationFilter, userId: Long?, placeId: String?, priceLevel: HotelPriceLevel, pageable: Pageable): List<MyHotelDto> {
        val priceLevelhotels: List<MyHotelDto> = if (suggestionHistory.isNullOrEmpty()) {
            googleHotelRepository.findSuggestionHotel(SpatialUtils.createCircle(location.latitude, location.longitude, 0.5), location.language ?: Constants.DEFAULT_LANGUAGE, userId, priceLevel.id, placeId, pageable)
                .content.map { dto -> dto.tomyHotelDto() }
        } else {
            googleHotelRepository.findSuggestionHotel(SpatialUtils.createCircle(location.latitude, location.longitude, 0.5), location.language ?: Constants.DEFAULT_LANGUAGE, userId, priceLevel.id, placeId, suggestionHistory, pageable)
                .content.map { dto -> dto.tomyHotelDto() }
        }
        return priceLevelhotels
    }


    override fun getTopTrending(location: LocationFilter, pageOptions: GraphQLPage): Page<MyHotelDto>? {
        if(!SpatialUtils.isValidLatLang(location.latitude, location.longitude)) {
            throw ValidationErrorCustomException(Constants.INVALID_LOCATION)
        }
        val hotels = myHotelRepository.findMostAddedHotel(SpatialUtils.createCircle(location.latitude, location.longitude,0.5),location.language?:Constants.DEFAULT_LANGUAGE, dateProvider.getPreviousDate(),pageOptions.toPageable())
        return hotels?.map { entity -> entity.tohotelDto() }
    }
    override fun getFriendsTrending(location: LocationFilter, pageOptions: GraphQLPage): Page<MyHotelDto>? {
        if(!SpatialUtils.isValidLatLang(location.latitude, location.longitude)) {
            throw ValidationErrorCustomException(Constants.INVALID_LOCATION)
        }
        val principal = SecurityUtils.getLoggedInUser()
        return myHotelRepository.findByFollowing(SpatialUtils.createCircle(location.latitude, location.longitude,0.5), principal.id, location.language?:Constants.DEFAULT_LANGUAGE, dateProvider.getPreviousDate(), pageOptions.toPageable())?.map { entity -> entity.tohotelDto() }
    }
    override fun searchUser(filters: QueryFilter, pageOptions: GraphQLPage): Page<UserDto>? {
        if (filters.latitude!=null && filters.longitude != null && !SpatialUtils.isValidLatLang(filters.latitude, filters.longitude)) {
            throw ValidationErrorCustomException(Constants.INVALID_LOCATION)
        }
        var countryId = SecurityUtils.getLoggedInUser().country?.id
        if(countryId == null) {
            countryId = regionDetailService.getGoogleCountry(QueryFilter(null, filters.latitude, filters.longitude))
        }
        val users = userRepository.findByLocation(filters.userQuery, countryId, filters.language, pageOptions.toPageable()).map { entity -> translationService.mapUserDto(entity, filters.language) }
        return users
    }

    override fun executeSuggestions(event: Event) {
        val user = userRepository.findByIdOrNull(event.sentUser!!)
        if (user!=null){
            if(!event.hotelIds.isNullOrEmpty()) {
                val hotels = myHotelRepository.findAllById(event.hotelIds)
                hotels.stream().forEach { hotel -> addmyHotelSuggestion(user, hotel) }
            }
            if(!event.placeIds.isNullOrEmpty()) {
                val hotels = googleHotelRepository.findAllById(event.placeIds)
                hotels.stream().forEach { hotel -> addGooglehotelSuggestion(user, hotel) }
            }
        }
    }
    private fun addmyHotelSuggestion(user: User, hotel: MyHotel) {
        val suggestionFound = suggestionRepository.findByUserAndmyHotel(user, hotel)
        if (suggestionFound == null){
            val newSuggestion = Suggestion(user, hotel, null, dateProvider.getCurrentDateTime())
            suggestionRepository.save(newSuggestion)
        }
        else {
            suggestionFound.createdAt = dateProvider.getCurrentDateTime()
            suggestionRepository.save(suggestionFound)
        }
    }
    private fun addGooglehotelSuggestion(user: User, hotel: GoogleHotel) {
        val suggestionFound = suggestionRepository.findByUserAndGoogleHotel(user, hotel)
        if (suggestionFound == null){
            val newSuggestion = Suggestion(user, null, hotel, dateProvider.getCurrentDateTime())
            suggestionRepository.save(newSuggestion)
        }
        else {
            suggestionFound.createdAt = dateProvider.getCurrentDateTime()
            suggestionRepository.save(suggestionFound)
        }
    }

}