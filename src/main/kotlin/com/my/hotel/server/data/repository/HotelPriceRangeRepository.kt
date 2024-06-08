package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.HotelPriceRange
import com.my.hotel.server.data.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface HotelPriceRangeRepository : JpaRepository<HotelPriceRange, Long>, JpaSpecificationExecutor<HotelPriceRange> {
    fun findByUserAndHotel(user: User, hotel: MyHotel) : HotelPriceRange?
    fun findByHotel(hotel: MyHotel) : List<HotelPriceRange>?
    @Query("SELECT f from HotelPriceRange f where f.hotel.id = :hotelId OR f.hotel.placeId = :placeId ")
    fun findByHotel(hotelId: Long?, placeId: String?) : List<HotelPriceRange>?
    @Query(value = "select avg(r.range) from HotelPriceRange r where r.hotel.id = :hotelId OR r.hotel.placeId = :placeId ")
    fun findAveragePriceRating(hotelId: Long?, placeId: String?) : Float?
    @Modifying
    @Transactional
    fun deleteByUser(user: User)

    @Modifying
    @Transactional
    fun deleteByHotel(hotel: MyHotel)
}