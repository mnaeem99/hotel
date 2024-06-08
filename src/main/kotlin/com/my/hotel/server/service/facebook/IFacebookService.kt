package com.my.hotel.server.service.facebook

import com.fasterxml.jackson.databind.JsonNode
import com.my.hotel.server.data.model.User

interface IFacebookService {
    fun getFacebookResponse(userAccessToken: String?): JsonNode?
    fun getFacebookFriends(): List<String>?
    fun getInviteFriends(): List<User>?
}