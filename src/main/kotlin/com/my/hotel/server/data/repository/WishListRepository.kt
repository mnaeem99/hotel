package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.model.WishList
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface WishListRepository : JpaRepository<WishList, Long>, JpaSpecificationExecutor<WishList> {
    fun findByUserAndHotel(user: User, hotel: MyHotel) : WishList?
    @Query("SELECT w from WishList w where w.user.id = :userId AND ( w.hotel.id = :hotelId OR w.hotel.placeId = :placeId )")
    fun findByUserAndHotel(userId: Long, hotelId: Long?, placeId: String?) : WishList?
    @Query(value = "select " +
            "NEW com.my.hotel.server.graphql.dto.response.UserAddedHotel( "+
            "r.id AS id, " +
            "t.name AS name, " +
            "t.address AS address, " +
            "r.hotelPriceLevel.id AS hotelPriceLevel, " +
            "i AS photo, " +
            "( select (count(e) > 0) from Favorites e WHERE e.user.id = :userId AND e.hotel.id = r.id ) AS onFavorite, " +
            "( select (count(e) > 0) from WishList e WHERE e.user.id = :userId AND e.hotel.id = r.id ) AS onWishList, " +
            "f.createdAt AS createdAt) " +
            "from WishList f JOIN MyHotel r ON f.hotel.id = r.id " +
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
        countQuery = "select count(f) from WishList f JOIN MyHotel r ON f.hotel.id = r.id " +
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
    fun findByUserLocationKeyword(@Param("userId") userId: Long?, @Param("countryId") countryId: Long?, @Param("language") language: String, @Param("keyword") keyword: String?, pageable: Pageable): Page<com.my.hotel.server.graphql.dto.response.UserAddedHotel>

    @Modifying
    @Transactional
    fun deleteByUser(user: User)

    @Modifying
    @Transactional
    fun deleteByHotel(hotel: MyHotel)
}