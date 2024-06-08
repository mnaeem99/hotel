package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.Country
import com.my.hotel.server.data.model.CountryTranslation
import com.my.hotel.server.graphql.dto.response.CountryLanguageDto
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface CountryTranslationRepository : JpaRepository<CountryTranslation, Long>, JpaSpecificationExecutor<CountryTranslation> {

    fun findByCountry(country: Country): List<CountryTranslation>?

    @Query("SELECT r FROM CountryTranslation r WHERE r.country.id = ?1 and r.language = ?2")
    fun findByCountry(countryId: Long?, language: String) : CountryTranslation?
    @Modifying
    @Transactional
    fun deleteByCountry(country: Country)

    @Query("SELECT r.language FROM CountryTranslation r WHERE r.country.id = ?1 ")
    fun findLanguages(countryId: Long?): List<String>?

    @Query(
        "SELECT NEW com.my.hotel.server.graphql.dto.response.CountryLanguageDto(t.language, t.name) FROM Favorites f " +
                "JOIN CountryTranslation t ON t.country = f.hotel.country " +
                "JOIN LocalityTranslation lt ON lt.locality = f.hotel.locality AND lt.language = t.language " +
                "WHERE f.hotel.country.id = :countryId AND f.hotel.city IS NOT NULL AND f.hotel.locality IS NOT NULL " +
                "GROUP BY t.id "
    )
    fun findAvailableTranslation(countryId: Long?): List<CountryLanguageDto>

}