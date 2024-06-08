package com.my.hotel.server.security.dto

data class AdminUserInput constructor(
    var username: String? = null,
    var password: String? = null
)