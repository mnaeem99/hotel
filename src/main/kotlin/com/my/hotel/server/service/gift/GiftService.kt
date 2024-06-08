package com.my.hotel.server.service.gift

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.Gift
import com.my.hotel.server.data.model.Image
import com.my.hotel.server.data.repository.GiftRepository
import com.my.hotel.server.data.repository.ImageRepository
import com.my.hotel.server.data.repository.MyHotelRepository
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.response.GiftDto
import com.my.hotel.server.graphql.dto.response.MyHotelDto
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.graphql.error.ValidationErrorCustomException
import com.my.hotel.server.provider.translation.TranslationService
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
class GiftService @Autowired constructor(
    private val giftRepository: GiftRepository,
    private val myHotelRepository: MyHotelRepository,
    private val awsService: AWSService,
    private val imageRepository: ImageRepository,
    private val translationService: TranslationService
): IGiftService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun getGifts(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.GiftDto>? {
        val gifts = giftRepository.findByhotelLanguage(hotelId,language?:Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())
        return gifts?.map { entity -> translationService.mapGift(entity, language?:Constants.DEFAULT_LANGUAGE) }
    }
    override fun getAllGifts(language: String?, pageOptions: GraphQLPage): Page<com.my.hotel.server.graphql.dto.response.GiftDto>? {
        val gifts = giftRepository.findByLanguage(language?:Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())
        return gifts?.map { entity -> translationService.mapGift(entity, language?:Constants.DEFAULT_LANGUAGE) }
    }

    override fun addGift(input: com.my.hotel.server.graphql.dto.request.GiftInput): com.my.hotel.server.graphql.dto.response.GiftDto? {
        val hotel = myHotelRepository.findByIdOrNull(input.hotelId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"hotelId")
        val gift = Gift(input.name,points = input.points, otherInfo = input.otherInfo.toString(), hotel = hotel)
        if(input.picture != null) {
            if(input.picture?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri = awsService.savePicture(input.picture!!.content, "${input.name}-gift-${input.hotelId}", input.picture!!.contentType)
            val newImage = Image(photoUri?.toURL())
            imageRepository.save(newImage)
            gift.picture = newImage
        }
        logger.info("New Gift ${gift.name} is added on hotel ${gift.hotel.id}")
        val newGift = giftRepository.save(gift)
        return getGiftAdmin(newGift.id!!,input.language)
    }

    override fun updateGift(input: com.my.hotel.server.graphql.dto.request.UpdateGift): com.my.hotel.server.graphql.dto.response.GiftDto? {
        val gift = giftRepository.findByIdOrNull(input.id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        if (input.name != null){
            gift.name = input.name
        }
        if (input.points != null){
            gift.points = input.points!!
        }
        if (input.otherInfo != null){
            gift.otherInfo = input.otherInfo!!
        }
        if (input.points != null){
            gift.points = input.points!!
        }
        if(input.picture != null) {
            if(input.picture?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri = awsService.savePicture(input.picture!!.content, "${gift.name}-gift-${gift.hotel.id}", input.picture!!.contentType)
            if (gift.picture == null) {
                val newImage = Image(photoUri?.toURL())
                imageRepository.save(newImage)
                gift.picture = newImage
            }else{
                val image = imageRepository.findById(gift.picture?.id!!).get()
                image.imageUrl = photoUri?.toURL()
                imageRepository.save(image)
            }
        }
        logger.info("Gift ${gift.name} is updated on hotel ${gift.hotel.id}")
        val newGift = giftRepository.save(gift)
        return getGiftAdmin(newGift.id!!,input.language)
    }

    override fun deleteGift(id: Long): Boolean {
        val gift = giftRepository.findByIdOrNull(id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        giftRepository.delete(gift)
        logger.info("Gift ${gift.name} is deleted on hotel ${gift.hotel.id}")
        return true
    }

    override fun getGiftAdmin(id: Long, language: String?): com.my.hotel.server.graphql.dto.response.GiftDto? {
        val entity = giftRepository.findById(id,language?:Constants.DEFAULT_LANGUAGE) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        return translationService.mapGift(entity, language?:Constants.DEFAULT_LANGUAGE)
    }
    override fun getGifts(hotelDto: MyHotelDto): List<GiftDto>? {
        return giftRepository.findByHotel(hotelDto.id, hotelDto.placeId)?.map { entity ->
            GiftDto(
                entity.name,
                entity.picture,
                entity.points,
                entity.otherInfo,
                hotelDto,
                entity.id
            )
        }
    }
}