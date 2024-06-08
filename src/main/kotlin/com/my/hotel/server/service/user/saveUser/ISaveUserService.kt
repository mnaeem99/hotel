package com.my.hotel.server.service.user.saveUser

import com.fasterxml.jackson.databind.JsonNode
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.my.hotel.server.data.model.ConfirmationCode
import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.model.UserAuthentication

interface ISaveUserService {
    fun saveUser(type: UserAuthentication.Type, email:String?, phone:String?): ConfirmationCode
    fun verifyCode(verificationCode: ConfirmationCode, code: Int): Boolean
    fun deleteUserAuthentication(auth: UserAuthentication)
    fun addUser(input: com.my.hotel.server.graphql.dto.request.UserInput): com.my.hotel.server.graphql.dto.response.UserDto?
    fun updateUser(input: com.my.hotel.server.graphql.dto.request.UpdateUser): com.my.hotel.server.graphql.dto.response.UserDto?
    fun deleteUser(id: Long): Boolean
    fun createNickName(name: String): String
    fun createGoogleUser(payload: GoogleIdToken.Payload): User
    fun createFacebookUser(response: JsonNode): User
    fun createAppleUser(email: String?, appleId: String?): User
}