package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.QRCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional


@Repository
interface QRCodeRepository : JpaRepository<QRCode, Long>, JpaSpecificationExecutor<QRCode>{
    @Modifying
    @Transactional
    @Query("delete from QRCode q where q.order.id in (select o.id from Order o where o.user.id = ?1 )")
    fun deleteByUser(userId: Long)

    @Modifying
    @Transactional
    fun deleteByHotel(hotel: MyHotel)
}