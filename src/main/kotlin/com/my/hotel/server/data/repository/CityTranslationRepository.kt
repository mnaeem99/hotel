package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.City
import com.my.hotel.server.data.model.CityTranslation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface CityTranslationRepository : JpaRepository<CityTranslation, Long>, JpaSpecificationExecutor<CityTranslation> {

    fun findByCity(city: City): List<CityTranslation>?

    @Query("SELECT r FROM CityTranslation r WHERE r.city.id = ?1 and r.language = ?2")
    fun findByCity(cityId: Long, language: String) : CityTranslation?

    @Modifying
    @Transactional
    fun deleteByCity(city: City)

    @Query("SELECT r.language FROM CityTranslation r WHERE r.city.id = ?1 ")
    fun findLanguages(cityId: Long?): List<String>?
}