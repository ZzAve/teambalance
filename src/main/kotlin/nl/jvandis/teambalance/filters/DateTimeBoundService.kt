package nl.jvandis.teambalance.filters

import org.springframework.beans.factory.annotation.Value
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZonedDateTime

class InvalidDateTimeException(msg: String) : RuntimeException(msg)

@Service
class DateTimeBoundService(
    @Value("\${app.datetime-limit}") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) private val dateTimeLimit: ZonedDateTime
) {

    fun ensureDateTimeLimit(since: LocalDateTime) {
        if (dateTimeLimit.toLocalDateTime().isAfter(since)) {
            throw InvalidDateTimeException("The date $since is not allowed. It's too early")
        }
    }
}
