package com.my.hotel.server.security

import java.util.concurrent.TimeUnit


object SecurityConstants {
    const val SECRET_ACCESS = "SecretKeyToGenACCESS"
    const val SECRET_Ref = "SecretKeyToGenREF"
    const val GUEST_KEY = "Anonymous"
    val EXPIRATION_TIME = TimeUnit.HOURS.toMillis(3)
    const val TOKEN_PREFIX = "Bearer "
    const val HEADER_STRING = "Authorization"
    const val DEVICE_HEADER = "deviceID"
}
