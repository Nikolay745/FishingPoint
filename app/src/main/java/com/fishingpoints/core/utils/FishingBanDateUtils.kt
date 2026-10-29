package com.fishingpoints.core.utils

import com.fishingpoints.data.local.entity.FishingBan
import java.time.LocalDate
import java.time.MonthDay

fun FishingBan.isActiveOn(date: LocalDate): Boolean {

    // 1. Проверяем период действия самой записи.
    val recordIsActive =
        !date.isBefore(validFrom) &&
                (
                        validUntil == null ||
                                !date.isAfter(validUntil)
                        )

    if (!recordIsActive) return false

    // 2. Проверяем сезон.
    return if (seasonRepeatsAnnually) {

        val startMonth = seasonStartMonth ?: return false
        val startDay = seasonStartDay ?: return false
        val endMonth = seasonEndMonth ?: return false
        val endDay = seasonEndDay ?: return false

        val current = MonthDay.of(
            date.monthValue,
            date.dayOfMonth
        )

        val start = MonthDay.of(
            startMonth,
            startDay
        )

        val end = MonthDay.of(
            endMonth,
            endDay
        )

        if (!end.isBefore(start)) {
            // Например, с 1 апреля по 31 мая.
            !current.isBefore(start) &&
                    !current.isAfter(end)
        } else {
            // Например, с 1 декабря по 31 января.
            !current.isBefore(start) ||
                    !current.isAfter(end)
        }

    } else {

        val start = seasonStartDate ?: return false
        val end = seasonEndDate ?: return false

        !date.isBefore(start) &&
                !date.isAfter(end)
    }
}