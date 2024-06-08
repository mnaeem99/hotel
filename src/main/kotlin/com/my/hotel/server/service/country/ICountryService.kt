package com.my.hotel.server.service.country

import com.my.hotel.server.data.model.CountryAddressConfig
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.CountryAddressConfigInput
import com.my.hotel.server.graphql.dto.request.UpdateCountryAddressConfig
import org.springframework.data.domain.Page

interface ICountryService {
    fun addCountry(input: com.my.hotel.server.graphql.dto.request.CountryInput): com.my.hotel.server.graphql.dto.response.CountryDto?
    fun updateCountry(input: com.my.hotel.server.graphql.dto.request.UpdateCountry): com.my.hotel.server.graphql.dto.response.CountryDto?
    fun getCountries(language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.CountryDto>?
    fun deleteCountry(countryId: Long): Boolean?
    fun getCountryAdmin(id: Long, language: String?): com.my.hotel.server.graphql.dto.response.CountryDto?
    fun getCountryAdminByLocality(localityId: Long, language: String?): com.my.hotel.server.graphql.dto.response.CountryDto?
    fun getLanguages(countryId: Long): List<String>?
    fun addCountryAddressConfig(input: CountryAddressConfigInput): CountryAddressConfig?
    fun updateCountryAddressConfig(input: UpdateCountryAddressConfig): CountryAddressConfig?
    fun deleteCountryAddressConfig(id: Long): Boolean?
    fun getCountryAddressConfig(countryId: Long): List<CountryAddressConfig>?
    fun getCountryAddressConfigDetails(id: Long): CountryAddressConfig?
}