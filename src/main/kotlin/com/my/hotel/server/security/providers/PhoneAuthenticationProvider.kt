package com.my.hotel.server.security.providers

import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.model.UserAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.core.Authentication
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component


@Component
class PhoneAuthenticationProvider @Autowired constructor(
    private val passwordEncoder: PasswordEncoder
) : AuthenticationProvider {
    override fun authenticate(authentication: Authentication?): Authentication? {

        if (authentication != null) {

            val user = authentication.principal as User
            val phoneAuth = user.auths?.find { auth -> auth.type == UserAuthentication.Type.PHONE && auth.verified == true }
            if (phoneAuth != null && passwordEncoder.matches(authentication.credentials as String, phoneAuth.password)) {
                return authentication
            }
        }
        return null
    }

    override fun supports(authentication: Class<*>?): Boolean {
        return authentication!! == PhoneAuthenticationToken::class.java
    }


}