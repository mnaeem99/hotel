package com.my.hotel.server.security.jwt

import com.my.hotel.server.data.model.Admin
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class JWTAuthenticationTokenAdmin(
    private val token: String,
    private val user: Admin,
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

    override fun getAuthorities(): MutableCollection<GrantedAuthority> {
        return authorities
    }
}