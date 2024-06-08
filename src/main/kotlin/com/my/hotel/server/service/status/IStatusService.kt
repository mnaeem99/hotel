package com.my.hotel.server.service.status

import com.my.hotel.server.data.model.Status
import com.my.hotel.server.data.model.User
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.response.LoyaltyPointsDto
import com.my.hotel.server.graphql.dto.response.PointsHistoryDto
import com.my.hotel.server.service.status.dto.SpendingStatus
import com.my.hotel.server.service.status.dto.StatusHistoryDto
import org.springframework.data.domain.Page

interface IStatusService {
    fun getStatus(): SpendingStatus
    fun getLoyaltyPoints(language: String?, pageOptions: GraphQLPage): Page<LoyaltyPointsDto>?
    fun getPointsHistory(language: String?, pageOptions: GraphQLPage): Page<PointsHistoryDto>?
    fun calculateStatuses(userId: Long?, topPercentage:Double): List<Status>?
    fun calculatePercentage(user: User): Int
    fun executeStatusJob()
    fun getStatusHistory(language: String?): List<StatusHistoryDto>?
}