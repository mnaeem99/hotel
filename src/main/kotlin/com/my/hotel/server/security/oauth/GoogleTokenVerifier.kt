package com.my.hotel.server.security.oauth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.gson.GsonFactory
import com.my.hotel.server.service.aws.AWSService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.stereotype.Component


@Component
class GoogleTokenVerifier @Autowired constructor(
    private val awsService: AWSService,
    @Value("\${aws.secrets.ClientIds}")
    private var secretsClientIds:String,
) {
    val transport: HttpTransport = NetHttpTransport()
    val jsonFactory: JsonFactory = GsonFactory.getDefaultInstance()
    fun verifyToken(idTokenString: String): GoogleIdToken {
        val secretsJson = awsService.getValue(secretsClientIds)
        val androidClientId = secretsJson?.get("androidGoogleClientId")?.asText()
        val appleClientId = secretsJson?.get("appleGoogleClientId")?.asText()
        val webClientId = secretsJson?.get("webGoogleClientId")?.asText()
        val web2ClientId = secretsJson?.get("web2GoogleClientId")?.asText()
        val verifier: GoogleIdTokenVerifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
            .setAudience(listOf(androidClientId, appleClientId, webClientId, web2ClientId))
            .build()
        return verifier.verify(idTokenString)
            ?: throw AuthenticationServiceException("Invalid Token")
    }
}