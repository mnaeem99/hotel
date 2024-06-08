package com.my.hotel.server.graphql.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.my.hotel.server.data.model.Image
import lombok.experimental.Accessors
import java.io.Serializable

@Accessors
data class CountryDto(
    @JsonProperty("name")
    override var name: String?,
    @JsonProperty("code")
    override var code: String?,
    @JsonProperty("picture")
    override var picture: Image?  = null,
    @JsonProperty("flag")
    override var flag: Image?  = null,
    @JsonProperty("id")
    override var id: Long? = null,
) : ICountryDto, Serializable
interface ICountryDto {
    var name: String?
    var code: String?
    var picture: Image?
    var flag: Image?
    var id: Long?
}
