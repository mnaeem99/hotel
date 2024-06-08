package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.DeviceLocation
import com.my.hotel.server.data.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Modifying
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface DeviceLocationRepository : JpaRepository<DeviceLocation, String>, JpaSpecificationExecutor<DeviceLocation> {
    @Modifying
    @Transactional
    fun deleteByUser(user: User)
}