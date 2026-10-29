package com.fishingpoints.data.repository

import androidx.room.withTransaction
import com.fishingpoints.data.local.database.FishingDatabase
import com.fishingpoints.data.local.database.dao.BaitDao
import com.fishingpoints.data.local.database.dao.FeedDao
import com.fishingpoints.data.local.database.dao.FishDao
import com.fishingpoints.data.local.database.dao.TackleDao
import com.fishingpoints.data.local.entity.BaitFishCrossRef
import com.fishingpoints.data.local.entity.FeedFishCrossRef
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.FishSeasonCrossRef
import com.fishingpoints.data.local.entity.FishTimeOfDayCrossRef
import com.fishingpoints.data.local.entity.TackleFishCrossRef
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.domain.lunar.LunarInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Singleton

@Singleton
class FishRepository(
    private val database: FishingDatabase,
    private val fishDao: FishDao,
    private val baitDao: BaitDao,
    private val tackleDao: TackleDao,
    private val feedDao: FeedDao
) {

// ---------------------------------------------------------
// СОХРАНЕНИЕ РЫБЫ
// ---------------------------------------------------------

    /**
     * Сохраняет рыбу, сезоны и время суток.
     *
     * Используется, если не нужно обновлять связи
     * с наживками, снастями и прикормкой.
     */
    suspend fun saveFishFull(
        fish: Fish,
        seasons: List<Season>,
        timeOfDay: List<TimeOfDay>
    ): Long {
        return fishDao.upsertFishComplete(
            fish = fish,
            seasons = seasons.distinct(),
            timeOfDay = timeOfDay.distinct()
        )
    }

    /**
     * Полностью сохраняет рыбу и все её связи
     * в одной транзакции Room.
     */
    suspend fun saveFishWithRelations(
        fish: Fish,
        seasons: List<Season>,
        timeOfDay: List<TimeOfDay>,
        baitIds: Set<Long>,
        tackleIds: Set<Long>,
        feedIds: Set<Long>
    ): Long = database.withTransaction {

        // 1. Сохраняем рыбу, сезоны и время суток.
        val fishId = fishDao.upsertFishComplete(
            fish = fish,
            seasons = seasons.distinct(),
            timeOfDay = timeOfDay.distinct()
        )

        // 2. Обновляем связи с наживками.
        baitDao.deleteBaitsForFish(fishId)

        if (baitIds.isNotEmpty()) {
            baitDao.insertBaitFishCrossRefs(
                baitIds.map { baitId ->
                    BaitFishCrossRef(
                        baitId = baitId,
                        fishId = fishId
                    )
                }
            )
        }

        // 3. Обновляем связи со снастями.
        tackleDao.deleteTacklesForFish(fishId)

        if (tackleIds.isNotEmpty()) {
            tackleDao.insertTackleFishCrossRefs(
                tackleIds.map { tackleId ->
                    TackleFishCrossRef(
                        tackleId = tackleId,
                        fishId = fishId
                    )
                }
            )
        }

        // 4. Обновляем связи с прикормкой.
        feedDao.deleteFeedsForFish(fishId)

        if (feedIds.isNotEmpty()) {
            feedDao.insertFeedFishCrossRefs(
                feedIds.map { feedId ->
                    FeedFishCrossRef(
                        feedId = feedId,
                        fishId = fishId
                    )
                }
            )
        }

        fishId
    }


// ---------------------------------------------------------
// ПОЛУЧЕНИЕ РЫБЫ
// ---------------------------------------------------------

    suspend fun getFishById(id: Long): Fish? =
        fishDao.getFishById(id)

    suspend fun getFishByName(name: String): Fish? =
        fishDao.getFishByName(name.trim())

    suspend fun getFishByNameList(names: List<String>): List<Fish> =
        if (names.isEmpty()) {
            emptyList()
        } else {
            fishDao.getFishByNameList(names)
        }

    fun getAllFish(): Flow<List<Fish>> =
        fishDao.getAllFish()

    fun getFilteredFish(
        query: String,
        season: Season? = null,
        timeOfDay: TimeOfDay? = null
    ): Flow<List<Fish>> =
        fishDao.getFilteredFish(
            query = query.trim(),
            season = season,
            timeOfDay = timeOfDay
        )


// ---------------------------------------------------------
// СЕЗОНЫ И ВРЕМЯ СУТОК
// ---------------------------------------------------------

    suspend fun getSeasonsForFish(fishId: Long): List<Season> =
        fishDao.getSeasonsForFish(fishId)

    suspend fun getTimeOfDayForFish(
        fishId: Long
    ): List<TimeOfDay> =
        fishDao.getTimeOfDayForFish(fishId)


// ---------------------------------------------------------
// РЕКОМЕНДАЦИИ
// ---------------------------------------------------------

    suspend fun getRecommendedFish(
        season: Season,
        timeOfDay: TimeOfDay,
        weather: FishingWeather,
        lunarInfo: LunarInfo
    ): List<Fish> {

        // Пока учитываются только сезон и время суток.
        // Weather и lunarInfo можно использовать
        // при реализации полноценного алгоритма рекомендаций.
        return fishDao
            .getFilteredFish(
                query = "",
                season = season,
                timeOfDay = timeOfDay
            )
            .first()
    }


// ---------------------------------------------------------
// УДАЛЕНИЕ РЫБЫ
// ---------------------------------------------------------

    /**
     * Удаляет рыбу и возвращает данные, необходимые
     * для её последующего восстановления.
     *
     * Чтение связей и удаление выполняются транзакционно.
     */
    suspend fun deleteFishWithRelations(
        fish: Fish
    ): DeletedFishData = database.withTransaction {

        val seasons = fishDao.getSeasonsForFish(fish.id)

        val timeOfDay =
            fishDao.getTimeOfDayForFish(fish.id)

        val baitIds = baitDao
            .getBaitsForFish(fish.id)
            .map { it.id }

        val tackleIds = tackleDao
            .getTacklesForFish(fish.id)
            .map { it.id }

        val feedIds = feedDao
            .getFeedForFish(fish.id)
            .map { it.id }

        // Удаляем связи явно, чтобы не зависеть
        // от наличия ON DELETE CASCADE.
        fishDao.deleteSeasonsForFish(fish.id)
        fishDao.deleteTimeOfDayForFish(fish.id)

        baitDao.deleteBaitsForFish(fish.id)
        tackleDao.deleteTacklesForFish(fish.id)
        feedDao.deleteFeedsForFish(fish.id)

        fishDao.deleteFish(fish)

        DeletedFishData(
            fish = fish.copy(
                seasons = seasons,
                bestTimeOfDay = timeOfDay
            ),
            baitIds = baitIds,
            tackleIds = tackleIds,
            feedIds = feedIds
        )
    }


// ---------------------------------------------------------
// ВОССТАНОВЛЕНИЕ РЫБЫ
// ---------------------------------------------------------

    /**
     * Восстанавливает рыбу и все связанные данные
     * в одной транзакции.
     *
     * Если восстановление не удаётся, изменения
     * транзакции отменяются.
     */
    suspend fun restoreFish(
        data: DeletedFishData
    ) = database.withTransaction {

        val fish = data.fish

        // Восстанавливаем рыбу с исходным ID.
        fishDao.insertFish(fish)

        // Восстанавливаем сезоны.
        val seasons = fish.seasons.orEmpty().distinct()

        if (seasons.isNotEmpty()) {
            fishDao.insertFishSeasonCrossRefs(
                seasons.map { season ->
                    FishSeasonCrossRef(
                        fishSpeciesId = fish.id,
                        season = season
                    )
                }
            )
        }

        // Восстанавливаем время суток.
        val timeOfDay =
            fish.bestTimeOfDay.orEmpty().distinct()

        if (timeOfDay.isNotEmpty()) {
            fishDao.insertFishTimeOfDayCrossRefs(
                timeOfDay.map { time ->
                    FishTimeOfDayCrossRef(
                        fishSpeciesId = fish.id,
                        timeOfDay = time
                    )
                }
            )
        }

        // Восстанавливаем наживки.
        if (data.baitIds.isNotEmpty()) {
            baitDao.insertBaitFishCrossRefs(
                data.baitIds.distinct().map { baitId ->
                    BaitFishCrossRef(
                        baitId = baitId,
                        fishId = fish.id
                    )
                }
            )
        }

        // Восстанавливаем снасти.
        if (data.tackleIds.isNotEmpty()) {
            tackleDao.insertTackleFishCrossRefs(
                data.tackleIds.distinct().map { tackleId ->
                    TackleFishCrossRef(
                        tackleId = tackleId,
                        fishId = fish.id
                    )
                }
            )
        }

        // Восстанавливаем прикормку.
        if (data.feedIds.isNotEmpty()) {
            feedDao.insertFeedFishCrossRefs(
                data.feedIds.distinct().map { feedId ->
                    FeedFishCrossRef(
                        feedId = feedId,
                        fishId = fish.id
                    )
                }
            )
        }
    }

    /**
     * Совместимость со старым API.
     *
     * Восстанавливает рыбу и её сезоны/время суток.
     * Связи со справочниками восстанавливаются только
     * при наличии соответствующих ID в DeletedFishData.
     */
    suspend fun restoreFish(fish: Fish) {
        restoreFish(
            DeletedFishData(
                fish = fish,
                baitIds = emptyList(),
                tackleIds = emptyList(),
                feedIds = emptyList()
            )
        )
    }


// ---------------------------------------------------------
// ПРОСТЫЕ ОПЕРАЦИИ
// ---------------------------------------------------------

    suspend fun insertFish(fish: Fish): Long =
        fishDao.insertFish(fish)

    suspend fun deleteFish(fish: Fish) =
        database.withTransaction {
            fishDao.deleteSeasonsForFish(fish.id)
            fishDao.deleteTimeOfDayForFish(fish.id)

            baitDao.deleteBaitsForFish(fish.id)
            tackleDao.deleteTacklesForFish(fish.id)
            feedDao.deleteFeedsForFish(fish.id)

            fishDao.deleteFish(fish)
        }


// ---------------------------------------------------------
// ДАННЫЕ ДЛЯ ВОССТАНОВЛЕНИЯ
// ---------------------------------------------------------

    data class DeletedFishData(
        val fish: Fish,
        val baitIds: List<Long>,
        val tackleIds: List<Long>,
        val feedIds: List<Long>
    )



}