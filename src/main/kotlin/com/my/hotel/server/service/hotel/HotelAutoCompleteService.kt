package com.my.hotel.server.service.hotel

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.my.hotel.server.commons.Constants
import com.my.hotel.server.commons.SpatialUtils
import com.my.hotel.server.commons.SpatialUtils.getPoint
import com.my.hotel.server.data.model.AutoCompleteQuery
import com.my.hotel.server.data.model.AutoCompleteHotel
import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.repository.AutoCompleteQueryRepository
import com.my.hotel.server.data.repository.AutoCompleteHotelRepository
import com.my.hotel.server.graphql.dto.request.QueryFilter
import com.my.hotel.server.graphql.dto.response.AutocompleteResponse
import com.my.hotel.server.graphql.error.ValidationErrorCustomException
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.provider.idProvider.IDProvider
import com.my.hotel.server.service.aws.AWSService
import com.my.hotel.server.service.hotel.dto.AutoCompleteDTO
import com.my.hotel.server.service.hotel.dto.PredictionDTO
import org.apache.http.client.utils.URIBuilder
import org.locationtech.jts.geom.Geometry
import org.locationtech.jts.geom.Point
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.time.LocalDateTime
import java.util.stream.Collectors


@Service
class HotelAutoCompleteService @Autowired constructor(
    private val autoCompleteHotelRepository: AutoCompleteHotelRepository,
    private val autoCompleteQueryRepository: AutoCompleteQueryRepository,
    private val awsService: AWSService,
    @Value("\${aws.secrets.googleMapsKey}")
    private var secretsGoogleMapsKey:String,
    private val restTemplate: RestTemplate,
    private val dateProvider: DateProvider,
    private val iDProvider: IDProvider
){
    fun getResponseFromGoogleMaps(query:  QueryFilter, sessionToken : String?, secretsJson: JsonNode?): JsonNode? {
        val googleMapsKey = secretsJson?.get( "googleMapsKey")?.asText()
        val radius = secretsJson?.get( "autocompleteRadius")?.asText()
        val types = secretsJson?.get( "autocompleteTypes")?.asText()
        val builder = URIBuilder(String.format(Constants.GOOGLE_API_AUTO_URL, Constants.GOOGLE_API_BASE_URL))
        builder.addParameter("input",query.userQuery )
        if (query.latitude!=null && query.longitude!=null) {
            builder.addParameter("locationbias", "circle:${radius}@${query.latitude},${query.longitude}")
        }
        if (sessionToken!=null){
            builder.addParameter("sessiontoken", sessionToken)
        }
        if (query.language!=null)
            builder.addParameter("language", query.language)
        builder.addParameter("types", types)
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
    fun getPredictions(query:  QueryFilter, sessionToken : String?, secretsJson: JsonNode?): List<PredictionDTO>? {
        val response = getResponseFromGoogleMaps(query,sessionToken,secretsJson)
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val result = mapper.readValue(response.toString(), AutoCompleteDTO::class.java)
        return result.predictions
    }
    fun getAllhotels(query:  QueryFilter): AutocompleteResponse? {
        val secretsJson = awsService.getValue(secretsGoogleMapsKey)
        val types = secretsJson?.get( "autocompleteTypes")?.asText().toString()
        var circle: Geometry? = null
        if (query.latitude!=null && query.longitude != null){
            if(!SpatialUtils.isValidLatLang(query.latitude, query.longitude)) {
                throw ValidationErrorCustomException(Constants.INVALID_LOCATION)
            }
            circle = SpatialUtils.createCircle(query.latitude, query.longitude, 0.1)
        }
        val existQuery = autoCompleteQueryRepository.findByInput(circle, query.userQuery.toString(), query.language?:Constants.DEFAULT_LANGUAGE, types)
        if (existQuery.isNullOrEmpty())
            return getNewQueryResults(query, secretsJson)
        if (existQuery.first().expiryDate!! > dateProvider.getCurrentDateTime()) {
            return AutocompleteResponse(existQuery.first().response, query.sessionToken ?: existQuery.first().sessionToken)
        }
        return updateQueryResults(query, existQuery.first(), secretsJson)
    }
    fun getAutoCompleteHotels(prediction: PredictionDTO): AutoCompleteHotel {
        val existAutoComplete = autoCompleteHotelRepository.findByIdOrNull(prediction.placeId)
        if (existAutoComplete!=null)
            return existAutoComplete
        val newAutoComplete = AutoCompleteHotel(prediction.description, prediction.reference, prediction.structuredFormatting?.mainText.toString(), prediction.structuredFormatting?.secondaryText.toString(), prediction.types,dateProvider.getGoogleExpiryDate(), prediction.placeId.toString())
        return autoCompleteHotelRepository.save(newAutoComplete)
    }
    fun getNewQueryResults(query:  QueryFilter, secretsJson: JsonNode?): AutocompleteResponse? {
        val radius = secretsJson?.get( "autocompleteRadius")?.asText().toString()
        val types = secretsJson?.get( "autocompleteTypes")?.asText().toString()
        val sessionToken = query.sessionToken ?: iDProvider.getUUID()
        val prediction = getPredictions(query,sessionToken, secretsJson)
        if (prediction.isNullOrEmpty())
            return null
        val autoCompleteHotels = prediction.stream().map { entity -> getAutoCompleteHotels(entity) }?.collect(Collectors.toList())
        var user: User? = null
        val principal = SecurityContextHolder.getContext().authentication.principal
        if(principal is User){
            user = principal
        }
        val expiryDate: LocalDateTime = dateProvider.getGoogleExpiryDate()
        var point: Point? = null
        if (query.latitude!=null && query.longitude != null) {
            point = getPoint(query.latitude.toFloat(),query.longitude.toFloat())
        }
        val newQuery = autoCompleteQueryRepository.save(AutoCompleteQuery(query.userQuery,radius.toDouble(),null,query.language?:Constants.DEFAULT_LANGUAGE,null,null,sessionToken,types, user,null, expiryDate, point))
        newQuery.response = autoCompleteHotels
        autoCompleteQueryRepository.save(newQuery)
        return AutocompleteResponse(
            autoCompleteHotels,
            sessionToken
        )
    }
    fun updateQueryResults(query:  QueryFilter, existQuery: AutoCompleteQuery, secretsJson: JsonNode?): AutocompleteResponse? {
        val sessionToken = query.sessionToken ?: iDProvider.getUUID()
        val prediction = getPredictions(query,sessionToken, secretsJson)
        if (prediction.isNullOrEmpty())
            return null
        val autoCompleteHotels = prediction.stream().map { entity -> getAutoCompleteHotels(entity) }?.collect(Collectors.toList())
        existQuery.response = autoCompleteHotels
        existQuery.expiryDate = dateProvider.getGoogleExpiryDate()
        existQuery.sessionToken = sessionToken
        autoCompleteQueryRepository.save(existQuery)
        return AutocompleteResponse(
            autoCompleteHotels,
            sessionToken
        )
    }
}