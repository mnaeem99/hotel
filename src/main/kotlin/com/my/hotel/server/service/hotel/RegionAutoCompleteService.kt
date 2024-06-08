package com.my.hotel.server.service.hotel

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.my.hotel.server.commons.Constants
import com.my.hotel.server.commons.SpatialUtils
import com.my.hotel.server.commons.SpatialUtils.getPoint
import com.my.hotel.server.data.model.AutoCompleteHotel
import com.my.hotel.server.data.model.AutoCompleteQuery
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
class RegionAutoCompleteService @Autowired constructor(
    private val autoCompleteHotelRepository: AutoCompleteHotelRepository,
    private val autoCompleteQueryRepository: AutoCompleteQueryRepository,
    private val awsService: AWSService,
    @Value("\${aws.secrets.googleMapsKey}")
    private var secretsGoogleMapsKey:String,
    private val restTemplate: RestTemplate,
    private val dateProvider: DateProvider,
    private val iDProvider: IDProvider
){
    fun getResponseFromGoogleMaps(query: QueryFilter, sessionToken : String?, secretsJson: JsonNode?, type: String): JsonNode? {
        val googleMapsKey = secretsJson?.get( "googleMapsKey")?.asText()
        val radius = secretsJson?.get( "autocompleteRadius")?.asText()
        val builder = URIBuilder(String.format(Constants.GOOGLE_API_AUTO_URL, Constants.GOOGLE_API_BASE_URL))
        builder.addParameter("input",query.userQuery )
        builder.addParameter("radius",  radius.toString())
        if (query.latitude!=null && query.longitude!=null) {
            builder.addParameter("location", "${query.latitude},${query.longitude}")
            builder.addParameter("radius", radius)
        }
        if (sessionToken!=null){
            builder.addParameter("sessiontoken", sessionToken)
        }
        if (query.language!=null)
            builder.addParameter("language", query.language)

        builder.addParameter("types", type)
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
    fun getPredictions(query: QueryFilter, sessionToken : String?, secretsJson: JsonNode?, type: String): List<PredictionDTO>? {
        val response = getResponseFromGoogleMaps(query,sessionToken, secretsJson, type)
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        val result = mapper.readValue(response.toString(), AutoCompleteDTO::class.java)
        return result.predictions
    }

    fun getAutoCompleteHotel(prediction: PredictionDTO): AutoCompleteHotel {
        val existAutoComplete = autoCompleteHotelRepository.findByIdOrNull(prediction.placeId)
        if (existAutoComplete!=null)
            return existAutoComplete
        val newAutoComplete = AutoCompleteHotel(prediction.description, prediction.reference, prediction.structuredFormatting?.mainText.toString(), prediction.structuredFormatting?.secondaryText.toString(), prediction.types,dateProvider.getGoogleExpiryDate(), prediction.placeId.toString())
        return autoCompleteHotelRepository.save(newAutoComplete)
    }

    fun getAllRegions(query: QueryFilter, type: String): AutocompleteResponse? {
        val secretsJson = awsService.getValue(secretsGoogleMapsKey)
        var circle: Geometry? = null
        if (query.latitude!=null && query.longitude != null){
            if(!SpatialUtils.isValidLatLang(query.latitude, query.longitude)) {
                throw ValidationErrorCustomException(Constants.INVALID_LOCATION)
            }
            circle = SpatialUtils.createCircle(query.latitude, query.longitude, 0.1)
        }
        val existQuery = autoCompleteQueryRepository.findByInput(circle, query.userQuery.toString(), query.language?:Constants.DEFAULT_LANGUAGE, type)
        if (existQuery.isNullOrEmpty())
            return getNewRegionsQueryResults(query, secretsJson, type)
        if (existQuery.first().expiryDate!! > dateProvider.getCurrentDateTime())
            return AutocompleteResponse(
                existQuery.first().response,
                query.sessionToken ?: existQuery.first().sessionToken
            )
        return updateRegionsQueryResults(query, existQuery.first(), secretsJson, type)
    }
    fun getNewRegionsQueryResults(query: QueryFilter, secretsJson: JsonNode?, type: String): AutocompleteResponse? {
        val radius = secretsJson?.get( "autocompleteRadius")?.asText()
        val regionType = secretsJson?.get( "autocompleteRegionType")?.asText()
        val sessionToken = query.sessionToken ?: iDProvider.getUUID()
        val prediction = getPredictions(query,sessionToken, secretsJson, type)
        if (prediction.isNullOrEmpty())
            return null
        val autoCompleteHotels = prediction.stream().map { entity -> getAutoCompleteHotel(entity) }?.collect(Collectors.toList())
        var user: User? = null
        val principal = SecurityContextHolder.getContext().authentication.principal
        if(principal is User){
            user = principal
        }
        val expiryDate: LocalDateTime = dateProvider.getGoogleExpiryDate()
        var point: Point? = null
        if (query.latitude!=null && query.longitude != null) {
            point = getPoint(query.latitude.toFloat(), query.longitude.toFloat())
        }
        val newQuery = autoCompleteQueryRepository.save(AutoCompleteQuery(query.userQuery,radius?.toDouble(),null,query.language?:Constants.DEFAULT_LANGUAGE,null,null,sessionToken,regionType, user,null, expiryDate,point))
        newQuery.response = autoCompleteHotels
        autoCompleteQueryRepository.save(newQuery)
        return AutocompleteResponse(
            autoCompleteHotels,
            sessionToken
        )
    }
    fun updateRegionsQueryResults(query: QueryFilter, existQuery: AutoCompleteQuery, secretsJson: JsonNode?, type: String): AutocompleteResponse? {
        val sessionToken = query.sessionToken ?: iDProvider.getUUID()
        val prediction = getPredictions(query, sessionToken, secretsJson, type)
        if (prediction.isNullOrEmpty())
            return null
        val autoCompleteHotels = prediction.stream().map { entity -> getAutoCompleteHotel(entity) }?.collect(Collectors.toList())
        val expiryDate: LocalDateTime = dateProvider.getGoogleExpiryDate()
        existQuery.response = autoCompleteHotels
        existQuery.expiryDate = expiryDate
        existQuery.sessionToken = sessionToken
        autoCompleteQueryRepository.save(existQuery)
        return AutocompleteResponse(
            autoCompleteHotels,
            sessionToken
        )
    }
}