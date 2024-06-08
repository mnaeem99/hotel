package com.my.hotel.server.provider.dateProvider

import java.time.LocalDate
import java.time.LocalDateTime

interface IDateProvider {
    fun getCurrentDateTime(): LocalDateTime
    fun getDateTimeZone(timeZoneId: String?): LocalDateTime
    fun getGoogleExpiryDate(): LocalDateTime
    fun getPreviousDate(): LocalDateTime
    fun getCurrentDate(): LocalDate
}