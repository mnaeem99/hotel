package com.my.hotel.server.graphql.query

import com.my.hotel.server.data.model.Language
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.UserHotelFilter
import com.my.hotel.server.graphql.dto.response.*
import com.my.hotel.server.graphql.security.Unsecured
import com.my.hotel.server.service.favorites.FavoriteService
import com.my.hotel.server.service.web.WebService
import com.my.hotel.server.service.wishlist.WishListService
import graphql.kickstart.tools.GraphQLQueryResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Component

@Component
class WebQueries @Autowired constructor(
    private val webService: WebService,
    private val favoriteService: FavoriteService,
    private val wishListService: WishListService
) : GraphQLQueryResolver {

    @Unsecured
    fun getMyCountries(language: String?, pageOptions: GraphQLPage): Page<CountryDto> {
        return webService.getMyCountries(language, pageOptions)
    }
    @Unsecured
    fun getMyCities(countryId: Long, language: String?, pageOptions: GraphQLPage): Page<CityDto>? {
        return webService.getMyCities(countryId, language, pageOptions)
    }
    @Unsecured
    fun getGlobalCities(language: String?, pageOptions: GraphQLPage): Page<GlobalCityDto>? {
        return webService.getGlobalCities(language, pageOptions)
    }
    @Unsecured
    fun getFeaturedCountries(language: String?, pageOptions: GraphQLPage): Page<CountryDto> {
        return webService.getFeaturedCountries(language, pageOptions)
    }
    @Unsecured
    fun getFeaturedChefs(countryId:Long?, language: String?, pageOptions: GraphQLPage): Page<Chef>? {
        return webService.getFeaturedChefs(countryId, language, pageOptions)
    }
    @Unsecured
    fun getPopularSearches(countryId: Long): List<String>? {
        return webService.getPopularSearches(countryId)
    }
    @Unsecured
    fun getNeighborhood(cityId: Long, language: String?, pageOptions: GraphQLPage): Page<LocalityDto>? {
        return webService.getNeighborhood(cityId, language, pageOptions)
    }
    @Unsecured
    fun getLocalities(countryId: Long?, cityId:Long?, language: String?, pageOptions: GraphQLPage): Page<LocalityHotel>? {
        return webService.getLocalityHotel(countryId,cityId,language,pageOptions)
    }
    @Unsecured
    fun getTophotels(localityId: Long, language: String?, pageOptions: GraphQLPage): Page<MyHotelDto>?{
        return webService.gethotelsByLocality(localityId, language, pageOptions)
    }
    @Unsecured
    fun getTrendinghotels(countryId: Long?, cityId: Long?, language: String?, pageOptions: GraphQLPage): Page<MyHotelDto>? {
        return webService.getTopTrending(countryId, cityId, language, pageOptions)
    }
    @Unsecured
    fun getLocality(localityId: Long, language: String?): LocalityDto? {
        return webService.getLocality(localityId, language)
    }
    @Unsecured
    fun getCity(localityId: Long, language: String?): CityDto? {
        return webService.getCity(localityId, language)
    }
    @Unsecured
    fun getCountry(cityId: Long, language: String?): CountryDto? {
        return webService.getCountry(cityId, language)
    }
    @Unsecured
    fun getCountryByLocality(localityId: Long, language: String?): CountryDto? {
        return webService.getCountryByLocality(localityId, language)
    }
    @Unsecured
    fun getInternationalization(language: String, refresh: Boolean?): Internationalization? {
        return webService.getInternationalization(language,refresh)
    }
    @Unsecured
    fun getWebUserProfile(username: String): UserDto? {
        return webService.getUserProfile(username)
    }
    @Unsecured
    fun getWebFavoriteHotels(filter: UserHotelFilter, pageOptions: GraphQLPage): Page<UserAddedHotel>? {
        return favoriteService.getFavoriteHotels(filter, pageOptions)
    }
    @Unsecured
    fun getWebWishListhotels(filter: UserHotelFilter, pageOptions: GraphQLPage): Page<UserAddedHotel>? {
        return wishListService.getWishListHotel(filter,pageOptions)
    }
    @Unsecured
    fun getWebLocationsOfFavoritesHotel(userId: Long?, language: String?, pageOptions: GraphQLPage): Page<PlaceHotelDto>? {
        return favoriteService.getLocationsOfFavoritesHotel(userId, language,pageOptions)
    }
    @Unsecured
    fun getWebLocationsOfWishlistHotel(userId: Long?, language: String?, pageOptions: GraphQLPage): Page<PlaceHotelDto>? {
        return wishListService.getLocationsOfWishlistHotel(userId, language, pageOptions)
    }
    @Unsecured
    fun getWebhotelDetail(hotelId: Long, language: String?): MyHotelDto? {
        return webService.getWebhotelDetail(hotelId, language)
    }
    @Unsecured
    fun getWebSimilarHotel(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<MyHotelDto>? {
        return webService.getWebSimilarHotel(hotelId,language, pageOptions)
    }
    @Unsecured
    fun getAvailableLanguages(countryId: Long?): List<Language>? {
        return webService.getAvailableLanguages(countryId)
    }
    @Unsecured
    fun translateCountry(name: String, language: String): CountryDto? {
        return webService.translateCountry(name, language)
    }

    @Unsecured
    fun getLocalitySitemaps(language: String?, countryCode: String, pageOptions: GraphQLPage): Page<LocalitySitemap>? {
        return webService.getLocalitySitemaps(language, countryCode, pageOptions)
    }
    @Unsecured
    fun getCitySitemaps(language: String?, countryCode: String, pageOptions: GraphQLPage): Page<CitySitemap>? {
        return webService.getCitySitemaps(language, countryCode, pageOptions)
    }
    @Unsecured
    fun gethotelSitemaps(language: String?, countryCode: String, pageOptions: GraphQLPage): Page<HotelSitemap>? {
        return webService.gethotelSitemaps(language, countryCode, pageOptions)
    }

    @Unsecured
    fun getUserSitemaps(language: String?, countryCode: String, pageOptions: GraphQLPage): Page<UserSitemap>? {
        return webService.getUserSitemaps(language, countryCode, pageOptions)
    }


}