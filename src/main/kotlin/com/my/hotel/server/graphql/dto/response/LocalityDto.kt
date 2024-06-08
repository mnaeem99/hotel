package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.Image

data class LocalityDto(
    override var name: String?,
    override var picture: Image?  = null,
    override var placeId: String? = null,
    override var id: Long? = null,
    override var cityId: Long? = null,
) : ILocalityDto
interface ILocalityDto {
    var name: String?
    var picture: Image?
    var placeId: String?
    var id: Long?
    var cityId: Long?
}
