package com.my.hotel.server.service.quality

import com.my.hotel.server.data.model.Quality
import com.my.hotel.server.data.model.QualityType
import com.my.hotel.server.graphql.GraphQLPage
import org.springframework.data.domain.Page

interface IQualityService {
    fun getQualities(): List<Quality>?
    fun getQualityTypes(): List<QualityType>?
    fun getQualities(pageOptions: GraphQLPage): Page<Quality>?
    fun getQualityTypes(pageOptions: GraphQLPage): Page<QualityType>?
    fun addQuality(input: com.my.hotel.server.graphql.dto.request.QualityInput): Quality?
    fun addQualityType(name: String): QualityType?
    fun updateQuality(input: com.my.hotel.server.graphql.dto.request.UpdateQuality): Quality?
    fun updateQualityType(input: com.my.hotel.server.graphql.dto.request.UpdateQualityType): QualityType?
    fun deleteQuality(id: Long): Boolean
    fun deleteQualityType(id: Long): Boolean
    fun getQualityAdmin(id: Long): Quality?
    fun qualityTypeAdmin(id: Long): QualityType?
}