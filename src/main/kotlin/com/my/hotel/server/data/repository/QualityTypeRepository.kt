package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.QualityType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository


@Repository
interface QualityTypeRepository : JpaRepository<QualityType, Long>, JpaSpecificationExecutor<QualityType>