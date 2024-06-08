package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.TargetAudience
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface TargetAudienceRepository : JpaRepository<TargetAudience, Long>, JpaSpecificationExecutor<TargetAudience> {
}