package com.my.hotel.server.data.repository

import com.my.hotel.server.data.model.Image
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface ImageRepository : JpaRepository<Image, Long>, JpaSpecificationExecutor<Image>