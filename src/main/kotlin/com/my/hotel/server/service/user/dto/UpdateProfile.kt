package com.my.hotel.server.service.user.dto

import com.my.hotel.server.commons.FileUpload
import java.time.LocalDate

data class UpdateProfile(
    var firstName: String? = null,
    var lastName: String? = null,
    var nickName: String? = null,
    var bio: String? = null,
    var dob: LocalDate? = null,
    var language: String?=null,
    var countryCode: String?=null,
    var profilePicture: FileUpload? = null
)