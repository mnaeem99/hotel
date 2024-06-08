package com.my.hotel.server.security.dto

import com.my.hotel.server.data.model.UserAuthentication

data class LoginUserInput(
    var type: UserAuthentication.Type,
    var email: String? = null,
    var phone: String? = null,
    var password: String? = null,
    var deviceID: String? = null
)