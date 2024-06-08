package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.CountryTranslation
import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.HotelTranslation

data class MyHotelTranslationDto(
    var hotel: MyHotel,
    var hotelTranslation: HotelTranslation,
    var countryTranslation: CountryTranslation?,
) {
    fun tohotelDto(rank: Int? = null) = MyHotelDto(
        hotelTranslation.name,
        hotelTranslation.address,
        hotel.phone,
        toCountryDto(),
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
        hotel.id,
        rank
    )
    private fun toCountryDto(): CountryDto? {
        if (hotel.country == null)
            return null
        return CountryDto(
            name = countryTranslation?.name, code = hotel.country?.code, picture = hotel.country?.picture, flag = hotel.country?.flag, id = hotel.country?.id
        )
    }
}