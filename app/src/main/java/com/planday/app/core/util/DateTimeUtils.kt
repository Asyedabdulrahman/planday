package com.planday.app.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtils {
    val APP_TIMEZONE: ZoneId = ZoneId.of("Asia/Kolkata")

    fun now(): ZonedDateTime = ZonedDateTime.now(APP_TIMEZONE)

    fun today(): LocalDate = LocalDate.now(APP_TIMEZONE)

    fun currentTime(): LocalTime = LocalTime.now(APP_TIMEZONE)

    fun toUtcInstant(date: LocalDate, time: LocalTime): Instant {
        val zonedDateTime = ZonedDateTime.of(date, time, APP_TIMEZONE)
        return zonedDateTime.toInstant()
    }

    fun toZonedDateTime(instant: Instant): ZonedDateTime {
        return instant.atZone(APP_TIMEZONE)
    }

    fun formatPrettyDate(date: LocalDate): String {
        val current = today()
        return when (date) {
            current -> "Today"
            current.plusDays(1) -> "Tomorrow"
            current.minusDays(1) -> "Yesterday"
            else -> date.format(DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH))
        }
    }

    fun formatTime(time: LocalTime): String {
        return time.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH))
    }
}
