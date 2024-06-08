package com.my.hotel.server.service.status

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.response.*
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.security.SecurityUtils
import com.my.hotel.server.service.event.EventService
import com.my.hotel.server.service.event.dto.Event
import com.my.hotel.server.service.status.dto.SpendingStatus
import com.my.hotel.server.service.status.dto.StatusHistoryDto
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import java.util.stream.Collectors


@Service
@Slf4j
class StatusService @Autowired constructor(
    private val favoriteRepository: FavoriteRepository,
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val statusRepository: StatusRepository,
    private val statusHistoryRepository: StatusHistoryRepository,
    private val notificationService: EventService,
    private val dateProvider: DateProvider,
    private val hotelTranslationRepository: HotelTranslationRepository,
    private val translationService: TranslationService,
    private val loyaltyPointRepository: LoyaltyPointRepository,
    private val pointsHistoryRepository: PointsHistoryRepository,
    ) : IStatusService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun getStatus(): SpendingStatus {
        val principal = SecurityUtils.getLoggedInUser()
        val statuses = principal.status
        val percentage = calculatePercentage(principal)
        return SpendingStatus(percentage,statuses)
    }
    override fun getLoyaltyPoints(language: String?, pageOptions: GraphQLPage): Page<LoyaltyPointsDto>? {
        val principal = SecurityUtils.getLoggedInUser()
        var key = "-${principal.id}-${language}-${pageOptions.page}-${pageOptions.size}"
        pageOptions.sort.orders.forEach { order ->
            key = key.plus("-${order.property}-${order.direction.name}-${order.nullHandling.name}-${order.ignoreCase}")
        }
        val points = loyaltyPointRepository.findByUser(principal, pageOptions.toPageable()).map { entity ->
            LoyaltyPointsDto(
                translationService.mapUserDto(entity.user, language),
                translationService.mapmyHotelDto(entity.hotel, language),
                entity.loyaltyPoints,
                entity.id
            )
        }
        return points
    }
    override fun getPointsHistory(language: String?, pageOptions: GraphQLPage): Page<PointsHistoryDto>? {
        val principal = SecurityUtils.getLoggedInUser()
        var key = "-${principal.id}-${language}-${pageOptions.page}-${pageOptions.size}"
        pageOptions.sort.orders.forEach { order ->
            key = key.plus("-${order.property}-${order.direction.name}-${order.nullHandling.name}-${order.ignoreCase}")
        }
        val points = pointsHistoryRepository.findByUser(principal, pageOptions.toPageable()).map { entity ->
            PointsHistoryDto(
                translationService.mapUserDto(entity.user, language),
                translationService.mapmyHotelDto(entity.hotel, language),
                entity.pointsSpent,
                entity.id
            )
        }
        return points
    }
    override fun calculateStatuses(userId: Long?, topPercentage:Double): List<Status>? {
        val user = userRepository.findById(userId!!).get()
        val noOfFavorites = favoriteRepository.countByUser(user) ?: 0
        val noOfOrder = orderRepository.countByUser(user.id!!) ?: 0
        if (noOfFavorites >=10)
            addStatus(user, 1)
        else
            updateStatus(user, 1)
        if (noOfOrder>=1)
            addStatus(user,2)
        else
            updateStatus(user, 2)
        if (topPercentage<=70)
            addStatus(user,3)
        else
            updateStatus(user, 3)
        if (topPercentage<=40)
            addStatus(user,4)
        else
            updateStatus(user, 4)
        if (topPercentage<=10)
            addStatus(user,5)
        else
            updateStatus(user, 5)
        return user.status
    }
    override fun calculatePercentage(user: User): Int {
        val usersPercentage = calculateUserSpendingPercentage()
        if (usersPercentage.isEmpty() || !usersPercentage.containsKey(user.id))
            return 100
        return usersPercentage.getValue(user.id!!).toInt()
    }
    override fun executeStatusJob() {
        val spendingPercentage = calculateUserSpendingPercentage()
        spendingPercentage.entries.stream().map { entity -> calculateStatuses(entity.key, entity.value) }
    }
    override fun getStatusHistory(language: String?): List<StatusHistoryDto>? {
        val principal = SecurityUtils.getLoggedInUser()
        val orders = orderRepository.findUserOrder(principal.id!!)
        val statusHistory = statusHistoryRepository.findByUser(principal)
        return orders?.stream()?.map { entity -> toStatusHistoryDto(entity, statusHistory, language) }?.collect(Collectors.toList())
    }
    private fun toStatusHistoryDto(order: Order, statusHistory: List<StatusHistory>?, language: String?): StatusHistoryDto{
        val hotelTranslation = hotelTranslationRepository.findByHotel(order.hotel.id!!, language ?:Constants.DEFAULT_LANGUAGE)
        return StatusHistoryDto(hotelTranslation?.name,hotelTranslation?.address,statusHistory,order.hotel.photo, order.amount,order.updatedAt)
    }
    private fun addStatus(user: User, statusId: Long): List<Status>? {
        if (!statusExist(user,statusId)) {
            val status = statusRepository.findById(statusId).get()
            var previousStatus: Status? = null
            if (!user.status.isNullOrEmpty())
                previousStatus = user.status?.last()
            user.status = user.status?.plus(status)
            userRepository.save(user)
            logger.info("${user.firstName} ${user.lastName} got new status ${status.name}")
            notificationService.createEvent(Event(NotificationType.MY_STATUS,user.id,user.id, statusId))
            notificationService.createEvent(Event(NotificationType.FRIEND_STATUS,user.id,user.id,statusId))
            val statusHistory = statusHistoryRepository.findUserStatus(user.id!!, statusId)
            if (statusHistory==null)
                statusHistoryRepository.save(StatusHistory(user, previousStatus, status, dateProvider.getCurrentDate(), dateProvider.getCurrentDate()))
        }
        return user.status
    }
    private fun deleteStatus(user: User, statusId: Long): List<Status>? {
        if (statusExist(user,statusId)) {
            val status = statusRepository.findById(statusId).get()
            user.status = user.status?.minus(status)
            userRepository.save(user)
            logger.info("${user.firstName} ${user.lastName} removed from status ${status.name}")
        }
        return user.status
    }
    private fun updateStatus(user: User, statusId: Long): List<Status>? {
        if (!statusExist(user,statusId))
            return null
        val statusHistory = statusHistoryRepository.findUserStatus(user.id!!, statusId) ?: return null
        if (statusHistory.createdAt!! < dateProvider.getCurrentDate().minusMonths(6))
            deleteStatus(user, statusId)
        else {
            statusHistory.updatedAt = dateProvider.getCurrentDate()
            statusHistoryRepository.save(statusHistory)
        }
        return user.status
    }
    private fun statusExist(user: User, statusId: Long) : Boolean {
        val statuses = user.status ?: return false
        for (status in statuses) {
            if (status.id == statusId)
                return true
        }
        return false
    }
    private fun calculateUserSpendingPercentage(): HashMap<Long,Double> {
        val total = orderRepository.findTotalSpending()
        val usersPercentage = orderRepository.findUserPercentage(total ?: 0.0)
        val sortedPercentage = HashMap<Long, Double>()
        if (usersPercentage != null) {
            for (userPercentage in usersPercentage) {
                sortedPercentage[userPercentage.user!!] = userPercentage.amount!!
            }
        }
        return sortedPercentage
    }
}