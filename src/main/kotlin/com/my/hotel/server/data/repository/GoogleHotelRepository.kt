package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.GoogleHotel
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Point
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface GoogleHotelRepository : JpaRepository<GoogleHotel, String>, JpaSpecificationExecutor<GoogleHotel>{

    @Query("select r from GoogleHotel r " +
            "WHERE within(r.point, :circle) = true AND ( r.placeId IS NULL OR  r.expiryDate > :currentTime ) " +
            "AND ( :keyword IS NULL OR " +
            "LOWER(r.name) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(r.address) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') " +
            ") ORDER BY ST_Distance(r.point, :point) ASC"
    )
    fun findByNearestLocation(@Param("circle") circle: Geometry, @Param("keyword") keyword: String?, @Param("point") point: Point?, @Param("currentTime") currentTime: LocalDateTime?, toPageable: Pageable?): Page<GoogleHotel>
    @Query("select r from GoogleHotel r " +
            "WHERE within(r.point, :circle) = true " +
            "AND ( :priceLevelId is null OR r.hotelPriceLevel.id = :priceLevelId ) " +
            "AND ( :placeId is null OR r.placeId <> :placeId ) " +
            "AND r.language = :language " +
            "AND r.placeId NOT IN ( SELECT g.placeId from MyHotel g JOIN Favorites f ON f.hotel.id = g.id WHERE f.user.id = :userId ) " +
            "GROUP BY r.placeId "
    )
    fun findSuggestionHotel(circle: Geometry, language: String, userId: Long?, priceLevelId: Long?, placeId: String?, pageable: Pageable) : Page<GoogleHotel>
    @Query("select r from GoogleHotel r " +
            "WHERE within(r.point, :circle) = true " +
            "AND ( :priceLevelId is null OR r.hotelPriceLevel.id = :priceLevelId ) " +
            "AND ( :placeId is null OR r.placeId <> :placeId ) " +
            "AND r.language = :language " +
            "AND r.placeId NOT IN ( SELECT g.placeId from MyHotel g JOIN Favorites f ON f.hotel.id = g.id WHERE f.user.id = :userId ) " +
            "AND r.placeId NOT IN :suggestionHistory " +
            "GROUP BY r.placeId "
    )
    fun findSuggestionHotel(circle: Geometry, language: String, userId: Long?, priceLevelId: Long?, placeId: String?, suggestionHistory: List<String>, pageable: Pageable) : Page<GoogleHotel>
    @Query("select r from GoogleHotel r " +
            "WHERE within(r.point, :circle) = true " +
            "AND r.hotelPriceLevel IS NULL " +
            "AND r.language = :language " +
            "AND r.placeId NOT IN ( SELECT g.placeId from MyHotel g JOIN Favorites f ON f.hotel.id = g.id WHERE f.user.id = :userId ) " +
            "GROUP BY r.placeId "
    )
    fun findSuggestionhotelForNoPriceLevel(circle: Geometry, language: String, userId: Long?, pageable: Pageable) : Page<GoogleHotel>
}