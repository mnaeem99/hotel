package com.my.hotel.server.service.facebook

import com.fasterxml.jackson.databind.JsonNode
import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.repository.UserRepository
import com.my.hotel.server.security.SecurityConstants
import org.apache.http.client.utils.URIBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI
import javax.servlet.http.HttpServletRequest

@Service
class FacebookService @Autowired constructor(
    private val req: HttpServletRequest,
    private val userRepository: UserRepository,
    private val restTemplate: RestTemplate
) : IFacebookService {
    override fun getFacebookResponse(userAccessToken: String?): JsonNode?{
        val builder = URIBuilder(String.format("%s/me", Constants.GRAPH_API_URL))
        builder.addParameter("fields", "id,first_name,last_name,name,birthday,email,picture,friends{id,name,picture}" )
        builder.addParameter("input_token", "access_token" )
        builder.addParameter("access_token", userAccessToken)
        val uri: URI = builder.build()
        val response: JsonNode
        try {
            response = restTemplate.getForObject(uri, JsonNode::class.java)!!
        }catch (e: Exception){
            e.stackTrace
            return null
        }
        return response
    }
    override fun getFacebookFriends(): List<String>? {
        var list = emptyList<String>()
        val header = req.getHeader(SecurityConstants.HEADER_STRING)
        val token = header.replace(SecurityConstants.TOKEN_PREFIX, "")
        val response = getFacebookResponse(token)
        val friends = response?.findValue("friends") ?: return null
        val friendData = friends.findValue("data")?.toList()
        for (data in friendData!!){
            val id =   data.findValue("id").asText()
            list = list.plus(id.toString())
        }
        return list
    }
    override fun getInviteFriends(): List<User>? {
        var list = emptyList<User>()
        val header = req.getHeader(SecurityConstants.HEADER_STRING)
        val token = header.replace(SecurityConstants.TOKEN_PREFIX, "")
        val response = getFacebookResponse(token)
        val friends = response?.findValue("friends") ?: return null
        val friendData = friends.findValue("data")?.toList()
        for (data in friendData!!){
            val id =   data.findValue("id").asText()
            val firstName =   data.findValue("first_name").asText()
            val lastName =   data.findValue("last_name").asText()
            val facebookUser = userRepository.findByFacebookId(id)
            if (facebookUser == null) {
                val findFriends = User( firstName, lastName,id = id.toLong(), auths = listOf())
                list = list.plus(findFriends)
            }
        }
        return list
    }
    }