package com.my.hotel.server.security.oauth

import com.my.hotel.server.service.aws.AWSService
import lombok.extern.slf4j.Slf4j
import org.jose4j.jwk.HttpsJwks
import org.jose4j.jwt.JwtClaims
import org.jose4j.jwt.consumer.InvalidJwtException
import org.jose4j.jwt.consumer.JwtConsumer
import org.jose4j.jwt.consumer.JwtConsumerBuilder
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component


@Component
@Slf4j
class AppleTokenVerifier @Autowired constructor(
    private val awsService: AWSService,
    @Value("\${aws.secrets.ClientIds}")
    private var secretsClientIds:String,
) {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    fun verifyToken(token: String?): JwtClaims? {
        val secretsJson = awsService.getValue(secretsClientIds)
        val clientId = secretsJson?.get("appleClientId")?.asText()
        val httpsJwks = HttpsJwks("https://appleid.apple.com/auth/keys")
        val httpsJwksKeyResolver = HttpsJwksVerificationKeyResolver(httpsJwks)
        val jwtConsumer: JwtConsumer = JwtConsumerBuilder()
            .setVerificationKeyResolver(httpsJwksKeyResolver)
            .setExpectedIssuer("https://appleid.apple.com")
            .setExpectedAudience(clientId)
            .build()
        val claims: JwtClaims?
        try {
            claims = jwtConsumer.processToClaims(token.toString())
        }
        catch (e: InvalidJwtException)
        {
            e.stackTrace
            logger.error("Invalid Apple Token: $token")
            return null
        }
        if (claims!=null)
            return claims
        return null
    }
}