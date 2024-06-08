package com.my.hotel.server.security.jwt

import com.my.hotel.server.data.model.User
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class JWTAuthenticationToken(
    private val token: String,
    private val user: User,
    private val deviceID: String?=null,
    private val  authorities: MutableList<GrantedAuthority>
) : AbstractAuthenticationToken(authorities) {

   init {
       super.setAuthenticated(true)
   }

    override fun getCredentials(): Any {
        return token
    }

    override fun getPrincipal(): Any {
        return user
    }
    override fun getDetails(): Any? {
        return deviceID
    }
    override fun getAuthorities(): MutableCollection<GrantedAuthority> {
        return authorities
    }
}