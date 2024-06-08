package com.my.hotel.server.security.providers

import org.springframework.security.authentication.AbstractAuthenticationToken


class GuestAuthenticationToken(
    private val key: String,
    private val deviceID: String?=null,
) : AbstractAuthenticationToken(null) {

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