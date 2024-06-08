package com.my.hotel.server.security.jwt.checkCredentialType

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.UserAuthentication
import com.my.hotel.server.data.repository.UserRepository
import com.my.hotel.server.security.dto.LoginUserInput
import com.my.hotel.server.security.providers.EmailAuthenticationToken
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.stereotype.Component

@Component
class CredentialTypeEmail @Autowired constructor(private val userRepository: UserRepository) : ICredentialType{
    override fun credentialAuthentication(credential: LoginUserInput): Authentication {
        if (credential.email == null || credential.password == null){
            throw BadCredentialsException(Constants.AUTHENTICATION_ERROR)
        }
        val user = userRepository.findByEmail(credential.email!!) ?: throw BadCredentialsException("User does not exist")
        val userAuth = user.auths?.find { auth -> auth.type == UserAuthentication.Type.EMAIL && auth.email == credential.email }
        if (userAuth?.verified == false) {
            throw BadCredentialsException("This email is not verified")
        }
        val grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("USER")
        return EmailAuthenticationToken(user, credential.password!!,credential.deviceID,grantedAuthorities)
    }
}
