package com.my.hotel.server.service.locality

import com.my.hotel.server.graphql.GraphQLPage
import org.springframework.data.domain.Page

interface ILocalityService {
    fun addLocality(input: com.my.hotel.server.graphql.dto.request.LocalityInput): com.my.hotel.server.graphql.dto.response.LocalityDto?
    fun updateLocality(input: com.my.hotel.server.graphql.dto.request.UpdateLocality): com.my.hotel.server.graphql.dto.response.LocalityDto?
    fun getLocalities(cityId: Long, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.LocalityDto>?
    fun deleteLocality(localityId: Long): Boolean?
    fun getLocalityAdmin(id: Long, language: String?): com.my.hotel.server.graphql.dto.response.LocalityDto?
    fun getLanguages(localityId: Long): List<String>?
}