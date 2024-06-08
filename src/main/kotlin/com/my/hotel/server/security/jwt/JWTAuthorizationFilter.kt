package com.my.hotel.server.security.jwt

import com.fasterxml.jackson.databind.ObjectMapper
import com.my.hotel.server.mvc.error.ApplicationException
import com.my.hotel.server.security.SecurityConstants
import com.my.hotel.server.security.oauth.AppleTokenVerifier
import com.my.hotel.server.security.oauth.FacebookTokenVerifier
import com.my.hotel.server.security.oauth.GoogleTokenVerifier
import org.springframework.http.MediaType
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JWTAuthorizationFilter(
    private val authManager: AuthenticationManager?,
    private val googleTokenVerifier: GoogleTokenVerifier,
    private val facebookTokenVerifier: FacebookTokenVerifier,
    private val appleTokenVerifier: AppleTokenVerifier,
    private val jwtDecoder: JWTDecoder
) : BasicAuthenticationFilter(authManager) {

    @Throws(IOException::class, ServletException::class)
    override fun doFilterInternal(
        req: HttpServletRequest,
        res: HttpServletResponse,
        chain: FilterChain
    ) {
        val header = req.getHeader(SecurityConstants.HEADER_STRING)
        if (req.servletPath.equals("/api/accessToken")){
            val authentication = jwtDecoder.getRefreshTokenAuthentication(
                req.getHeader(SecurityConstants.HEADER_STRING)
            )
            SecurityContextHolder.getContext().authentication = authentication
            chain.doFilter(req, res)
        }
        if (req.servletPath.equals("/api/social/login")){
            val deviceID = req.getHeader(SecurityConstants.DEVICE_HEADER)
            var facebookAuthentication: JWTAuthenticationToken? = null
            var googleAuthentication: JWTAuthenticationToken? = null
            var appleAuthentication: JWTAuthenticationToken? = null
            try {
                facebookAuthentication = jwtDecoder.getFacebookAuthentication(header, facebookTokenVerifier, deviceID)
                googleAuthentication = jwtDecoder.getGoogleAuthentication(header, googleTokenVerifier, deviceID)
                appleAuthentication = jwtDecoder.getAppleAuthentication(header, appleTokenVerifier, deviceID)
            }catch (exception: ApplicationException){
                applicationException(res,exception.message)
            }
            if(facebookAuthentication != null) {
                SecurityContextHolder.getContext().authentication = facebookAuthentication
            }
            if(googleAuthentication != null) {
                SecurityContextHolder.getContext().authentication = googleAuthentication
            }
            if(appleAuthentication != null) {
                SecurityContextHolder.getContext().authentication = appleAuthentication
            }
            chain.doFilter(req, res)
            return
        }
        // If no header or if the prefix is wrong, do not authenticate.
        // Security framework will take care of rejecting the request if we need authentication
        if (header == null || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            chain.doFilter(req, res)
            return
        }

        //authenticate
        val authentication = jwtDecoder.getJWTAuthentication(header)
        val adminAuthentication = jwtDecoder.getAdminJWTAuthentication(header)
        val guestAuthentication = jwtDecoder.getGuestJWTAuthentication(header)
        if(authentication != null) {
            SecurityContextHolder.getContext().authentication = authentication
        } else if (adminAuthentication!=null){
            SecurityContextHolder.getContext().authentication = adminAuthentication
        } else if (guestAuthentication!=null){
            SecurityContextHolder.getContext().authentication = guestAuthentication
        }
        chain.doFilter(req, res)
        return

    }
    private fun applicationException(response: HttpServletResponse, message: String) {
        val errorMessage: HashMap<String, String> = HashMap<String, String>()
        errorMessage["error"] = message
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        val objectMapper = ObjectMapper()
        objectMapper.writeValue(response.outputStream, errorMessage)
    }
}