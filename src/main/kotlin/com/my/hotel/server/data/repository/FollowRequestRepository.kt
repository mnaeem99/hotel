package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.FollowRequest
import com.my.hotel.server.data.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface FollowRequestRepository : JpaRepository<FollowRequest, Long>, JpaSpecificationExecutor<FollowRequest>{
    fun findByFollowerAndFollowing(follower: User, following: User) : FollowRequest?
    @Query("SELECT follow from FollowRequest follow where follow.follower.id = :follower AND follow.following.id = :following ")
    fun findByFollowerAndFollowing(follower: Long?, following: Long?) : FollowRequest?

}