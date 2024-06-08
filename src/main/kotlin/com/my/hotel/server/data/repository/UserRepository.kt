package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.Country
import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.model.UserAuthentication
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Point
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
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Query(value = "SELECT u FROM User u INNER JOIN u.auths auth WHERE auth.type = 'EMAIL' AND auth.email = ?1")
    fun findByEmail(email: String) : User?

    @Query(value = "SELECT u FROM User u INNER JOIN u.auths auth WHERE auth.type = 'PHONE' AND auth.phone = ?1")
    fun findByPhone(phone: String) : User?

    @Query(value = "SELECT auth FROM User u INNER JOIN u.auths auth WHERE auth.phone like CONCAT('%', SUBSTRING(?1, 2)) OR auth.phone like CONCAT('%', SUBSTRING(?1, 3)) OR auth.phone like CONCAT('%', SUBSTRING(?1, 4)) OR auth.phone like CONCAT('%', SUBSTRING(?1, 5))")
    fun searchByPhone(phone: String) : List<UserAuthentication>?

    @Query(value = "SELECT u FROM User u INNER JOIN u.auths auth WHERE auth.type = 'GOOGLE' AND auth.googleId = ?1")
    fun findByGoogleId(googleId: String) : User?

    @Query(value = "SELECT u FROM User u INNER JOIN u.auths auth WHERE auth.type = 'FACEBOOK' AND auth.facebookId = ?1")
    fun findByFacebookId(facebookId: String) : User?

    @Query(value = "SELECT u FROM User u INNER JOIN u.auths auth WHERE auth.type = 'APPLE' AND auth.appleId = ?1")
    fun findByAppleId(appleId: String): User?

    @Query(value = "SELECT u FROM User u JOIN u.following follow WHERE follow.id = :userId " +
            "AND ( :keyword IS NULL OR " +
            "LOWER(u.nickName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.firstName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.lastName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.bio) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') ) ")
    fun findUserFollowers(userId: Long, keyword: String?, toPageable: Pageable): Page<User>

    @Query(value = "SELECT u FROM User u JOIN u.followers follow WHERE follow.id = :userId " +
            "AND ( :keyword IS NULL OR " +
            "LOWER(u.nickName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.firstName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.lastName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.bio) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') ) ")
    fun findUserFollowing(userId: Long, keyword: String?, toPageable: Pageable): Page<User>

    @Query(value = "SELECT u FROM User u JOIN u.pendingFollowing requests ON requests.follower.id = u.id WHERE requests.following.id = ?1")
    fun findUserFollowingRequest(userId: Long, toPageable: Pageable): Page<User>?

    @Query("SELECT * from users where id in (SELECT following_id from follow WHERE follower_id = ?1 and following_id = ?2)", nativeQuery = true)
    fun isFollowing(followerId: Long, followingId: Long): User?

    @Query(value = "SELECT u FROM User u JOIN u.blocks blocker WHERE u.id = :userId and blocker.id = :blockerId ")
    fun isBlock(userId: Long, blockerId: Long): User?

    @Query(value = "SELECT COUNT(u) FROM User u JOIN u.following follow WHERE follow.id = ?1")
    fun countFollowers(userId: Long): Int

    @Query(value = "SELECT COUNT(u) FROM User u JOIN u.followers follow WHERE follow.id = ?1")
    fun countFollowing(userId: Long): Int

    @Query(value = "SELECT u from User u JOIN Favorites f ON f.user.id = u.id WHERE f.hotel.id = :hotelId OR f.hotel.placeId = :placeId")
    fun findByFavoriteHotel(hotelId: Long?, placeId: String?, toPageable: Pageable?) : Page<User>

    @Query(value = "SELECT u from User u JOIN WishList w ON w.user.id = u.id WHERE w.hotel.id = ?1")
    fun findByWishlistHotel(hotelId: Long, toPageable: Pageable?) : Page<User>

    @Query(value = "SELECT u from User u JOIN Favorites f ON f.user.id = u.id JOIN f.quality fq JOIN Quality q ON fq.id = q.id WHERE ( f.hotel.id = :hotelId OR f.hotel.placeId = :placeId ) AND q.id = :qualityId")
    fun findByHotelQuality(hotelId: Long?, placeId: String?, qualityId: Long, toPageable: Pageable?) : Page<User>

    @Query(value = "SELECT u FROM User u JOIN u.followers follow " +
            "WHERE ( :userId IS NULL OR u.id <> :userId ) " +
            "AND ( :keyword IS NULL OR " +
            "LOWER(u.nickName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.firstName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.lastName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.bio) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') ) " +
            "AND u.firstName IS NOT NULL AND u.lastName IS NOT NULL " +
            "GROUP BY u.id ORDER BY count(u.id) DESC, ST_Distance(u.point, :point) ASC"
    )
    fun findPopularUser(userId: Long?, point: Point?, keyword: String?, pageable: Pageable?) : Page<User>

    @Query(value = "SELECT u FROM User u INNER JOIN u.auths auth " +
            "WHERE auth.type = 'PHONE' AND auth.phone IN :phone AND u.id <> :userId " +
            "AND ( :keyword IS NULL OR " +
            "LOWER(u.nickName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.firstName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.lastName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.bio) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') ) " +
            "AND u.firstName IS NOT NULL AND u.lastName IS NOT NULL"
    )
    fun findByPhones(phone: List<String>, userId: Long?, keyword: String?, toPageable: Pageable?) : Page<User>

    @Query(value = "SELECT u FROM User u INNER JOIN u.auths auth " +
            "WHERE auth.type = 'FACEBOOK' AND auth.facebookId IN :facebookId AND u.id <> :userId " +
            "AND ( :keyword IS NULL OR " +
            "LOWER(u.nickName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.firstName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.lastName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.bio) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') ) " +
            "AND u.firstName IS NOT NULL AND u.lastName IS NOT NULL"
    )
    fun findByFacebook(facebookId: List<String>?, userId: Long?, keyword: String?, toPageable: Pageable?) : Page<User>
    @Query(value = "SELECT u FROM User u INNER JOIN u.auths auth WHERE auth.type = 'FACEBOOK' AND auth.facebookId IN :facebookId")
    fun findByFacebook(facebookId: List<String>?) : List<User>
    @Query("SELECT u FROM User u " +
            "  WHERE " +
            "  (:userId IS NULL OR u.id <> :userId) " +
            "  AND ( " +
            "    :userId IS NULL  " +
            "    OR ( " +
            "      u.id NOT IN ( " +
            "        SELECT u2.id FROM User u2 JOIN u2.followers follow2 WHERE follow2.id = :userId " +
            "      ) " +
            "    ) " +
            "  ) " +
            "  AND ( " +
            "    (u.id, 1) IN ( " +
            "      SELECT u.id, 1 as ranking " +
            "      FROM User u " +
            "      JOIN u.following follow " +
            "      JOIN follow.followers followOfFollow " +
            "      WHERE followOfFollow.id = :userId " +
            "    ) " +
            "    OR " +
            "    u.id IN ( " +
            "      SELECT u.id " +
            "      FROM Favorites f " +
            "      JOIN f.quality fq " +
            "      JOIN Quality q ON fq.id = q.id " +
            "      JOIN User u ON u.id = f.user.id " +
            "      WHERE q.id IN ( " +
            "        SELECT q2.id FROM Favorites f2 " +
            "        JOIN f2.quality fq2 " +
            "        JOIN Quality q2 ON fq2.id = q2.id " +
            "        WHERE f2.user.id = :userId " +
            "      ) " +
            "      AND ( :circle IS NULL OR within(u.point, :circle) = true)  " +
            "    ) " +
            "    OR " +
            "    u.id IN ( " +
            "      SELECT u.id " +
            "      FROM User u " +
            "      JOIN Favorites f ON f.user.id = u.id " +
            "      WHERE :circle IS NULL OR within(u.point, :circle) = true " +
            "      ) " +
            "    ) " +
            "    GROUP BY u.id " +
            "    ORDER BY " +
            "      CASE " +
            "        WHEN (u.id, 1) IN ( " +
            "          SELECT u.id, 1 as rank " +
            "          FROM User u " +
            "          JOIN u.following follow " +
            "          JOIN follow.followers followOfFollow " +
            "          WHERE followOfFollow.id = :userId " +
            "        ) " +
            "        THEN 1 " +
            "        ELSE 2 " +
            "      END ASC, " +
            "      ST_Distance(u.point, :point) ASC "
    )
    fun findSuggestedUsers(userId: Long?, circle: Geometry?, point: Point?, pageable: Pageable?): Page<User>
    @Query(value = "SELECT u from User u Where u.nickName = :nickName AND u.id <> :userId")
    fun findByNickName(nickName: String, userId: Long) : User?
    @Query(value = "SELECT u from User u Where u.nickName = :nickName ")
    fun findByNickName(nickName: String) : User?

    @Query(value = "SELECT u from User u WHERE LOWER(u.nickName) LIKE LOWER(CONCAT(CONCAT('%', ?1), '%'))")
    fun searchByNickName(nickName: String) : List<User>?

    @Query(value = "SELECT u.id AS id, " +
            "u.firstName AS firstName, " +
            "u.lastName AS lastName, " +
            "u.nickName AS nickName, " +
            "u.bio AS bio, " +
            "i AS photo " +
            "from User u JOIN Favorites f ON f.user.id = u.id LEFT OUTER JOIN Image i ON u.photo.id = i.id " +
            "WHERE ( :countryId IS NULL OR u.country.id = :countryId ) and u.isChef = true GROUP BY u.id, i.id ORDER BY count(u.id) DESC")
    fun findByChefs(countryId: Long?, pageable: Pageable): Page<com.my.hotel.server.graphql.dto.response.Chef>?
    @Query("SELECT u from User u JOIN Country c ON c.id = u.country.id " +
            "WHERE ( :keyword IS NULL OR " +
            "LOWER(u.nickName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.firstName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.lastName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.bio) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') ) " +
            "AND ( :countryId IS NULL OR c.id = :countryId ) " +
            "AND ( :language IS NULL OR u.language = :language ) ")
    fun findByLocation(@Param("keyword") keyword: String?, @Param("countryId") countryId: Long?, @Param("language") language: String?, pageable: Pageable) : Page<User>

    @Query(value = "SELECT u from User u JOIN Favorites f ON f.user.id = u.id JOIN WishList w ON w.user.id = u.id " +
            "WHERE ( f.hotel.id = :hotelId OR w.hotel.id = :hotelId ) " +
            "AND ( :keyword IS NULL OR " +
            "LOWER(u.nickName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.firstName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.lastName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.bio) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') ) " +
            "GROUP BY u.id "
    )
    fun searchClients( @Param("hotelId") hotelId: Long, @Param("keyword") keyword: String?, toPageable: Pageable?) : Page<User>?

    @Modifying
    @Transactional
    @Query(value = "delete from block_user where user_id = ?1 or blocker_id = ?1", nativeQuery = true)
    fun deleteBlockUser(user: Long?)

    @Query("select u.firstName AS firstName, u.lastName AS lastName, " +
            "u.nickName AS nickName, u.bio AS bio, " +
            "i AS photo, u.language AS language, " +
            "ct.name AS countryName, c.code AS countryCode, ci AS countryPicture, ci2 AS countryFlag, c.id AS countryId, " +
            "u.isPrivate AS private, u.isChef AS chef, " +
            "u.isBlocked AS blocked, u.userType AS userType, " +
            "u.id AS id from User u LEFT OUTER JOIN Image i ON u.photo.id = i.id " +
            "LEFT OUTER JOIN Country c ON u.country.id = c.id LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = c.id " +
            "LEFT OUTER JOIN Image ci ON c.picture.id = ci.id " +
            "LEFT OUTER JOIN Image ci2 ON c.flag.id = ci2.id " +
            "WHERE ( u.language IS NULL OR u.language = :language ) AND ( c IS NULL OR ct.language = :language ) " +
            "AND ( :countryId IS NULL OR c.id = :countryId ) " +
            "AND ( :userType IS NULL OR u.userType = :userType ) " +
            "AND ( :keyword IS NULL OR " +
            "LOWER(u.nickName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.firstName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.lastName) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') OR " +
            "LOWER(u.bio) LIKE CONCAT('%', LOWER(cast(:keyword AS text)), '%') ) "
    )
    fun findAll(language: String?,countryId: Long?,userType: User.UserType?, keyword: String?, pageable: Pageable): Page<com.my.hotel.server.graphql.dto.response.IUserDto>?
    fun findByCountry(country: Country?): List<User>

    @Query("select u from User u LEFT JOIN u.notificationSetting setting WHERE setting IS NULL OR u.notificationSetting.newsfeedAlert = false")
    fun findByNewsfeedAlert(): List<User>
    @Query(value = "SELECT u FROM User u JOIN u.following follow JOIN Favorites f ON f.user.id = u.id WHERE follow.id = :userId AND f.postTime > :date GROUP BY u.id")
    fun getFriendsWhoAddedHotel(userId: Long, date: LocalDateTime): List<User>

}