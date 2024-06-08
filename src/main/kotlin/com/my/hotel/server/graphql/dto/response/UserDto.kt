package com.my.hotel.server.graphql.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.my.hotel.server.data.model.*
import java.io.Serializable
import java.time.LocalDate

data class UserDto(
    @JsonProperty("id")
    var id: Long? = null,
    @JsonProperty("firstName")
    var firstName: String? = null,
    @JsonProperty("lastName")
    var lastName: String? = null,
    @JsonProperty("nickName")
    var nickName: String? = null,
    @JsonProperty("bio")
    var bio: String? = null,
    @JsonProperty("photo")
    var photo: Image? = null,
    @JsonProperty("language")
    var language: String?=null,
    @JsonProperty("country")
    var country: CountryDto?=null,
    @JsonProperty("isPrivate")
    var isPrivate: Boolean? = false,
    @JsonProperty("isChef")
    var isChef: Boolean? = false,
    @JsonProperty("isBlocked")
    var isBlocked: Boolean? = false,
    @JsonProperty("userType")
    var userType: User.UserType?=null,
    @JsonProperty("dob")
    var dob: LocalDate?=null,
    @JsonProperty("auths")
    val auths: List<UserAuthentication>?=null,
    @JsonProperty("status")
    var status: Status?=null,
): Serializable