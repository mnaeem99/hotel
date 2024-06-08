package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.PointsHistory
import com.my.hotel.server.data.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface PointsHistoryRepository : JpaRepository<PointsHistory, Long>, JpaSpecificationExecutor<PointsHistory>{
    fun findByUser(user: User, pageable: Pageable) : Page<PointsHistory>
    @Modifying
    @Transactional
    fun deleteByUser(user: User)

    @Modifying
    @Transactional
    fun deleteByHotel(hotel: MyHotel)
}