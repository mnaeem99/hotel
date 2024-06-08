package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.StatusHistory
import com.my.hotel.server.data.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional


@Repository
interface StatusHistoryRepository : JpaRepository<StatusHistory, Long>, JpaSpecificationExecutor<StatusHistory> {
    fun findByUser(user: User) : List<StatusHistory>?
    @Query(value = "SELECT sh FROM StatusHistory sh WHERE sh.user.id = ?1 AND sh.to.id = ?2 ")
    fun findUserStatus(userId: Long, statusId: Long) : StatusHistory?
    @Modifying
    @Transactional
    fun deleteByUser(user: User)
}