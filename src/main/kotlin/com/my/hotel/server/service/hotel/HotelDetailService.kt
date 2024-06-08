package com.my.hotel.server.service.hotel

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.MyHotel
import com.my.hotel.server.data.repository.MyHotelRepository
import com.my.hotel.server.data.repository.HotelTranslationRepository
import com.my.hotel.server.graphql.dto.response.MyHotelDto
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.service.aws.AWSService
import com.my.hotel.server.service.hotel.dto.DetailsDTO
import com.my.hotel.server.service.hotel.dto.ResultDTO
import org.apache.http.client.utils.URIBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI

@Service
class HotelDetailService @Autowired constructor(
    private val myMyHotelRepository: MyHotelRepository,
    private val saveHotelService: SaveHotelService,
    private val awsService: AWSService,
    @Value("\${aws.secrets.googleMapsKey}")
    private var secretsGoogleMapsKey:String,
    private val hotelTranslationRepository: HotelTranslationRepository,
    private val translationService: TranslationService,
    private val restTemplate: RestTemplate,
    private val dateProvider: DateProvider,
){
    fun getDetailGoogleResponse(id:String, sessionToken : String?, language: String?, isUpdateHotel: Boolean): JsonNode?{
        val secretsJson = awsService.getValue(secretsGoogleMapsKey)
        val googleMapsKey = secretsJson?.get( "googleMapsKey")?.asText()
        val builder = URIBuilder(String.format(Constants.GOOGLE_API_DETAIL_URL, Constants.GOOGLE_API_BASE_URL))
        builder.addParameter("place_id", id)
        if (language!=null){
            builder.addParameter("language", language)
        }
        if (sessionToken!=null){
            builder.addParameter("sessiontoken", sessionToken)
        }
        if(isUpdateHotel)
            builder.addParameter("fields", Constants.UPDATE_DETAIL_hotel_FIELDS)
        else
            builder.addParameter("fields", Constants.NEW_DETAIL_hotel_FIELDS)
        builder.addParameter("key", googleMapsKey)
        val uri: URI = builder.build()
        val response: JsonNode
        try {
            response = restTemplate.getForObject(uri, JsonNode::class.java)!!
        }catch (e: Exception){
            e.stackTrace
            return null
        }
        return response
    }
    fun getResults(id: String, sessionToken : String?, language: String?, isUpdateHotel: Boolean): ResultDTO? {
        val response = getDetailGoogleResponse(id,sessionToken, language, isUpdateHotel)
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper.readValue(response.toString(), DetailsDTO::class.java).result
    }
    fun gethotelInner(hotelId: Long, language: String): MyHotelDto?{
        val hotel = gethotelById(hotelId, language)
        return hotel
    }
    fun gethotelById(hotelId: Long, language: String): MyHotelDto{
        val hotel = myMyHotelRepository.findByIdOrNull(hotelId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"hotelId")
        if (hotel.placeId == null){
            return translationService.mapmyHotelDto(hotel, language)
        } else if (hotel.expiryDate!=null && hotel.expiryDate!! < dateProvider.getCurrentDateTime()){
            val result = getResults(hotel.placeId!!, null, language, true) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"hotelId")
            return saveHotelService.updateDetailHotel(hotel, result, language)
        }
        val hotelTranslation = hotelTranslationRepository.findByHotel(hotel.id!!, language)
        if (hotelTranslation!=null)
            return translationService.mapmyHotelDto(hotelTranslation, hotel, language)
        val result = getResults(hotel.placeId!!, null, language, true) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"hotelId")
        return saveHotelService.updateDetailHotel(hotel, result, language)
    }
    fun getDetailMy(placeId: String, sessionToken: String?, language: String): MyHotelDto? {
        val hotel = gethotelByPlaceId(placeId, sessionToken, language)
        return hotel
    }
    fun gethotelByPlaceId(placeId: String, sessionToken: String?, language: String): MyHotelDto {
        val hotel = myMyHotelRepository.findByPlaceId(placeId)
        if (hotel == null){
            val result = getResults(placeId, sessionToken, language, false) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"placeId")
            return saveHotelService.getmyHotelDto(result, language)
        } else if(hotel.expiryDate!=null && hotel.expiryDate!! < dateProvider.getCurrentDateTime()){
            val result = getResults(placeId, sessionToken, language, true) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"placeId")
            return saveHotelService.updateDetailHotel(hotel, result, language)
        }
        val hotelTranslation = hotelTranslationRepository.findByHotel(hotel.id!!, language)
        if (hotelTranslation!=null)
            return translationService.mapmyHotelDto(hotelTranslation, hotel, language)
        val result = getResults(placeId, sessionToken, language, true) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"placeId")
        return saveHotelService.updateDetailHotel(hotel, result, language)
    }
    fun getGoogleHotel(placeId: String, language: String): MyHotel? {
        val hotel = myMyHotelRepository.findByPlaceId(placeId)
        if (hotel == null){
            val result = getResults(placeId, null, language, false) ?: return null
            val myHotel = saveHotelService.addmyHotel(result, language)
            saveHotelService.addhotelTranslation(result,language,myHotel)
            return myHotel
        } else if(hotel.expiryDate!=null && hotel.expiryDate!! < dateProvider.getCurrentDateTime()){
            val result = getResults(placeId, null, language, true) ?: return hotel
            saveHotelService.updateDetailHotel(hotel,result, language)
            return hotel
        }
        val hotelTranslation = hotelTranslationRepository.findByHotel(hotel.id!!, language)
        if (hotelTranslation!=null)
            return hotel
        val result = getResults(placeId, null, language, true) ?: return hotel
        saveHotelService.updateDetailHotel(hotel,result, language)
        return hotel
    }
    fun getmyHotel(hotelId: Long?, placeId: String?, language: String): MyHotel {
        var hotel: MyHotel? = null
        if (hotelId != null) {
            hotel = myMyHotelRepository.findByIdOrNull(hotelId)
        }
        if (placeId != null) {
            hotel = getGoogleHotel(placeId,language)
        }
        if (hotel == null)
            throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "hotelId")
        return hotel
    }
}