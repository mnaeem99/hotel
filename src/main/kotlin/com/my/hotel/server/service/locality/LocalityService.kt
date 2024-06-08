package com.my.hotel.server.service.locality

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.Image
import com.my.hotel.server.data.model.Locality
import com.my.hotel.server.data.model.LocalityTranslation
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.LocalityInput
import com.my.hotel.server.graphql.dto.request.UpdateLocality
import com.my.hotel.server.graphql.dto.response.LocalityDto
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
class LocalityService @Autowired constructor(
    private val imageRepository: ImageRepository,
    private val awsService: AWSService,
    private val localityRepository: LocalityRepository,
    private val localityTranslationRepository: LocalityTranslationRepository,
    private val cityRepository: CityRepository,
    private val myHotelRepository: MyHotelRepository,
) : ILocalityService{
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun addLocality(input: LocalityInput): LocalityDto? {
        if (input.cityId!=null) {
            val foundLocality = localityRepository.findByName(input.name, input.cityId!!, input.language ?: Constants.DEFAULT_LANGUAGE)
            if (foundLocality != null) {
                throw AlreadyExistCustomException(Constants.LOCALITY_ALREADY_EXIST)
            }
        }
        val locality = Locality(placeId = input.placeId)
        if(input.picture != null) {
            if(input.picture?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri = awsService.savePicture(input.picture!!.content, "picture-${input.name}", input.picture!!.contentType)
            val newImage = Image(photoUri?.toURL())
            imageRepository.save(newImage)
            locality.picture = newImage
        }

        logger.info("New Locality ${input.name} is added")
        val newLocality = localityRepository.save(locality)
        val localityTranslation = localityTranslationRepository.save(LocalityTranslation(name = input.name, language = input.language ?: Constants.DEFAULT_LANGUAGE, locality = newLocality))
        if (input.cityId!=null){
            val city = cityRepository.findByIdOrNull(input.cityId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"cityId")
            city.locality = city.locality?.plus(newLocality)
            cityRepository.save(city)
        }
        return LocalityDto(
            localityTranslation.name,
            newLocality.picture,
            newLocality.placeId,
            newLocality.id,
            input.cityId
        )
    }

    override fun updateLocality(input: UpdateLocality): LocalityDto? {
        val locality = localityRepository.findByIdOrNull(input.id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        val localityTranslation = localityTranslationRepository.findByLocality(input.id, input.language?:Constants.DEFAULT_LANGUAGE)
            ?: return addLocalityTranslation(input, locality)
        if (input.name != null){
            localityTranslation.name = input.name
        }
        if (input.language!=null) {
            localityTranslation.language = input.language
        }
        if (input.placeId!=null){
            locality.placeId = input.placeId
        }
        if(input.picture != null) {
            if(input.picture?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri = awsService.savePicture(input.picture!!.content, "picture-${localityTranslation.name}", input.picture!!.contentType)
            if (locality.picture == null) {
                val newImage = Image(photoUri?.toURL())
                imageRepository.save(newImage)
                locality.picture = newImage
            }else{
                val image = imageRepository.findById(locality.picture?.id!!).get()
                image.imageUrl = photoUri?.toURL()
                imageRepository.save(image)
            }
        }
        val updateLocalityTranslation = localityTranslationRepository.save(localityTranslation)
        val updateLocality = localityRepository.save(locality)
        if (input.cityId!=null){
            val city = cityRepository.findByIdOrNull(input.cityId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"cityId")
            city.locality = city.locality?.plus(updateLocality)
            cityRepository.save(city)
        }
        logger.info("Locality ${locality.id} is updated")
        return LocalityDto(
            updateLocalityTranslation.name,
            updateLocality.picture,
            updateLocality.placeId,
            updateLocality.id,
            input.cityId
        )
    }

    private fun addLocalityTranslation(input: UpdateLocality, locality: Locality): LocalityDto {
        val newLocalityTranslation = localityTranslationRepository.save(
            LocalityTranslation(
                name = input.name,
                language = input.language ?: Constants.DEFAULT_LANGUAGE,
                locality = locality
            )
        )
        if (input.placeId != null) {
            locality.placeId = input.placeId
        }
        if (input.picture != null) {
            if (input.picture?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri = awsService.savePicture(
                input.picture!!.content,
                "picture-${newLocalityTranslation.name}",
                input.picture!!.contentType
            )
            if (locality.picture == null) {
                val newImage = Image(photoUri?.toURL())
                imageRepository.save(newImage)
                locality.picture = newImage
            } else {
                val image = imageRepository.findById(locality.picture?.id!!).get()
                image.imageUrl = photoUri?.toURL()
                imageRepository.save(image)
            }
        }
        val updateLocality = localityRepository.save(locality)
        return LocalityDto(
            newLocalityTranslation.name,
            updateLocality.picture,
            updateLocality.placeId,
            updateLocality.id
        )
    }

    override fun getLocalities(cityId: Long, language: String?, pageOptions: GraphQLPage): Page<LocalityDto>? {
        return localityRepository.findByCity(cityId, language ?: Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())
            .map { entity ->
                LocalityDto(
                    entity.name,
                    entity.picture,
                    entity.placeId,
                    entity.id,
                    entity.cityId
                )
            }
    }

    override fun deleteLocality(localityId: Long): Boolean?  {
        val locality = localityRepository.findByIdOrNull(localityId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"localityId")
        val foundhotel = myHotelRepository.findByLocality(locality)
        if (foundhotel.isNotEmpty()){
            throw AlreadyExistCustomException(Constants.LOCALITY_ALREADY_PRESENT + "hotels")
        }
        try {
            localityTranslationRepository.deleteByLocality(locality)
            localityRepository.delete(locality)
            return true
        } catch (e: Exception) {
            logger.error("Exception while delete country:${localityId} ${e.message}")
            throw ExecutionAbortedCustomException("Error while delete country")
        }
    }

    override fun getLocalityAdmin(id: Long, language: String?): LocalityDto? {
        val localityDto = localityRepository.findById(id, language?: Constants.DEFAULT_LANGUAGE) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        return LocalityDto(
            localityDto.name,
            localityDto.picture,
            localityDto.placeId,
            localityDto.id,
            localityDto.cityId
        )
    }

    override fun getLanguages(localityId: Long): List<String>? {
        return localityTranslationRepository.findLanguages(localityId)
    }
}