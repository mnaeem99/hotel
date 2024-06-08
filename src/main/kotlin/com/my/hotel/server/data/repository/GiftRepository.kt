package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.Gift
import com.my.hotel.server.data.model.MyHotel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface GiftRepository : JpaRepository<Gift, Long>, JpaSpecificationExecutor<Gift>{
    fun findByHotel(hotel: MyHotel) : List<Gift>?
    @Query("SELECT f from Gift f where f.hotel.id = :hotelId OR f.hotel.placeId = :placeId ")
    fun findByHotel(hotelId: Long?, placeId: String?) : List<Gift>?
    fun findByHotel(hotel: MyHotel, pageable: Pageable) : Page<Gift>?

    @Modifying
    @Transactional
    fun deleteByHotel(hotel: MyHotel)

    @Query("select NEW com.my.hotel.server.graphql.dto.response.HotelGift (" +
            "g AS gift, " +
            "t AS hotelTranslation ) " +
            "from Gift g JOIN HotelTranslation t ON t.hotel.id =  g.hotel.id " +
            "where t.language = :language ")
    fun findByLanguage(language: String, pageable: Pageable): Page<com.my.hotel.server.graphql.dto.response.HotelGift>?

    @Query("select NEW com.my.hotel.server.graphql.dto.response.HotelGift (" +
            "g AS gift, " +
            "t AS hotelTranslation ) " +
            "from Gift g JOIN HotelTranslation t ON t.hotel.id =  g.hotel.id " +
            "where g.hotel.id = :hotelId AND t.language = :language ")
    fun findByhotelLanguage(hotelId: Long, language: String, pageable: Pageable): Page<com.my.hotel.server.graphql.dto.response.HotelGift>?

    @Query("select NEW com.my.hotel.server.graphql.dto.response.HotelGift (" +
            "g AS gift, " +
            "t AS hotelTranslation ) " +
            "from Gift g JOIN HotelTranslation t ON t.hotel.id =  g.hotel.id " +
            "where g.id = :id AND t.language = :language ")
    fun findById(id: Long, language: String): com.my.hotel.server.graphql.dto.response.HotelGift?

}