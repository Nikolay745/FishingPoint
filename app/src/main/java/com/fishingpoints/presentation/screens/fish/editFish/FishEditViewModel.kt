package com.fishingpoints.presentation.screens.fish.editFish

import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.local.database.dao.BaitDao
import com.fishingpoints.data.local.database.dao.FeedDao
import com.fishingpoints.data.local.database.dao.TackleDao
import com.fishingpoints.data.local.entity.Bait
import com.fishingpoints.data.local.entity.BaitFishCrossRef
import com.fishingpoints.data.local.entity.Feed
import com.fishingpoints.data.local.entity.FeedFishCrossRef
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.Tackle
import com.fishingpoints.data.local.entity.TackleFishCrossRef
import com.fishingpoints.data.local.enums.FeedingActivity
import com.fishingpoints.data.local.enums.Season
import com.fishingpoints.data.local.enums.TimeOfDay
import com.fishingpoints.data.repository.FishRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

data class FishEditFormState(
    val name: String = "",
    val imagePath: String? = null,
    val feedingActivity: FeedingActivity = FeedingActivity.LOW,

    val temperatureMin: Int? = null,
    val temperatureMax: Int? = null,

    val pressureMin: Int? = null,
    val pressureMax: Int? = null,

    val description: String = "",
    val tipsBaits: String = "",
    val tackles: String = "",
    val baitForFish: String = "",
    val adviceFromFishermen: String = "",

    val seasons: List<Season> = emptyList(),
    val timeOfDay: List<TimeOfDay> = emptyList()
) {
    val isValid: Boolean
        get() = name.isNotBlank()
}


data class FishEditUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val form: FishEditFormState = FishEditFormState(),
    val originalForm: FishEditFormState? = null,

    val baits: List<Bait> = emptyList(),
    val tackles: List<Tackle> = emptyList(),
    val feeds: List<Feed> = emptyList(),

    val selectedBaitIds: Set<Long> = emptySet(),
    val selectedTackleIds: Set<Long> = emptySet(),
    val selectedFeedIds: Set<Long> = emptySet(),

    val originalBaitIds: Set<Long> = emptySet(),
    val originalTackleIds: Set<Long> = emptySet(),
    val originalFeedIds: Set<Long> = emptySet()
) {

val isDirty: Boolean get() {
    val original = originalForm ?: return form.isValid ||
            selectedBaitIds.isNotEmpty() ||
            selectedTackleIds.isNotEmpty() ||
            selectedFeedIds.isNotEmpty()

    return original != form ||
            selectedBaitIds != originalBaitIds ||
            selectedTackleIds != originalTackleIds ||
            selectedFeedIds != originalFeedIds
}

    val canSave: Boolean
        get() = !isLoading && form.isValid && isDirty

}

@HiltViewModel
class FishEditViewModel @Inject constructor(
    private val fishRepository: FishRepository,
    private val baitDao: BaitDao,
    private val tackleDao: TackleDao,
    private val feedDao: FeedDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val fishId: Long = savedStateHandle.get<Long>("fishId") ?: -1L
    private val _uiState = MutableStateFlow(FishEditUiState(isLoading = true))
    val uiState: StateFlow<FishEditUiState> = _uiState.asStateFlow()

    init {
        loadFish()
    }

    // ---------- Маппинг ----------
    private fun Fish.toEditForm() = FishEditFormState(
        name = name,
        imagePath = imageUrl,
        feedingActivity = feedingActivity ?: FeedingActivity.LOW,
        temperatureMin = temperatureMin,
        temperatureMax = temperatureMax,
        pressureMin = pressureMin,
        pressureMax = pressureMax,
        description = description.orEmpty(),
        tipsBaits = tipsBaits.orEmpty(),
        tackles = tackles.orEmpty(),
        baitForFish = baitForFish.orEmpty(),
        adviceFromFishermen = adviceFromFishermen.orEmpty(),
        seasons = seasons.orEmpty(),
        timeOfDay = bestTimeOfDay.orEmpty()
    )

    private fun FishEditFormState.toFish(id: Long): Fish =
        Fish(
            id = id,
            name = name.trim(),
            imageUrl = imagePath?.takeIf { it.isNotBlank() },
            feedingActivity = feedingActivity,
            temperatureMin = temperatureMin,
            temperatureMax = temperatureMax,
            pressureMin = pressureMin,
            pressureMax = pressureMax,
            description = description,
            tipsBaits = tipsBaits,
            tackles = tackles,
            baitForFish = baitForFish,
            adviceFromFishermen = adviceFromFishermen,
            bestTimeOfDay = timeOfDay,
            seasons = seasons
        )

    // ---------- Загрузка ----------
    private fun loadFish() {
        viewModelScope.launch {
            try {
                val baits = baitDao.getAllBaits().first()
                val tackles = tackleDao.getAllTackles().first()
                val feeds = feedDao.getAllFeed().first()

                if (fishId == -1L) {
                    _uiState.value = FishEditUiState(
                        isLoading = false,
                        baits = baits,
                        tackles = tackles,
                        feeds = feeds
                    )
                    return@launch
                }
                val fish = fishRepository.getFishById(fishId)
                    ?: throw IllegalStateException("Рыба не найдена")

                val seasons = fishRepository.getSeasonsForFish(fishId)
                val timeOfDays = fishRepository.getTimeOfDayForFish(fishId)

                val selectedBaits =
                    baitDao.getBaitsForFish(fishId)
                        .map { it.id }
                        .toSet()

                val selectedTackles =
                    tackleDao.getTacklesForFish(fishId)
                        .map { it.id }
                        .toSet()

                val selectedFeeds =
                    feedDao.getFeedForFish(fishId)
                        .map { it.id }
                        .toSet()

                val completeFish = fish.copy(
                    seasons = seasons,
                    bestTimeOfDay = timeOfDays
                )

                val form = completeFish.toEditForm()


                _uiState.value = FishEditUiState(
                    isLoading = false,
                    form = form,
                    originalForm = form,
                    baits = baits,
                    tackles = tackles,
                    feeds = feeds,
                    selectedBaitIds = selectedBaits,
                    selectedTackleIds = selectedTackles,
                    selectedFeedIds = selectedFeeds,
                    originalBaitIds = selectedBaits,
                    originalTackleIds = selectedTackles,
                    originalFeedIds = selectedFeeds
                )

        } catch (e: Exception) {
            _uiState.value = FishEditUiState(
                isLoading = false,
                error = e.message ?: "Не удалось загрузить рыбу"
            )
        }
    }
}

    fun updateName(value: String) =  updateForm { copy(name = value) }

//    fun updateImagePath(value: String?) = updateForm { copy(imagePath = value) }
fun updateImagePath(value: String?) {
    Log.d("FishImage", "updateImagePath = $value")
    updateForm { copy(imagePath = value) }
}


    fun updateFeedingActivity(value: FeedingActivity) = updateForm { copy(feedingActivity = value) }

    fun updateTemperatureMin(value: Int?) = updateForm { copy(temperatureMin = value) }

    fun updateTemperatureMax(value: Int?) =  updateForm { copy(temperatureMax = value) }

    fun updatePressureMin(value: Int?) =
        updateForm { copy(pressureMin = value) }

    fun updatePressureMax(value: Int?) =
        updateForm { copy(pressureMax = value) }

    fun updateDescription(value: String) =
        updateForm { copy(description = value) }

    fun updateAdvice(value: String) =
        updateForm { copy(adviceFromFishermen = value) }

    fun updateSeasons(value: List<Season>) =
        updateForm { copy(seasons = value) }

    fun updateTimeOfDay(value: List<TimeOfDay>) =
        updateForm { copy(timeOfDay = value) }

    private inline fun updateForm(
        crossinline update: FishEditFormState.() -> FishEditFormState
    ) {
        _uiState.update { state ->
            state.copy(
                form = state.form.update()
            )
        }
    }


    // ---------- Переключение связей ----------
    fun toggleBait(id: Long) {
        _uiState.update { state ->
            state.copy(
                selectedBaitIds = state.selectedBaitIds.toggle(id)
            )
        }
    }

    fun toggleTackle(id: Long) {
        _uiState.update { state ->
            state.copy(
                selectedTackleIds = state.selectedTackleIds.toggle(id)
            )
        }
    }

    fun toggleFeed(id: Long) {
        _uiState.update { state ->
            state.copy(
                selectedFeedIds = state.selectedFeedIds.toggle(id)
            )
        }
    }

    // ---------- Сохранение ----------
    fun save(onDone: () -> Unit) { val initialState = _uiState.value

        if (!initialState.canSave || initialState.isLoading) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                // Получаем актуальное состояние формы.
                val currentState = _uiState.value
                val form = currentState.form
                val name = form.name.trim()

                // Проверяем уникальность названия.
                val existingFish = fishRepository.getFishByName(name)

                if (existingFish != null && existingFish.id != fishId) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Рыба «$name» уже существует"
                        )
                    }
                    return@launch
                }

                // Преобразуем форму в сущность Fish.
                val fish = form.toFish(
                    id = if (fishId == -1L) 0L else fishId
                )

                // Сохраняем рыбу и все связи в одной транзакции.
                fishRepository.saveFishWithRelations(
                    fish = fish,
                    seasons = form.seasons,
                    timeOfDay = form.timeOfDay,
                    baitIds = currentState.selectedBaitIds,
                    tackleIds = currentState.selectedTackleIds,
                    feedIds = currentState.selectedFeedIds
                )

                // Сохранение успешно.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null
                    )
                }

                onDone()

            } catch (e: kotlinx.coroutines.CancellationException) {
                // Не подавляем отмену корутины.
                throw e

            } catch (e: Exception) {
                Log.e("FishEditViewModel", "Ошибка сохранения рыбы", e)

                val errorMessage = when (e) {
                    is android.database.sqlite.SQLiteConstraintException ->
                        "Рыба с таким названием уже существует"

                    is java.io.IOException ->
                        "Ошибка ввода-вывода при сохранении данных"

                    else ->
                        e.message ?: "Не удалось сохранить рыбу"
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
        }


    }






    private suspend fun saveRelations(
        fishId: Long,
        baitIds: Set<Long>,
        tackleIds: Set<Long>,
        feedIds: Set<Long>
    ) {
        // Наживки
        baitDao.deleteBaitsForFish(fishId)
        if (baitIds.isNotEmpty()) {
            baitDao.insertBaitFishCrossRefs(
                baitIds.map { BaitFishCrossRef(baitId = it, fishId = fishId) }
            )
        }

        // Снасти
        tackleDao.deleteTacklesForFish(fishId)
        if (tackleIds.isNotEmpty()) {
            tackleDao.insertTackleFishCrossRefs(
                tackleIds.map { TackleFishCrossRef(tackleId = it, fishId = fishId) }
            )
        }

        // Прикормка
        feedDao.deleteFeedsForFish(fishId)
        if (feedIds.isNotEmpty()) {
            feedDao.insertFeedFishCrossRefs(
                feedIds.map { FeedFishCrossRef(feedId = it, fishId = fishId) }
            )
        }
    }

}


// ---------- Утилитарные расширения ----------
private fun Set<Long>.toggle(id: Long): Set<Long> =
    if (id in this) this - id else this + id