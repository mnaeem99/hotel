package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.Status
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository


@Repository
interface StatusRepository : JpaRepository<Status, Long>, JpaSpecificationExecutor<Status>