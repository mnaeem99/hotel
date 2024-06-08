package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.GoogleHotel
import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.Suggestion
import com.my.hotel.server.data.model.User
import com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface SuggestionRepository : JpaRepository<Suggestion, Long>, JpaSpecificationExecutor<Suggestion>{
    @Modifying
    @Transactional
    fun deleteByUser(user: User)

    @Query("select NEW com.my.hotel.server.graphql.dto.response.MyHotelTranslationDto( " +
            "r AS hotel, t AS hotelTranslation, ct AS countryTranslation " +
            ") " +
            "from Suggestion f inner join f.myHotel r " +
            "JOIN HotelTranslation t ON t.hotel.id = r.id " +
            "LEFT OUTER JOIN CountryTranslation ct ON ct.country.id = r.country.id " +
            "where t.language = :language AND ( r.country IS NULL OR ct.language = :language ) " +
            "AND f.user.id = :userId " +
            "AND ( f.createdAt BETWEEN :startOfDay AND :endOfDay ) " +
            "GROUP BY r.id, t.id, ct.id "
    )
    fun getTodaySuggestion(userId: Long?, language: String, startOfDay: LocalDateTime?, endOfDay: LocalDateTime?) : List<MyHotelTranslationDto>

    @Query("select r from Suggestion f inner join f.googleHotel r " +
            "where f.user.id = :userId " +
            "AND ( f.createdAt BETWEEN :startOfDay AND :endOfDay ) " +
            "GROUP BY r.placeId "
    )
    fun getTodayGoogleSuggestion(userId: Long?, startOfDay: LocalDateTime?, endOfDay: LocalDateTime?) : List<GoogleHotel>

    fun findByUserAndmyHotel(user: User, hotel: MyHotel?) : Suggestion?
    fun findByUserAndGoogleHotel(user: User, hotel: GoogleHotel?) : Suggestion?
    @Query("select r.id from Suggestion f inner join f.myHotel r " +
            "where f.user.id = :userId " +
            "AND f.createdAt <= :lastDayDate AND  f.createdAt > :lastWeekDate " +
            "GROUP BY r.id "
    )
    fun findUserSuggestion(userId: Long?, lastDayDate: LocalDateTime, lastWeekDate: LocalDateTime) : List<Long>?
    @Query("select r.placeId from Suggestion f inner join f.googleHotel r " +
            "where f.user.id = :userId " +
            "AND f.createdAt <= :lastDayDate AND  f.createdAt > :lastWeekDate " +
            "GROUP BY r.placeId "
    )
    fun findUserGoogleSuggestion(userId: Long?, lastDayDate: LocalDateTime, lastWeekDate: LocalDateTime) : List<String>?
    @Query("select MAX(s.createdAt) from Suggestion s where s.user.id = :userId")
    fun getLastRequestTime(userId: Long?): LocalDateTime?

    @Modifying
    @Transactional
    fun deleteBymyHotel(myHotel: MyHotel)


}