package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.customFavoriteRepository.FavoriteRepositoryCustom
import com.my.hotel.server.graphql.dto.response.RankingQuality
import com.my.hotel.server.graphql.dto.response.HotelUser
import com.my.hotel.server.graphql.dto.response.UserAddedHotel
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface FavoriteRepository : JpaRepository<Favorites, Long>, JpaSpecificationExecutor<Favorites>, FavoriteRepositoryCustom{
    fun countByUser(user: User) : Int?
    fun findByUserAndHotel(user: User, hotel: MyHotel) : Favorites?
    @Query("SELECT f from Favorites f where f.user.id = :userId AND ( f.hotel.id = :hotelId OR f.hotel.placeId = :placeId )")
    fun findByUserAndHotel(userId: Long, hotelId: Long?, placeId: String?) : Favorites?
    fun findByHotel(hotel: MyHotel) : List<Favorites>?

    @Query(value = "select " +
            "NEW com.my.hotel.server.graphql.dto.response.UserAddedHotel( "+
            "r.id AS id, " +
            "t.name AS name, " +
            "t.address AS address, " +
            "r.hotelPriceLevel.id AS hotelPriceLevel, " +
            "i AS photo, " +
            "( select (count(e) > 0) from Favorites e WHERE e.user.id = :userId AND e.hotel.id = r.id ) AS onFavorite, " +
            "( select (count(e) > 0) from WishList e WHERE e.user.id = :userId AND e.hotel.id = r.id ) AS onWishList, " +
            "f.postTime AS createdAt) " +
            "from Favorites f JOIN MyHotel r ON f.hotel.id = r.id " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN Image i ON r.photo.id = i.id JOIN User u ON f.user.id = u.id " +
            "LEFT OUTER JOIN Country c ON r.country.id = c.id LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = c.id " +
            "WHERE f.user.id = :userId " +
            "AND ( :countryId IS NULL OR r.country.id = :countryId ) " +
            "AND t.language = :language AND ( c IS NULL OR ct.language = :language ) " +
            "AND ( :keyword IS NULL OR " +
            "LOWER(t.name) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(t.address) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(ct.name) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') " +
            ")",
        countQuery = "select count(f) from Favorites f JOIN MyHotel r ON f.hotel.id = r.id " +
                "JOIN HotelTranslation t ON t.hotel.id = r.id " +
                "LEFT OUTER JOIN Image i ON r.photo.id = i.id JOIN User u ON f.user.id = u.id " +
                "LEFT OUTER JOIN Country c ON r.country.id = c.id LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = c.id " +
                "WHERE f.user.id = :userId " +
                "AND ( :countryId IS NULL OR r.country.id = :countryId ) " +
                "AND t.language = :language AND ( c IS NULL OR ct.language = :language ) " +
                "AND ( :keyword IS NULL OR " +
                "LOWER(t.name) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
                "LOWER(t.address) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
                "LOWER(ct.name) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') " +
                ")"
    )
    fun findByUserLocationKeyword(@Param("userId") userId: Long?, @Param("countryId") countryId: Long?, @Param("language") language: String, @Param("keyword") keyword: String?, pageable: Pageable): Page<UserAddedHotel>
    @Modifying
    @Transactional
    fun deleteByUser(user: User)

    @Modifying
    @Transactional
    fun deleteByHotel(hotel: MyHotel)

    @Query("SELECT q.name from Favorites f JOIN f.quality q where f.hotel.country.id = :countryId GROUP BY q.id ORDER BY COUNT(q.id) DESC ")
    fun findPopularSearches(countryId: Long, pageable: Pageable): Page<String>?
    @Query("SELECT q from Favorites f JOIN f.quality q WHERE f.user.id = ?1 GROUP BY q.id ORDER BY COUNT(q.id) DESC ")
    fun findChefQualities(userId: Long?, pageable: Pageable): Page<Quality>?

    @Query(value = "SELECT NEW com.my.hotel.server.graphql.dto.response.HotelUser(f.hotel.id AS hotelId, f.user AS user ) from Favorites f WHERE f.hotel.id IN ?1 ")
    fun findUsersByHotel(hotelIDs: List<Long?>): List<HotelUser>?
    @Query("SELECT q from Favorites f JOIN f.quality fq JOIN Quality q ON fq.id = q.id WHERE f.hotel.id = :hotelId OR f.hotel.placeId = :placeId GROUP BY q.id ORDER BY COUNT(f.id) DESC ")
    fun findhotelQualities(hotelId: Long?, placeId: String?, pageable: Pageable): Page<Quality>?

    @Query(value = "WITH hotel_quality_count AS (  " +
            "              SELECT  " +
            "                f.hotel_id,  " +
            "                fq.quality_id,  " +
            "                COUNT(f.id) AS quality_count  " +
            "              FROM  " +
            "                favorites f  " +
            "                JOIN favorites_quality fq ON fq.favorites_id = f.id " +
            "                JOIN my_hotels g ON g.id = f.hotel_id " +
            "              WHERE g.city_id = :cityId " +
            "              GROUP BY " +
            "                f.hotel_id, fq.quality_id  " +
            "            ), " +
            "            quality_rank AS (  " +
            "              SELECT  " +
            "                rc.hotel_id,  " +
            "                rc.quality_id,  " +
            "                rc.quality_count,  " +
            "                DENSE_RANK() OVER (ORDER BY rc.quality_count DESC) AS hotelQualityRank  " +
            "              FROM  " +
            "                hotel_quality_count rc  " +
            "            )  " +
            "            SELECT  " +
            "              q.name AS name,  " +
            "              q.id AS id,  " +
            "              cqr.hotelQualityRank AS rank  " +
            "            FROM  " +
            "              hotel_quality_count rqc  " +
            "              JOIN quality_rank cqr ON cqr.hotel_id = rqc.hotel_id AND cqr.quality_id = rqc.quality_id  " +
            "              JOIN quality q ON q.id = rqc.quality_id  " +
            "            WHERE  " +
            "               rqc.hotel_id = :hotelId  " +
            "            ORDER BY  " +
            "               cqr.hotelQualityRank ASC LIMIT 2 ",
        nativeQuery = true)
    fun findRankingQuality(hotelId: Long?, cityId: Long?): List<RankingQuality>?
    @Query(value = " SELECT res.rank  " +
            "            FROM ( " +
            "                SELECT g.id,  COUNT(f.id) AS most_count, RANK() OVER (ORDER BY COUNT(f.id) DESC) AS rank  " +
            "                   FROM favorites f " +
            "                   JOIN my_hotels g ON g.id = f.hotel_id   " +
            "                   WHERE g.city_id = :cityId " +
            "                   GROUP BY g.id  " +
            "            ) AS res  " +
            "            where res.id = :hotelId ",
        nativeQuery = true)
    fun findRankingByCity(hotelId: Long?, cityId: Long?): Int?
    @Query("SELECT MAX(f.postTime) FROM Favorites f where f.hotel.locality.id = :localityId ")
    fun findLatesthotelAddedAt(localityId: Long): LocalDateTime?
}