package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.*
import com.my.hotel.server.graphql.dto.response.*
import org.locationtech.jts.geom.Geometry
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MyHotelRepository : JpaRepository<MyHotel, Long>, JpaSpecificationExecutor<MyHotel>{

    fun findByCountry(country: Country?): List<MyHotel>
    fun findByCity(city: City?): List<MyHotel>
    fun findByLocality(locality: Locality?): List<MyHotel>
    @Query("select r from MyHotel r WHERE r.country.id = :countryId")
    fun findByCountry(countryId: Long?): List<MyHotel>
    @Query("select r from MyHotel r WHERE r.city.id = :cityId")
    fun findByCity(cityId: Long?): List<MyHotel>
    @Query("select NEW com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto( " +
            "r AS hotel, t AS hotelTranslation, ct AS countryTranslation " +
            ") " +
            "from Favorites f inner join f.hotel r " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = r.country.id " +
            "where within(r.point, :circle) = true AND t.language = :language AND ( r.country IS NULL OR ct.language = :language ) " +
            "AND f.postTime > :date " +
            "GROUP BY r.id, t.id, ct.id " +
            "ORDER BY COUNT(f.user.id) DESC"
    )
    fun findMostAddedHotel(circle: Geometry, language: String, date: LocalDateTime?, pageable: Pageable) : Page<MyHotelTranslationDto>?

    @Query("select NEW com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto( " +
            "r AS hotel, t AS hotelTranslation, ct AS countryTranslation " +
            ") " +
            "from Favorites f inner join f.hotel r " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = r.country.id " +
            "where (:circle IS NULL OR within(r.point, :circle) = true) " +
            "AND r.hotelPriceLevel.id = :priceLevelId " +
            "AND t.language = :language AND ( r.country IS NULL OR ct.language = :language ) " +
            "AND ( " +
                "r.id in ( SELECT r2.id FROM Favorites f2 inner join f2.hotel r2 WHERE r2.id = r.id GROUP BY r2.id HAVING COUNT(f2.user) >= 2 ) " +
                "OR f.user.id in (SELECT u.id FROM User u JOIN u.followers follow WHERE follow.id = :userId) " +
                "OR f.user.id in (SELECT u.id FROM User u JOIN u.following follow JOIN follow.followers followOfFollow WHERE followOfFollow.id = :userId) " +
                "OR ( r.createdAt > :date AND r.placeId is not null ) " +
                "OR ( r.id in ( SELECT r2.id FROM Favorites f2 inner join f2.hotel r2 WHERE r2.id = r.id ) ) " +
            ") " +
            "AND ( :userId IS NULL OR r.id NOT IN ( SELECT r2.id from Favorites ff JOIN MyHotel r2 ON ff.hotel.id = r2.id WHERE ff.user.id = :userId ) ) " +
            "AND r.id NOT IN :suggestionHistory " +
            "GROUP BY r.id, t.id, ct.id "
    )
    fun findSuggestionHotel(userId: Long?, circle: Geometry?, language: String, date: LocalDateTime, priceLevelId: Long?, suggestionHistory: List<Long>?, pageable: Pageable) : Page<MyHotelTranslationDto>
    @Query("select NEW com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto( " +
            "r AS hotel, t AS hotelTranslation, ct AS countryTranslation " +
            ") " +
            "from Favorites f inner join f.hotel r " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = r.country.id " +
            "where (:circle IS NULL OR within(r.point, :circle) = true) " +
            "AND r.hotelPriceLevel.id = :priceLevelId " +
            "AND t.language = :language AND ( r.country IS NULL OR ct.language = :language ) " +
            "AND ( " +
                "r.id in ( SELECT r2.id FROM Favorites f2 inner join f2.hotel r2 WHERE r2.id = r.id GROUP BY r2.id HAVING COUNT(f2.user) >= 2 ) " +
                "OR f.user.id in (SELECT u.id FROM User u JOIN u.followers follow WHERE follow.id = :userId) " +
                "OR f.user.id in (SELECT u.id FROM User u JOIN u.following follow JOIN follow.followers followOfFollow WHERE followOfFollow.id = :userId) " +
                "OR ( r.createdAt > :date AND r.placeId is not null ) " +
                "OR ( r.id in ( SELECT r2.id FROM Favorites f2 inner join f2.hotel r2 WHERE r2.id = r.id ) ) " +
            ") " +
            "AND ( :userId IS NULL OR r.id NOT IN ( SELECT r2.id from Favorites ff JOIN MyHotel r2 ON ff.hotel.id = r2.id WHERE ff.user.id = :userId ) ) " +
            "GROUP BY r.id, t.id, ct.id "
    )
    fun findSuggestionHotel(userId: Long?, circle: Geometry?, language: String, date: LocalDateTime, priceLevelId: Long?, pageable: Pageable) : Page<MyHotelTranslationDto>
    @Query("select NEW com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto( " +
            "r AS hotel, t AS hotelTranslation, ct AS countryTranslation " +
            ") " +
            "from Favorites f inner join f.hotel r " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = r.country.id " +
            "where (:circle IS NULL OR within(r.point, :circle) = true) " +
            "AND r.hotelPriceLevel = null " +
            "AND t.language = :language AND ( r.country IS NULL OR ct.language = :language ) " +
            "AND ( f.user.id in (SELECT u.id FROM User u JOIN u.followers follow WHERE follow.id = :userId) " +
                "OR f.user.id in (SELECT u.id FROM User u JOIN u.following follow JOIN follow.followers followOfFollow WHERE followOfFollow.id = :userId) " +
                "OR ( r.createdAt > :date AND r.placeId is not null ) " +
                "OR ( r.id in ( SELECT r2.id FROM Favorites f2 inner join f2.hotel r2 WHERE r2.id = r.id GROUP BY r2.id HAVING COUNT(f2.user) >= 2 ) ) " +
                "OR ( r.id in ( SELECT r2.id FROM Favorites f2 inner join f2.hotel r2 WHERE r2.id = r.id ) ) " +
            ") " +
            "AND ( :userId IS NULL OR r.id NOT IN ( SELECT r2.id from Favorites ff JOIN MyHotel r2 ON ff.hotel.id = r2.id WHERE ff.user.id = :userId ) ) " +
            "AND r.id NOT IN :suggestionHistory " +
            "GROUP BY r.id, t.id, ct.id "
    )
    fun findSuggestionhotelForNoPriceLevel(userId: Long?, circle: Geometry?, language: String, date: LocalDateTime, suggestionHistory: List<Long>?, pageable: Pageable) : Page<MyHotelTranslationDto>
    @Query("select NEW com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto( " +
            "r AS hotel, t AS hotelTranslation, ct AS countryTranslation " +
            ") " +
            "from Favorites f inner join f.hotel r " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = r.country.id " +
            "where (:circle IS NULL OR within(r.point, :circle) = true) " +
            "AND r.hotelPriceLevel IS NULL " +
            "AND t.language = :language AND ( r.country IS NULL OR ct.language = :language ) " +
            "AND ( f.user.id in (SELECT u.id FROM User u JOIN u.followers follow WHERE follow.id = :userId) " +
                "OR f.user.id in (SELECT u.id FROM User u JOIN u.following follow JOIN follow.followers followOfFollow WHERE followOfFollow.id = :userId) " +
                "OR ( r.createdAt > :date AND r.placeId is not null ) " +
                "OR ( r.id in ( SELECT r2.id FROM Favorites f2 inner join f2.hotel r2 WHERE r2.id = r.id GROUP BY r2.id HAVING COUNT(f2.user) >= 2 ) ) " +
                "OR ( r.id in ( SELECT r2.id FROM Favorites f2 inner join f2.hotel r2 WHERE r2.id = r.id ) ) " +
            ") " +
            "AND ( :userId IS NULL OR r.id NOT IN ( SELECT r2.id from Favorites ff JOIN MyHotel r2 ON ff.hotel.id = r2.id WHERE ff.user.id = :userId ) ) " +
            "GROUP BY r.id, t.id, ct.id "
    )
    fun findSuggestionhotelForNoPriceLevel(userId: Long?, circle: Geometry?, language: String, date: LocalDateTime, pageable: Pageable) : Page<MyHotelTranslationDto>

    @Query("select NEW com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto( " +
            "r AS hotel, t AS hotelTranslation, ct AS countryTranslation " +
            ") " +
            "from Favorites f inner join f.hotel r " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = r.country.id " +
            "where ( :countryId IS NULL OR r.country.id = :countryId ) AND t.language = :language AND ct.language = :language " +
            "GROUP BY r.id, t.id, ct.id ORDER BY COUNT(f.user.id) DESC"
    )
    fun findMostAddedhotelByCountry(countryId: Long?, language: String, pageable: Pageable) : Page<MyHotelTranslationDto>?

    @Query("select NEW com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto( " +
            "r AS hotel, t AS hotelTranslation, ct AS countryTranslation " +
            ") " +
            "from Favorites f inner join f.hotel r " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = r.country.id " +
            "where r.city.id = :cityId " +
            "AND t.language = :language AND ct.language = :language " +
            "GROUP BY r.id, t.id, ct.id ORDER BY COUNT(f.user.id) DESC"
    )
    fun findMostAddedhotelByCity(cityId: Long, language: String, pageable: Pageable) : Page<MyHotelTranslationDto>?

    @Query("select NEW com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto( " +
            "r AS hotel, t AS hotelTranslation, ct AS countryTranslation " +
            ") " +
            "from Favorites f inner join f.quality fq inner join f.hotel r " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = r.country.id " +
            "WHERE r.id <> :hotelId " +
            "AND within(r.point, :circle) = true " +
            "AND fq.id in (select q.id from Favorites f inner join f.quality q where f.hotel.id = :hotelId) " +
            "AND t.language = :language AND ( r.country IS NULL OR ct.language = :language ) " +
            "GROUP BY r.id, t.id, ct.id "
    )
    fun findSimilarhotels(hotelId: Long, circle: Geometry, language: String, pageable: Pageable) : Page<MyHotelTranslationDto>?

    fun findByPlaceId(placeId: String?) : MyHotel?

    @Query("select NEW com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto( " +
            "r AS hotel, t AS hotelTranslation, ct AS countryTranslation " +
            ") " +
            "from Favorites f join f.hotel r " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = r.country.id " +
            "WHERE within(r.point, :circle) = true AND f.user in (SELECT u FROM User u JOIN u.followers follow WHERE follow.id = :userId) " +
            "AND t.language = :language AND ( r.country IS NULL OR ct.language = :language ) " +
            "AND f.postTime > :date " +
            "GROUP BY r.id, t.id, ct.id "
    )
    fun findByFollowing(circle: Geometry, userId: Long?, language: String, date: LocalDateTime?, pageable: Pageable) : Page<MyHotelTranslationDto>?

    @Query(value = "SELECT c.id AS countryId, " +
            "count(r.id) AS noOfhotel, " +
            "ct.name AS countryName, " +
            "MAX(img.imageUrl) AS countryImageUrl FROM " +
            "Favorites w JOIN MyHotel r ON w.hotel.id = r.id " +
            "LEFT OUTER JOIN Country c ON r.country.id = c.id LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = c.id LEFT OUTER JOIN Image img ON c.picture.id = img.id " +
            "JOIN User u ON w.user.id = u.id WHERE u.id = ?1 AND ( r.country IS NULL OR ct.language = ?2 )" +
            "GROUP BY c.id, ct.id")
    fun findFavoriteHotelLocation(userId: Long?, language: String, toPageable: Pageable?): Page<PlaceHotelDto>

    @Query(value = "SELECT c.id AS countryId, " +
        "count(r.id) AS noOfhotel, " +
        "ct.name AS countryName, " +
        "MAX(img.imageUrl) AS countryImageUrl FROM " +
        "WishList w JOIN MyHotel r ON w.hotel.id = r.id " +
        "LEFT OUTER JOIN Country c ON r.country.id = c.id LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = c.id LEFT OUTER JOIN Image img ON c.picture.id = img.id " +
        "JOIN User u ON w.user.id = u.id WHERE u.id = ?1 AND ( r.country IS NULL OR ct.language = ?2 ) " +
        "GROUP BY c.id, ct.id")
    fun findWishlisthotelLocation(userId: Long?, language: String, toPageable: Pageable?): Page<PlaceHotelDto>

    @Query("select NEW com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto( " +
            "r AS hotel, t AS hotelTranslation, ct AS countryTranslation " +
            ") " +
            "from Favorites f inner join f.hotel r " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = r.country.id " +
            "where r.locality.id = :localityId AND t.language = :language " +
            "AND ( r.country IS NULL OR ct.language = :language ) " +
            "GROUP BY r.id, t.id, ct.id ORDER BY COUNT(f.user.id) DESC"
    )
    fun findByLocalities(localityId: Long,language: String, toPageable: Pageable): Page<MyHotelTranslationDto>?

    @Query("select NEW com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto( " +
            "r AS hotel, t AS hotelTranslation, ct AS countryTranslation " +
            ") " +
            "from MyHotel r " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = r.country.id " +
            "WHERE t.language = :language AND ( r.country IS NULL OR ct.language = :language ) " +
            "AND ( :countryId IS NULL OR r.country.id = :countryId ) " +
            "AND ( :cityId IS NULL OR r.city.id = :cityId ) " +
            "AND ( :localityId IS NULL OR r.locality.id = :localityId ) " +
            "AND ( :priceLevelId IS NULL OR r.hotelPriceLevel.id = :priceLevelId ) " +
            "AND ( :keyword IS NULL OR " +
            "LOWER(t.name) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(t.address) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') " +
            ")"
    )

    fun findAll(language: String,countryId: Long?,cityId: Long?,localityId: Long?,priceLevelId: Long?, keyword: String?,pageable: Pageable): Page<MyHotelTranslationDto>?

    @Query("select NEW com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto( " +
            "r AS hotel, t AS hotelTranslation, ct AS countryTranslation " +
            ") " +
            "from MyHotel r " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = r.country.id " +
            "WHERE r.id = :id AND t.language = :language AND ( r.country IS NULL OR ct.language = :language ) ")
    fun findById(id: Long, language: String) : MyHotelTranslationDto?
    @Query("select NEW com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto( " +
            "r AS hotel, t AS hotelTranslation, ct AS countryTranslation " +
            ") " +
            "from MyHotel r " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = r.country.id " +
            "WHERE r.placeId IN :placeIds " +
            "AND t.language = :language " +
            "AND ( r.country IS NULL OR ct.language = :language ) "
    )
    fun findAllByPlaceId(placeIds: List<String>, language: String): List<MyHotelTranslationDto>?

    @Query("SELECT " +
            "g.id AS hotelId, " +
            "t.name AS hotelName, " +
            "l.id AS localityId, " +
            "ct.name AS cityName, " +
            "c.name AS countryName, " +
            "MAX(f.postTime) AS lastModified " +
            "FROM Locality l JOIN MyHotel g ON g.locality.id = l.id " +
            "JOIN Favorites f ON f.hotel.id = g.id " +
            "JOIN HotelTranslation t ON t.hotel.id = g.id " +
            "JOIN LocalityTranslation lt ON lt.locality.id = l.id " +
            "JOIN CityTranslation ct ON ct.city.id = g.city.id " +
            "JOIN CountryTranslation c ON c.country.id = g.country.id " +
            "WHERE g.country.code = :countryCode AND t.language = :language AND lt.language = :language AND ct.language = :language AND c.language = :language " +
            "GROUP BY g.id, t.name, l.id, ct.name, c.name " +
            "ORDER BY MAX(f.postTime) DESC"
    )
    fun findSitemaphotels(language: String, countryCode: String, pageable: Pageable) : Page<HotelSitemap>?
    @Query("SELECT " +
            "l.id AS localityId, " +
            "ct.name AS cityName, " +
            "c.name AS countryName, " +
            "MAX(f.postTime) AS lastModified " +
            "FROM Locality l JOIN MyHotel g ON g.locality.id = l.id " +
            "JOIN Favorites f ON f.hotel.id = g.id " +
            "JOIN LocalityTranslation lt ON lt.locality.id = l.id " +
            "JOIN CityTranslation ct ON ct.city.id = g.city.id " +
            "JOIN CountryTranslation c ON c.country.id = g.country.id " +
            "WHERE g.country.code = :countryCode AND lt.language = :language AND ct.language = :language AND c.language = :language " +
            "GROUP BY l.id, ct.name, c.name " +
            "ORDER BY MAX(f.postTime) DESC"
    )
    fun findSitemapLocalities(language: String, countryCode: String, pageable: Pageable) : Page<LocalitySitemap>?
    @Query("SELECT " +
            "ct.name AS cityName, " +
            "c.name AS countryName, " +
            "MAX(f.postTime) AS lastModified " +
            "FROM MyHotel g JOIN Favorites f ON f.hotel.id = g.id " +
            "JOIN CityTranslation ct ON ct.city.id = g.city.id " +
            "JOIN CountryTranslation c ON c.country.id = g.country.id " +
            "WHERE g.country.code = :countryCode AND ct.language = :language AND c.language = :language " +
            "GROUP BY ct.name, c.name " +
            "ORDER BY MAX(f.postTime) DESC"
    )
    fun findSitemapCities(language: String, countryCode: String, pageable: Pageable): Page<CitySitemap>?
    @Query("SELECT " +
            "u.id AS id, " +
            "u.nickName AS nickName " +
            "FROM User u JOIN Favorites f ON f.user.id = u.id " +
            "LEFT JOIN MyHotel g ON g.id = f.hotel.id " +
            "LEFT JOIN HotelTranslation t ON t.hotel.id = g.id " +
            "WHERE ( u.country.code = :countryCode OR g.country.code = :countryCode )" +
            "AND ( u.language = :language OR t.language = :language ) " +
            "GROUP BY u.id " +
            "ORDER BY MAX(f.postTime) DESC"
    )
    fun findSitemapUsers(language: String, countryCode: String, pageable: Pageable): Page<UserSitemap>?

}
