package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.UserAuthentication
import java.time.LocalDate

data class UserProfile(val user: com.my.hotel.server.graphql.dto.response.UserDto) {
    fun getEmail() : String? {
        if (user.auths.isNullOrEmpty())
            return null
        val emailAuth = user.auths.find { auth -> auth.type == UserAuthentication.Type.EMAIL && auth.verified == true }
        return emailAuth?.email
    }

    fun getPhoneNumber() : String? {
        if (user.auths.isNullOrEmpty())
            return null
        val phoneAuth = user.auths.find { auth -> auth.type == UserAuthentication.Type.PHONE && auth.verified == true }
        return phoneAuth?.phone
    }

    fun getDateOfBirth() : LocalDate? {
        return user.dob
    }
}