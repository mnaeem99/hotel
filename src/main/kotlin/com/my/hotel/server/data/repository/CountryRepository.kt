package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.Country
import com.my.hotel.server.graphql.dto.response.ICountryDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository


@Repository
interface CountryRepository : JpaRepository<Country, Long>,
    JpaSpecificationExecutor<Country>
{
    @Query("select c from Country c WHERE UPPER(c.code) = UPPER(?1) ")
    fun findByCode(code: String) : Country?

    @Query("select c from Country c JOIN CountryTranslation t ON t.country.id = c.id WHERE t.name = ?1 AND t.language = ?2")
    fun findByName(name: String, language: String) : Country?
    @Query("select c from Country c JOIN CountryTranslation t ON t.country.id = c.id WHERE t.name = ?1 ")
    fun findByName(name: String) : List<Country>?
    @Query("select t.name AS name, " +
            "c.code AS code, " +
            "i AS picture, " +
            "i2 AS flag, " +
            "c.id AS id from Favorites f JOIN MyHotel r ON r.id = f.hotel.id JOIN Country c ON r.country.id = c.id JOIN CountryTranslation t ON t.country.id = c.id LEFT OUTER JOIN Image i ON c.picture.id = i.id LEFT OUTER JOIN Image i2 ON c.flag.id = i2.id WHERE t.language = ?1 GROUP By c.id, t.id, i.id, i2.id ORDER BY COUNT(r.id) DESC")
    fun findMostAddedCountries(language: String, toPageable: Pageable): Page<ICountryDto>

    @Query("SELECT COALESCE(t.name, MAX(tFallback.name)) AS name, " +
            "c.code AS code, " +
            "i AS picture, " +
            "i2 AS flag, " +
            "c.id AS id from " +
            "Favorites f JOIN Country c ON f.hotel.country.id = c.id " +
            "LEFT JOIN CountryTranslation t ON t.country.id = c.id AND t.language = ?1 " +
            "LEFT JOIN Image i ON c.picture.id = i.id " +
            "LEFT JOIN Image i2 ON c.flag.id = i2.id " +
            "LEFT JOIN CountryTranslation tFallback ON tFallback.country.id = c.id " +
            "WHERE f.hotel.city IS NOT NULL AND f.hotel.locality IS NOT NULL " +
            "GROUP BY c.id, i.id, i2.id, t.name "
    )
    fun findMyCountries(language: String, toPageable: Pageable): Page<ICountryDto>

    @Query("select t.name AS name, " +
            "c.code AS code, " +
            "i AS picture, " +
            "i2 AS flag, " +
            "c.id AS id from Country c JOIN CountryTranslation t ON t.country.id = c.id " +
            "LEFT OUTER JOIN Image i ON c.picture.id = i.id " +
            "LEFT OUTER JOIN Image i2 ON c.flag.id = i2.id " +
            "WHERE t.language = ?1")
    fun findAllCountries(language: String, toPageable: Pageable): Page<ICountryDto>
    @Query("select c.id from Country c JOIN c.city ct where ct.id = :cityId")
    fun findByCity(cityId: Long): Long

    @Query(value = "select t.name AS name, " +
            "c.code AS code, " +
            "i AS picture, " +
            "i2 AS flag, " +
            "c.id AS id from Country c JOIN CountryTranslation t ON t.country.id = c.id " +
            "LEFT OUTER JOIN Image i ON c.picture.id = i.id " +
            "LEFT OUTER JOIN Image i2 ON c.flag.id = i2.id " +
            "WHERE t.language = :language " +
            "AND c = ( SELECT DISTINCT country FROM Country country INNER JOIN country.city city WHERE city.id = :cityId )")
    fun findByCityLanguage(@Param("cityId") cityId: Long, @Param("language") language: String): ICountryDto?

    @Query(value = "select t.name AS name, " +
            "c.code AS code, " +
            "i AS picture, " +
            "i2 AS flag, " +
            "c.id AS id from Country c JOIN CountryTranslation t ON t.country.id = c.id " +
            "LEFT OUTER JOIN Image i ON c.picture.id = i.id " +
            "LEFT OUTER JOIN Image i2 ON c.flag.id = i2.id " +
            "WHERE c.id = :id AND t.language = :language ")
    fun findById(id: Long, language: String) : ICountryDto?

    @Query(value = "select t.name AS name, " +
            "c.code AS code, " +
            "i AS picture, " +
            "i2 AS flag, " +
            "c.id AS id from Country c JOIN CountryTranslation t ON t.country.id = c.id " +
            "LEFT OUTER JOIN Image i ON c.picture.id = i.id " +
            "LEFT OUTER JOIN Image i2 ON c.flag.id = i2.id " +
            "INNER JOIN c.city ct INNER JOIN ct.locality l " +
            "WHERE t.language = :language " +
            "AND l.id  = :localityId ")
    fun findByLocality(localityId: Long, language: String): ICountryDto?

}