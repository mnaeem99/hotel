package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.Image


data class GlobalCityDto(
    var name: String?,
    var picture: Image?  = null,
    var country: CountryDto?,
    var id: Long? = null,
)
interface IGlobalCityDto{
    var name: String?
    var picture: Image?
    var countryName: String?
    var countryCode: String?
    var countryPicture: Image?
    var countryFlag: Image?
    var countryId: Long?
    var id: Long?
}