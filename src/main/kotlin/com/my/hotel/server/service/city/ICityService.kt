package com.my.hotel.server.service.city

import com.my.hotel.server.data.model.CityAddressConfig
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.CityAddressConfigInput
import com.my.hotel.server.graphql.dto.request.UpdateCityAddressConfig
import org.springframework.data.domain.Page

interface ICityService {
    fun addCity(input: com.my.hotel.server.graphql.dto.request.CityInput): com.my.hotel.server.graphql.dto.response.CityDto?
    fun updateCity(input: com.my.hotel.server.graphql.dto.request.UpdateCity): com.my.hotel.server.graphql.dto.response.CityDto?
    fun getCities(countryId: Long, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.CityDto>?
    fun deleteCity(cityId: Long): Boolean?
    fun getCityAdmin(id: Long, language: String?): com.my.hotel.server.graphql.dto.response.CityDto?
    fun getLanguages(cityId: Long): List<String>?
    fun getCityAddressConfig(cityId: Long): List<CityAddressConfig>?
    fun addCityAddressConfig(input: CityAddressConfigInput): CityAddressConfig?
    fun updateCityAddressConfig(input: UpdateCityAddressConfig): CityAddressConfig?
    fun deleteCityAddressConfig(id: Long): Boolean?
    fun getCityAddressConfigDetails(id: Long): CityAddressConfig?
}