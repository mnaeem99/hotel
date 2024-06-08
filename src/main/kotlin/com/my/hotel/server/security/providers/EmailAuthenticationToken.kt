package com.my.hotel.server.security.providers

import com.my.hotel.server.data.model.User
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority

class EmailAuthenticationToken(
    private val user: User,
    private val password: String,
    private val deviceID: String?=null,
    private val authorities: MutableList<GrantedAuthority>
    ) : AbstractAuthenticationToken(authorities) {

    override fun getCredentials(): Any {
        return password
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