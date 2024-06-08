package com.my.hotel.server.data.repository.customFavoriteRepository

import org.locationtech.jts.geom.Point
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Service
class FavoriteRepositoryImpl @Autowired constructor(
    @PersistenceContext
    val entityManager: EntityManager
): FavoriteRepositoryCustom {
    override fun findMostAddedHotel(userId: Long?, countryId: Long?, point: Point?, language: String, pageable: Pageable): Page<com.my.hotel.server.graphql.dto.response.NewsFeedDto> {
        val query = entityManager.createQuery("select " +
                "NEW com.my.hotel.server.graphql.dto.response.NewsFeedDto( " +
                    "f as favorites, " +
                    "t AS hotelTranslation, " +
                    "(SELECT count(u2) AS favCount from User u2 JOIN Favorites f2 ON f2.user.id = u2.id WHERE f2.hotel.id = f.hotel.id) AS favCount " +
                ") " +
                "from Favorites f JOIN HotelTranslation t ON t.hotel.id = f.hotel.id " +
                "where t.language = :language " +
                "AND ( :userId is NULL OR f.user NOT IN ( " +
                    "SELECT u2 FROM User u2 JOIN u2.followers follow WHERE follow.id = :userId " +
                    ") " +
                ") AND ( :userId is NULL OR f.user.id <> :userId ) " +
                "AND ( :countryId is NULL OR f.user.country.id = :countryId ) " +
                "ORDER BY ST_Distance(f.user.point, :point) ASC, " +
                "( ( SELECT COUNT(u2) FROM User u2 JOIN u2.following follow WHERE follow.id = f.user.id ) - 0 ) DESC, " +
                "(CURRENT_TIMESTAMP() - f.postTime) * (1 / log( " +
                "( SELECT COUNT(u2) " +
                "FROM User u2 " +
                "JOIN Favorites f2 ON f2.user.id = u2.id " +
                "WHERE f2.hotel.id = f.hotel.id " +
                ") + 2.718281828459)) ASC",
            com.my.hotel.server.graphql.dto.response.NewsFeedDto::class.java
        )
            .setParameter("language", language)
            .setParameter("userId", userId)
            .setParameter("countryId", countryId)
            .setParameter("point", point)
            .setMaxResults(pageable.pageSize)
            .setFirstResult(pageable.pageNumber * pageable.pageSize)
        val results = query.resultList
        val queryTotal = entityManager.createQuery("select count(f) " +
                "from Favorites f JOIN HotelTranslation t ON t.hotel.id = f.hotel.id " +
                "where t.language = :language " +
                "AND ( :userId is NULL OR f.user NOT IN ( " +
                    "SELECT u2 FROM User u2 JOIN u2.followers follow WHERE follow.id = :userId " +
                    ") " +
                ") AND ( :userId is NULL OR f.user.id <> :userId ) " +
                "AND ( :countryId is NULL OR f.user.country.id = :countryId ) "
        )
            .setParameter("language", language)
            .setParameter("userId", userId)
            .setParameter("countryId", countryId)
        val countResult = queryTotal.singleResult as Long
        return PageImpl(results,pageable, countResult)
    }
    override fun findFriendsAddedHotel(userId: Long, language: String, pageable: Pageable): Page<com.my.hotel.server.graphql.dto.response.NewsFeedDto> {
        val query = entityManager.createQuery("select " +
                "NEW com.my.hotel.server.graphql.dto.response.NewsFeedDto( " +
                    "f as favorites, " +
                    "t AS hotelTranslation, " +
                    "(SELECT count(u2) AS favCount from User u2 JOIN Favorites f2 ON f2.user.id = u2.id WHERE f2.hotel.id = f.hotel.id) AS favCount " +
                ") " +
                "from Favorites f JOIN HotelTranslation t ON t.hotel.id = f.hotel.id " +
            "where f.user in ( " +
                "SELECT u2 FROM User u2 JOIN u2.followers follow WHERE follow.id = :userId " +
            ") " +
            "AND t.language = :language " +
            "ORDER BY (CURRENT_TIMESTAMP() - f.postTime) * (1 / log( " +
            "( SELECT COUNT(u2) " +
            "FROM User u2 " +
            "JOIN Favorites f2 ON f2.user.id = u2.id " +
            "WHERE f2.hotel.id = f.hotel.id " +
            ") + 2.718281828459)) ASC",
            com.my.hotel.server.graphql.dto.response.NewsFeedDto::class.java
        )
            .setParameter("language", language)
            .setParameter("userId", userId)
            .setMaxResults(pageable.pageSize)
            .setFirstResult(pageable.pageNumber * pageable.pageSize)
        val results = query.resultList
        val queryTotal = entityManager.createQuery("select count(f) " +
                "from Favorites f JOIN HotelTranslation t ON t.hotel.id = f.hotel.id " +
                "where f.user in ( " +
                    "SELECT u2 FROM User u2 JOIN u2.followers follow WHERE follow.id = :userId " +
                ") " +
                "AND t.language = :language "
        )
            .setParameter("language", language)
            .setParameter("userId", userId)
        val countResult = queryTotal.singleResult as Long
        return PageImpl(results,pageable, countResult)
    }
}