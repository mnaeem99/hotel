package com.my.hotel.server.security.providers

import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.model.UserAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class GoogleAuthenticationProvider @Autowired constructor(
    private val passwordEncoder: PasswordEncoder
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication?): Authentication? {
        if (authentication != null) {
            val user = authentication.principal as User
            val googleAuth = user.auths?.find { auth -> auth.type == UserAuthentication.Type.GOOGLE && auth.verified == true }
            if (googleAuth != null && passwordEncoder.matches(authentication.credentials as String, googleAuth.googleId)) {
                return authentication
            }
        }
        return null
    }
    override fun supports(authentication: Class<*>?): Boolean {
        return authentication!! == GoogleAuthenticationToken::class.java
    }
}