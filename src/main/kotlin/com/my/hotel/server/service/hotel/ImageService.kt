package com.my.hotel.server.service.hotel

import com.my.hotel.server.data.model.Image
import com.my.hotel.server.data.repository.ImageRepository
import com.my.hotel.server.service.aws.AWSService
import com.my.hotel.server.service.hotel.dto.Photo
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.InputStream
import java.net.URL

@Service
@Slf4j
class ImageService @Autowired constructor(
    private val imageRepository: ImageRepository,
    private val awsService: AWSService,
    @Value("\${aws.secrets.googleMapsKey}")
    private var secretsGoogleMapsKey:String,
){
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    fun toImage(photo: Photo): Image? {
        val image: Image?
        val url = getPhotoURL(photo)
        try {
            val con = url.openConnection()
            con.connect()
            val read: InputStream = con.getInputStream()
            val imageURL = con.url
            read.close()
            image = Image(imageURL,null)
        }catch (e: Exception){
            logger.error(e.message)
            return null
        }
        return imageRepository.save(image)
    }
    fun getPhotoURL(photo: Photo): URL {
        val secretsJson = awsService.getValue(secretsGoogleMapsKey)
        val googleMapsKey = secretsJson?.get( "googleMapsKey")?.asText()
        return URL("https://maps.googleapis.com/maps/api/place/photo?maxwidth=${photo.width}&maxheight=${photo.height}&photo_reference=${photo.photoReference}&key=${googleMapsKey}")
    }
}