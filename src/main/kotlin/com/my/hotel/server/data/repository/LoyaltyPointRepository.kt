package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.LoyaltyPoints
import com.my.hotel.server.data.model.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface LoyaltyPointRepository : JpaRepository<LoyaltyPoints, Long>, JpaSpecificationExecutor<LoyaltyPoints>{
    fun findByUserAndHotel(user: User, hotel: MyHotel) : LoyaltyPoints?
    @Query("SELECT w from LoyaltyPoints w where w.user.id = :userId AND ( w.hotel.id = :hotelId OR w.hotel.placeId = :placeId )")
    fun findByUserAndHotel(userId: Long, hotelId: Long?, placeId: String?) : LoyaltyPoints?
    @Modifying
    @Transactional
    fun deleteByUser(user: User)
    fun findByUser(user: User, pageable: Pageable) : Page<LoyaltyPoints>

    @Modifying
    @Transactional
    fun deleteByHotel(hotel: MyHotel)
}