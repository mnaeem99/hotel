package com.my.hotel.server.security.providers

import com.my.hotel.server.security.SecurityConstants
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component


@Component
class GuestAuthenticationProvider : AuthenticationProvider {
    override fun authenticate(authentication: Authentication?): Authentication? {

        if (authentication != null && authentication.principal.equals(SecurityConstants.GUEST_KEY)) {
            return authentication
        }
        return null
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return authentication!! == GuestAuthenticationToken::class.java
    }


}