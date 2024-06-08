package com.my.hotel.server.security.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.my.hotel.server.commons.Constants
import com.my.hotel.server.security.SecurityConstants
import com.my.hotel.server.security.SecurityUtils
import com.my.hotel.server.security.dto.LoginGuestInput
import com.my.hotel.server.security.jwt.checkCredentialType.AuthenticateFactory
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class JWTGuestAuthenticationFilter(
    private val authManager: AuthenticationManager,
    private val securityUtils: SecurityUtils,
    private val authenticateFactory: AuthenticateFactory,
) : AbstractAuthenticationProcessingFilter(
    AntPathRequestMatcher("/api/guest/login", "POST"), authManager) {

    @Throws(AuthenticationException::class)
    override fun attemptAuthentication(
        req: HttpServletRequest,
        res: HttpServletResponse
    ): Authentication {
        val credential: LoginGuestInput = jacksonObjectMapper()
            .readerFor(LoginGuestInput::class.java)
            .readValue(req.inputStream)
        return authManager.authenticate(authenticateFactory.authenticateGuest(credential))
    }

    override fun successfulAuthentication(
        req: HttpServletRequest?,
        res: HttpServletResponse,
        chain: FilterChain?,
        auth: Authentication
    ) {
        val deviceID = auth.details as String?
        val guestToken = securityUtils.generateGuestToken(deviceID)
        val tokens: HashMap<String,String> = HashMap<String,String>()
        tokens["guestToken"] = SecurityConstants.TOKEN_PREFIX + guestToken
        res.contentType = MediaType.APPLICATION_JSON_VALUE
        val objectMapper = ObjectMapper()
        objectMapper.writeValue(res.outputStream, tokens)

    }

    override fun unsuccessfulAuthentication(
        request: HttpServletRequest?,
        response: HttpServletResponse,
        exception: AuthenticationException?
    ) {
        if (exception is BadCredentialsException){
            val errorMessage: HashMap<String, String> = HashMap()
            errorMessage["error"] = exception.message ?: Constants.AUTHENTICATION_ERROR
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            val objectMapper = ObjectMapper()
            objectMapper.writeValue(response.outputStream, errorMessage)
        }
        else {
            super.unsuccessfulAuthentication(request, response, exception)
        }
    }
}
