package com.my.hotel.server.service.promotion

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.Image
import com.my.hotel.server.data.model.NotificationType
import com.my.hotel.server.data.model.Promotion
import com.my.hotel.server.data.model.TargetAudience
import com.my.hotel.server.data.repository.ImageRepository
import com.my.hotel.server.data.repository.PromotionRepository
import com.my.hotel.server.data.repository.MyHotelRepository
import com.my.hotel.server.data.repository.TargetAudienceRepository
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.PromotionInput
import com.my.hotel.server.graphql.dto.request.UpdatePromotion
import com.my.hotel.server.graphql.dto.response.PromotionDto
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.graphql.error.ValidationErrorCustomException
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.service.aws.AWSService
import com.my.hotel.server.service.event.EventService
import com.my.hotel.server.service.event.dto.Event
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.util.stream.Collectors


@Service
@Slf4j
@Validated
class PromotionService @Autowired constructor(
    private val myHotelRepository: MyHotelRepository,
    private val translationService: TranslationService,
    private val promotionRepository: PromotionRepository,
    private val targetAudienceRepository: TargetAudienceRepository,
    private val eventService: EventService,
    private val dateProvider: DateProvider,
    private val awsService: AWSService,
    private val imageRepository: ImageRepository,
) : IPromotionService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun getPromotions(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<PromotionDto>? {
        val hotel = myHotelRepository.findByIdOrNull(hotelId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"hotelId")
        val promotions = promotionRepository.findByHotel(hotel, pageOptions.toPageable())
        val list = promotions?.content?.stream()?.map { entity -> toPromotionDto(entity,language) }?.collect(Collectors.toList())
        return PageImpl(list ?: emptyList(), pageOptions.toPageable(), promotions?.totalElements?: 0 )
    }
    fun toPromotionDto(promotion: Promotion, language: String?): PromotionDto {
        val hotelDto = translationService.mapmyHotelDto(promotion.hotel, language)
        return PromotionDto(
            promotion.title,
            promotion.titleColor,
            promotion.subTitle,
            promotion.subTitleColor,
            promotion.buttonText,
            promotion.buttonColor,
            promotion.budget,
            promotion.duration,
            promotion.showLogo,
            promotion.cover,
            promotion.geolat,
            promotion.geolong,
            promotion.radius,
            promotion.region,
            promotion.active,
            hotelDto,
            promotion.targetAudience,
            promotion.createdAt,
            promotion.modifiedAt,
            promotion.id
        )
    }

    override fun getTargetAudiences(): List<TargetAudience>? {
        return targetAudienceRepository.findAll()
    }

    override fun addPromotion(input: PromotionInput): PromotionDto? {
        val hotel = myHotelRepository.findByIdOrNull(input.hotelId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"hotelId")
        val targetAudience = targetAudienceRepository.findAllById(input.targetAudienceId)
        val promotion = promotionRepository.save(Promotion(input.title, input.titleColor, input.subTitle, input.subTitleColor,input.buttonText,input.buttonColor,input.budget,input.duration,input.showLogo,null,input.geolat,input.geolong,input.radius,input.region,input.active,hotel,targetAudience,dateProvider.getCurrentDateTime(),dateProvider.getCurrentDateTime()))
        if(input.cover != null) {
            // upload to s3
            if(input.cover?.content!!.size > (5 * 1000000)) {
                throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
            }
            val photoUri = awsService.savePicture(
                input.cover!!.content,
                "${promotion.id}-promotion",
                input.cover!!.contentType,
            )
            // save to db
            val newImage = Image(photoUri?.toURL(),null)
            imageRepository.save(newImage)
            promotion.cover = newImage
            promotionRepository.save(promotion)
        }
        logger.info("New Promotion ${promotion.title} is added on hotel ${promotion.hotel.id}")
        eventService.createEvent(Event(NotificationType.PROMOTION_FROM_hotel,null,null,promotion.id))
        return toPromotionDto(promotion, input.language ?: Constants.DEFAULT_LANGUAGE)
    }

    override fun updatePromotion(input: UpdatePromotion): PromotionDto? {
        val promotion = promotionRepository.findByIdOrNull(input.id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        if (input.title!=null)
            promotion.title = input.title
        if (input.titleColor!=null)
            promotion.titleColor = input.titleColor
        if (input.subTitle!=null)
            promotion.subTitle = input.subTitle
        if (input.subTitleColor!=null)
            promotion.subTitleColor = input.subTitleColor
        if (input.buttonText!=null)
            promotion.buttonText = input.buttonText
        if (input.buttonColor!=null)
            promotion.buttonColor = input.buttonColor
        if (input.budget!=null)
            promotion.budget = input.budget
        if (input.duration!=null)
            promotion.duration = input.duration
        if (input.showLogo!=null)
            promotion.showLogo = input.showLogo
        if (input.cover!=null) {
            // upload to s3
            setCover(input, promotion)
        }
        if (input.active!=null)
            promotion.active = input.active
        if (input.geolat!=null)
            promotion.geolat = input.geolat
        if (input.geolong!=null)
            promotion.geolong = input.geolong
        if (input.radius!=null)
            promotion.radius = input.radius
        if (input.region!=null)
            promotion.region = input.region
        if (input.targetAudienceId!=null) {
            val targetAudience = targetAudienceRepository.findAllById(input.targetAudienceId!!)
            promotion.targetAudience = promotion.targetAudience?.plus(targetAudience)
        }
        promotion.modifiedAt = dateProvider.getCurrentDateTime()
        logger.info("Updated Promotion ${promotion.title} on hotel ${promotion.hotel.id}")
        val newPromotion = promotionRepository.save(promotion)
        return toPromotionDto(newPromotion, input.language ?: Constants.DEFAULT_LANGUAGE)
    }

    private fun setCover(input: UpdatePromotion, promotion: Promotion) {
        if (input.cover?.content!!.size > (5 * 1000000)) {
            throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
        }
        val photoUri = awsService.savePicture(
            input.cover!!.content,
            "${input.id}-promotion",
            input.cover!!.contentType,
        )
        // save to db
        if (promotion.cover == null || promotion.cover?.id == null) {
            val newImage = Image(photoUri?.toURL(), null)
            imageRepository.save(newImage)
            promotion.cover = newImage
        } else {
            val image = imageRepository.findByIdOrNull(promotion.cover!!.id)
            if (image != null) {
                image.imageUrl = photoUri?.toURL()
                imageRepository.save(image)
                promotion.cover = image
            }
        }
    }

    override fun deletePromotion(id: Long): Boolean {
        val promotion = promotionRepository.findByIdOrNull(id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        promotionRepository.delete(promotion)
        logger.info("Promotion ${promotion.title} is deleted on hotel ${promotion.hotel.id}")
        return true
    }
}