package com.my.hotel.server.mvc

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import com.my.hotel.server.data.repository.UserRepository
import com.my.hotel.server.mvc.error.ApplicationException
import com.my.hotel.server.security.SecurityConstants
import com.my.hotel.server.security.SecurityUtils
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class TokenController(val securityUtils: SecurityUtils, val userRepository: UserRepository) {
    @GetMapping("/api/accessToken")
    fun refreshToken(request: HttpServletRequest, response: HttpServletResponse) {
        val authorizationHeader: String = request.getHeader(SecurityConstants.HEADER_STRING)
        if (authorizationHeader.startsWith(SecurityConstants.TOKEN_PREFIX)){
            try {
                val user = SecurityUtils.getLoggedInUser()
                val token = authorizationHeader.replace(SecurityConstants.TOKEN_PREFIX, "")
                val jwtDecoder = JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET_Ref.toByteArray()))
                    .build()
                    .verify(token)
                val deviceID = jwtDecoder.claims["deviceID"]?.asString()
                val accessToken = securityUtils.generateAccessToken(user, deviceID)
                val tokens: HashMap<String,String> = HashMap<String,String>()
                tokens["accessToken"] = SecurityConstants.TOKEN_PREFIX + accessToken
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                val objectMapper = ObjectMapper()
                objectMapper.writeValue(response.outputStream, tokens)
            }
            catch (exception: Exception){
                throw ApplicationException("Invalid Token")
            }
        }
        else{
            throw ApplicationException("Missing Token")
        }
    }
    @GetMapping("/api/social/login")
    fun socialToken(request: HttpServletRequest, response: HttpServletResponse) {
        val authorizationHeader: String = request.getHeader(SecurityConstants.HEADER_STRING)
        if (authorizationHeader.startsWith(SecurityConstants.TOKEN_PREFIX)){
            try {
                val deviceID: String = request.getHeader(SecurityConstants.DEVICE_HEADER)
                val user = SecurityUtils.getLoggedInUser()
                val accessToken = securityUtils.generateAccessToken(user,deviceID)
                val refreshToken = securityUtils.generateRefreshToken(user,deviceID)
                val tokens: HashMap<String,String> = HashMap<String,String>()
                tokens["accessToken"] = SecurityConstants.TOKEN_PREFIX + accessToken
                tokens["refreshToken"] = SecurityConstants.TOKEN_PREFIX + refreshToken
                response.contentType = MediaType.APPLICATION_JSON_VALUE
                val objectMapper = ObjectMapper()
                objectMapper.writeValue(response.outputStream, tokens)
            }
            catch (exception: Exception){
                throw ApplicationException("Invalid Token")
            }
        }
        else{
            throw ApplicationException("Missing Token")
        }
    }

}