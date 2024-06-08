package com.my.hotel.server.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.DeviceLocationRepository
import com.my.hotel.server.data.repository.RefreshTokenAdminRepository
import com.my.hotel.server.data.repository.RefreshTokenRepository
import com.my.hotel.server.provider.dateProvider.DateProvider
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.*

@Component
class SecurityUtils(
    private val refreshTokenRepository: RefreshTokenRepository,
    private val refreshTokenAdminRepository: RefreshTokenAdminRepository,
    private val deviceLocationRepository: DeviceLocationRepository,
    private val dateProvider: DateProvider
) {
    private fun saveDeviceLocation(deviceID: String, user: User?) {
        val deviceLocation = deviceLocationRepository.findByIdOrNull(deviceID)
        if (deviceLocation==null) {
            val newDeviceLocation = DeviceLocation(user, null, dateProvider.getCurrentDateTime(), dateProvider.getCurrentDateTime(), deviceID)
            deviceLocationRepository.save(newDeviceLocation)
        }
        else if (user!=null && (deviceLocation.user == null || deviceLocation.user!!.id != user.id)) {
            deviceLocation.user = user
            deviceLocationRepository.save(deviceLocation)
        }
    }

    fun generateAccessToken(user: User, deviceID: String?): String {
        if (deviceID!=null){
            saveDeviceLocation(deviceID, user)
        }
        val accessToken = RefreshToken()
        accessToken.user = user
        accessToken.token = this.token()
        refreshTokenRepository.save(accessToken)
        return this.jwtAccess(user, accessToken.token, deviceID)
    }
    fun generateRefreshToken(user: User, deviceID: String?): String {
        if (deviceID!=null){
            saveDeviceLocation(deviceID, user)
        }
        val refreshToken = RefreshToken()
        refreshToken.user = user
        refreshToken.token = this.token()
        refreshTokenRepository.save(refreshToken)
        return this.jwtRefresh(user, refreshToken.token, deviceID)
    }

    /**
     * Generate a JWT token for the specified user.
     * @param user User to generate the token for.
     * @return Either the JWT token string, or null if one couldn't be generated.
     */
    private fun jwtAccess(user: User, accessToken: String?, deviceID: String?): String {
        return JWT.create()
            .withSubject(user.id.toString())
            .withClaim("id", user.id)
            .withClaim("accessToken", accessToken)
            .withClaim("deviceID", deviceID)
            .withClaim("role", "USER")
            .withExpiresAt(
                Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME)
            )
            .sign(Algorithm.HMAC512(SecurityConstants.SECRET_ACCESS.toByteArray()))
    }

    private fun jwtRefresh(user: User, refreshToken: String?, deviceID: String?): String {
        return JWT.create()
            .withSubject(user.id.toString())
            .withClaim("id", user.id)
            .withClaim("refreshToken", refreshToken)
            .withClaim("deviceID", deviceID)
            .withClaim("role", "USER")
            .sign(Algorithm.HMAC512(SecurityConstants.SECRET_Ref.toByteArray()))
    }

    private fun token(): String {
        val random = SecureRandom()
        val bytes = ByteArray(128)
        random.nextBytes(bytes)
        val encoder: Base64.Encoder = Base64.getUrlEncoder().withoutPadding()
        return encoder.encodeToString(bytes)
    }

    fun generateAccessToken(admin: Admin): String {
        val accessToken = RefreshTokenAdmin()
        accessToken.admin = admin
        accessToken.token = this.token()
        refreshTokenAdminRepository.save(accessToken)
        return this.jwtAccess(admin, accessToken.token)
    }
    fun generateRefreshToken(admin: Admin): String {
        val refreshToken = RefreshTokenAdmin()
        refreshToken.admin = admin
        refreshToken.token = this.token()
        refreshTokenAdminRepository.save(refreshToken)
        return this.jwtRefresh(admin, refreshToken.token)
    }

    /**
     * Generate a JWT token for the specified admin.
     * @param admin Admin to generate the token for.
     * @return Either the JWT token string, or null if one couldn't be generated.
     */
    private fun jwtAccess(admin: Admin, accessToken: String?): String {
        return JWT.create()
            .withSubject(admin.id.toString())
            .withClaim("id", admin.id)
            .withClaim("accessToken", accessToken)
            .withClaim("role", "ADMIN")
            .withExpiresAt(
                Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME)
            )
            .sign(Algorithm.HMAC512(SecurityConstants.SECRET_ACCESS.toByteArray()))
    }

    private fun jwtRefresh(admin: Admin, refreshToken: String?): String {
        return JWT.create()
            .withSubject(admin.id.toString())
            .withClaim("id", admin.id)
            .withClaim("refreshToken", refreshToken)
            .sign(Algorithm.HMAC512(SecurityConstants.SECRET_Ref.toByteArray()))
    }


    /**
     * Generate a JWT token for the specified guest.
     * @return Either the JWT token string, or null if one couldn't be generated.
     */
    private fun jwtGuestAccess(accessToken: String?, deviceID: String?): String {
        return JWT.create()
            .withClaim("id", SecurityConstants.GUEST_KEY)
            .withClaim("accessToken", accessToken)
            .withClaim("deviceID", deviceID)
            .withExpiresAt(
                Date(System.currentTimeMillis() + SecurityConstants.EXPIRATION_TIME)
            )
            .sign(Algorithm.HMAC512(SecurityConstants.SECRET_ACCESS.toByteArray()))
    }
    fun generateGuestToken(deviceID: String?): String {
        if (deviceID!=null){
            saveDeviceLocation(deviceID, null)
        }
        return this.jwtGuestAccess(this.token(), deviceID)
    }

    companion object {
        fun getLoggedInUser(): User {
            val principal = SecurityContextHolder.getContext().authentication.principal
            if (principal is User)
                return principal
            throw AccessDeniedException("User not authenticated")
        }
        fun getPrincipalUser(): User? {
            val principal = SecurityContextHolder.getContext().authentication.principal
            if (principal is User)
                return principal
            return null
        }
        fun getLoggedInUserId(): Long? {
            val principal = SecurityContextHolder.getContext().authentication.principal
            var userId: Long? = null
            if (principal is User){
                userId = principal.id
            }
            return userId
        }
        fun getLoggedInDevice(): String? {
            val auth = SecurityContextHolder.getContext().authentication
            if (auth.details is String) {
                return auth.details as String
            }
            return null
        }
    }

}