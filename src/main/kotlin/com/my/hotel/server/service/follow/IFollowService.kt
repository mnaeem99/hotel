package com.my.hotel.server.service.follow

import com.my.hotel.server.graphql.GraphQLPage
import org.springframework.data.domain.Page

interface IFollowService {
    fun followRequest(userId: Long): com.my.hotel.server.graphql.dto.response.FollowStatus
    fun unfollowRequest(userId: Long): com.my.hotel.server.graphql.dto.response.FollowStatus
    fun cancelFollowRequest(userId: Long): com.my.hotel.server.graphql.dto.response.FollowStatus
    fun rejectRequest(userId: Long): com.my.hotel.server.graphql.dto.response.FollowStatus
    fun acceptRequest(userId: Long): com.my.hotel.server.graphql.dto.response.FollowStatus
    fun blockRequest(userId: Long): Boolean
    fun unBlockRequest(userId: Long): Boolean
    fun getFollowers(filter: com.my.hotel.server.graphql.dto.request.SearchUserFilter, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>?
    fun getFollowing(filter: com.my.hotel.server.graphql.dto.request.SearchUserFilter, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>?
    fun getFollowRequest(language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>?

}