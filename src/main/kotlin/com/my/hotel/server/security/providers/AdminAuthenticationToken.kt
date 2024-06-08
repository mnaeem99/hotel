package com.my.hotel.server.security.providers

import com.my.hotel.server.data.model.Admin
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority


class AdminAuthenticationToken(
    private val user: Admin,
    private val password: String,
    private val authorities: MutableList<GrantedAuthority>,
) : AbstractAuthenticationToken(authorities) {

    override fun getCredentials(): Any {
        return password
    }

    override fun getPrincipal(): Any {
        return user
    }

    override fun getAuthorities(): MutableCollection<GrantedAuthority> {
        return authorities
    }

}