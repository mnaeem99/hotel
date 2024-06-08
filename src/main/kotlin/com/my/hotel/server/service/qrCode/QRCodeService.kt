package com.my.hotel.server.service.qrCode

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.my.hotel.server.commons.Constants
import com.my.hotel.server.data.model.LoyaltyPoints
import com.my.hotel.server.data.model.Order
import com.my.hotel.server.data.model.PointsHistory
import com.my.hotel.server.data.model.QRCode
import com.my.hotel.server.data.repository.*
import com.my.hotel.server.graphql.error.NotFoundCustomException
import com.my.hotel.server.graphql.error.ValidationErrorCustomException
import com.my.hotel.server.provider.dateProvider.DateProvider
import com.my.hotel.server.service.qrCode.dto.OrderDetails
import lombok.extern.slf4j.Slf4j
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated
import java.io.ByteArrayOutputStream
import java.util.*
import javax.servlet.http.HttpServletRequest


@Service
@Validated
@Slf4j
class QRCodeService @Autowired constructor(
    private val loyaltyPointRepository: LoyaltyPointRepository,
    private val userRepository: UserRepository,
    private val giftRepository: GiftRepository,
    private val qrCodeRepository: QRCodeRepository,
    private val orderRepository: OrderRepository,
    private val request: HttpServletRequest,
    private val pointsHistoryRepository: PointsHistoryRepository,
    private val dateProvider: DateProvider
) : IQRCodeService {
    val logger: Logger = LoggerFactory.getLogger(this.javaClass.name)
    override fun generateQRCode(text: String): ByteArray {
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200)
        val pngOutputStream = ByteArrayOutputStream()
        val con = MatrixToImageConfig(-0xfffffe, -0x3fbf)
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream, con)
        return pngOutputStream.toByteArray()
    }
    override fun getQRCode(userId: Long, giftId: Long): String? {
        logger.info("GetQRCode Called")
        val gift = giftRepository.findByIdOrNull(giftId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"giftId")
        val user = userRepository.findByIdOrNull(userId) ?: throw NotFoundCustomException(Constants.RECORD_NOT_FOUND,"userId")
        val loyaltyPoints = loyaltyPointRepository.findByUserAndHotel(user,gift.hotel)
        if (loyaltyPoints?.loyaltyPoints!! >= gift.points) {
            val order = orderRepository.save(Order(0.0,gift.points, gift.hotel, user, Order.OrderType.REDEMPTION, dateProvider.getCurrentDate(), dateProvider.getCurrentDate()))
            val qrCode = qrCodeRepository.save(QRCode(false,gift.hotel, order))
            val rootUrl = request.requestURL.toString()
            val baseUrl = rootUrl.replace("/api/graphql","")
            val url = "$baseUrl/api/qr/${qrCode.id}"
            logger.info("${user.firstName} ${user.lastName} generate new QRCode ${qrCode.id}")
            return Base64.getEncoder().encodeToString(generateQRCode(url))
        }
        val requiredPoints = loyaltyPoints.loyaltyPoints!! - gift.points
        throw ValidationErrorCustomException("$requiredPoints points left to get the gift")
    }
    override fun scanQRCode(qrCode: QRCode, loyaltyPoints: LoyaltyPoints): OrderDetails {
        logger.info("ScanQRCode Called")
        loyaltyPoints.loyaltyPoints = loyaltyPoints.loyaltyPoints?.minus(qrCode.order.points!!)
        loyaltyPointRepository.save(loyaltyPoints)
        val pointsHistory = PointsHistory(loyaltyPoints.user,loyaltyPoints.hotel, qrCode.order.points)
        pointsHistoryRepository.save(pointsHistory)
        qrCode.redeemed = true
        qrCodeRepository.save(qrCode)
        logger.info("QRCode ${qrCode.id} is redeemed")
        return OrderDetails(qrCode.order.amount, qrCode.order.points, qrCode.hotel.id!!, qrCode.order.user.id!!, qrCode.order.orderType, qrCode.order.createdAt, qrCode.order.updatedAt)
    }
}