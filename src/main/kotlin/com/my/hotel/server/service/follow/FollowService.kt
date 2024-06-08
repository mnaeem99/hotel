package com.my.hotel.server.service.follow

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.FollowRequest
import com.my.hotel.server.data.model.NotificationType
import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.repository.FollowRequestRepository
import com.my.hotel.server.data.repository.UserNotificationRepository
import com.my.hotel.server.data.repository.UserRepository
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.error.AlreadyExistCustomException
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.security.SecurityUtils
import com.my.hotel.server.service.event.EventService
import com.my.hotel.server.service.event.dto.Event
import com.my.hotel.server.service.notification.NotificationService
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
@Slf4j
class FollowService @Autowired constructor(
    private val userRepository: UserRepository,
    private val followRequestRepository: FollowRequestRepository,
    private val eventService: EventService,
    private val notificationService: NotificationService,
    private val userNotificationRepository: UserNotificationRepository,
    private val dateProvider: DateProvider,
    private val translationService: TranslationService,
    ): IFollowService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun followRequest(userId: Long): com.my.hotel.server.graphql.dto.response.FollowStatus {
        logger.info("Follow Request Called")
        val principal = SecurityUtils.getLoggedInUser()
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "userId")
        if (isFollowing(user) || isRequestedFollowing(user) || userId==principal.id) {
            return followStatus(user)
        }
        if (user.isPrivate == true) {
            followRequestRepository.save(FollowRequest(principal,user, dateProvider.getCurrentDateTime()))
            eventService.createEvent(Event(NotificationType.REQUEST_FOLLOWING,principal.id,userId))
            logger.info("${principal.firstName} ${principal.lastName} requested to follow  ${user.firstName} ${user.lastName} ")
            return followStatus(user)
        }
        principal.following.add(user)
        userRepository.save(principal)
        eventService.createEvent(Event(NotificationType.NEW_FOLLOWER,principal.id,userId))
        logger.info("${principal.firstName} ${principal.lastName} started following  ${user.firstName} ${user.lastName} ")
        return followStatus(user)
    }
    override fun unfollowRequest(userId: Long): com.my.hotel.server.graphql.dto.response.FollowStatus {
        logger.info("unfollow Request Called")
        val principal = SecurityUtils.getLoggedInUser()
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "userId")
        if (isFollowing(user)) {
            principal.following.removeIf { u -> u.id == userId }
            userRepository.save(principal)
            logger.info("${principal.firstName} ${principal.lastName} requested to unfollow  ${user.firstName} ${user.lastName} ")
            return followStatus(user)
        }
        else if (isRequestedFollowing(user))
            throw AlreadyExistCustomException(Constants.REQUEST_ALREADY_IN_USE)
        else
            throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "userId")
    }
    override fun cancelFollowRequest(userId: Long): com.my.hotel.server.graphql.dto.response.FollowStatus {
        logger.info("Cancel Follow Request Called")
        val principal = SecurityUtils.getLoggedInUser()
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"userId")
        val request = followRequestRepository.findByFollowerAndFollowing(principal, user) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"userId")
        userNotificationRepository.deleteFollowRequestNotification(principal.id!!, userId)
        followRequestRepository.deleteById(request.id!!)
        logger.info("${principal.firstName} ${principal.lastName} requested to cancel follow request  ${user.firstName} ${user.lastName} ")
        return followStatus(user)
    }
    override fun acceptRequest(userId: Long): com.my.hotel.server.graphql.dto.response.FollowStatus {
        logger.info("Accept Follow Request Called")
        val principal = SecurityUtils.getLoggedInUser()
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"userId")
        val request = followRequestRepository.findByFollowerAndFollowing(user, principal) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"userId")
        notificationService.updateFollowRequestNotification(userId, principal.id!!)
        followRequestRepository.deleteById(request.id!!)
        principal.followers.add(user)
        userRepository.save(principal)
        logger.info("${principal.firstName} ${principal.lastName} accept the following request of ${user.firstName} ${user.lastName} ")
        return followStatus(user)
    }
    override fun rejectRequest(userId: Long): com.my.hotel.server.graphql.dto.response.FollowStatus {
        logger.info("Reject Follow Request Called")
        val principal = SecurityUtils.getLoggedInUser()
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"userId")
        val request = followRequestRepository.findByFollowerAndFollowing(user, principal) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"userId")
        userNotificationRepository.deleteFollowRequestNotification(userId, principal.id!!)
        followRequestRepository.deleteById(request.id!!)
        logger.info("${principal.firstName} ${principal.lastName} reject the follow request of ${user.firstName} ${user.lastName} ")
        return followStatus(user)
    }
    override fun blockRequest(userId: Long): Boolean {
        logger.info("Block Request Called")
        val principal = SecurityUtils.getLoggedInUser()
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"userId")
        if (isBlock(user))
            throw AlreadyExistCustomException("Already blocked user with id \"$userId\"")
        principal.blocks.add(user)
        userRepository.save(principal)
        logger.info("${principal.firstName} ${principal.lastName} blocked ${user.firstName} ${user.lastName} ")
        return true
    }
    override fun unBlockRequest(userId: Long): Boolean {
        logger.info("unBlock Request Called")
        val principal = SecurityUtils.getLoggedInUser()
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"userId")
        if (!isBlock(user))
            throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"userId")
        principal.blocks.removeIf { u -> u.id == userId }
        userRepository.save(principal)
        logger.info("${principal.firstName} ${principal.lastName} unblock ${user.firstName} ${user.lastName} ")
        return true
    }

    fun isFollowing(user: User) : Boolean {
        val principal = SecurityUtils.getLoggedInUser()
        return userRepository.isFollowing(principal.id!!, user.id!!)!=null
    }
    fun isBlock(user: User) : Boolean {
        val principal = SecurityUtils.getLoggedInUser()
        return userRepository.isBlock(principal.id!!, user.id!!)!=null
    }
    fun isRequestedFollowing(user: User) : Boolean {
        val principal = SecurityUtils.getLoggedInUser()
        return followRequestRepository.findByFollowerAndFollowing(principal,user)!=null
    }
    fun followStatus(user: User) : com.my.hotel.server.graphql.dto.response.FollowStatus {
        return if(isFollowing(user))
            com.my.hotel.server.graphql.dto.response.FollowStatus.FOLLOWING
        else if (isRequestedFollowing(user))
            com.my.hotel.server.graphql.dto.response.FollowStatus.REQUESTED
        else
            com.my.hotel.server.graphql.dto.response.FollowStatus.FOLLOW
    }
    override fun getFollowers(filter: com.my.hotel.server.graphql.dto.request.SearchUserFilter, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>? {
        val user = userRepository.findByIdOrNull(filter.userId)
            ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "id")
        if (user.isPrivate == true){
            val principal = SecurityUtils.getLoggedInUser()
            if (isFollowUser(user.id!!, principal.id!!)){
                return userRepository.findUserFollowers(filter.userId, filter.keyword, pageOptions.toPageable()).map { entity -> translationService.mapUserDto(entity, filter.language) }
            }
            return null
        }
        return userRepository.findUserFollowers(filter.userId, filter.keyword, pageOptions.toPageable()).map { entity -> translationService.mapUserDto(entity, filter.language) }
    }
    override fun getFollowing(filter: com.my.hotel.server.graphql.dto.request.SearchUserFilter, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>? {
        val user = userRepository.findByIdOrNull(filter.userId)
            ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "id")
        if (user.isPrivate == true){
            val principal = SecurityUtils.getLoggedInUser()
            if (isFollowUser(user.id!!, principal.id!!)){
                return userRepository.findUserFollowing(filter.userId, filter.keyword, pageOptions.toPageable()).map { entity -> translationService.mapUserDto(entity, filter.language) }
            }
            return null
        }
        return userRepository.findUserFollowing(filter.userId, filter.keyword, pageOptions.toPageable()).map { entity -> translationService.mapUserDto(entity, filter.language) }
    }
    fun isFollowUser(userId: Long, principalId: Long): Boolean {
        return (userRepository.isFollowing(principalId, userId) != null
                || userRepository.isFollowing(userId, principalId) != null
                || principalId == userId)
    }
    override fun getFollowRequest(language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.UserDto>? {
        val principal = SecurityUtils.getLoggedInUser()
        return userRepository.findUserFollowingRequest(principal.id!!,pageOptions.toPageable())?.map { entity -> translationService.mapUserDto(entity, language) }
    }
}