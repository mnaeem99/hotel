package com.my.hotel.server.service.hotel

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.my.hotel.server.commons.SpatialUtils
import com.my.hotel.server.data.model.*
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.dto.request.QueryFilter
import com.my.hotel.server.graphql.dto.response.MyHotelDto
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.provider.translation.TranslationService
import com.my.hotel.server.service.hotel.dto.AddressComponent
import com.my.hotel.server.service.hotel.dto.Location
import com.my.hotel.server.service.hotel.dto.Photo
import com.my.hotel.server.service.hotel.dto.ResultDTO
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.StringReader
import java.time.LocalDateTime
import java.util.stream.Collectors
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory


@Service
@Slf4j
class SaveHotelService @Autowired constructor(
    private val myMyHotelRepository: MyHotelRepository,
    private val googleHotelRepository: GoogleHotelRepository,
    private val countryRepository: CountryRepository,
    private val hotelPriceLevelRepository: HotelPriceLevelRepository,
    private val cityRepository: CityRepository,
    private val localityRepository: LocalityRepository,
    private val hotelTranslationRepository: HotelTranslationRepository,
    private val countryTranslationRepository: CountryTranslationRepository,
    private val regionDetailService: RegionDetailService,
    private val imageService: ImageService,
    private val dateProvider: DateProvider,
    private val translationService: TranslationService,
    private val googleHotelAddressRepository: GoogleHotelAddressRepository,
){
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    fun getmyHotelDto(result: ResultDTO, language: String): MyHotelDto {
        val hotel = addmyHotel(result, language)
        val newhotelTranslation = addhotelTranslation(result, language,hotel)
        return MyHotelDto(
            newhotelTranslation.name,
            newhotelTranslation.address,
            hotel.phone,
            translationService.mapCountryDto(hotel.country, language),
            hotel.geolat,
            hotel.geolong,
            hotel.hotelPriceLevel,
            hotel.hotelPriceLevel,
            hotel.photoList,
            hotel.photo,
            hotel.placeId,
            hotel.expiryDate,
            hotel.status,
            hotel.googleMapUrl,
            hotel.id
        )
    }
    fun addmyHotel(result: ResultDTO, language: String): MyHotel {
        val location = result.geometry?.location
        var image: Image? = null
        var images: List<Image>? = null
        if (!result.photos.isNullOrEmpty()) {
            images = getImages(result.photos?.take(5)!!)
            image = images.first()
        }
        var status: MyHotel.BusinessStatus? = null
        if (result.businessStatus != null) {
            try {
                status = MyHotel.BusinessStatus.valueOf(result.businessStatus!!)
            } catch (e: Exception) {
                logger.error("Could not match the business status ${result.businessStatus} : ${e.message}")
            }
        }
        val hotelPrice = result.priceLevel?.toLong()?.let { hotelPriceLevelRepository.findByIdOrNull(it) }
        val expiryDate: LocalDateTime = dateProvider.getGoogleExpiryDate()
        val countries = result.addressComponents?.stream()?.map { entity -> getCountry(entity, language) }?.collect(Collectors.toList())?.filterNotNull()
        val country: Country? = if (countries.isNullOrEmpty()) getCountry(result.adrAddress, language) else countries.first()
        val city: City? = getCity(result.addressComponents, country, language, location)
        val locality: Locality? = getLocality(result.addressComponents, result.adrAddress, city, country, language, location)
        addGooglehotelAddress(result)
        val hotel = MyHotel(
            phone = result.internationalPhoneNumber ?: result.formattedPhoneNumber,
            country = country,
            city = city,
            locality = locality,
            geolat = location?.lat?.toFloat(),
            geolong = location?.lng?.toFloat(),
            hotelPriceLevel = hotelPrice,
            googlePriceLevel = hotelPrice,
            photo = image,
            photoList = images,
            placeId = result.placeId,
            point = SpatialUtils.getPoint(location?.lat?.toFloat(), location?.lng?.toFloat()),
            expiryDate = expiryDate,
            status = status,
            googleMapUrl = result.url,
            createdAt = dateProvider.getCurrentDateTime()
        )
        return myMyHotelRepository.save(hotel)
    }

    private fun getImages(photos: List<Photo>): List<Image> {
        return photos.stream().map { photo -> imageService.toImage(photo) }.filter { img -> img != null }.map { img -> img!! }.collect(Collectors.toList())
    }

    fun addhotelTranslation(result: ResultDTO, language: String, hotel: MyHotel): HotelTranslation {
        val newHotelTranslation = HotelTranslation(
            name = result.name,
            address = result.formattedAddress,
            language = language,
            hotel = hotel
        )
        return hotelTranslationRepository.save(newHotelTranslation)
    }
    fun updateDetailHotel(hotel: MyHotel, result: ResultDTO, language: String): MyHotelDto {
        val hotelTranslation = hotelTranslationRepository.findByHotel(hotel.id!!, language)
        if (hotelTranslation==null) {
            addhotelTranslation(result,language,hotel)
        }
        val myHotel = updatemyHotel(result, hotel, language)
        return MyHotelDto(
            result.name,
            result.formattedAddress,
            myHotel.phone,
            translationService.mapCountryDto(myHotel.country, language),
            myHotel.geolat,
            myHotel.geolong,
            myHotel.hotelPriceLevel,
            myHotel.googlePriceLevel,
            myHotel.photoList,
            myHotel.photo,
            myHotel.placeId,
            myHotel.expiryDate,
            myHotel.status,
            myHotel.googleMapUrl,
            myHotel.id
        )
    }
    fun updatemyHotel(result: ResultDTO, hotel: MyHotel, language: String): MyHotel {
        val location = result.geometry?.location
        if (hotel.country==null){
            val countries = result.addressComponents?.stream()?.map { entity -> getCountry(entity, language) }?.collect(Collectors.toList())?.filterNotNull()
            val country: Country? = if (countries.isNullOrEmpty()) getCountry(result.adrAddress, language) else countries.first()
            hotel.country = country
        }
        if (hotel.city == null){
            val city: City? = getCity(result.addressComponents, hotel.country, language, location)
            hotel.city = city
        }
        if (hotel.locality == null){
            val locality: Locality? = getLocality(result.addressComponents, result.adrAddress, hotel.city, hotel.country, language, location)
            hotel.locality = locality
        }
        if (result.addressComponents!=null){
            addGooglehotelAddress(result)
        }
        hotel.geolat = location?.lat?.toFloat()
        hotel.geolong = location?.lng?.toFloat()
        hotel.googleMapUrl = result.url
        hotel.point = SpatialUtils.getPoint(location?.lat?.toFloat(), location?.lng?.toFloat())
        hotel.expiryDate = dateProvider.getGoogleExpiryDate()
        return myMyHotelRepository.save(hotel)
    }

    fun getGoogleHotel(result: ResultDTO, language: String): GoogleHotel {
        val existhotel = googleHotelRepository.findByIdOrNull(result.placeId)
        if(existhotel==null)
            return addGoogleHotel(result, language)
        else if(existhotel.expiryDate!! < dateProvider.getCurrentDateTime())
            return updateGoogleHotel(existhotel, result, language)
        return existhotel
    }
    fun addGoogleHotel(result: ResultDTO, language: String): GoogleHotel {
        val location = result.geometry?.location
        var image: Image? = null
        if (result.photos!=null && result.photos!!.isNotEmpty()){
            image = imageService.toImage(result.photos!!.first())
        }
        val hotelPrice = result.priceLevel?.toLong()?.let { hotelPriceLevelRepository.findByIdOrNull(it) }
        val expiryDate: LocalDateTime = dateProvider.getGoogleExpiryDate()
        var status: MyHotel.BusinessStatus? = null
        if (result.businessStatus!=null){
            try{
                status = MyHotel.BusinessStatus.valueOf(result.businessStatus!!)
            }catch (e: Exception){
                logger.error("Could not match the business status ${result.businessStatus} : ${e.message}")
            }
        }
        val hotel = GoogleHotel(
            name = result.name,
            address = result.formattedAddress ?: result.vicinity,
            language = language,
            phone = result.formattedPhoneNumber,
            geolat = location?.lat?.toFloat(),
            geolong = location?.lng?.toFloat(),
            hotelPriceLevel = hotelPrice,
            photo = image,
            placeId = result.placeId,
            point = SpatialUtils.getPoint(location?.lat?.toFloat(), location?.lng?.toFloat()),
            expiryDate = expiryDate,
            status = status,
            googleMapUrl = result.url,
            createdAt = dateProvider.getCurrentDateTime()
        )
        return googleHotelRepository.save(hotel)
    }
    fun updateGoogleHotel(hotel: GoogleHotel, result: ResultDTO, language: String): GoogleHotel {
        val location = result.geometry?.location
        hotel.name = result.name
        if (result.formattedAddress!=null) {
            hotel.address = result.formattedAddress
        }
        hotel.language = language
        if (result.businessStatus!=null){
            try{
                hotel.status = MyHotel.BusinessStatus.valueOf(result.businessStatus!!)
            }catch (e: Exception){
                logger.error("Could not match the business status ${result.businessStatus} : ${e.message}")
            }
        }
        if (hotel.photo == null && result.photos!=null && result.photos!!.isNotEmpty()){
            hotel.photo = imageService.toImage(result.photos!!.first())
        }
        hotel.geolat = location?.lat?.toFloat()
        hotel.geolong = location?.lng?.toFloat()
        hotel.googleMapUrl = result.url
        hotel.point = SpatialUtils.getPoint(location?.lat?.toFloat(),location?.lng?.toFloat())
        hotel.expiryDate = dateProvider.getGoogleExpiryDate()
        return googleHotelRepository.save(hotel)
    }

    fun getCountry(addressComponent: AddressComponent, language: String): Country? {
        if (addressComponent.types?.contains("country") == false) {
            return null
        }
        val code = addressComponent.shortName
        val name = addressComponent.longName
        val country = countryRepository.findByCode(code.toString())
        if (country!=null) {
            val countryTranslation = countryTranslationRepository.findByCountry(country.id!!, language)
            if (countryTranslation == null){
                val newCountryTranslation = CountryTranslation(name, language, country)
                countryTranslationRepository.save(newCountryTranslation)
            }
            return country
        }
        val newCountry = countryRepository.save(Country(code.toString()))
        val newCountryTranslation = CountryTranslation(name, language, newCountry)
        countryTranslationRepository.save(newCountryTranslation)
        return newCountry
    }
    fun getCountry(adrAddress: String?, language: String): Country? {
        val countryName = getValueSpan(adrAddress, "/span[@class='country-name']") ?: getValueSpanByReg(adrAddress,"<span class=\"country-name\">(.*?)</span>") ?: return null
        return countryRepository.findByName(countryName, language)
    }
    fun getCity(addressComponents: List<AddressComponent>?, country: Country?, language: String, location: Location?): City? {
        if (addressComponents.isNullOrEmpty() || country == null)
            return null
        val configs = country.addressConfig.filter { config -> config.level == CountryAddressConfig.AddressLevel.LEVEL1 }
        if (configs.isNotEmpty()) {
            val priorityConfigs = configs.sortedBy { it.priority }
            for (config in priorityConfigs) {
                val matchingComponent = addressComponents.find { it.types?.contains(config.type) == true }
                if (matchingComponent != null) {
                    return createCity(matchingComponent.longName!!, country, language, location)
                }
            }
        }
        var administrativeAreaLevel1: String? = null
        var administrativeAreaLevel2: String? = null
        for (addressComponent in addressComponents) {
            when {
                addressComponent.types?.contains("administrative_area_level_1") == true -> administrativeAreaLevel1 = addressComponent.longName
                addressComponent.types?.contains("administrative_area_level_2") == true -> administrativeAreaLevel2 = addressComponent.longName
            }
        }
        if (administrativeAreaLevel2!=null){
            return createCity(administrativeAreaLevel2, country, language, location)
        }
        if (administrativeAreaLevel1!=null){
            return createCity(administrativeAreaLevel1, country, language, location)
        }
        return null
    }

    private fun createCity(name: String, country: Country, language: String, location: Location?): City? {
        val city = cityRepository.findByName(name, country.id!!, language)
        if (city != null)
            return city
        val query = QueryFilter(name, location?.lat, location?.lng, language)
        return regionDetailService.getGoogleCity(query, country)
    }
    fun getValueSpan(adrAddress: String?, expression: String): String? {
        try {
            var addressList: MutableList<String>? = adrAddress?.split(",".toRegex())?.toMutableList()
            addressList = addressList?.filter { str -> str.contains("<span") }?.stream()?.collect(Collectors.toList())
            if (!addressList.isNullOrEmpty()) {
                for (address in addressList) {
                    val dbFactory = DocumentBuilderFactory.newInstance()
                    val dBuilder = dbFactory.newDocumentBuilder()
                    val xmlInput = InputSource(StringReader(address))
                    val doc = dBuilder.parse(xmlInput)
                    val xPath = XPathFactory.newInstance().newXPath()
                    val nodeList = xPath.compile(expression).evaluate(doc, XPathConstants.NODESET) as NodeList
                    if (nodeList.length > 0) {
                        return nodeList.item(0).textContent
                    }
                }
            }
            return null
        }catch (e: Exception){
            logger.error(e.message)
            return null
        }
    }
    fun getValueSpanByReg(adrAddress: String?, expression: String): String? {
        return try {
            val expressionPattern = Regex(expression)
            val expressionMatch = adrAddress?.let { expressionPattern.find(it) }
            expressionMatch?.groupValues?.getOrNull(1)
        }catch (e: Exception){
            logger.error(e.message)
            null
        }
    }
    fun getLocality(addressComponents: List<AddressComponent>?, adrAddress: String?, city: City?,country: Country?, language: String, location: Location?): Locality? {
        if (addressComponents.isNullOrEmpty() || city == null)
            return null
        if (city.addressConfig.isNotEmpty()) {
            val priorityConfigs = city.addressConfig.sortedBy { it.priority }
            for (config in priorityConfigs) {
                val matchingComponent = addressComponents.find { it.types?.contains(config.type) == true }
                if (matchingComponent != null) {
                    return createLocality(matchingComponent.longName!!, city, language, location)
                }
            }
        }
        val countryBasedConfig = country?.addressConfig?.filter { config -> config.level == CountryAddressConfig.AddressLevel.LEVEL2 }
        if (!countryBasedConfig.isNullOrEmpty()) {
            val priorityConfigs = countryBasedConfig.sortedBy { it.priority }
            for (config in priorityConfigs) {
                val matchingComponent = addressComponents.find { it.types?.contains(config.type) == true }
                if (matchingComponent != null) {
                    return createLocality(matchingComponent.longName!!, city, language, location)
                }
            }
        }
        var locality: String? = null
        var neighborhood: String? = null
        for (addressComponent in addressComponents){
            when {
                addressComponent.types?.contains("locality") == true -> locality = addressComponent.longName
                addressComponent.types?.contains("neighborhood") == true -> neighborhood = addressComponent.longName
            }
        }
        if (neighborhood!=null){
            return createLocality(neighborhood, city, language, location)
        }

        if (locality==null && adrAddress!=null){
            locality = getValueSpan(adrAddress, "/span[@class='locality']") ?: getValueSpanByReg(adrAddress,"<span class=\"locality\">(.*?)</span>")
        }
        if (locality!=null){
            return createLocality(locality, city, language, location)
        }
        return null
    }
    fun createLocality(name: String, city: City, language: String, location: Location?): Locality? {
        val locality = localityRepository.findByName(name, city.id!!, language)
        if (locality != null)
            return locality
        val query = QueryFilter(name, location?.lat, location?.lng, language)
        return regionDetailService.getGoogleLocality(query, city)
    }
    fun addGooglehotelAddress(result: ResultDTO) {
        val googleHotelAddress = GoogleHotelAddress(null, result.adrAddress, dateProvider.getCurrentDateTime(), result.placeId!!)
        if (!result.addressComponents.isNullOrEmpty()){
            val jsonAddress = Gson().toJson(result.addressComponents)
            googleHotelAddress.addressComponents = jsonAddress
        }
        googleHotelAddressRepository.save(googleHotelAddress)
    }

    fun updatehotelAddress(countryId: Long?, cityId: Long?): Boolean {
        if (countryId!=null){
            updatehotelAddressByCountry(countryId)
        }
        if (cityId!=null){
            updatehotelAddressByCity(cityId)
        }
        return true
    }
    fun updatehotelAddressByCountry(countryId: Long){
        val hotels = myMyHotelRepository.findByCountry(countryId)
        for (hotel in hotels){
            val googleAddress = googleHotelAddressRepository.findByIdOrNull(hotel.placeId)
            if (googleAddress != null){
                val addressComponents = readAddressComponents(googleAddress.addressComponents!!)
                val location = Location()
                location.lat = hotel.geolat?.toDouble()!!
                location.lng = hotel.geolong?.toDouble()!!
                val languages = hotelTranslationRepository.findLanguages(hotel.id!!)
                var city: City? = null
                var locality: Locality? = null
                if (languages != null) {
                    for (language in languages){
                        city = getCity(addressComponents, hotel.country, language, location)
                        locality = getLocality(addressComponents, googleAddress.adrAddress, city, hotel.country, language, location)
                    }
                }
                if (city!=null){
                    hotel.city = city
                }
                if (locality!=null){
                    hotel.locality = locality
                }
                myMyHotelRepository.save(hotel)
            }
        }
    }
    fun updatehotelAddressByCity(cityId: Long){
        val hotels = myMyHotelRepository.findByCity(cityId)
        for (hotel in hotels){
            val googleAddress = googleHotelAddressRepository.findByIdOrNull(hotel.placeId)
            if (googleAddress?.addressComponents != null){
                val addressComponents = readAddressComponents(googleAddress.addressComponents!!)
                val location = Location()
                location.lat = hotel.geolat?.toDouble()!!
                location.lng = hotel.geolong?.toDouble()!!
                val languages = hotelTranslationRepository.findLanguages(hotel.id!!)
                var locality: Locality? = null
                if (languages != null) {
                    for (language in languages){
                        locality = getLocality(addressComponents, googleAddress.adrAddress, hotel.city, hotel.country, language, location)
                    }
                }
                if (locality!=null){
                    hotel.locality = locality
                }
                myMyHotelRepository.save(hotel)
            }
        }
    }
    private fun readAddressComponents(json: String): List<AddressComponent> {
        val mapper = ObjectMapper().apply {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
        return mapper.readValue(json, object : TypeReference<List<AddressComponent>>() {})
    }

}
