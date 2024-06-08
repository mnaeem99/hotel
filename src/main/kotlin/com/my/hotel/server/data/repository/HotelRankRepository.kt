package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.HotelRank
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional


@Repository
interface HotelRankRepository : JpaRepository<HotelRank, Long>, JpaSpecificationExecutor<HotelRank> {

    @Query("SELECT rr FROM HotelRank rr WHERE rr.hotel.id = :hotelId ORDER BY rr.updatedAt DESC")
    fun findLatestRank(hotelId: Long?, pageable: Pageable): Page<HotelRank>

    @Modifying
    @Transactional
    fun deleteByHotel(hotel: MyHotel)

}