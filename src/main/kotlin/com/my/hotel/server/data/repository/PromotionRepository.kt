package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.Promotion
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface PromotionRepository : JpaRepository<Promotion, Long>, JpaSpecificationExecutor<Promotion>{
    fun findByHotel(hotel: MyHotel, pageable: Pageable) : Page<Promotion>?
    @Query("select n from Promotion n where n in (select n2 from User u inner join u.promotion n2 where u.id = :userId)")
    fun findByUser(userId: Long, toPageable: Pageable): Page<Promotion>?

    @Modifying
    @Transactional
    fun deleteByHotel(hotel: MyHotel)
}