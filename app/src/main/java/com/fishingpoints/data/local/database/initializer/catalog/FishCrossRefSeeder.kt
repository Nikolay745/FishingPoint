package com.fishingpoints.data.local.database.initializer.catalog


import com.fishingpoints.data.local.database.dao.FishDao
import com.fishingpoints.data.local.database.initializer.core.fishByName
import com.fishingpoints.data.local.entity.FishSeasonCrossRef
import com.fishingpoints.data.local.entity.FishTimeOfDayCrossRef
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import javax.inject.Inject

internal class FishCrossRefSeeder @Inject constructor(
    private val fishDao: FishDao
) {
    suspend fun seedIfEmpty() {
        // Идемпотентность через счётчики связей (добавьте методы в FishDao):
        // if (fishDao.getSeasonCrossRefCount() != 0 && fishDao.getTimeOfDayCrossRefCount() != 0) return

        val fishMap = fishDao.fishByName()

        val seasonRefs = mutableListOf<FishSeasonCrossRef>()
        val timeRefs = mutableListOf<FishTimeOfDayCrossRef>()

        fishMap.values.forEach { fish ->
            fishSeasonsMap[fish.name]?.forEach { season ->
                seasonRefs.add(FishSeasonCrossRef(fishSpeciesId = fish.id, season = season))
            }
            fishTimeOfDayMap[fish.name]?.forEach { timeOfDay ->
                timeRefs.add(FishTimeOfDayCrossRef(fishSpeciesId = fish.id, timeOfDay = timeOfDay))
            }
        }

        if (seasonRefs.isNotEmpty()) fishDao.insertFishSeasonCrossRefs(seasonRefs)
        if (timeRefs.isNotEmpty()) fishDao.insertFishTimeOfDayCrossRefs(timeRefs)
    }

    private val fishSeasonsMap = mapOf(
        "Белый амур" to listOf(Season.SUMMER, Season.AUTUMN),
        "Голавль" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Ерш" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN, Season.WINTER),
        "Жерех" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Карась" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Карась серебряный" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Карп" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Красноперка" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Лещ" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN, Season.WINTER),
        "Линь" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Налим" to listOf(Season.AUTUMN, Season.WINTER, Season.SPRING),
        "Нельма" to listOf(Season.SUMMER, Season.AUTUMN),
        "Обыкновенный подкаменщик" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Осетр сибирский" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Окунь" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN, Season.WINTER),
        "Плотва" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN, Season.WINTER),
        "Рипус" to listOf(Season.AUTUMN, Season.WINTER, Season.SPRING),
        "Рыбец" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Сазан" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Сиг" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN, Season.WINTER),
        "Сом" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Стерлядь" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Судак" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN, Season.WINTER),
        "Таймень" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Толстолобик" to listOf(Season.SUMMER, Season.AUTUMN),
        "Форель" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Чехонь" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Хариус сибирский" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Хариус европейский" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN),
        "Щука" to listOf(Season.SPRING, Season.SUMMER, Season.AUTUMN, Season.WINTER)
    )


    private val fishTimeOfDayMap = mapOf(
        "Белый амур" to listOf(TimeOfDay.MORNING, TimeOfDay.DAY, TimeOfDay.EVENING),
        "Голавль" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING, TimeOfDay.DUSK),
        "Ерш" to listOf(TimeOfDay.MORNING, TimeOfDay.DAY, TimeOfDay.EVENING, TimeOfDay.NIGHT),
        "Жерех" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING),
        "Карась" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING),
        "Карась серебряный" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING),
        "Карп" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING, TimeOfDay.DUSK),
        "Красноперка" to listOf(TimeOfDay.MORNING, TimeOfDay.DAY, TimeOfDay.AFTERNOON, TimeOfDay.EVENING),
        "Лещ" to listOf(TimeOfDay.NIGHT, TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING),
        "Линь" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING, TimeOfDay.DUSK),
        "Налим" to listOf(TimeOfDay.NIGHT, TimeOfDay.DUSK),
        "Нельма" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING),
        "Обыкновенный подкаменщик" to listOf(TimeOfDay.NIGHT, TimeOfDay.DUSK),
        "Осетр сибирский" to listOf(TimeOfDay.NIGHT, TimeOfDay.DUSK, TimeOfDay.DAWN),
        "Окунь" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING, TimeOfDay.DUSK),
        "Плотва" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.DAY, TimeOfDay.EVENING),
        "Рипус" to listOf(TimeOfDay.EVENING, TimeOfDay.DUSK, TimeOfDay.NIGHT),
        "Рыбец" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING),
        "Сазан" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING, TimeOfDay.DUSK),
        "Сиг" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING),
        "Сом" to listOf(TimeOfDay.EVENING, TimeOfDay.DUSK, TimeOfDay.NIGHT),
        "Стерлядь" to listOf(TimeOfDay.NIGHT, TimeOfDay.DUSK, TimeOfDay.DAWN),
        "Судак" to listOf(TimeOfDay.DAWN, TimeOfDay.EVENING, TimeOfDay.DUSK, TimeOfDay.NIGHT),
        "Таймень" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING),
        "Толстолобик" to listOf(TimeOfDay.MORNING, TimeOfDay.DAY, TimeOfDay.AFTERNOON),
        "Форель" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING, TimeOfDay.DUSK),
        "Чехонь" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.DAY, TimeOfDay.EVENING),
        "Хариус сибирский" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING),
        "Хариус европейский" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING),
        "Щука" to listOf(TimeOfDay.DAWN, TimeOfDay.MORNING, TimeOfDay.EVENING, TimeOfDay.DUSK)
    )

}