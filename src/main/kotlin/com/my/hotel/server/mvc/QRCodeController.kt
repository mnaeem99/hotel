package com.my.hotel.server.mvc

import com.fasterxml.jackson.databind.ObjectMapper
import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.repository.LoyaltyPointRepository
import com.my.hotel.server.data.repository.QRCodeRepository
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.service.qrCode.QRCodeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
class QRCodeController @Autowired constructor(
    private var qrCodeService: QRCodeService,
    private val qrCodeRepository: QRCodeRepository,
    private val loyaltyPointRepository: LoyaltyPointRepository
){
    @PutMapping("/api/qr/{id}")
    fun qrCode(@PathVariable id: Long, request: HttpServletRequest, response: HttpServletResponse) {
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        val objectMapper = ObjectMapper()
        val qrCode = qrCodeRepository.findByIdOrNull(id) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND, "id")
        val loyaltyPoints = loyaltyPointRepository.findByUserAndHotel(qrCode.order.user,qrCode.hotel)
        if (qrCode.redeemed || loyaltyPoints?.loyaltyPoints!! < qrCode.order.points!!) {
            val errors: HashMap<String,String> = HashMap<String,String>()
            errors["error"] = "QR Code is Invalid"
            objectMapper.writeValue(response.outputStream, errors)
        }
        else {
            val orderDetails = qrCodeService.scanQRCode(qrCode, loyaltyPoints)
            val res: HashMap<String,String> = HashMap()
            res["orderType"] = orderDetails.orderType.toString()
            res["amount"] = orderDetails.amount.toString()
            res["points"] = orderDetails.points.toString()
            res["hotelId"] = orderDetails.hotelId.toString()
            res["userId"] = orderDetails.userId.toString()
            res["createdAt"] = orderDetails.createdAt.toString()
            res["updatedAt"] = orderDetails.updatedAt.toString()
            objectMapper.writeValue(response.outputStream, res)
        }
    }
}