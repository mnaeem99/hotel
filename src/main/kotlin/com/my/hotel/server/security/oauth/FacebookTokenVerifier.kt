package com.my.hotel.server.security.oauth

import com.fasterxml.jackson.databind.JsonNode
import com.my.hotel.server.commons.Constants
import com.my.hotel.server.service.aws.AWSService
import com.my.hotel.server.service.facebook.FacebookService
import lombok.extern.slf4j.Slf4j
import org.apache.http.client.utils.URIBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.AuthenticationServiceException
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.net.URI

@Component
@Slf4j
class FacebookTokenVerifier @Autowired constructor(
    private val restTemplate: RestTemplate,
    private val awsService: AWSService,
    @Value("\${aws.secrets.ClientIds}")
    private var secretsClientIds:String,
    private val facebookService: FacebookService,
) {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    fun verifyToken(userAccessToken: String?): JsonNode? {
        val builder = URIBuilder(String.format("%s/app", Constants.GRAPH_API_URL))
        builder.addParameter("input_token", "access_token" )
        builder.addParameter("access_token", userAccessToken)
        val uri: URI = builder.build()
        val response: JsonNode
        try {
            response = restTemplate.getForObject(uri, JsonNode::class.java)!!
        }catch (e: Exception){
            e.stackTrace
            logger.error("Invalid Facebook Token: $userAccessToken")
            return null
        }
        val facebookAppId = response.findValue("id")?.asText()
        val secretsJson = awsService.getValue(secretsClientIds)
        val appId = secretsJson?.get("facebookAppId")?.asText()
        if (facebookAppId == null || facebookAppId != appId) {
            throw AuthenticationServiceException(String.format("Presented identity: %s did not match verified identity: %s", appId, facebookAppId))
        }
        return facebookService.getFacebookResponse(userAccessToken)
    }
}
