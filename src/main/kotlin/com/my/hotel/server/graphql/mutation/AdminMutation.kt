package com.my.hotel.server.graphql.mutation

import com.my.hotel.server.data.model.*
import com.my.hotel.server.graphql.dto.request.*
import com.my.hotel.server.graphql.dto.response.*
import com.my.hotel.server.service.cache.CacheService
import com.my.hotel.server.service.city.CityService
import com.my.hotel.server.service.country.CountryService
import com.my.hotel.server.service.gift.GiftService
import com.my.hotel.server.service.locality.LocalityService
import com.my.hotel.server.service.quality.QualityService
import com.my.hotel.server.service.hotel.SaveHotelService
import com.my.hotel.server.service.hotelPrice.HotelPriceRangeService
import com.my.hotel.server.service.hotelProfile.HotelProfileService
import com.my.hotel.server.service.user.UserService
import graphql.kickstart.tools.GraphQLMutationResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component

@Component
class AdminMutation @Autowired constructor(
    private val qualityService: QualityService,
    private val hotelProfileService: HotelProfileService,
    private val countryService: CountryService,
    private val cityService: CityService,
    private val localityService: LocalityService,
    private val userService: UserService,
    private val giftService: GiftService,
    private val hotelPriceRangeService: HotelPriceRangeService,
    private val cacheService: CacheService,
    private val saveHotelService: SaveHotelService,
): GraphQLMutationResolver {

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun addUserAdmin(input: UserInput): UserDto? {
        return userService.addUser(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun updateUserAdmin(input: UpdateUser): UserDto?{
        return userService.updateUser(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun deleteUserAdmin(id: Long): Boolean{
        return userService.deleteUser(id)
    }

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun addQualityAdmin(input: QualityInput): Quality? {
        return qualityService.addQuality(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun addQualityTypeAdmin(name: String): QualityType? {
        return qualityService.addQualityType(name)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun updateQualityAdmin(input: UpdateQuality): Quality? {
        return qualityService.updateQuality(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun updateQualityTypeAdmin(input: UpdateQualityType): QualityType? {
        return qualityService.updateQualityType(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun deleteQualityAdmin(id: Long): Boolean {
        return qualityService.deleteQuality(id)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun deleteQualityTypeAdmin(id: Long): Boolean {
        return qualityService.deleteQualityType(id)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun addhotelAdmin(input: HotelInput): MyHotelDto?{
        return hotelProfileService.addHotel(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun updatehotelAdmin(input: UpdateHotel): MyHotelDto?{
        return hotelProfileService.updateHotel(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun deletehotelAdmin(hotelId: Long): Boolean?{
        return hotelProfileService.deleteHotel(hotelId)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun addCountryAdmin(input: CountryInput): CountryDto?{
        return countryService.addCountry(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun updateCountryAdmin(input: UpdateCountry): CountryDto?{
        return countryService.updateCountry(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun deleteCountryAdmin(countryId: Long): Boolean?{
        return countryService.deleteCountry(countryId)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun addCityAdmin(input: CityInput): CityDto?{
        return cityService.addCity(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun updateCityAdmin(input: UpdateCity): CityDto?{
        return cityService.updateCity(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun deleteCityAdmin(cityId: Long): Boolean?{
        return cityService.deleteCity(cityId)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun addLocalityAdmin(input: LocalityInput): LocalityDto?{
        return localityService.addLocality(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun updateLocalityAdmin(input: UpdateLocality): LocalityDto?{
        return localityService.updateLocality(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun deleteLocalityAdmin(localityId: Long): Boolean?{
        return localityService.deleteLocality(localityId)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun addGiftAdmin(input: GiftInput): GiftDto? {
        return giftService.addGift(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun updateGiftAdmin(input: UpdateGift): GiftDto? {
        return giftService.updateGift(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun deleteGiftAdmin(id: Long): Boolean {
        return giftService.deleteGift(id)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun blockUserAdmin(userId: Long, block: Boolean): Boolean {
        return userService.blockUserAdmin(userId, block)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun updatePriceRangeConfigAdmin(input: ConfigInput): HotelPriceConfig {
        return hotelPriceRangeService.setPriceRangeConfig(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun clearCache(cacheName: String): Boolean {
        cacheService.evictCache(cacheName)
        return true
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun addCountryAddressConfig(input: CountryAddressConfigInput): CountryAddressConfig?{
        return countryService.addCountryAddressConfig(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun updateCountryAddressConfig(input: UpdateCountryAddressConfig): CountryAddressConfig?{
        return countryService.updateCountryAddressConfig(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun deleteCountryAddressConfig(id: Long): Boolean?{
        return countryService.deleteCountryAddressConfig(id)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun addCityAddressConfig(input: CityAddressConfigInput): CityAddressConfig?{
        return cityService.addCityAddressConfig(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun updateCityAddressConfig(input: UpdateCityAddressConfig): CityAddressConfig?{
        return cityService.updateCityAddressConfig(input)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun deleteCityAddressConfig(id: Long): Boolean?{
        return cityService.deleteCityAddressConfig(id)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun updatehotelAddress(countryId: Long?, cityId: Long?): Boolean{
        return saveHotelService.updatehotelAddress(countryId, cityId)
    }

}