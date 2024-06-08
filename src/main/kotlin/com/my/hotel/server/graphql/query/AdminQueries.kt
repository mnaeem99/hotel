package com.my.hotel.server.graphql.query

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.HotelPriceLevelRepository
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.*
import com.my.hotel.server.graphql.dto.response.*
import com.my.hotel.server.service.city.CityService
import com.my.hotel.server.service.country.CountryService
import com.my.hotel.server.service.gift.GiftService
import com.my.hotel.server.service.locality.LocalityService
import com.my.hotel.server.service.quality.QualityService
import com.my.hotel.server.service.hotelProfile.HotelProfileService
import com.my.hotel.server.service.user.UserService
import graphql.kickstart.tools.GraphQLQueryResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Component

@Component
class AdminQueries @Autowired constructor(
    private val qualityService: QualityService,
    private val hotelProfileService: HotelProfileService,
    private val localityService: LocalityService,
    private val cityService: CityService,
    private val countryService: CountryService,
    private val userService: UserService,
    private val giftService: GiftService,
    private val hotelPriceLevelRepository: HotelPriceLevelRepository,
) : GraphQLQueryResolver {

    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getUsersAdmin(input: AdminUserFilter, pageOptions: GraphQLPage): Page<UserDto>?{
        return userService.getUsers(input, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun gethotelsAdmin(input: AdminHotelFilter, pageOptions: GraphQLPage): Page<MyHotelDto>?{
        return hotelProfileService.gethotels(input, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun gethotelVerificationAppointmentsAdmin(language: String?, pageOptions: GraphQLPage): Page<HotelVerificationAppointmentDto>?{
        return hotelProfileService.gethotelVerificationAppointments(language, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getLocalitiesAdmin(cityId: Long, language: String?, pageOptions: GraphQLPage): Page<LocalityDto>? {
        return localityService.getLocalities(cityId, language, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getLocalityAdmin(id: Long, language: String?): LocalityDto? {
        return localityService.getLocalityAdmin(id, language)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getCitiesAdmin(countryId: Long, language: String?, pageOptions: GraphQLPage): Page<CityDto>? {
        return cityService.getCities(countryId, language, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getCityAdmin(id: Long, language: String?): CityDto? {
        return cityService.getCityAdmin(id, language)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getCountriesAdmin(language: String?, pageOptions: GraphQLPage): Page<CountryDto>? {
        return countryService.getCountries(language, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getCountryAdmin(id: Long, language: String?): CountryDto? {
        return countryService.getCountryAdmin(id, language)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getCountryAddressConfig(countryId: Long): List<CountryAddressConfig>? {
        return countryService.getCountryAddressConfig(countryId)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getCityAddressConfig(cityId: Long): List<CityAddressConfig>? {
        return cityService.getCityAddressConfig(cityId)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getCountryAddressConfigDetails(id: Long): CountryAddressConfig? {
        return countryService.getCountryAddressConfigDetails(id)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getCityAddressConfigDetails(id: Long): CityAddressConfig? {
        return cityService.getCityAddressConfigDetails(id)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getQualitiesAdmin(pageOptions: GraphQLPage): Page<Quality>? {
        return qualityService.getQualities(pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getQualityAdmin(id: Long): Quality? {
        return qualityService.getQualityAdmin(id)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getQualityTypesAdmin(pageOptions: GraphQLPage): Page<QualityType>? {
        return qualityService.getQualityTypes(pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun qualityTypeAdmin(id: Long): QualityType? {
        return qualityService.qualityTypeAdmin(id)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getGiftsAdmin(language: String?, pageOptions: GraphQLPage): Page<GiftDto>? {
        return giftService.getAllGifts(language, pageOptions)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getGiftAdmin(id: Long, language: String?): GiftDto? {
        return giftService.getGiftAdmin(id, language)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun gethotelPriceLevelsAdmin(): List<HotelPriceLevel>? {
        return hotelPriceLevelRepository.findAll()
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun gethotelAdmin(hotelId: Long, language: String?): MyHotelDto?{
        return hotelProfileService.gethotelInner(hotelId, language?: Constants.DEFAULT_LANGUAGE)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getUserProfileAdmin(id: Long, language: String?): UserProfileAdmin {
        return userService.getUserAdmin(id,language)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun searchUserByNicknameAdmin(nickName: String, language: String?): UserDto? {
        return userService.searchUserByNickname(nickName, language)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getCountryAdminByLocality(localityId: Long, language: String?): CountryDto? {
        return countryService.getCountryAdminByLocality(localityId,language)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun gethotelLanguagesAdmin(hotelId: Long): List<String>? {
        return hotelProfileService.getLanguages(hotelId)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getCountryLanguagesAdmin(countryId: Long): List<String>? {
        return countryService.getLanguages(countryId)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getCityLanguagesAdmin(cityId: Long): List<String>? {
        return cityService.getLanguages(cityId)
    }
    @PreAuthorize("hasAnyAuthority('ADMIN')")
    fun getLocalityLanguagesAdmin(localityId: Long): List<String>? {
        return localityService.getLanguages(localityId)
    }
}