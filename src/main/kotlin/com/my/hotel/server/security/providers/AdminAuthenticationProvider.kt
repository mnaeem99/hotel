package com.my.hotel.server.security.providers

import com.my.hotel.server.data.model.Admin
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component


@Component
class AdminAuthenticationProvider @Autowired constructor(
    private val passwordEncoder: PasswordEncoder
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication?): Authentication? {

        if (authentication != null) {

            val user = authentication.principal as Admin
            if (passwordEncoder.matches(authentication.credentials as String, user.password)) {
                    return authentication
            }
        }
        return null
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return authentication!! == AdminAuthenticationToken::class.java
    }


}