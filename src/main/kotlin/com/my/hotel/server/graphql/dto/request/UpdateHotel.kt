package com.my.hotel.server.graphql.dto.request

import com.my.hotel.server.commons.FileUpload
import com.my.hotel.server.data.model.MyHotel

data class UpdateHotel(
    var name: String? = null,
    var address: String? = null,
    var geolat: Float? = null,
    var geolong: Float? = null,
    var photo: FileUpload? = null,
    var photoList: List<FileUpload>? = null,
    var removePhotoId: List<Long>? = null,
    var phone : String? = null,
    var language : String? = null,
    var countryId: Long?=null,
    var cityId: Long?=null,
    var localityId: Long?=null,
    var hotelPriceLevelId: Long?=null,
    var placeId: String?=null,
    var status: MyHotel.BusinessStatus?=null,
    var id: Long
)