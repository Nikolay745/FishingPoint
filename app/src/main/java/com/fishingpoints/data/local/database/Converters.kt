package com.fishingpoints.data.local.database

import androidx.room.TypeConverter
import com.fishingpoints.data.local.enums.BaitType
import com.fishingpoints.data.local.enums.FeedingActivity
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TackleType
import com.fishingpoints.data.local.enums.TimeOfDay
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class Converters {

    @TypeConverter
    fun fromFishIds(ids: List<Long>): String {
        return ids.joinToString(",")
    }

    @TypeConverter
    fun toFishIds(value: String): List<Long> {
        return value
            .takeIf { it.isNotBlank() }
            ?.split(",")
            ?.mapNotNull { it.toLongOrNull() }
            ?: emptyList()
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Season
    // Конвертирует Enum в String (название константы) для сохранения в БД
    @TypeConverter
    fun fromSeason(season: Season?): String? {
        return season?.name // Возвращает, например, "SPRING"
    }

    // Конвертирует String из БД обратно в Enum
    @TypeConverter
    fun seasonToString(value: String?): Season? {
        return value?.let { Season.valueOf(it) } // Находит Season.valueOf("SPRING")
    }


    // TimeOfDay
    @TypeConverter
    fun fromTimeOfDay(value: String): TimeOfDay {
        return TimeOfDay.valueOf(value)
    }

    @TypeConverter
    fun timeOfDayToString(timeOfDay: TimeOfDay): String {
        return timeOfDay.name
    }

    // FeedingActivity

    @TypeConverter
    fun fromFeedingActivity(value: String): FeedingActivity {
        return FeedingActivity.valueOf(value)
    }

    @TypeConverter
    fun feedingActivityToString(activity: FeedingActivity): String {
        return activity.name
    }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let(LocalDate::parse)
    }

    // BaitType

    @TypeConverter
    fun fromBaitType(value: String): BaitType {
        return BaitType.valueOf(value)
    }

    @TypeConverter
    fun baitTypeToString(type: BaitType): String {
        return type.name
    }

    // TackleType

    @TypeConverter
    fun fromTackleType(type: TackleType): String {
        return type.name
    }

    @TypeConverter
    fun toTackleType(value: String): TackleType {
        return TackleType.valueOf(value)
    }

    @TypeConverter
    fun fromZoneId(zoneId: ZoneId?): String? {
        return zoneId?.id
    }

    @TypeConverter
    fun toZoneId(value: String?): ZoneId? {
        return value?.let(ZoneId::of)
    }

}