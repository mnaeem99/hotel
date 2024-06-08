package com.my.hotel.server.service.hotelProfile

import com.my.hotel.server.commons.Constants
import com.my.hotel.server.commons.FileUpload
import com.my.hotel.server.commons.SpatialUtils
import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.model.Image
import com.my.hotel.server.data.model.HotelTranslation
import com.my.hotel.server.data.model.HotelVerificationAppointment
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.AdminHotelFilter
import com.my.hotel.server.graphql.dto.request.HotelInput
import com.my.hotel.server.graphql.dto.request.HotelVerificationAppointmentInput
import com.my.hotel.server.graphql.dto.request.UpdateHotel
import com.my.hotel.server.graphql.dto.response.MyHotelDto
import com.my.hotel.server.graphql.dto.response.HotelVerificationAppointmentDto
import com.my.hotel.server.graphql.error.AlreadyExistCustomException
import com.my.hotel.server.graphql.error.ExecutionAbortedCustomException
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.graphql.error.ValidationErrorCustomException
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.provider.idProvider.IDProvider
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.service.aws.AWSService
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
class HotelProfileService @Autowired constructor(
    private val myHotelRepository: MyHotelRepository,
    private val imageRepository: ImageRepository,
    private val awsService: AWSService,
    private val countryRepository: CountryRepository,
    private val cityRepository: CityRepository,
    private val localityRepository: LocalityRepository,
    private val hotelPriceLevelRepository: HotelPriceLevelRepository,
    private val hotelVerificationAppointmentRepository: HotelVerificationAppointmentRepository,
    private val hotelTranslationRepository: HotelTranslationRepository,
    private val translationService: TranslationService,
    private val dateProvider: DateProvider,
    private val idProvider: IDProvider,
    private val favoriteRepository: FavoriteRepository,
    private val wishListRepository: WishListRepository,
    private val hotelPriceRangeRepository: HotelPriceRangeRepository,
    private val giftRepository: GiftRepository,
    private val loyaltyPointRepository: LoyaltyPointRepository,
    private val pointsHistoryRepository: PointsHistoryRepository,
    private val orderRepository: OrderRepository,
    private val promotionRepository: PromotionRepository,
    private val qrCodeRepository: QRCodeRepository,
    private val notificationRepository: NotificationRepository,
    private val hotelRankRepository: HotelRankRepository,
    private val suggestionRepository: SuggestionRepository,
) : IHotelProfileService{
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun addHotel(input: HotelInput): MyHotelDto? {
        val hotel = MyHotel(phone = input.phone, geolat = input.geolat, geolong = input.geolong, createdAt = dateProvider.getCurrentDateTime())
        hotel.status = input.status
        if (input.geolat!=null && input.geolong!=null){
            val point = SpatialUtils.getPoint(input.geolat,input.geolong)
            hotel.point = point
        }
        if (input.placeId != null){
            val existhotel = myHotelRepository.findByPlaceId(input.placeId)
            if(existhotel != null){
                throw AlreadyExistCustomException(Constants.hotel_ALREADY_EXIST)
            }
            hotel.placeId = input.placeId
            hotel.expiryDate = dateProvider.getCurrentDateTime().plusDays(30)
        }
        if(input.photo != null) {
            hotel.photo = addImage(input.photo!!,input.name)
        }
        if(!input.photoList.isNullOrEmpty()) {
            hotel.photoList = input.photoList!!.stream().map { photo -> addImage(photo,input.name) }.collect(Collectors.toList())
        }
        if (input.countryId != null){
            val country = countryRepository.findByIdOrNull(input.countryId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"countryId")
            hotel.country = country
        }
        if (input.cityId != null){
            val city = cityRepository.findByIdOrNull(input.cityId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"cityId")
            hotel.city = city
        }
        if (input.localityId != null){
            val locality = localityRepository.findByIdOrNull(input.localityId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"localityId")
            hotel.locality = locality
        }
        if (input.hotelPriceLevelId != null){
            val hotelPriceLevel = hotelPriceLevelRepository.findByIdOrNull(input.hotelPriceLevelId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"hotelPriceLevelId")
            hotel.hotelPriceLevel = hotelPriceLevel
        }
        logger.info("New hotel ${input.name} is added")
        val newhotel = myHotelRepository.save(hotel)
        val hotelTranslation = HotelTranslation(name = input.name, address = input.address, language = input.language ?: Constants.DEFAULT_LANGUAGE, hotel = newhotel)
        hotelTranslationRepository.save(hotelTranslation)
        return translationService.mapmyHotelDto(hotel,input.language)
    }
    override fun updateHotel(input: UpdateHotel): MyHotelDto? {
        val hotel = myHotelRepository.findByIdOrNull(input.id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"id")
        val hotelTranslation = hotelTranslationRepository.findByHotel(input.id, input.language?:Constants.DEFAULT_LANGUAGE)
        if (hotelTranslation == null && input.name!=null && input.language!=null) {
            val updatehotel = updatemyHotel(input, hotel)
            val newHotelTranslation = HotelTranslation(name = input.name, address = input.address, language = input.language ?: Constants.DEFAULT_LANGUAGE, hotel = updatehotel)
            hotelTranslationRepository.save(newHotelTranslation)
            logger.info("hotel ${updatehotel.id} is updated")
            return translationService.mapmyHotelDto(updatehotel,newHotelTranslation.language)
        }
        if (hotelTranslation!=null){
            if (input.name != null){
                hotelTranslation.name = input.name
            }
            if (input.address!=null) {
                hotelTranslation.address = input.address
            }
            if (input.language!=null){
                hotelTranslation.language = input.language
            }
            hotelTranslationRepository.save(hotelTranslation)
        }
        val updatehotel = updatemyHotel(input, hotel)
        logger.info("hotel ${updatehotel.id} is updated")
        return translationService.mapmyHotelDto(updatehotel, hotelTranslation?.language ?: Constants.DEFAULT_LANGUAGE)
    }

    private fun updatemyHotel(input: UpdateHotel, hotel: MyHotel): MyHotel {
        if (input.phone != null)
            hotel.phone = input.phone
        if (input.geolat != null) {
            hotel.geolat = input.geolat
        }
        if (input.geolong != null) {
            hotel.geolong = input.geolong
        }
        if (input.geolat != null && input.geolong != null) {
            hotel.point = SpatialUtils.getPoint(input.geolat, input.geolong)
        }
        if (input.photo != null) {
            if (hotel.photo == null) {
                hotel.photo = addImage(input.photo!!, hotel.id.toString())
            } else {
                val image = updateImage(input.photo!!, hotel)
                hotel.photo = image
            }
        }
        if (!input.removePhotoId.isNullOrEmpty()){
            input.removePhotoId!!.stream().map { id -> deleteImage(id) }.collect(Collectors.toList())
        }
        if (!input.photoList.isNullOrEmpty()) {
            input.photoList!!.forEach { photo ->
                val image = addImage(photo,hotel.id.toString())
                hotel.photoList = hotel.photoList?.plus(image)
            }
        }
        if (input.countryId != null) {
            val country = countryRepository.findByIdOrNull(input.countryId)
                ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "countryId")
            hotel.country = country
        }
        if (input.cityId != null) {
            val city = cityRepository.findByIdOrNull(input.cityId) ?: throw NotFoundCustomException(
                Constants.RECORD_NOT_FOUND,
                "cityId"
            )
            hotel.city = city
        }
        if (input.localityId != null) {
            val locality = localityRepository.findByIdOrNull(input.localityId) ?: throw NotFoundCustomException(
                Constants.RECORD_NOT_FOUND,
                "localityId"
            )
            hotel.locality = locality
        }
        if(input.placeId != null){
            hotel.placeId = input.placeId
        }
        if(input.status != null){
            hotel.status = input.status
        }
        if (input.hotelPriceLevelId != null) {
            val hotelPriceLevel = hotelPriceLevelRepository.findByIdOrNull(input.hotelPriceLevelId)
                ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "hotelPriceLevelId")
            hotel.hotelPriceLevel = hotelPriceLevel
        }
        return myHotelRepository.save(hotel)
    }

    private fun updateImage(photo: FileUpload, hotel: MyHotel): Image {
        if (photo.content.size > (5 * 1000000)) {
            throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
        }
        val photoUri = awsService.savePicture(photo.content, "${hotel.id}-hotel", photo.contentType)
        val image = imageRepository.findById(hotel.photo?.id!!).get()
        image.imageUrl = photoUri?.toURL()
        imageRepository.save(image)
        return image
    }

    private fun deleteImage(imageId: Long) {
        val image = imageRepository.findById(imageId).get()
        try {
            imageRepository.delete(image)
        }catch (e: Exception){
            logger.error("Error while delete photo $imageId : ${e.message}")
        }
    }

    private fun addImage(photo: FileUpload, name: String?): Image {
        if (photo.content.size > (5 * 1000000)) {
            throw ValidationErrorCustomException("${Constants.PROFILE_PICTURE_MAX_SIZE_EXCEEDS} 5MBs")
        }
        val photoUri = awsService.savePicture(photo.content, "${name}-hotel-${idProvider.getUUID()}", photo.contentType)
        val newImage = Image(photoUri?.toURL())
        return imageRepository.save(newImage)
    }

    override fun gethotels(input: AdminHotelFilter, pageOptions: GraphQLPage): Page<MyHotelDto>? {
        return myHotelRepository.findAll(input.language?:Constants.DEFAULT_LANGUAGE,
            input.countryId,input.cityId,input.localityId,input.priceLevelId,input.searchKeyword,
            pageOptions.toPageable())?.map { entity -> entity.tohotelDto() }
    }
    override fun deleteHotel(hotelId: Long): Boolean? {
        val hotel = myHotelRepository.findByIdOrNull(hotelId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"hotelId")
        try {
            hotelTranslationRepository.deleteByHotel(hotel)
            favoriteRepository.deleteByHotel(hotel)
            wishListRepository.deleteByHotel(hotel)
            hotelPriceRangeRepository.deleteByHotel(hotel)
            giftRepository.deleteByHotel(hotel)
            loyaltyPointRepository.deleteByHotel(hotel)
            pointsHistoryRepository.deleteByHotel(hotel)
            orderRepository.deleteByHotel(hotel)
            promotionRepository.deleteByHotel(hotel)
            qrCodeRepository.deleteByHotel(hotel)
            notificationRepository.deleteByHotel(hotel.id)
            notificationRepository.deleteByHotel(hotel)
            hotelRankRepository.deleteByHotel(hotel)
            suggestionRepository.deleteBymyHotel(hotel)
            hotelVerificationAppointmentRepository.deleteByHotel(hotel)
            myHotelRepository.delete(hotel)
            return true
        } catch (e: Exception) {
            logger.error("Exception while delete hotel:${hotelId} ${e.message}")
            throw ExecutionAbortedCustomException("Error while delete hotel")
        }
    }
    override fun hotelVerificationAppointment(input: HotelVerificationAppointmentInput): HotelVerificationAppointmentDto? {
        val hotel = myHotelRepository.findByIdOrNull(input.hotelId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"hotelId")
        if (input.phone!=null){
            hotel.phone = input.phone
            myHotelRepository.save(hotel)
        }
        val hotelVerificationAppointment = hotelVerificationAppointmentRepository.findByHotel(hotel)
        if (hotelVerificationAppointment!=null){
            throw AlreadyExistCustomException(Constants.REQUEST_ALREADY_IN_USE)
        }
        logger.info("New HotelVerificationAppointment date ${input.date} is added for hotel ${hotel.id}")
        val newHotelVerificationAppointment = HotelVerificationAppointment(input.date,hotel)
        val appointment = hotelVerificationAppointmentRepository.save(newHotelVerificationAppointment)
        return HotelVerificationAppointmentDto(
            appointment.appointmentDate,
            translationService.mapmyHotelDto(appointment.hotel, input.language),
            appointment.id
        )
    }

    override fun gethotelVerificationAppointments(language: String?, pageOptions: GraphQLPage): Page<HotelVerificationAppointmentDto>? {
        val appointments = hotelVerificationAppointmentRepository.findAll(pageOptions.toPageable())
        val list = appointments.content.stream().map { entity ->
            HotelVerificationAppointmentDto(
                entity.appointmentDate,
                translationService.mapmyHotelDto(entity.hotel, language),
                entity.id
            )
        }?.collect(Collectors.toList())
        return PageImpl(list ?: emptyList(), pageOptions.toPageable(), appointments.totalElements )
    }
    override fun gethotelInner(hotelId: Long, language: String?): MyHotelDto?{
        val hotel = myHotelRepository.findByIdOrNull(hotelId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"hotelId")
        val hotelTranslation = hotelTranslationRepository.findByHotel(hotel.id!!, language ?: Constants.DEFAULT_LANGUAGE) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"language")
        return MyHotelDto(
            hotelTranslation.name,
            hotelTranslation.address,
            hotel.phone,
            translationService.mapCountryDto(hotel.country, language),
            hotel.geolat,
            hotel.geolong,
            hotel.hotelPriceLevel,
            hotel.googlePriceLevel,
            hotel.photoList,
            hotel.photo,
            hotel.placeId,
            hotel.expiryDate,
            hotel.status,
            hotel.googleMapUrl,
            hotel.id
        )
    }

    override fun getLanguages(hotelId: Long): List<String>? {
        return hotelTranslationRepository.findLanguages(hotelId)
    }
}