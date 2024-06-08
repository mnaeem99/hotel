package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.City
import com.my.hotel.server.graphql.dto.response.ICityDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface CityRepository : JpaRepository<City, Long>,
    JpaSpecificationExecutor<City>
{
    @Query( "select c from Country country JOIN country.city c JOIN CityTranslation ct on c.id = ct.city.id where ct.name = :name and country.id = :countryId and ct.language = :language ")
    fun findByName(name: String, countryId: Long, language: String) : City?

    @Query("select t.name AS name, " +
            "i AS picture, " +
            "ct.id AS countryId, " +
            "c.placeId AS placeId, " +
            "c.id AS id from Country ct INNER JOIN ct.city c JOIN CityTranslation t ON t.city.id = c.id " +
            "LEFT OUTER JOIN Image i ON c.picture.id = i.id WHERE ct.id = ?1 and t.language = ?2")
    fun findByCountry(countryId: Long, language: String, pageable: Pageable) : Page<com.my.hotel.server.graphql.dto.response.ICityDto>?
    @Query("select c from City c JOIN c.locality l where l.id = :localityId ")
    fun getCityByLocalityId(localityId: Long): City?

    @Query("select t.name AS name, " +
            "i AS picture, " +
            "ct.id AS countryId, " +
            "c.placeId AS placeId, " +
            "c.id AS id from Country ct INNER JOIN ct.city c JOIN CityTranslation t ON t.city.id = c.id " +
            "LEFT OUTER JOIN Image i ON c.picture.id = i.id WHERE c.id = ?1 AND t.language = ?2")
    fun findById(id: Long, language: String) : ICityDto?

    @Query("select ct.name AS name, c.id AS id " +
            "from Favorites f " +
            "JOIN City c ON c.id = f.hotel.city.id JOIN CityTranslation ct ON c.id = ct.city.id " +
            "WHERE f.hotel.country.id = ?1 and ct.language = ?2 and f.hotel.locality IS NOT NULL " +
            "GROUP By c.id, ct.id",
    )
    fun findMyCities(countryId: Long, language: String, pageable: Pageable) : Page<ICityDto>?

    @Query("select ct.name AS name, " +
                "c.id AS id, " +
                "ct1.name AS countryName, " +
                "ctr.code AS countryCode, " +
                "i AS countryPicture, " +
                "i2 AS countryFlag, " +
                "ctr.id AS countryId " +
                "from Favorites f " +
                "JOIN City c ON c = f.hotel.city JOIN CityTranslation ct ON c.id = ct.city.id " +
                "JOIN Country ctr ON f.hotel.country = ctr JOIN CountryTranslation ct1 ON ctr.id = ct1.country.id " +
                "JOIN LocalityTranslation lt ON lt.locality = f.hotel.locality " +
                "LEFT OUTER JOIN Image i ON ctr.picture.id = i.id " +
                "LEFT OUTER JOIN Image i2 ON ctr.flag.id = i2.id " +
                "WHERE ct.language = ?1 and ct1.language = ?1 and lt.language = ?1 and f.hotel.locality IS NOT NULL " +
                "GROUP By c.id, ct.id, ct1.id, ctr.id, i.id, i2.id " +
                "ORDER By COUNT(c.id) DESC")
    fun findGlobalCities(language: String, pageable: Pageable) : Page<com.my.hotel.server.graphql.dto.response.IGlobalCityDto>?
    fun findByPlaceId(placeId: String?): City?
}