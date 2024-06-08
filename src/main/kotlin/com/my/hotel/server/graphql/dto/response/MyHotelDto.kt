package com.my.hotel.server.graphql.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.Image
import com.my.hotel.server.data.model.HotelPriceLevel
import java.io.Serializable
import java.time.LocalDateTime

data class MyHotelDto(
    @JsonProperty("name")
    var name: String?,
    @JsonProperty("address")
    var address: String?=null,
    @JsonProperty("phone")
    var phone : String? = null,
    @JsonProperty("country")
    var country: CountryDto?=null,
    @JsonProperty("geolat")
    var geolat: Float?=null,
    @JsonProperty("geolong")
    var geolong: Float?=null,
    @JsonProperty("hotelPriceLevel")
    var hotelPriceLevel: HotelPriceLevel?=null,
    @JsonProperty("googlePriceLevel")
    var googlePriceLevel: HotelPriceLevel?=null,
    @JsonProperty("photoList")
    var photoList: List<Image>?  = ArrayList(),
    @JsonProperty("photo")
    var photo: Image?  = null,
    @JsonProperty("placeId")
    var placeId: String?=null,
    @JsonProperty("expiryDate")
    var expiryDate: LocalDateTime?=null,
    @JsonProperty("status")
    var status: MyHotel.BusinessStatus? = null,
    @JsonProperty("googleMapUrl")
    var googleMapUrl: String? = null,
    @JsonProperty("id")
    var id: Long? = null,
    @JsonProperty("localityRanking")
    var localityRanking: Int? = null
): Serializable