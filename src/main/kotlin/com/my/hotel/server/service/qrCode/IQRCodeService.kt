package com.my.hotel.server.service.qrCode

import com.my.hotel.server.data.model.LoyaltyPoints
import com.my.hotel.server.data.model.QRCode
import com.my.hotel.server.service.qrCode.dto.OrderDetails

interface IQRCodeService {
    fun generateQRCode(text: String): ByteArray
    fun getQRCode(userId: Long,giftId: Long): String?
    fun scanQRCode(qrCode: QRCode, loyaltyPoints: LoyaltyPoints): OrderDetails
}