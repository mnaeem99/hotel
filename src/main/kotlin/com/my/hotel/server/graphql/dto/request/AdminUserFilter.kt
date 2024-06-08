package com.my.hotel.server.graphql.dto.request

import com.my.hotel.server.data.model.User

data class AdminUserFilter(
    val language: String? = null,
    val countryId: Long? = null,
    val userType: User.UserType? = null,
    val searchKeyword: String? = null,
)