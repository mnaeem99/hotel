package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.Locality
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface LocalityRepository : JpaRepository<Locality, Long>,
    JpaSpecificationExecutor<Locality>
{
    @Query("select l from City c JOIN c.locality l JOIN LocalityTranslation lt on l.id = lt.locality.id where lt.name = :name and c.id = :cityId and lt.language = :language")
    fun findByName(name: String, cityId: Long, language: String) : Locality?

    @Query("select t.name AS name, " +
            "i AS picture, " +
            "ct.id AS cityId, " +
            "c.placeId AS placeId, " +
            "c.id AS id from City ct INNER JOIN ct.locality c JOIN LocalityTranslation t ON t.locality.id = c.id LEFT OUTER JOIN Image i ON c.picture.id = i.id WHERE c.id = ?1 AND t.language = ?2")
    fun findById(localityId: Long, language: String) : com.my.hotel.server.graphql.dto.response.ILocalityDto?

    @Query("SELECT " +
            "l.id AS localityId, " +
            "lt.name AS localityName, " +
            "i.imageUrl AS localityImageUrl, " +
            "COUNT(g.id) AS noOfHotel  " +
            "FROM MyHotel g JOIN Locality l ON g.locality.id = l.id " +
            "JOIN LocalityTranslation lt ON lt.locality.id = l.id " +
            "LEFT OUTER JOIN Image i ON l.picture.id = i.id " +
            "WHERE lt.language = :language AND g.city.id = :cityId " +
            "AND g.id IN (SELECT f.hotel.id FROM Favorites f WHERE f.hotel.id = g.id) " +
            "GROUP BY g.locality.id, l.id, lt.id, i.id " +
            "ORDER BY COUNT(g.id) DESC"
    )
    fun findByMostHotelCity(cityId: Long, language: String, pageable: Pageable) : Page<com.my.hotel.server.graphql.dto.response.LocalityHotel>?
    @Query("SELECT " +
            "l.id AS localityId, " +
            "lt.name AS localityName, " +
            "i.imageUrl AS localityImageUrl, " +
            "COUNT(g.id) AS noOfHotel  " +
            "FROM MyHotel g JOIN Locality l ON g.locality.id = l.id " +
            "JOIN LocalityTranslation lt ON lt.locality.id = l.id " +
            "LEFT OUTER JOIN Image i ON l.picture.id = i.id " +
            "WHERE lt.language = :language AND g.country.id = :countryId " +
            "AND g.id IN (SELECT f.hotel.id FROM Favorites f WHERE f.hotel.id = g.id) " +
            "GROUP BY g.locality.id, l.id, lt.id, i.id " +
            "ORDER BY COUNT(g.id) DESC"
    )
    fun findByMostHotelCountry(countryId: Long, language: String, pageable: Pageable) : Page<com.my.hotel.server.graphql.dto.response.LocalityHotel>?
    @Query("SELECT " +
            "l.id AS localityId, " +
            "lt.name AS localityName, " +
            "i.imageUrl AS localityImageUrl, " +
            "COUNT(g.id) AS noOfHotel  " +
            "FROM MyHotel g JOIN Locality l ON g.locality.id = l.id " +
            "JOIN LocalityTranslation lt ON lt.locality.id = l.id " +
            "LEFT OUTER JOIN Image i ON l.picture.id = i.id " +
            "WHERE lt.language = :language " +
            "AND g.id IN (SELECT f.hotel.id FROM Favorites f WHERE f.hotel.id = g.id) " +
            "GROUP BY g.locality.id, l.id, lt.id, i.id " +
            "ORDER BY COUNT(g.id) DESC"
    )
    fun findByMostHotel(language: String, pageable: Pageable) : Page<com.my.hotel.server.graphql.dto.response.LocalityHotel>?
    fun findByPlaceId(placeId: String): Locality?

    @Query("select t.name AS name, " +
            "i AS picture, " +
            "ct.id AS cityId, " +
            "c.placeId AS placeId, " +
            "c.id AS id from City ct INNER JOIN ct.locality c JOIN LocalityTranslation t ON t.locality.id = c.id LEFT OUTER JOIN Image i ON c.picture.id = i.id " +
            "where ct.id = ?1 and t.language = ?2")
    fun findByCity(cityId: Long, language: String, pageable: Pageable): Page<com.my.hotel.server.graphql.dto.response.ILocalityDto>


}