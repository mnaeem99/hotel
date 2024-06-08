package com.my.hotel.server.service.country

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.CountryAddressConfig
import com.my.hotel.server.data.model.Country
import com.my.hotel.server.data.model.CountryTranslation
import com.my.hotel.server.data.model.Image
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.CountryAddressConfigInput
import com.my.hotel.server.graphql.dto.request.CountryInput
import com.my.hotel.server.graphql.dto.request.UpdateCountry
import com.my.hotel.server.graphql.dto.request.UpdateCountryAddressConfig
import com.my.hotel.server.graphql.dto.response.CountryDto
import com.my.hotel.server.graphql.error.AlreadyExistCustomException
import com.my.hotel.server.graphql.error.ExecutionAbortedCustomException
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.graphql.error.ValidationErrorCustomException
import com.my.hotel.server.service.aws.AWSService
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Slf4j
@Validated
class CountryService @Autowired constructor(
    private val imageRepository: ImageRepository,
    private val awsService: AWSService,
    private val countryRepository: CountryRepository,
    private val countryTranslationRepository: CountryTranslationRepository,
    private val myHotelRepository: MyHotelRepository,
    private val userRepository: UserRepository,
    private val regionRepository: RegionRepository,
    private val regionTranslationRepository: RegionTranslationRepository,
    private val countryAddressConfigRepository: CountryAddressConfigRepository,
) : ICountryService{
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun addCountry(input: CountryInput): CountryDto? {
        val foundCountry = countryRepository.findByCode(input.code)
        if(foundCountry!=null){
            throw AlreadyExistCustomException(Constants.COUNTRY_ALREADY_EXIST)
        }
        val country = Country(input.code)
        if(input.picture != null) {
            if(input.picture?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri = awsService.savePicture(input.picture!!.content, "picture-${input.code}", input.picture!!.contentType)
            val newImage = Image(photoUri?.toURL())
            imageRepository.save(newImage)
            country.picture = newImage
        }
        if(input.flag != null) {
            if(input.flag?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri = awsService.savePicture(input.flag!!.content, "flag-${input.code}", input.flag!!.contentType)
            val newImage = Image(photoUri?.toURL())
            imageRepository.save(newImage)
            country.flag = newImage
        }
        logger.info("New Country ${input.name} is added")
        val newCountry = countryRepository.save(country)
        val countryTranslation = countryTranslationRepository.save(CountryTranslation(name = input.name, language = input.language ?: Constants.DEFAULT_LANGUAGE, country = newCountry))
        return CountryDto(
            countryTranslation.name,
            newCountry.code,
            newCountry.picture,
            newCountry.flag,
            newCountry.id
        )
    }
    override fun updateCountry(input: UpdateCountry): CountryDto? {
        val country = countryRepository.findByIdOrNull(input.id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        val countryTranslation = countryTranslationRepository.findByCountry(input.id, input.language?:Constants.DEFAULT_LANGUAGE)
            ?: return addCountryTranslation(input, country)
        if (input.name != null){
            countryTranslation.name = input.name
        }
        if (input.language!=null) {
            countryTranslation.language = input.language
        }
        if (input.code!=null){
            country.code = input.code!!
        }
        if(input.picture != null) {
            if(input.picture?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri = awsService.savePicture(input.picture!!.content, "picture-${country.code}", input.picture!!.contentType)
            if (country.picture == null) {
                val newImage = Image(photoUri?.toURL())
                imageRepository.save(newImage)
                country.picture = newImage
            }else{
                val image = imageRepository.findById(country.picture?.id!!).get()
                image.imageUrl = photoUri?.toURL()
                imageRepository.save(image)
            }
        }
        if(input.flag != null) {
            if(input.flag?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri = awsService.savePicture(input.flag!!.content, "flag-${country.code}", input.flag!!.contentType)
            if (country.flag == null) {
                val newImage = Image(photoUri?.toURL())
                imageRepository.save(newImage)
                country.flag = newImage
            }else{
                val image = imageRepository.findById(country.flag?.id!!).get()
                image.imageUrl = photoUri?.toURL()
                imageRepository.save(image)
            }
        }
        val updateCountryTranslation = countryTranslationRepository.save(countryTranslation)
        val updateCountry = countryRepository.save(country)
        logger.info("Country ${country.id} is updated")
        return CountryDto(
            updateCountryTranslation.name,
            updateCountry.code,
            updateCountry.picture,
            updateCountry.flag,
            updateCountry.id
        )
    }

    private fun addCountryTranslation(
        input: UpdateCountry,
        country: Country
    ): CountryDto {
        val newCountryTranslation = countryTranslationRepository.save(
            CountryTranslation(
                name = input.name,
                language = input.language ?: Constants.DEFAULT_LANGUAGE,
                country = country
            )
        )
        if (input.code != null) {
            country.code = input.code!!
        }
        if (input.picture != null) {
            if (input.picture?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri =
                awsService.savePicture(input.picture!!.content, "picture-${country.code}", input.picture!!.contentType)
            if (country.picture == null) {
                val newImage = Image(photoUri?.toURL())
                imageRepository.save(newImage)
                country.picture = newImage
            } else {
                val image = imageRepository.findById(country.picture?.id!!).get()
                image.imageUrl = photoUri?.toURL()
                imageRepository.save(image)
            }
        }
        if (input.flag != null) {
            if (input.flag?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri =
                awsService.savePicture(input.flag!!.content, "flag-${country.code}", input.flag!!.contentType)
            if (country.flag == null) {
                val newImage = Image(photoUri?.toURL())
                imageRepository.save(newImage)
                country.flag = newImage
            } else {
                val image = imageRepository.findById(country.flag?.id!!).get()
                image.imageUrl = photoUri?.toURL()
                imageRepository.save(image)
            }
        }
        val updateCountry = countryRepository.save(country)
        return CountryDto(
            newCountryTranslation.name,
            updateCountry.code,
            updateCountry.picture,
            updateCountry.flag,
            updateCountry.id
        )
    }

    override fun getCountries(language: String?, pageOptions: GraphQLPage): Page<CountryDto>? {
        return countryRepository.findAllCountries(language ?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable()).map { entity ->
            CountryDto(
                entity.name,
                entity.code,
                entity.picture,
                entity.flag,
                entity.id
            )
        }
    }
    override fun deleteCountry(countryId: Long): Boolean? {
        val country = countryRepository.findByIdOrNull(countryId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"countryId")
        val foundhotel = myHotelRepository.findByCountry(country)
        if (foundhotel.isNotEmpty()){
            throw AlreadyExistCustomException(Constants.COUNTRY_ALREADY_PRESENT + "hotels")
        }
        val foundUser = userRepository.findByCountry(country)
        if (foundUser.isNotEmpty()){
            throw AlreadyExistCustomException(Constants.COUNTRY_ALREADY_PRESENT + "users")
        }
        try {
            regionTranslationRepository.deleteByCountry(country.id!!)
            regionRepository.deleteByCountry(country)
            countryTranslationRepository.deleteByCountry(country)
            countryRepository.delete(country)
            return true
        } catch (e: Exception) {
            logger.error("Exception while delete country:${countryId} ${e.message}")
            throw ExecutionAbortedCustomException("Error while delete country")
        }
    }

    override fun getCountryAdmin(id: Long, language: String?): CountryDto? {
        val country = countryRepository.findById(id, language?: Constants.DEFAULT_LANGUAGE) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        return CountryDto(
            country.name,
            country.code,
            country.picture,
            country.flag,
            country.id
        )
    }
    override fun getCountryAdminByLocality(localityId: Long, language: String?): CountryDto? {
        val country = countryRepository.findByLocality(localityId, language?:Constants.DEFAULT_LANGUAGE) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "localityId")
        return CountryDto(
            country.name,
            country.code,
            country.picture,
            country.flag,
            country.id
        )
    }
    override fun getLanguages(countryId: Long): List<String>? {
        return countryTranslationRepository.findLanguages(countryId)
    }

    override fun addCountryAddressConfig(input: CountryAddressConfigInput): CountryAddressConfig? {
        val country = countryRepository.findByIdOrNull(input.countryId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"countryId")
        val foundConfig = countryAddressConfigRepository.findByTypeAndCountry(input.type, country)
        if (foundConfig!=null) {
            throw AlreadyExistCustomException(Constants.ADDRESS_TYPE_ALREADY_EXIST)
        }
        return countryAddressConfigRepository.save(CountryAddressConfig(country, input.type, input.priority, input.level))
    }

    override fun updateCountryAddressConfig(input: UpdateCountryAddressConfig): CountryAddressConfig? {
        val countryAddressConfig = countryAddressConfigRepository.findByIdOrNull(input.configId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"configId")
        if (input.countryId!=null){
            val country = countryRepository.findByIdOrNull(input.countryId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"countryId")
            countryAddressConfig.country = country
        }
        if (input.type!=null){
            countryAddressConfig.type = input.type!!
        }
        if (input.priority!=null){
            countryAddressConfig.priority = input.priority!!
        }
        if (input.level!=null){
            countryAddressConfig.level = input.level!!
        }
        return countryAddressConfigRepository.save(countryAddressConfig)
    }

    override fun deleteCountryAddressConfig(id: Long): Boolean? {
        val countryAddressConfig = countryAddressConfigRepository.findByIdOrNull(id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        countryAddressConfigRepository.delete(countryAddressConfig)
        return true
    }
    override fun getCountryAddressConfig(countryId: Long): List<CountryAddressConfig>? {
        return countryAddressConfigRepository.findByCountry(countryId)
    }
    override fun getCountryAddressConfigDetails(id: Long): CountryAddressConfig? {
        return countryAddressConfigRepository.findByIdOrNull(id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
    }
}