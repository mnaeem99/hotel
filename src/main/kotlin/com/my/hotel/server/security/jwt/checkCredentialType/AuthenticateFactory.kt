package com.my.hotel.server.security.jwt.checkCredentialType

import com.my.hotel.server.data.model.UserAuthentication
import com.my.hotel.server.security.SecurityConstants
import com.my.hotel.server.security.dto.AdminUserInput
import com.my.hotel.server.security.dto.LoginGuestInput
import com.my.hotel.server.security.dto.LoginUserInput
import com.my.hotel.server.security.providers.GuestAuthenticationToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Service

@Service
class AuthenticateFactory @Autowired constructor(
    private val credentialTypeEmail: CredentialTypeEmail,
    private val credentialTypePhone: CredentialTypePhone,
    private val credentialTypeAdmin: CredentialTypeAdmin
    ){
    fun authenticate(credential: LoginUserInput): Authentication? {
        if (credential.type == UserAuthentication.Type.EMAIL) {
            return credentialTypeEmail.credentialAuthentication(credential)
        } else if (credential.type == UserAuthentication.Type.PHONE) {
            return credentialTypePhone.credentialAuthentication(credential)
        }
        else{
            throw BadCredentialsException("Authentication type not recognized")
        }
    }
    fun authenticateAdmin(credential: AdminUserInput): Authentication? {
        return credentialTypeAdmin.credentialAuthentication(credential)
    }
    fun authenticateGuest(credential: LoginGuestInput): Authentication? {
        return GuestAuthenticationToken(SecurityConstants.GUEST_KEY,credential.deviceID)
    }
}