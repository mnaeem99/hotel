package com.my.hotel.server.service.web

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.response.CountryDto
import com.my.hotel.server.graphql.dto.response.LocalityDto
import com.my.hotel.server.data.model.Language
import com.my.hotel.server.graphql.dto.response.*
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.service.aws.AWSService
import com.my.hotel.server.service.cache.CacheService
import com.my.hotel.server.service.hotel.HotelSearchService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service


@Service
class WebService @Autowired constructor(
    private val countryRepository: CountryRepository,
    private val favoriteRepository: FavoriteRepository,
    private val myHotelRepository: MyHotelRepository,
    private val cityRepository: CityRepository,
    private val localityRepository: LocalityRepository,
    private val userRepository: UserRepository,
    private val cacheService: CacheService,
    private val translationService: TranslationService,
    private val awsService: AWSService,
    private val hotelSearchService: HotelSearchService,
    private val languageRepository: LanguageRepository,
    private val hotelTranslationRepository: HotelTranslationRepository,
) : IWebService {
    override fun getCountries(language: String?, pageOptions: GraphQLPage): Page<CountryDto> {
        return countryRepository.findAllCountries(language ?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable()).map { entity ->
            CountryDto(
                entity.name,
                entity.code,
                entity.picture,
                entity.flag,
                entity.id
            )
        }
    }
    override fun getFeaturedCountries(language: String?, pageOptions: GraphQLPage): Page<CountryDto> {
        return countryRepository.findMostAddedCountries(language ?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable()).map { entity ->
            CountryDto(
                entity.name,
                entity.code,
                entity.picture,
                entity.flag,
                entity.id
            )
        }
    }
    override fun getFeaturedChefs(countryId: Long?, language: String?, pageOptions: GraphQLPage): Page<Chef>? {
        val chefs = userRepository.findByChefs(countryId, pageOptions.toPageable())
        if (chefs!=null && !chefs.isEmpty)
            return chefs
        return userRepository.findByChefs(null, pageOptions.toPageable())
    }
    override fun getPopularSearches(countryId: Long): List<String>? {
        return favoriteRepository.findPopularSearches(countryId, GraphQLPage(0,10).toPageable())?.content
    }
    override fun getNeighborhood(cityId: Long, language: String?, pageOptions: GraphQLPage): Page<LocalityDto>? {
        return localityRepository.findByCity(cityId, language ?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable()).map { dto -> LocalityDto(dto.name,dto.picture,dto.placeId,dto.id,dto.cityId) }
    }
    override fun getCities(countryId: Long, language: String?, pageOptions: GraphQLPage): Page<CityDto>? {
        return cityRepository.findByCountry(countryId, language ?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())?.map { entity ->
            CityDto(
                entity.name,
                entity.picture,
                entity.locality,
                entity.placeId,
                entity.id,
                entity.countryId
            )
        }
    }
    override fun getLocalityHotel(countryId: Long?, cityId:Long?, language: String?, pageOptions: GraphQLPage): Page<LocalityHotel>? {
        if (cityId == null && countryId != null){
            return localityRepository.findByMostHotelCountry(countryId,language ?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())
        }
        if (cityId!=null) {
            val hotels = localityRepository.findByMostHotelCity(cityId, language ?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())
            if (hotels != null && !hotels.isEmpty)
                return hotels
            return localityRepository.findByMostHotelCountry(countryId ?: countryRepository.findByCity(cityId),language ?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())
        }
        return localityRepository.findByMostHotel(language?:Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())
    }
    override fun gethotelsByLocality(localityId: Long, language: String?, pageOptions: GraphQLPage): Page<MyHotelDto>?{
        val page = hotelTranslationRepository.findByLocalities(localityId, language ?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())
        val content = page?.content ?: emptyList()
        val startIndex = pageOptions.toPageable().offset.toInt() ?: 0
        val hotels = content.mapIndexed { index, entity -> translationService.mapmyHotelDto(entity, startIndex + index + 1) }
        return PageImpl(hotels, pageOptions.toPageable(), page?.totalElements ?: 0)
    }
    override fun getTopTrending(countryId: Long?, cityId: Long?, language: String?, pageOptions: GraphQLPage): Page<MyHotelDto>? {
        if (cityId == null)
            return myHotelRepository.findMostAddedhotelByCountry(countryId,language?:Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())?.map { entity -> entity.tohotelDto() }
        val hotels = myHotelRepository.findMostAddedhotelByCity(cityId,language?:Constants.DEFAULT_LANGUAGE,pageOptions.toPageable())
        if (hotels!=null && !hotels.isEmpty)
            return hotels.map { entity -> entity.tohotelDto() }
        return myHotelRepository.findMostAddedhotelByCountry(countryRepository.findByCity(cityId),language?:Constants.DEFAULT_LANGUAGE,pageOptions.toPageable())?.map { entity -> entity.tohotelDto() }
    }
    override fun getMyCountries(language: String?, pageOptions: GraphQLPage): Page<CountryDto> {
        val countries = countryRepository.findMyCountries(language ?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable()).map { entity ->
            CountryDto(
                entity.name,
                entity.code,
                entity.picture,
                entity.flag,
                entity.id,
            )
        }
        return countries
    }

    override fun getMyCities(countryId: Long, language: String?, pageOptions: GraphQLPage): Page<CityDto>? {
        return cityRepository.findMyCities(countryId, language ?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())?.map { entity ->
            CityDto(
                entity.name,
                entity.picture,
                entity.locality,
                entity.placeId,
                entity.id,
                countryId
            )
        }
    }

    override fun getGlobalCities(language: String?, pageOptions: GraphQLPage): Page<GlobalCityDto>? {
        return cityRepository.findGlobalCities(language ?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())?.map { entity ->
            GlobalCityDto(
                entity.name,
                entity.picture,
                CountryDto(
                    entity.countryName,
                    entity.countryCode,
                    entity.picture,
                    entity.countryFlag,
                    id = entity.countryId
                ),
                entity.id
            )
        }
    }

    override fun getLocality(localityId: Long, language: String?): LocalityDto? {
        val locality = localityRepository.findByIdOrNull(localityId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "localityId")
        val city = cityRepository.getCityByLocalityId(localityId)
        return translationService.mapLocalityDto(locality, city?.id, language)
    }
    override fun getCity(localityId: Long, language: String?): CityDto? {
        val city = cityRepository.getCityByLocalityId(localityId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "localityId")
        return translationService.mapCityDto(city, language)
    }
    override fun getInternationalization(language: String, refresh: Boolean?): Internationalization? {
        if (refresh == true){
            cacheService.evictCache("localization")
        }
        val response = awsService.getLocalization(language)
        if (response == null || response.isEmpty){
            throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "language")
        }
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper.readValue(response.toString(), Internationalization::class.java)
    }

    override fun getCountry(cityId: Long, language: String?): CountryDto? {
        val country = countryRepository.findByCityLanguage(cityId, language?:Constants.DEFAULT_LANGUAGE) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "cityId")
        return CountryDto(
            country.name,
            country.code,
            country.picture,
            country.flag,
            country.id
        )
    }
    override fun getCountryByLocality(localityId: Long, language: String?): CountryDto? {
        val country = countryRepository.findByLocality(localityId, language?:Constants.DEFAULT_LANGUAGE) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "cityId")
        return CountryDto(
            country.name,
            country.code,
            country.picture,
            country.flag,
            country.id
        )
    }
    override fun getUserProfile(username: String): UserDto? {
        val user = userRepository.findByNickName(username) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "username")
        return translationService.mapUserDto(user, null)
    }
    override fun getWebhotelDetail(hotelId: Long, language: String?): MyHotelDto? {
        val hotel = myHotelRepository.findByIdOrNull(hotelId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"hotelId")
        return translationService.mapmyHotelDto(hotel, language)
    }
    override fun getWebSimilarHotel(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<MyHotelDto>? {
        return hotelSearchService.similarHotel(hotelId,language, pageOptions)
    }
    override fun getAvailableLanguages(countryId: Long?): List<Language>? {
        return languageRepository.findAvailableLanguages(countryId)
    }
    override fun translateCountry(name: String, language: String): CountryDto? {
        val country = countryRepository.findByName(name)
        if (country.isNullOrEmpty()) {
            return null
        }
        val dto = countryRepository.findById(country.first().id!!, language) ?: return null
        return CountryDto(dto.name, dto.code, dto.picture, dto.flag, dto.id)
    }

    override fun getLocalitySitemaps(language: String?, countryCode: String, pageOptions: GraphQLPage): Page<LocalitySitemap>? {
        return myHotelRepository.findSitemapLocalities(language?:Constants.DEFAULT_LANGUAGE, countryCode, pageOptions.toPageable())
    }
    override fun getCitySitemaps(language: String?, countryCode: String, pageOptions: GraphQLPage): Page<CitySitemap>? {
        return myHotelRepository.findSitemapCities(language?:Constants.DEFAULT_LANGUAGE, countryCode, pageOptions.toPageable())
    }
    override fun gethotelSitemaps(language: String?, countryCode: String, pageOptions: GraphQLPage): Page<HotelSitemap>? {
        return myHotelRepository.findSitemaphotels(language?:Constants.DEFAULT_LANGUAGE,countryCode, pageOptions.toPageable())
    }
    override fun getUserSitemaps(language: String?, countryCode: String, pageOptions: GraphQLPage): Page<UserSitemap>? {
        return myHotelRepository.findSitemapUsers(language?:Constants.DEFAULT_LANGUAGE,countryCode, pageOptions.toPageable())
    }
}