package com.my.hotel.server.service.web

import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.response.LocalityDto
import com.my.hotel.server.data.model.Language
import com.my.hotel.server.graphql.dto.response.*
import org.springframework.data.domain.Page

interface IWebService {
    fun getCountries(language: String?, pageOptions: GraphQLPage): Page<CountryDto>
    fun getFeaturedCountries(language: String?, pageOptions: GraphQLPage): Page<CountryDto>
    fun getFeaturedChefs(countryId:Long?, language: String?, pageOptions: GraphQLPage): Page<Chef>?
    fun getPopularSearches(countryId: Long): List<String>?
    fun getNeighborhood(cityId: Long, language: String?, pageOptions: GraphQLPage): Page<LocalityDto>?
    fun getCities(countryId: Long, language: String?, pageOptions: GraphQLPage): Page<CityDto>?
    fun getLocalityHotel(countryId: Long?, cityId:Long?, language: String?, pageOptions: GraphQLPage): Page<LocalityHotel>?
    fun gethotelsByLocality(localityId: Long, language: String?, pageOptions: GraphQLPage): Page<MyHotelDto>?
    fun getTopTrending(countryId: Long?, cityId: Long?, language: String?, pageOptions: GraphQLPage): Page<MyHotelDto>?
    fun getMyCountries(language: String?, pageOptions: GraphQLPage): Page<CountryDto>
    fun getMyCities(countryId: Long, language: String?, pageOptions: GraphQLPage): Page<CityDto>?
    fun getGlobalCities(language: String?, pageOptions: GraphQLPage): Page<GlobalCityDto>?
    fun getLocality(localityId: Long, language: String?): LocalityDto?
    fun getCity(localityId: Long, language: String?): CityDto?
    fun getInternationalization(language: String, refresh: Boolean?): Internationalization?
    fun getCountry(cityId: Long, language: String?): CountryDto?
    fun getCountryByLocality(localityId: Long, language: String?): CountryDto?
    fun getUserProfile(username: String): UserDto?
    fun getWebhotelDetail(hotelId: Long, language: String?): MyHotelDto?
    fun getWebSimilarHotel(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<MyHotelDto>?
    fun getAvailableLanguages(countryId: Long?): List<Language>?
    fun translateCountry(name: String, language: String): CountryDto?
    fun getLocalitySitemaps(language: String?, countryCode: String, pageOptions: GraphQLPage): Page<LocalitySitemap>?
    fun gethotelSitemaps(language: String?, countryCode: String, pageOptions: GraphQLPage): Page<HotelSitemap>?
    fun getCitySitemaps(language: String?, countryCode: String, pageOptions: GraphQLPage): Page<CitySitemap>?
    fun getUserSitemaps(language: String?, countryCode: String, pageOptions: GraphQLPage): Page<UserSitemap>?
}