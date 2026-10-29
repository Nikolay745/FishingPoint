package com.fishingpoints.domain.utils

import android.annotation.SuppressLint
import com.fishingpoints.data.local.entity.LocationEntity
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.Month
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.roundToLong
import kotlin.math.sin
import kotlin.math.tan

@Singleton
class FishingCalendar @Inject constructor(
    private val clock: Clock
) {

    /**
     * ============================================================
     * CURRENT TIME
     * ============================================================
     *
     * Получает текущее время именно в часовом поясе локации.
     */
    fun now(location: LocationEntity): ZonedDateTime {
        return ZonedDateTime.now(clock)
            .withZoneSameInstant(location.zoneId)
    }

    /**
     * ============================================================
     * DAY INFO
     * ============================================================
     */
    fun getDayInfo(
        location: LocationEntity,
        date: LocalDate = now(location).toLocalDate()
    ): DayInfo {

        val currentDateTime = now(location)
        val currentLocalTime =
            if (currentDateTime.toLocalDate() == date) {
                currentDateTime.toLocalTime()
            } else {
                LocalTime.NOON
            }

        val solarTimes = calculateSolarTimes(
            latitude = location.lat,
            longitude = location.lon,
            date = date,
            zoneId = location.zoneId
        )

        return DayInfo(
            date = date,
            sunrise = solarTimes.sunrise,
            sunset = solarTimes.sunset,
            solarNoon = solarTimes.solarNoon,
            currentLocalTime = currentLocalTime,
            dayLength = solarTimes.dayLength,
            season = getSeason(date)
        )
    }

    /**
     * ============================================================
     * TIME OF DAY
     * ============================================================
     */
    fun getTimeOfDay(
        location: LocationEntity,
        dateTime: ZonedDateTime = now(location)
    ): TimeOfDay {

        val localDateTime =
            dateTime.withZoneSameInstant(location.zoneId)

        val date = localDateTime.toLocalDate()
        val currentTime = localDateTime.toLocalTime()

        val dayInfo = getDayInfo(
            location = location,
            date = date
        )

        return determineTimeOfDay(
            currentTime = currentTime,
            dayInfo = dayInfo
        )
    }

    /**
     * ============================================================
     * TIME OF DAY CALCULATION
     * ============================================================
     */

    private fun determineTimeOfDay(
        currentTime: LocalTime,
        dayInfo: DayInfo
    ): TimeOfDay {

        val current = currentTime.toSecondOfDay()

        val sunrise = dayInfo.sunrise.toSecondOfDay()
        val sunset = dayInfo.sunset.toSecondOfDay()
        val solarNoon = dayInfo.solarNoon.toSecondOfDay()

        // Защита от некорректных данных.
        if (sunrise == sunset) {
            return TimeOfDay.NIGHT
        }

        // Рассвет заканчивается не позднее чем через 2 часа после восхода.
        val dawnEnd = minOf(
            sunrise + 2 * 3600,
            sunset
        )

        // Утренний период не должен начинаться раньше окончания рассвета.
        val morningEnd = maxOf(
            dawnEnd,
            solarNoon - 2 * 3600
        ).coerceAtMost(sunset)

        // Дневной период.
        val dayEnd = maxOf(
            morningEnd,
            solarNoon + 2 * 3600
        ).coerceAtMost(sunset)

        // Вечерний период начинается не раньше окончания дня.
        val eveningStart = maxOf(
            dayEnd,
            sunset - 2 * 3600
        ).coerceAtMost(sunset)

        // Конец сумерек.
        val duskEnd = sunset + 2 * 3600

        return when {
            current < sunrise ->
                TimeOfDay.NIGHT

            current < dawnEnd ->
                TimeOfDay.DAWN

            current < morningEnd ->
                TimeOfDay.MORNING

            current < dayEnd ->
                TimeOfDay.DAY

            current < eveningStart ->
                TimeOfDay.AFTERNOON

            current < sunset ->
                TimeOfDay.EVENING

            current < duskEnd ->
                TimeOfDay.DUSK

            else ->
                TimeOfDay.NIGHT
        }
    }

    /**
     * ============================================================
     * SEASON
     * ============================================================
     */
    fun getSeason(date: LocalDate): Season {

        return when (date.month) {

            Month.MARCH,
            Month.APRIL,
            Month.MAY -> {
                Season.SPRING
            }

            Month.JUNE,
            Month.JULY,
            Month.AUGUST -> {
                Season.SUMMER
            }

            Month.SEPTEMBER,
            Month.OCTOBER,
            Month.NOVEMBER -> {
                Season.AUTUMN
            }

            else -> {
                Season.WINTER
            }
        }
    }

    /**
     * ============================================================
     * SUNRISE
     * ============================================================
     *
     * Сначала считаем солнечное время.
     * Затем переводим его в ZoneId конкретной локации.
     */
    private fun calculateSunrise(
        latitude: Double,
        longitude: Double,
        date: LocalDate,
        zoneId: ZoneId
    ): LocalTime {

        val dayOfYear = date.dayOfYear

        val declination =
            23.44 *
                    sin(
                        radians(
                            360.0 / 365.0 *
                                    (dayOfYear - 81)
                        )
                    )

        val latRad = radians(latitude)
        val decRad = radians(declination)

        val cosHourAngle =
            -tan(latRad) * tan(decRad)

        /**
         * Полярный день.
         */
        if (cosHourAngle < -1.0) {
            return LocalTime.MIDNIGHT
        }

        /**
         * Полярная ночь.
         */
        if (cosHourAngle > 1.0) {
            return LocalTime.MIDNIGHT
        }

        val hourAngle =
            acos(cosHourAngle).toDegrees() / 15.0

        /**
         * Солнечное время UTC.
         */
        val utcHour =
            12.0 -
                    hourAngle -
                    longitude / 15.0

        val instant =
            localSolarHourToInstant(
                date = date,
                solarHourUtc = utcHour
            )

        return instant
            .atZone(zoneId)
            .toLocalTime()
    }

    /**
     * ============================================================
     * SUNSET
     * ============================================================
     */
    private fun calculateSunset(
        latitude: Double,
        longitude: Double,
        date: LocalDate,
        zoneId: ZoneId
    ): LocalTime {

        val dayLengthHours =
            calculateDayLengthHours(
                latitude = latitude,
                date = date
            )

        if (dayLengthHours <= 0.0) {
            return LocalTime.MIDNIGHT
        }

        if (dayLengthHours >= 24.0) {
            return LocalTime.MIDNIGHT
        }

        val sunrise =
            calculateSunrise(
                latitude = latitude,
                longitude = longitude,
                date = date,
                zoneId = zoneId
            )

        return sunrise.plusSeconds(
            (dayLengthHours * 3600.0).toLong()
        )
    }

    /**
     * ============================================================
     * SOLAR NOON
     * ============================================================
     */
    private fun calculateSolarNoon(
        sunrise: LocalTime,
        sunset: LocalTime
    ): LocalTime {

        val sunriseSeconds = sunrise.toSecondOfDay()
        var sunsetSeconds = sunset.toSecondOfDay()

        // Если закат оказался после полуночи,
        // считаем его временем следующего дня.
        if (sunsetSeconds < sunriseSeconds) {
            sunsetSeconds += 24 * 60 * 60
        }

        val seconds = (sunriseSeconds + sunsetSeconds) / 2

        return LocalTime.ofSecondOfDay(
            (seconds % (24 * 60 * 60)).toLong()
        )
    }


    /**
     * ============================================================
     * DAY LENGTH
     * ============================================================
     */

    private fun calculateDayLength(
        sunrise: LocalTime,
        sunset: LocalTime
    ): Duration {

        val seconds =
            sunset.toSecondOfDay() -
                    sunrise.toSecondOfDay()

        val normalizedSeconds =
            if (seconds < 0) {
                seconds + 24 * 60 * 60
            } else {
                seconds
            }

        return Duration.ofSeconds(
            normalizedSeconds.toLong()
        )
    }

    private fun calculateDayLengthHours(
        latitude: Double,
        date: LocalDate
    ): Double {

        val dayOfYear = date.dayOfYear

        val declination =
            23.44 *
                    sin(
                        radians(
                            360.0 / 365.0 *
                                    (dayOfYear - 81)
                        )
                    )

        val latRad = radians(latitude)
        val decRad = radians(declination)

        val cosHourAngle =
            -tan(latRad) * tan(decRad)

        return when {

            cosHourAngle < -1.0 -> {
                24.0
            }

            cosHourAngle > 1.0 -> {
                0.0
            }

            else -> {
                2.0 *
                        acos(cosHourAngle)
                            .toDegrees() /
                        15.0
            }
        }
    }

    /**
     * ============================================================
     * SOLAR TIME -> INSTANT
     * ============================================================
     */
    private fun localSolarHourToInstant(
        date: LocalDate,
        solarHourUtc: Double
    ): Instant {

        val seconds =
            (solarHourUtc * 3600.0).toLong()

        return date
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .plusSeconds(seconds)
    }

    private fun radians(
        degrees: Double
    ): Double {
        return degrees * PI / 180.0
    }

    private fun Double.toDegrees(): Double {
        return this * 180.0 / PI
    }
}

/**
 * Класс для хранения информации о световом дне
 */
data class DayInfo(
    val date: LocalDate,
    val sunrise: LocalTime,
    val sunset: LocalTime,
    val solarNoon: LocalTime,
    val currentLocalTime: LocalTime,
    val dayLength: Duration,
    val season: Season
) {
    @SuppressLint("DefaultLocale")
    fun getDayLengthFormatted(): String {
        val hours = dayLength.toHours()
        val minutes = dayLength.toMinutes() % 60
        return "${hours}ч ${minutes}мин"
    }
}

private data class SolarTimes(
    val sunrise: LocalTime,
    val sunset: LocalTime,
    val solarNoon: LocalTime,
    val dayLength: Duration
)

/**
 * Рассчитывает восход, закат, солнечный полдень
 * и продолжительность светового дня.
 */
private fun calculateSolarTimes(
    latitude: Double,
    longitude: Double,
    date: LocalDate,
    zoneId: ZoneId
): SolarTimes {

    val dayOfYear = date.dayOfYear

    // Дробный год в радианах.
    val gamma = 2.0 * PI / 365.0 *
            (dayOfYear - 1)

    // Уравнение времени, минуты.
    val equationOfTime = 229.18 * (
            0.000075 +
                    0.001868 * cos(gamma) -
                    0.032077 * sin(gamma) -
                    0.014615 * cos(2.0 * gamma) -
                    0.040849 * sin(2.0 * gamma)
            )

    // Склонение Солнца, радианы.
    val declination =
        0.006918 -
                0.399912 * cos(gamma) +
                0.070257 * sin(gamma) -
                0.006758 * cos(2.0 * gamma) +
                0.000907 * sin(2.0 * gamma) -
                0.002697 * cos(3.0 * gamma) +
                0.001480 * sin(3.0 * gamma)

    val latitudeRad = Math.toRadians(latitude)

    // Зенитный угол 90.833° учитывает
    // размер солнечного диска и рефракцию.
    val zenithRad = Math.toRadians(90.833)

    val denominator =
        cos(latitudeRad) * cos(declination)

    val cosHourAngle =
        (
                cos(zenithRad) / denominator -
                        tan(latitudeRad) * tan(declination)
                )

    // Для средней полосы эти условия обычно не возникают.
    // Тем не менее обработаем их корректно.
    if (cosHourAngle >= 1.0) {
        return SolarTimes(
            sunrise = LocalTime.MIDNIGHT,
            sunset = LocalTime.MIDNIGHT,
            solarNoon = LocalTime.NOON,
            dayLength = Duration.ZERO
        )
    }

    if (cosHourAngle <= -1.0) {
        return SolarTimes(
            sunrise = LocalTime.MIDNIGHT,
            sunset = LocalTime.MIDNIGHT,
            solarNoon = LocalTime.NOON,
            dayLength = Duration.ofHours(24)
        )
    }

    val hourAngleDegrees = Math.toDegrees(
        acos(cosHourAngle)
    )

    // Время событий в минутах от начала суток UTC.
    // Долгота: восток положительный, запад отрицательный.
    val sunriseUtcMinutes =
        720.0 -
                4.0 * (longitude + hourAngleDegrees) -
                equationOfTime

    val sunsetUtcMinutes =
        720.0 -
                4.0 * (longitude - hourAngleDegrees) -
                equationOfTime

    val solarNoonUtcMinutes =
        720.0 -
                4.0 * longitude -
                equationOfTime

    val utcMidnight = date
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()

    fun toLocalTime(utcMinutes: Double): LocalTime {
        val instant = utcMidnight.plusSeconds(
            (utcMinutes * 60.0).roundToLong()
        )

        return instant
            .atZone(zoneId)
            .toLocalTime()
    }

    val sunrise = toLocalTime(sunriseUtcMinutes)
    val sunset = toLocalTime(sunsetUtcMinutes)
    val solarNoon = toLocalTime(solarNoonUtcMinutes)

    val dayLength = Duration.ofSeconds(
        ((sunsetUtcMinutes - sunriseUtcMinutes) * 60.0)
            .roundToLong()
    )

    return SolarTimes(
        sunrise = sunrise,
        sunset = sunset,
        solarNoon = solarNoon,
        dayLength = dayLength
    )
}