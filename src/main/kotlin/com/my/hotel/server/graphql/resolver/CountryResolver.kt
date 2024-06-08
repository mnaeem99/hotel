package com.my.hotel.server.graphql.resolver


import com.my.hotel.server.data.repository.CountryTranslationRepository
import com.my.hotel.server.graphql.dto.response.CountryDto
import com.my.hotel.server.graphql.dto.response.CountryLanguageDto
import com.my.hotel.server.graphql.security.Unsecured
import graphql.kickstart.tools.GraphQLResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component


@Component
class CountryResolver @Autowired constructor(
    val countryTranslationRepository: CountryTranslationRepository
) : GraphQLResolver<CountryDto> {
    @Unsecured
    fun getAvailableLanguages(countryDto: CountryDto): List<CountryLanguageDto>? {
        return countryTranslationRepository.findAvailableTranslation(countryDto.id!!)
    }
}
