package com.my.hotel.server.service.hotel

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.my.hotel.server.commons.Constants
import com.my.hotel.server.commons.SpatialUtils
import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.dto.request.QueryFilter
import com.my.hotel.server.service.aws.AWSService
import com.my.hotel.server.service.hotel.dto.DetailsDTO
import com.my.hotel.server.service.hotel.dto.ResultDTO
import org.apache.http.client.utils.URIBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.net.URI

@Service
class RegionDetailService @Autowired constructor(
    private val localityRepository: LocalityRepository,
    private val cityRepository: CityRepository,
    private val countryRepository: CountryRepository,
    private val regionRepository: RegionRepository,
    private val regionTranslationRepository: RegionTranslationRepository,
    private val localityTranslationRepository: LocalityTranslationRepository,
    private val cityTranslationRepository: CityTranslationRepository,
    private val awsService: AWSService,
    @Value("\${aws.secrets.googleMapsKey}")
    private var secretsGoogleMapsKey:String,
    private val restTemplate: RestTemplate,
    private val imageService: ImageService,
    private val regionAutoCompleteService: RegionAutoCompleteService
){
    fun getDetailGoogleResponse(id:String, sessionToken : String?, language: String): JsonNode?{
        val secretsJson = awsService.getValue(secretsGoogleMapsKey)
        val googleMapsKey = secretsJson?.get( "googleMapsKey")?.asText()
        val builder = URIBuilder(String.format(Constants.GOOGLE_API_DETAIL_URL, Constants.GOOGLE_API_BASE_URL))
        builder.addParameter("place_id", id)
        builder.addParameter("language", language)
        if (sessionToken!=null){
            builder.addParameter("sessiontoken", sessionToken)
        }
        builder.addParameter("fields", Constants.DETAIL_REGION_FIELDS)
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
    fun getResults(id: String, sessionToken : String?, language: String): ResultDTO? {
        val response = getDetailGoogleResponse(id,sessionToken, language)
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return mapper.readValue(response.toString(), DetailsDTO::class.java).result
    }

    fun getDetailRegion(placeId: String, sessionToken: String?, language: String): com.my.hotel.server.graphql.dto.response.RegionDto? {
        val region = regionRepository.findByPlaceId(placeId)
        if (region != null) {
            val regionTranslation = regionTranslationRepository.findByRegion(region.placeId!!,language) ?: return addRegionTranslation(region, sessionToken, language)
            return com.my.hotel.server.graphql.dto.response.RegionDto(
                regionTranslation.name,
                regionTranslation.address,
                region.geolat,
                region.geolong,
                region.photo,
                region.country,
                region.placeId
            )
        }
        return addRegion(placeId, sessionToken, language)
    }
    fun addRegion(placeId: String, sessionToken: String?, language: String): com.my.hotel.server.graphql.dto.response.RegionDto? {
        val result = getResults(placeId,sessionToken,language) ?: return null
        var image: Image? = null
        if (!result.photos.isNullOrEmpty()) {
            val photo = result.photos?.first()
            image = imageService.toImage(photo!!)
        }
        var country: Country? = null
        if (result.addressComponents != null && result.addressComponents!!.isNotEmpty()) {
            for(addressComponent in result.addressComponents!!){
                if (addressComponent.types?.contains("country") == true)
                     country = countryRepository.findByCode(addressComponent.shortName.toString())
            }
        }
        val location = result.geometry?.location
        val point = SpatialUtils.getPoint(location?.lat?.toFloat(),location?.lng?.toFloat())
        val newRegion = regionRepository.save(Region(location?.lat?.toFloat(),location?.lng?.toFloat(),image,point,country,result.placeId))
        val newRegionTranslation = regionTranslationRepository.save(RegionTranslation(result.name,result.formattedAddress,language,newRegion))
        return com.my.hotel.server.graphql.dto.response.RegionDto(
            newRegionTranslation.name,
            newRegionTranslation.address,
            newRegion.geolat,
            newRegion.geolong,
            newRegion.photo,
            newRegion.country,
            newRegion.placeId
        )
    }
    fun addRegionTranslation(region: Region, sessionToken: String?, language: String): com.my.hotel.server.graphql.dto.response.RegionDto? {
        val result = getResults(region.placeId!!, sessionToken, language) ?: return null
        val newRegionTranslation = regionTranslationRepository.save(RegionTranslation(result.name,result.formattedAddress,language,region))
        return com.my.hotel.server.graphql.dto.response.RegionDto(
            newRegionTranslation.name,
            newRegionTranslation.address,
            region.geolat,
            region.geolong,
            region.photo,
            region.country,
            region.placeId
        )
    }

    fun getGoogleCity(queryFilter: QueryFilter, country: Country): City? {
        val autoCompleteResults = regionAutoCompleteService.getAllRegions(queryFilter,"geocode")
        if (autoCompleteResults == null || autoCompleteResults.autoCompleteHotels.isNullOrEmpty()) return null
        return getDetailRegionCity(autoCompleteResults.autoCompleteHotels.first().placeId!!,autoCompleteResults.sessionToken,queryFilter.language ?: Constants.DEFAULT_LANGUAGE, country)
    }
    fun getDetailRegionCity(placeId: String, sessionToken : String?, language: String,country: Country): City? {
        val city = cityRepository.findByPlaceId(placeId)
        if (city != null) {
            val translation = cityTranslationRepository.findByCity(city.id!!,language) ?: return addCityTranslation(city,sessionToken,language)
            return city
        }
        return addCity(placeId, sessionToken, language, country)
    }
    private fun addCity(placeId: String, sessionToken : String?, language: String, country: Country): City? {
        val result = getDetailRegion(placeId,sessionToken,language) ?: return null
        val city = cityRepository.findByName(result.name!!,country.id!!,language)
        if (city!=null)
            return city
        val newCity = cityRepository.save(City(result.photo, placeId = result.placeId))
        country.city = country.city?.plus(newCity)
        countryRepository.save(country)
        val newCityTranslation = CityTranslation(result.name, language, newCity)
        cityTranslationRepository.save(newCityTranslation)
        return newCity
    }
    private fun addCityTranslation(city: City, sessionToken : String?, language: String): City? {
        val result = getDetailRegion(city.placeId!!,sessionToken, language) ?: return null
        val newCityTranslation = CityTranslation(result.name, language, city)
        cityTranslationRepository.save(newCityTranslation)
        return city
    }

    fun getGoogleLocality(queryFilter: QueryFilter, city: City): Locality? {
        val autoCompleteResults = regionAutoCompleteService.getAllRegions(queryFilter,"geocode")
        if (autoCompleteResults == null || autoCompleteResults.autoCompleteHotels.isNullOrEmpty()) return null
        return getDetailRegionLocality(autoCompleteResults.autoCompleteHotels.first().placeId!!,autoCompleteResults.sessionToken,queryFilter.language ?: Constants.DEFAULT_LANGUAGE, city)
    }
    fun getDetailRegionLocality(placeId: String, sessionToken : String?, language: String,city: City): Locality? {
        val locality = localityRepository.findByPlaceId(placeId)
        if (locality != null) {
            val translation = localityTranslationRepository.findByLocality(locality.id!!,language) ?: return addLocalityTranslation(locality,sessionToken,language)
            return locality
        }
        return addLocality(placeId, sessionToken, language, city)
    }
    private fun addLocality(placeId: String, sessionToken : String?, language: String, city: City): Locality? {
        val result = getDetailRegion(placeId, sessionToken, language) ?: return null
        val locality = localityRepository.findByName(result.name!!,city.id!!,language)
        if(locality!=null)
            return locality
        val newLocality = localityRepository.save(Locality(result.photo, placeId = result.placeId))
        city.locality = city.locality?.plus(newLocality)
        cityRepository.save(city)
        val newLocalityTranslation = LocalityTranslation(result.name, language, newLocality)
        localityTranslationRepository.save(newLocalityTranslation)
        return newLocality
    }
    private fun addLocalityTranslation(locality: Locality, sessionToken : String?, language: String): Locality? {
        val result = getDetailRegion(locality.placeId!!, sessionToken, language) ?: return null
        val newLocalityTranslation = LocalityTranslation(result.name, language, locality)
        localityTranslationRepository.save(newLocalityTranslation)
        return locality
    }
    fun getGoogleCountry(queryFilter: QueryFilter): Long? {
        val autoCompleteResults = regionAutoCompleteService.getAllRegions(queryFilter,"(regions)")
        if (autoCompleteResults == null || autoCompleteResults.autoCompleteHotels.isNullOrEmpty()) return null
        val region = getDetailRegion(autoCompleteResults.autoCompleteHotels.first().placeId!!, autoCompleteResults.sessionToken, queryFilter.language?: Constants.DEFAULT_LANGUAGE)
        return region?.country?.id
    }
}