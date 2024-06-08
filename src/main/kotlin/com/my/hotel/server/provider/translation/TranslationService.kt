package com.my.hotel.server.provider.translation

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.CityTranslationRepository
import com.my.hotel.server.data.repository.CountryTranslationRepository
import com.my.hotel.server.data.repository.LocalityTranslationRepository
import com.my.hotel.server.data.repository.HotelTranslationRepository
import com.my.hotel.server.graphql.dto.response.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors

@Component
class TranslationService @Autowired constructor(
    private val hotelTranslationRepository: HotelTranslationRepository,
    private val countryTranslationRepository: CountryTranslationRepository,
    private val cityTranslationRepository: CityTranslationRepository,
    private val localityTranslationRepository: LocalityTranslationRepository,
){
    fun mapmyHotelDto(hotel: MyHotel, language: String?): MyHotelDto {
        val translation = hotelTranslationRepository.findByHotel(hotel.id!!, language?:Constants.DEFAULT_LANGUAGE)
            ?: hotelTranslationRepository.findByHotel(hotel)?.first()
        return mapmyHotelDto(translation!! ,hotel,language?:Constants.DEFAULT_LANGUAGE)
    }
    fun mapmyHotelDto(translation: HotelTranslation, hotel: MyHotel, language: String): MyHotelDto {
        return MyHotelDto(
            translation.name,
            translation.address,
            hotel.phone,
            mapCountryDto(hotel.country, language),
            hotel.geolat,
            hotel.geolong,
            hotel.hotelPriceLevel,
            hotel.googlePriceLevel,
            hotel.photoList,
            hotel.photo,
            hotel.placeId,
            hotel.expiryDate,
            hotel.status,
            hotel.googleMapUrl,
            hotel.id
        )
    }
    fun mapmyHotelDto(translation: HotelTranslation, rank: Int? = null): MyHotelDto {
        return MyHotelDto(
            translation.name,
            translation.address,
            translation.hotel.phone,
            null,
            translation.hotel.geolat,
            translation.hotel.geolong,
            translation.hotel.hotelPriceLevel,
            translation.hotel.googlePriceLevel,
            translation.hotel.photoList,
            translation.hotel.photo,
            translation.hotel.placeId,
            translation.hotel.expiryDate,
            translation.hotel.status,
            translation.hotel.googleMapUrl,
            translation.hotel.id,
            rank
        )
    }
    fun mapCountryDto(country: Country?, language: String?): CountryDto? {
        if (country == null)
            return null
        val translation = countryTranslationRepository.findByCountry(country.id!!, language ?: Constants.DEFAULT_LANGUAGE)
        if (translation == null){
            val otherTranslation = countryTranslationRepository.findByCountry(country)
            if (otherTranslation.isNullOrEmpty())
                return null
            return CountryDto(
                otherTranslation.first().name.toString(),
                country.code,
                country.picture,
                country.flag,
                country.id
            )
        }
        return CountryDto(
            translation.name.toString(),
            country.code,
            country.picture,
            country.flag,
            country.id
        )
    }
    fun mapCityDto(city: City?, language: String?): CityDto? {
        if (city == null)
            return null
        val translation = cityTranslationRepository.findByCity(city.id!!, language ?: Constants.DEFAULT_LANGUAGE)
        if (translation == null){
            val otherTranslation = cityTranslationRepository.findByCity(city)
            if (otherTranslation.isNullOrEmpty())
                return null
            return CityDto(
                otherTranslation.first().name.toString(),
                city.picture,
                city.locality,
                city.placeId,
                city.id
            )
        }
        return CityDto(
            translation.name.toString(),
            city.picture,
            city.locality,
            city.placeId,
            city.id
        )
    }
    fun mapLocalityDto(locality: Locality?, cityId: Long?, language: String?): LocalityDto? {
        if (locality == null)
            return null
        val translation = localityTranslationRepository.findByLocality(locality.id!!, language ?: Constants.DEFAULT_LANGUAGE)
        if (translation == null){
            val otherTranslation = localityTranslationRepository.findByLocality(locality)
            if (otherTranslation.isNullOrEmpty())
                return null
            return LocalityDto(
                otherTranslation.first().name.toString(),
                locality.picture,
                locality.placeId,
                locality.id,
                cityId
            )
        }
        return LocalityDto(
            translation.name.toString(),
            locality.picture,
            locality.placeId,
            locality.id,
            cityId
        )
    }
    fun mapUserDto(user: User, language: String?): UserDto {
        return UserDto(
            user.id,
            user.firstName,
            user.lastName,
            user.nickName,
            user.bio,
            user.photo,
            user.language,
            mapCountryDto(user.country, language),
            user.isPrivate,
            user.isChef,
            user.isBlocked,
            user.userType,
            user.dob,
            user.auths?.stream()?.map { auth -> UserAuthentication(
                type = auth.type,
                email = auth.email,
                phone = auth.phone,
                password = auth.password,
                googleId = auth.googleId,
                appleId = auth.appleId,
                facebookId = auth.facebookId,
                verified = auth.verified,
                confirmationCode = null,
                id = auth.id,
                user = null,
            ) }?.collect(Collectors.toList())
        )
    }
    fun mapUser(user: User, countryName: String?): UserDto {
        return UserDto(
            user.id,
            user.firstName,
            user.lastName,
            user.nickName,
            user.bio,
            user.photo,
            user.language,
            CountryDto(
                countryName,
                user.country?.code,
                user.country?.picture,
                id = user.country?.id
            ),
            user.isPrivate,
            user.isChef,
            user.isBlocked,
            user.userType,
            user.dob,
            user.auths?.stream()?.map { auth -> UserAuthentication(
                type = auth.type,
                email = auth.email,
                phone = auth.phone,
                password = auth.password,
                googleId = auth.googleId,
                appleId = auth.appleId,
                facebookId = auth.facebookId,
                verified = auth.verified,
                confirmationCode = null,
                id = auth.id,
                user = null,
            ) }?.collect(Collectors.toList())
        )
    }
    fun mapGift(hotelGift: HotelGift, language: String): GiftDto {
        val gift = hotelGift.gift
        val hotel = gift.hotel
        val hotelTranslation = hotelGift.hotelTranslation
        return GiftDto(
            gift.name,
            gift.picture,
            gift.points,
            gift.otherInfo,
            mapmyHotelDto(hotelTranslation, hotel, language),
            gift.id
        )
    }
}