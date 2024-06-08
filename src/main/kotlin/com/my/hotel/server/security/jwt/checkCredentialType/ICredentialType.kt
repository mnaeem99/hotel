package com.my.hotel.server.security.jwt.checkCredentialType

import com.my.hotel.server.security.dto.LoginUserInput
import org.springframework.security.core.Authentication

interface ICredentialType {
    fun credentialAuthentication(credential: LoginUserInput): Authentication
}
