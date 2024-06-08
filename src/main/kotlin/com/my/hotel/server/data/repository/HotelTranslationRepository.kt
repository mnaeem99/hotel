package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.HotelTranslation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface HotelTranslationRepository : JpaRepository<HotelTranslation, Long>, JpaSpecificationExecutor<HotelTranslation> {
    fun findByHotel(hotel: MyHotel) : List<HotelTranslation>?
    @Query("SELECT r FROM HotelTranslation r WHERE r.hotel.id = ?1 and r.language = ?2")
    fun findByHotel(hotelId: Long, language: String) : HotelTranslation?
    @Modifying
    @Transactional
    fun deleteByHotel(hotel: MyHotel)
    @Query("SELECT r.language FROM HotelTranslation r WHERE r.hotel.id = ?1")
    fun findLanguages(hotelId: Long): List<String>?

    @Query(
        value = "SELECT t.* " +
                "FROM favorites f " +
                "JOIN my_hotels r ON f.hotel_id = r.id " +
                "JOIN my_hotels_translations t ON t.hotel_id = r.id " +
                "WHERE " +
                "    r.locality_id = :localityId " +
                "    AND t.language = :language " +
                "    AND f.post_time IN ( " +
                "        SELECT ff.post_time " +
                "        FROM favorites ff " +
                "        WHERE ff.hotel_id = r.id " +
                "        ORDER BY ff.post_time DESC " +
                "        LIMIT 100 " +
                "    ) " +
                "GROUP BY " +
                "   r.id, t.id " +
                "ORDER BY " +
                "    COUNT(f.user_id) DESC ",
        countQuery = "SELECT COUNT(t.id) " +
                "FROM favorites f " +
                "JOIN my_hotels r ON f.hotel_id = r.id " +
                "JOIN my_hotels_translations t ON t.hotel_id = r.id " +
                "WHERE " +
                "    r.locality_id = :localityId " +
                "    AND t.language = :language " +
                "GROUP BY r.id, t.id ",
        nativeQuery = true)
    fun findByLocalities(localityId: Long,language: String, pageable: Pageable): Page<HotelTranslation>?


}