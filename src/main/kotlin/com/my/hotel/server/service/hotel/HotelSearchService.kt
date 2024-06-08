package com.my.hotel.server.service.hotel

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.my.hotel.server.commons.Constants
import com.my.hotel.server.commons.SpatialUtils
import com.my.hotel.server.commons.SpatialUtils.getPoint
import com.my.hotel.server.data.model.GoogleHotel
import com.my.hotel.server.data.model.SearchQuery
import com.my.hotel.server.data.model.User
import com.my.hotel.server.data.repository.GoogleHotelRepository
import com.my.hotel.server.data.repository.MyHotelRepository
import com.my.hotel.server.data.repository.SearchQueryRepository
import com.my.hotel.server.graphql.GraphQLPage
import com.my.hotel.server.graphql.dto.request.QueryFilter
import com.my.hotel.server.graphql.dto.response.MyHotelDto
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.graphql.error.ValidationErrorCustomException
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.service.aws.AWSService
import com.my.hotel.server.service.hotel.dto.ResultDTO
import com.my.hotel.server.service.hotel.dto.SearchDTO
import org.apache.http.client.utils.URIBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.time.LocalDateTime
import java.util.stream.Collectors


@Service
class HotelSearchService @Autowired constructor(
    private val saveHotelService: SaveHotelService,
    private val searchQueryRepository: SearchQueryRepository,
    private val myHotelRepository: MyHotelRepository,
    private val googleHotelRepository: GoogleHotelRepository,
    private val awsService: AWSService,
    @Value("\${aws.secrets.googleMapsKey}")
    private var secretsGoogleMapsKey:String,
    private val restTemplate: RestTemplate,
    private val dateProvider: DateProvider
){
    fun getSearchGoogleResponse(filter: QueryFilter, pageToken: String?, secretsJson: JsonNode?): JsonNode?{
        val googleMapsKey = secretsJson?.get( "googleMapsKey")?.asText()
        val radius = secretsJson?.get( "nearbySearchRadius")?.asText()
        val type = secretsJson?.get( "nearbySearchType")?.asText()
        val url = if (filter.userQuery.isNullOrEmpty()){
            String.format(Constants.GOOGLE_API_NEARBY_SEARCH_URL, Constants.GOOGLE_API_BASE_URL)
        }else{
            String.format(Constants.GOOGLE_API_TEXT_SEARCH_URL, Constants.GOOGLE_API_BASE_URL)
        }
        val builder = URIBuilder(url)
        if (filter.latitude!=null && filter.longitude!=null) {
            builder.addParameter("location","${filter.latitude},${filter.longitude}")
            builder.addParameter("radius", radius.toString())
        }
        if (filter.userQuery!=null)
            builder.addParameter("query", filter.userQuery)
        if (filter.language!=null)
            builder.addParameter("language", filter.language)
        if (type != null)
            builder.addParameter("type", type)
        builder.addParameter("key", googleMapsKey)
        if (pageToken!=null){
            builder.addParameter("pagetoken",pageToken)
        }
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
    fun getResults(query: QueryFilter, secretsJson: JsonNode?): ArrayList<ResultDTO>? {
        val page1Result = getPageResult(query, null, secretsJson)
        val results = page1Result?.results
        if (page1Result?.nextPageToken!=null){
            val page2Result = getPageResult(query, page1Result.nextPageToken, secretsJson)
            if (page2Result != null) {
                results?.addAll(page2Result.results!!)
                if (page2Result.nextPageToken!=null){
                    val page3Result = getPageResult(query, page1Result.nextPageToken, secretsJson)
                    results?.addAll(page3Result?.results!!)
                }
            }
        }
        return results
    }

    private fun getPageResult(query: QueryFilter, pageToken: String?, secretsJson: JsonNode?): SearchDTO? {
        val response = getSearchGoogleResponse(query, pageToken, secretsJson)
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper.readValue(response.toString(), SearchDTO::class.java)
    }
    fun getAllhotelsFromGoogle(query: QueryFilter): List<GoogleHotel>? {
        val secretsJson = awsService.getValue(secretsGoogleMapsKey)
        val results = getResults(query, secretsJson)
        if (!results.isNullOrEmpty()){
            val response = results.stream().map { entity -> saveHotelService.getGoogleHotel(entity, query.language?:Constants.DEFAULT_LANGUAGE) }?.collect(Collectors.toList())
            var user: User? = null
            val principal = SecurityContextHolder.getContext().authentication.principal
            if(principal is User){
                user = principal
            }
            val expiryDate: LocalDateTime = dateProvider.getGoogleExpiryDate()
            val radius = secretsJson?.get( "nearbySearchRadius")?.asText().toString()
            val type = secretsJson?.get( "nearbySearchType")?.asText().toString()
            val searchQuery = searchQueryRepository.save(SearchQuery(query.userQuery,radius.toDouble(),type,query.language?:Constants.DEFAULT_LANGUAGE, user,null, expiryDate, getPoint(query.latitude?.toFloat(),query.longitude?.toFloat())))
            searchQuery.response = response
            searchQueryRepository.save(searchQuery)
            return response
        }
        return null
    }
    fun updateAllhotelsFromGoogle(query: QueryFilter, existQuery: SearchQuery): List<GoogleHotel>? {
        val secretsJson = awsService.getValue(secretsGoogleMapsKey)
        val results = getResults(query, secretsJson)
        if (!results.isNullOrEmpty()) {
            val response = results.stream().map { entity -> saveHotelService.getGoogleHotel(entity, query.language?:Constants.DEFAULT_LANGUAGE) }?.collect(Collectors.toList())
            val expiryDate: LocalDateTime = dateProvider.getGoogleExpiryDate()
            existQuery.response = response
            existQuery.expiryDate = expiryDate
            searchQueryRepository.save(existQuery)
            return response
        }
        return null
    }
    fun googleSearchhotels(query: QueryFilter, pageOptions: GraphQLPage): Page<MyHotelDto>? {
        if(query.latitude == null || query.longitude == null || !SpatialUtils.isValidLatLang(query.latitude, query.longitude))
            throw ValidationErrorCustomException(Constants.INVALID_LOCATION)
        val hotels = googleHotelRepository.findByNearestLocation(SpatialUtils.createCircle(query.latitude, query.longitude, 1.0), query.userQuery, getPoint(query.latitude.toFloat(),query.longitude.toFloat()),dateProvider.getCurrentDateTime(), pageOptions.toPageable())
        if (hotels.totalElements >= pageOptions.size)
            return PageImpl(getmyHotels(hotels.content, query.language), pageOptions.toPageable(),hotels.totalElements)
        val existQuery = searchQueryRepository.findByInput(SpatialUtils.createCircle(query.latitude, query.longitude, 0.1), query.userQuery, query.language?:Constants.DEFAULT_LANGUAGE, getPoint(query.latitude.toFloat(),query.longitude.toFloat()))
        if (existQuery.isNullOrEmpty()){
            val list = getAllhotelsFromGoogle(query) ?: return null
            val start = pageOptions.toPageable().offset.toInt().coerceAtMost(list.size)
            val end = (start + pageOptions.toPageable().pageSize).coerceAtMost(list.size)
            val myHotels = getmyHotels(list.subList(start, end), query.language)
            return PageImpl(myHotels, pageOptions.toPageable(),list.size.toLong())
        }
        if (existQuery.first().expiryDate!! < dateProvider.getCurrentDateTime()){
            val list = updateAllhotelsFromGoogle(query, existQuery.first()) ?: return null
            val start = pageOptions.toPageable().offset.toInt().coerceAtMost(list.size)
            val end = (start + pageOptions.toPageable().pageSize).coerceAtMost(list.size)
            val myHotels = getmyHotels(list.subList(start, end), query.language)
            return PageImpl(myHotels, pageOptions.toPageable(),list.size.toLong())
        }
        val list = existQuery.first().response ?: return null
        val start = pageOptions.toPageable().offset.toInt().coerceAtMost(list.size)
        val end = (start + pageOptions.toPageable().pageSize).coerceAtMost(list.size)
        val myHotels = getmyHotels(list.subList(start, end), query.language)
        return PageImpl(myHotels, pageOptions.toPageable(),list.size.toLong())
    }
    private fun getmyHotels(googleHotels: List<GoogleHotel>, language: String?): List<MyHotelDto> {
        val placeIds: List<String> = googleHotels.map { hotel -> hotel.placeId!! }
        var myHotels = emptyList<MyHotelDto>()
        val myHotelTranslationDto = myHotelRepository.findAllByPlaceId(placeIds, language ?: Constants.DEFAULT_LANGUAGE)
        if (!myHotelTranslationDto.isNullOrEmpty()) {
            myHotels = myHotels.plus(myHotelTranslationDto.map { dto -> dto.tohotelDto() })
        }
        val filteredGooglehotels = googleHotels.filterNot { googlehotel ->
            myHotels.any { it.placeId == googlehotel.placeId }
        }
            .map { googlehotel -> googlehotel.tomyHotelDto() }
        myHotels = myHotels.plus(filteredGooglehotels)
        return myHotels
    }
    fun similarHotel(hotelId: Long, language: String?, pageOptions: GraphQLPage): Page<MyHotelDto>? {
        val hotel = myHotelRepository.findByIdOrNull(hotelId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"hotelId")
        val hotels = myHotelRepository.findSimilarhotels(hotelId, SpatialUtils.createCircle(hotel.geolat?.toDouble()!!, hotel.geolong?.toDouble()!!, 1.0), language?:Constants.DEFAULT_LANGUAGE, pageOptions.toPageable())
        return hotels?.map { entity -> entity.tohotelDto() }
    }
}