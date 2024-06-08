package com.my.hotel.server.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.my.hotel.server.service.aws.AWSService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.ByteArrayInputStream


@Configuration
class FirebaseConfig @Autowired constructor(
    @Value("\${aws.secrets.firebaseKey}")
    private var firebaseKey:String,
    private val awsService: AWSService
) {
    @Bean
    fun firebaseApp(): FirebaseApp? {
        val secretsJson = awsService.getValue(firebaseKey)
        val objectMapper = ObjectMapper()
        val bytes = objectMapper.writeValueAsBytes(secretsJson)
        val fcmCredential = ByteArrayInputStream(bytes)
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.fromStream(fcmCredential))
            .build()
        return FirebaseApp.initializeApp(options)
    }

    @Bean
    fun firebaseMessaging(): FirebaseMessaging? {
        return FirebaseMessaging.getInstance(firebaseApp())
    }
}