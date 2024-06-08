package com.my.hotel.server.service.city

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.CityAddressConfigInput
import com.my.hotel.server.graphql.dto.request.CityInput
import com.my.hotel.server.graphql.dto.request.UpdateCity
import com.my.hotel.server.graphql.dto.request.UpdateCityAddressConfig
import com.my.hotel.server.graphql.dto.response.CityDto
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
class CityService @Autowired constructor(
    private val imageRepository: ImageRepository,
    private val awsService: AWSService,
    private val cityRepository: CityRepository,
    private val cityTranslationRepository: CityTranslationRepository,
    private val countryRepository: CountryRepository,
    private val myHotelRepository: MyHotelRepository,
    private val cityAddressConfigRepository: CityAddressConfigRepository,
) : ICityService{
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun addCity(input: CityInput): CityDto? {
        if (input.countryId!=null) {
            val foundCity = cityRepository.findByName(input.name, input.countryId!!, input.language ?: Constants.DEFAULT_LANGUAGE)
            if (foundCity != null) {
                throw AlreadyExistCustomException(Constants.CITY_ALREADY_EXIST)
            }
        }
        val city = City(placeId = input.placeId)
        if(input.picture != null) {
            if(input.picture?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri = awsService.savePicture(input.picture!!.content, "picture-${input.name}", input.picture!!.contentType)
            val newImage = Image(photoUri?.toURL())
            imageRepository.save(newImage)
            city.picture = newImage
        }
        logger.info("New City ${input.name} is added")
        val newCity = cityRepository.save(city)
        if (input.countryId!=null){
            val country = countryRepository.findByIdOrNull(input.countryId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"countryId")
            country.city = country.city?.plus(newCity)
            countryRepository.save(country)
        }
        val cityTranslation = cityTranslationRepository.save(CityTranslation(name = input.name, language = input.language ?: Constants.DEFAULT_LANGUAGE, city = newCity))
        return CityDto(
            cityTranslation.name,
            newCity.picture,
            null,
            newCity.placeId,
            newCity.id,
            input.countryId
        )
    }
    override fun updateCity(input: UpdateCity): CityDto? {
        val city = cityRepository.findByIdOrNull(input.id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        val cityTranslation = cityTranslationRepository.findByCity(input.id, input.language?:Constants.DEFAULT_LANGUAGE)
            ?: return addCityTranslation(input, city)
        if (input.name != null){
            cityTranslation.name = input.name
        }
        if (input.language!=null) {
            cityTranslation.language = input.language
        }
        if (input.placeId!=null){
            city.placeId = input.placeId
        }
        if(input.picture != null) {
            if(input.picture?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri = awsService.savePicture(input.picture!!.content, "picture-${cityTranslation.name}", input.picture!!.contentType)
            if (city.picture == null) {
                val newImage = Image(photoUri?.toURL())
                imageRepository.save(newImage)
                city.picture = newImage
            }else{
                val image = imageRepository.findById(city.picture?.id!!).get()
                image.imageUrl = photoUri?.toURL()
                imageRepository.save(image)
            }
        }
        val updateCityTranslation = cityTranslationRepository.save(cityTranslation)
        val updateCity = cityRepository.save(city)
        if (input.countryId!=null){
            val country = countryRepository.findByIdOrNull(input.countryId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"countryId")
            country.city = country.city?.plus(updateCity)
            countryRepository.save(country)
        }
        val countryId = countryRepository.findByCity(updateCity.id!!)
        logger.info("City ${city.id} is updated")
        return CityDto(
            updateCityTranslation.name,
            updateCity.picture,
            null,
            updateCity.placeId,
            updateCity.id,
            countryId
        )
    }

    private fun addCityTranslation(input: UpdateCity, city: City): CityDto {
        val newCityTranslation = cityTranslationRepository.save(
            CityTranslation(
                name = input.name,
                language = input.language ?: Constants.DEFAULT_LANGUAGE,
                city = city
            )
        )
        if (input.placeId != null) {
            city.placeId = input.placeId
        }
        if (input.picture != null) {
            if (input.picture?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri = awsService.savePicture(
                input.picture!!.content,
                "picture-${newCityTranslation.name}",
                input.picture!!.contentType
            )
            if (city.picture == null) {
                val newImage = Image(photoUri?.toURL())
                imageRepository.save(newImage)
                city.picture = newImage
            } else {
                val image = imageRepository.findById(city.picture?.id!!).get()
                image.imageUrl = photoUri?.toURL()
                imageRepository.save(image)
            }
        }
        val updateCity = cityRepository.save(city)
        if (input.countryId != null) {
            val country = countryRepository.findByIdOrNull(input.countryId)
                ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "countryId")
            country.city = country.city?.plus(updateCity)
            countryRepository.save(country)
        }
        val countryId = countryRepository.findByCity(updateCity.id!!)
        return CityDto(
            newCityTranslation.name,
            updateCity.picture,
            null,
            updateCity.placeId,
            updateCity.id,
            countryId
        )
    }

    override fun getCities(countryId: Long, language: String?, pageOptions: GraphQLPage): Page<CityDto>? {
        return cityRepository.findByCountry(countryId, language ?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())?.map { entity ->
            CityDto(
                entity.name,
                entity.picture,
                entity.locality,
                entity.placeId,
                entity.id,
                entity.countryId
            )
        }
    }
    override fun deleteCity(cityId: Long): Boolean? {
        val city = cityRepository.findByIdOrNull(cityId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"cityId")
        val foundhotel = myHotelRepository.findByCity(city)
        if (foundhotel.isNotEmpty()){
            throw AlreadyExistCustomException(Constants.CITY_ALREADY_PRESENT + "hotels")
        }
        try {
            cityTranslationRepository.deleteByCity(city)
            cityRepository.delete(city)
            return true
        } catch (e: Exception) {
            logger.error("Exception while delete country:${cityId} ${e.message}")
            throw ExecutionAbortedCustomException("Error while delete country")
        }
    }

    override fun getCityAdmin(id: Long, language: String?): CityDto? {
        val city = cityRepository.findById(id, language?: Constants.DEFAULT_LANGUAGE) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        return CityDto(
            city.name,
            city.picture,
            city.locality,
            city.placeId,
            city.id,
            city.countryId
        )
    }

    override fun getLanguages(cityId: Long): List<String>? {
        return cityTranslationRepository.findLanguages(cityId)
    }

    override fun getCityAddressConfig(cityId: Long): List<CityAddressConfig>? {
        return cityAddressConfigRepository.findByCity(cityId)
    }

    override fun addCityAddressConfig(input: CityAddressConfigInput): CityAddressConfig? {
        val city = cityRepository.findByIdOrNull(input.cityId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"cityId")
        val foundConfig = cityAddressConfigRepository.findByTypeAndCity(input.type, city)
        if (foundConfig!=null) {
            throw AlreadyExistCustomException(Constants.ADDRESS_TYPE_ALREADY_EXIST)
        }
        return cityAddressConfigRepository.save(CityAddressConfig(city, input.type, input.priority))
    }

    override fun updateCityAddressConfig(input: UpdateCityAddressConfig): CityAddressConfig? {
        val cityAddressConfig = cityAddressConfigRepository.findByIdOrNull(input.configId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"configId")
        if (input.cityId!=null){
            val city = cityRepository.findByIdOrNull(input.cityId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"cityId")
            cityAddressConfig.city = city
        }
        if (input.type!=null){
            cityAddressConfig.type = input.type!!
        }
        if (input.priority!=null){
            cityAddressConfig.priority = input.priority!!
        }
        return cityAddressConfigRepository.save(cityAddressConfig)
    }

    override fun deleteCityAddressConfig(id: Long): Boolean? {
        val cityAddressConfig = cityAddressConfigRepository.findByIdOrNull(id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        cityAddressConfigRepository.delete(cityAddressConfig)
        return true
    }

    override fun getCityAddressConfigDetails(id: Long): CityAddressConfig? {
        return cityAddressConfigRepository.findByIdOrNull(id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
    }
}