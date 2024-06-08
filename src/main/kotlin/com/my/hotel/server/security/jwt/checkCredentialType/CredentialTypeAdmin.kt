package com.my.hotel.server.security.jwt.checkCredentialType

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.repository.AdminRepository
import com.my.hotel.server.security.dto.AdminUserInput
import com.my.hotel.server.security.providers.AdminAuthenticationToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.stereotype.Component

@Component
class CredentialTypeAdmin @Autowired constructor(private val adminRepository: AdminRepository){
    fun credentialAuthentication(credential: AdminUserInput): Authentication {
        if (credential.username == null || credential.password == null) {
            throw BadCredentialsException(Constants.AUTHENTICATION_ERROR)
        }
        val user = adminRepository.findByUsername(credential.username!!)
            ?: throw BadCredentialsException("User does not exist")
        val grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("ADMIN")
        return AdminAuthenticationToken(user, credential.password!!, grantedAuthorities)
    }
}
