package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.Language
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface LanguageRepository : JpaRepository<Language, String>, JpaSpecificationExecutor<Language>{

    @Query(
        "SELECT lang from Language lang WHERE :countryId IS NULL OR lang.code IN ( " +
            "SELECT t.language FROM Favorites f " +
            "JOIN CountryTranslation t ON t.country = f.hotel.country " +
            "JOIN LocalityTranslation lt ON lt.locality = f.hotel.locality AND lt.language = t.language " +
            "WHERE f.hotel.country.id = :countryId AND f.hotel.city IS NOT NULL AND f.hotel.locality IS NOT NULL " +
        ")"
    )
    fun findAvailableLanguages(countryId: Long?): List<Language>

}