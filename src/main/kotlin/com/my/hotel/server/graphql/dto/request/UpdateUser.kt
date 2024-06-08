package com.my.hotel.server.graphql.dto.request

import com.my.hotel.server.commons.FileUpload
import com.my.hotel.server.data.model.User
import java.time.LocalDate

data class UpdateUser(
    var firstName: String?=null,
    var lastName: String?=null,
    var nickName: String? = null,
    var bio: String? = null,
    var photo: FileUpload? = null,
    var language: String?=null,
    var userType: User.UserType?= null,
    var countryId: Long?=null,
    var isPrivate: Boolean? = false,
    var isChef: Boolean? = false,
    var dob: LocalDate? = null,
    val auths: List<com.my.hotel.server.graphql.dto.request.UserAuthInput>? = null,
    val userId: Long
)