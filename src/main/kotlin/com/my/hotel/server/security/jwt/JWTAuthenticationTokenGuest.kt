package com.my.hotel.server.security.jwt

import org.springframework.security.authentication.AbstractAuthenticationToken

class JWTAuthenticationTokenGuest(
    private val key: String,
    private val deviceID: String?=null,
) : AbstractAuthenticationToken(null) {

   init {
       super.setAuthenticated(true)
   }

    override fun getCredentials(): Any? {
        return null
    }

    override fun getPrincipal(): Any {
        return key
    }
    override fun getDetails(): Any? {
        return deviceID
    }
}