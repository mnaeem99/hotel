package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.Locality
import com.my.hotel.server.data.model.LocalityTranslation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface LocalityTranslationRepository : JpaRepository<LocalityTranslation, Long>, JpaSpecificationExecutor<LocalityTranslation> {

    fun findByLocality(locality: Locality) : List<LocalityTranslation>?

    @Query("SELECT r FROM LocalityTranslation r WHERE r.locality.id = ?1 and r.language = ?2")
    fun findByLocality(localityId: Long, language: String) : LocalityTranslation?

    @Modifying
    @Transactional
    fun deleteByLocality(locality: Locality)

    @Query("SELECT r.language FROM LocalityTranslation r WHERE r.locality.id = ?1 ")
    fun findLanguages(localityId: Long?): List<String>?
}