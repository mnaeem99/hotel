package com.my.hotel.server.provider.dateProvider

import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Component
class DateProvider : IDateProvider {

    override fun getCurrentDateTime(): LocalDateTime {
        return LocalDateTime.now()
    }
    override fun getDateTimeZone(timeZoneId: String?): LocalDateTime {
        if (timeZoneId==null){
            return LocalDateTime.now()
        }
        return LocalDateTime.now(ZoneId.of(timeZoneId))
    }

    override fun getGoogleExpiryDate(): LocalDateTime {
        return LocalDateTime.now().plusMonths(3)
    }
    override fun getPreviousDate(): LocalDateTime {
        return LocalDateTime.now().minusHours(24)
    }

    override fun getCurrentDate(): LocalDate {
        return LocalDate.now()
    }
}