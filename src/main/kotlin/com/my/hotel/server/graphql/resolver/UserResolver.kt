package com.my.hotel.server.graphql.resolver

import com.my.hotel.server.data.repository.FollowRequestRepository
import com.my.hotel.server.data.repository.UserRepository
import com.my.hotel.server.graphql.security.Unsecured
import com.my.hotel.server.security.SecurityUtils
import graphql.kickstart.tools.GraphQLResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class UserResolver @Autowired constructor(
    val followRequestRepository: FollowRequestRepository,
    val userRepository: UserRepository,
    ) : GraphQLResolver<com.my.hotel.server.graphql.dto.response.UserDto> {

    fun isBlocked(user: com.my.hotel.server.graphql.dto.response.UserDto) : Boolean {
        val userId = SecurityUtils.getLoggedInUserId()
        if(userId!=null) {
            return userRepository.isBlock(userId, user.id!!) != null
        }
        return false
    }
    fun isAdminBlocked(user: com.my.hotel.server.graphql.dto.response.UserDto) : Boolean? {
        return user.isBlocked
    }
    fun isFollowing(user: com.my.hotel.server.graphql.dto.response.UserDto) : Boolean {
        val userId = SecurityUtils.getLoggedInUserId()
        if(userId!=null) {
            return userRepository.isFollowing(userId, user.id!!) != null
        }
        return false
    }

    fun isFollower(user: com.my.hotel.server.graphql.dto.response.UserDto) : Boolean {
        val userId = SecurityUtils.getLoggedInUserId()
        if(userId!=null) {
            return userRepository.isFollowing(user.id!!, userId)!=null
        }
        return false
    }

    fun isRequestedFollowing(userDto: com.my.hotel.server.graphql.dto.response.UserDto) : Boolean {
        val userId = SecurityUtils.getLoggedInUserId()
        if(userId!=null) {
            val principal = SecurityUtils.getLoggedInUser()
            return followRequestRepository.findByFollowerAndFollowing(principal.id, userDto.id) != null
        }
        return false
    }
    fun isRequestedFollower(userDto: com.my.hotel.server.graphql.dto.response.UserDto) : Boolean {
        val userId = SecurityUtils.getLoggedInUserId()
        if(userId!=null) {
            val principal = SecurityUtils.getLoggedInUser()
            return followRequestRepository.findByFollowerAndFollowing(userDto.id, principal.id) != null
        }
        return false
    }

    fun followStatus(user: com.my.hotel.server.graphql.dto.response.UserDto) : com.my.hotel.server.graphql.dto.response.FollowStatus {
        if(isFollowing(user))
            return com.my.hotel.server.graphql.dto.response.FollowStatus.FOLLOWING
        else if (isRequestedFollowing(user))
            return com.my.hotel.server.graphql.dto.response.FollowStatus.REQUESTED
        else
            return com.my.hotel.server.graphql.dto.response.FollowStatus.FOLLOW
    }
    @Unsecured
    fun followersCount(user: com.my.hotel.server.graphql.dto.response.UserDto) : Int {
        return userRepository.countFollowers(user.id!!)
    }
    @Unsecured
    fun followingCount(user: com.my.hotel.server.graphql.dto.response.UserDto) : Int {
        return userRepository.countFollowing(user.id!!)
    }
}
