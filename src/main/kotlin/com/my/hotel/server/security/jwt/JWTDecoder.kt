package com.my.hotel.server.security.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.my.hotel.server.data.repository.AdminRepository
import com.my.hotel.server.data.repository.UserRepository
import com.my.hotel.server.security.SecurityConstants
import com.my.hotel.server.security.oauth.AppleTokenVerifier
import com.my.hotel.server.security.oauth.FacebookTokenVerifier
import com.my.hotel.server.security.oauth.GoogleTokenVerifier
import com.my.hotel.server.service.user.saveUser.SaveUserService
import lombok.extern.slf4j.Slf4j
import org.jose4j.jwt.JwtClaims
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.stereotype.Component

@Slf4j
@Component
class JWTDecoder @Autowired constructor(
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository,
    private val saveUserService: SaveUserService
) {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    //method for JWT authentication
    fun getJWTAuthentication(tokenHeader: String?): JWTAuthenticationToken? {
        if (tokenHeader != null) {
            // parse the token.
            val token = tokenHeader.replace(SecurityConstants.TOKEN_PREFIX, "")
            val userId: String
            val role: String?
            val deviceID: String?
            try {
                val jwtDecoder = JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET_ACCESS.toByteArray()))
                    .build()
                    .verify(token)
                userId = jwtDecoder.subject
                role = jwtDecoder.claims["role"]?.asString()
                deviceID = jwtDecoder.claims["deviceID"]?.asString()
            }catch (e:Exception){
                e.stackTrace
                return null
            }
            val user = userRepository.findById(userId.toLong())
            if (user.isPresent) {
                val grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(role)
                return JWTAuthenticationToken(token, user.get(), deviceID, grantedAuthorities)
            }
        }
        return null
    }

    //method for JWT authentication
    fun getAdminJWTAuthentication(tokenHeader: String?): JWTAuthenticationTokenAdmin? {
        if (tokenHeader != null) {
            // parse the token.
            val token = tokenHeader.replace(SecurityConstants.TOKEN_PREFIX, "")
            val userId: String
            val role: String?
            try {
                val jwtDecoder = JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET_ACCESS.toByteArray()))
                    .build()
                    .verify(token)
                userId = jwtDecoder.subject
                role = jwtDecoder.claims["role"]?.asString()
            }catch (e:Exception){
                e.stackTrace
                return null
            }
            val user = adminRepository.findById(userId.toLong())
            if (user.isPresent) {
                val grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(role)
                return JWTAuthenticationTokenAdmin(token, user.get(), grantedAuthorities)
            }
        }
        return null
    }

    fun getRefreshTokenAuthentication(tokenHeader: String?): JWTAuthenticationToken? {
        if (tokenHeader != null) {
            // parse the token.
            val token = tokenHeader.replace(SecurityConstants.TOKEN_PREFIX, "")
            val jwtDecoder = JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET_Ref.toByteArray()))
                .build()
                .verify(token)
            val userId = jwtDecoder.subject
            val role = jwtDecoder.claims["role"]?.asString()
            val deviceID = jwtDecoder.claims["deviceID"]?.asString()
            val user = userRepository.findById(userId.toLong())
            if (user.isPresent) {
                val grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList(role)
                return JWTAuthenticationToken(token, user.get(), deviceID, grantedAuthorities)
            }
        }
        return null
    }

    //method for Google authentication
    fun getGoogleAuthentication(tokenHeader: String?, googleTokenVerifier: GoogleTokenVerifier, deviceID: String?): JWTAuthenticationToken? {
        if (tokenHeader != null) {
            val token = tokenHeader.replace(SecurityConstants.TOKEN_PREFIX, "")
            val googleToken: GoogleIdToken?
            try {
                 googleToken = googleTokenVerifier.verifyToken(token)
            }
            catch (e: Exception){
                e.stackTrace
                logger.error("Invalid Google Token")
                return null
            }
            val userid = googleToken.payload.subject
            val user = userRepository.findByGoogleId(userid)
            if(user!=null) {
                val grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("USER")
                return JWTAuthenticationToken(token, user, deviceID, grantedAuthorities)
            }
            else{
                //register new user
                val newUser = saveUserService.createGoogleUser(googleToken.payload)
                val grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("USER")
                return JWTAuthenticationToken(token, newUser, deviceID, grantedAuthorities)
            }
        }
        return null
    }

    //method for Facebook authentication
    fun getFacebookAuthentication(tokenHeader: String?, facebookTokenVerify: FacebookTokenVerifier, deviceID: String?): JWTAuthenticationToken? {
        if (tokenHeader != null) {
            val token = tokenHeader.replace(SecurityConstants.TOKEN_PREFIX, "")
            val response = facebookTokenVerify.verifyToken(token) ?: return null
            val facebookId = response.findValue("id")?.asText()
            val user = userRepository.findByFacebookId(facebookId.toString())
            if (user!=null) {
                val grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("USER")
                return JWTAuthenticationToken(token, user, deviceID, grantedAuthorities)
            }
            else{
                //register new user with facebook id
                val newUser = saveUserService.createFacebookUser(response)
                val grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("USER")
                return JWTAuthenticationToken(token, newUser, deviceID, grantedAuthorities)
            }
        }
        return null
    }
    //method for Apple authentication
    fun getAppleAuthentication(tokenHeader: String?, appleTokenVerifier: AppleTokenVerifier, deviceID: String?): JWTAuthenticationToken? {
        if (tokenHeader != null) {
            val token = tokenHeader.replace(SecurityConstants.TOKEN_PREFIX, "")
            val claims: JwtClaims?
            try {
                claims = appleTokenVerifier.verifyToken(token)
            }
            catch (e: Exception){
                e.stackTrace
                return null
            }
            if (claims!=null) {
                val appleId = claims.subject
                val user = userRepository.findByAppleId(appleId.toString())
                if (user!=null) {
                    val grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("USER")
                    return JWTAuthenticationToken(token, user, deviceID, grantedAuthorities)
                } else {
                    //register new user
                    val email = claims.getClaimValue("email")?.toString()
                    val newUser = saveUserService.createAppleUser(email, appleId)
                    val grantedAuthorities = AuthorityUtils.commaSeparatedStringToAuthorityList("USER")
                    return JWTAuthenticationToken(token, newUser, deviceID, grantedAuthorities)
                }
            }
        }
        return null
    }
    fun getGuestJWTAuthentication(tokenHeader: String?): JWTAuthenticationTokenGuest? {
        if (tokenHeader != null) {
            // parse the token.
            val token = tokenHeader.replace(SecurityConstants.TOKEN_PREFIX, "")
            val deviceID: String?
            val key: String?
            try {
                val jwtDecoder = JWT.require(Algorithm.HMAC512(SecurityConstants.SECRET_ACCESS.toByteArray()))
                    .build()
                    .verify(token)
                key = jwtDecoder.claims["id"]?.asString()
                deviceID = jwtDecoder.claims["deviceID"]?.asString()
            }catch (e:Exception){
                e.stackTrace
                return null
            }
            if (key!=null && key == SecurityConstants.GUEST_KEY) {
                return JWTAuthenticationTokenGuest(SecurityConstants.GUEST_KEY,deviceID)
            }
        }
        return null
    }

}