package com.my.hotel.server.service.findFriends

import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.response.UserDto
import org.springframework.data.domain.Page

interface IFindFriendService {
    fun getPopularUser(language: String?, keyword: String?, pageOptions: GraphQLPage): Page<UserDto>?
    fun getMyUserFromContacts(phones: List<String>, language: String?, keyword: String?, pageOptions: GraphQLPage): Page<UserDto>?
    fun getMyUserFromFacebook(facebookId: List<String>, language: String?, keyword: String?, pageOptions: GraphQLPage): Page<UserDto>?
    fun getSuggestUser(language: String?, pageOptions: GraphQLPage): Page<UserDto>?
}