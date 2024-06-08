package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.Image
import com.my.hotel.server.data.model.Locality

data class CityDto(
    override var name: String?,
    override var picture: Image?  = null,
    override var locality: List<Locality>?  = ArrayList(),
    override var placeId: String? = null,
    override var id: Long? = null,
    override var countryId: Long? = null,
) : com.my.hotel.server.graphql.dto.response.ICityDto
interface ICityDto{
    var name: String?
    var picture: Image?
    var locality: List<Locality>?
    var placeId: String?
    var id: Long?
    var countryId: Long?
}
