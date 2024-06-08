package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.RegionTranslation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface RegionTranslationRepository : JpaRepository<RegionTranslation, Long>, JpaSpecificationExecutor<RegionTranslation> {
    @Query("SELECT r FROM RegionTranslation r WHERE r.region.placeId = ?1 and r.language = ?2")
    fun findByRegion(regionId: String, language: String) : RegionTranslation?

    @Modifying
    @Transactional
    @Query(value = "delete from my_region_translation WHERE region_id in ( " +
            "SELECT id from my_region WHERE country_id = ?1 " +
            ")", nativeQuery = true)
    fun deleteByCountry(countryId: Long)
}