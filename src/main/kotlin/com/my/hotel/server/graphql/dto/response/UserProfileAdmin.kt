package com.my.hotel.server.graphql.dto.response

import com.my.hotel.server.data.model.UserAuthentication
import java.time.LocalDate

data class UserProfileAdmin(val user: com.my.hotel.server.graphql.dto.response.UserDto) {
    fun getDateOfBirth() : LocalDate? {
        return user.dob
    }
    fun getUserAuths(): List<UserAuthentication>? {
        return user.auths
    }
}