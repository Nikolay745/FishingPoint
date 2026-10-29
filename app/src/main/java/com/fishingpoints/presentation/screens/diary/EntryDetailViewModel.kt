package com.fishingpoints.presentation.screens.diary

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Embedded
import com.fishingpoints.core.utils.FileUtil
import com.fishingpoints.data.local.entity.BaitWithFish
import com.fishingpoints.data.local.entity.Feed
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.FishingCatch
import com.fishingpoints.data.local.entity.FishingEntry
import com.fishingpoints.data.local.entity.Tackle
import com.fishingpoints.data.repository.BaitRepository
import com.fishingpoints.data.repository.FeedRepository
import com.fishingpoints.data.repository.FishRepository
import com.fishingpoints.data.repository.FishingRepository
import com.fishingpoints.data.repository.JournalRepository
import com.fishingpoints.data.repository.LocationRepository
import com.fishingpoints.data.repository.TackleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntryDetailViewModel @Inject constructor(
    private val repository: FishingRepository,
    private val locationRepository: LocationRepository,
    private val journalRepository: JournalRepository,
    fishRepository: FishRepository,
    baitRepository: BaitRepository,
    feedRepository: FeedRepository,
    tackleRepository: TackleRepository
) : ViewModel() {

    private val _showCatchDialog = MutableStateFlow(false)
    val showCatchDialog: StateFlow<Boolean> =
        _showCatchDialog.asStateFlow() // Хранит данные улова, который сейчас редактируется. Null если режим "Добавить".

    private val _catchBeingEdited = MutableStateFlow<CatchWithDetails?>(null)
    val catchBeingEdited: StateFlow<CatchWithDetails?> = _catchBeingEdited.asStateFlow()

    private val _entry = MutableStateFlow<FishingEntry?>(null)
    val entry: StateFlow<FishingEntry?> = _entry.asStateFlow()

    private val currentEntryId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val catches: StateFlow<List<CatchWithDetails>> =
        currentEntryId
            .filterNotNull()
            .distinctUntilChanged()
            .flatMapLatest { entryId ->
                journalRepository.getCatchesWithDetails(entryId)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private val _locationName = MutableStateFlow<String?>(null)
    val locationName: StateFlow<String?> = _locationName.asStateFlow()

    private val _showAddCatchDialog = MutableStateFlow(false)
    val showAddCatchDialog: StateFlow<Boolean> = _showAddCatchDialog.asStateFlow()

//    private val _photos = MutableStateFlow<List<String>>(emptyList())
//    val photos: StateFlow<List<String>> = _photos.asStateFlow()
private val _catchPhotos = MutableStateFlow<List<String>>(emptyList())
    val catchPhotos: StateFlow<List<String>> = _catchPhotos.asStateFlow()


    val fishList: StateFlow<List<Fish>> = fishRepository.getAllFish()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val baitList: StateFlow<List<BaitWithFish>> = baitRepository.baits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _feedList = MutableStateFlow<List<Feed>>(emptyList())
    val feedList: StateFlow<List<Feed>> = feedRepository.feeds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val tackleList: StateFlow<List<Tackle>> = tackleRepository.tackles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun loadEntry(entryId: Long) {
        currentEntryId.value = entryId

        viewModelScope.launch {
            try {
                val fishingEntry = repository.getEntryById(entryId)

                _entry.value = fishingEntry

                fishingEntry?.let {
                    loadLocationName(it.locationId)

                    // Фото улова
                    if (!it.catchPhotoUri.isNullOrEmpty()) {
                        _catchPhotos.value = listOf(it.catchPhotoUri)
                    } else {
                        _catchPhotos.value = emptyList()
                    }

                }
            } catch (e: Exception) {
                Log.e(
                    "EntryDetailVM",
                    "Ошибка загрузки записи",
                    e
                )
            }
        }
    }

    private suspend fun loadLocationName(locationId: Long) {
        try {
            val location = locationRepository.getLocationById(locationId)
            _locationName.value = location?.name ?: "Место не найдено"
        } catch (_: Exception) {
        }
    }

    fun showAddCatchDialog() {
        _catchBeingEdited.value = null
        _showAddCatchDialog.value = true
    }

    fun showEditCatchDialog(catchItem: CatchWithDetails) {
        _catchBeingEdited.value = catchItem
        _showCatchDialog.value = true
    }

    fun hideAddCatchDialog() {
        _showAddCatchDialog.value = false
    }

    fun saveCatch(
        fishId: Long,
        baitId: Long?,
        feedId: Long?,
        tackleId: Long?,
        count: Int,
        weight: Long,
        length: Int?,
        caughtAt: Long
    ) {
        viewModelScope.launch {

            try {
                val entry = _entry.value
                    ?: return@launch

                val fishingCatch = FishingCatch(
                    entryId = entry.id,
                    fishId = fishId,
                    baitId = baitId,
                    feedId = feedId,
                    tackleId = tackleId,
                    count = count,
                    totalWeight = weight,
                    avgLength = length,
                    caughtAt = caughtAt
                )

                journalRepository.insertCatch(fishingCatch)

                hideAddCatchDialog()

            } catch (e: Exception) {
                Log.e(
                    "EntryDetailVM",
                    "Ошибка сохранения улова",
                    e
                )
            }
        }
    }


    fun deleteCatch(catch: FishingCatch) {
        viewModelScope.launch {
            try {
                journalRepository.deleteCatch(catch)
            } catch (e: Exception) {
                Log.e(
                    "EntryDetailVM",
                    "Ошибка удаления улова",
                    e
                )
            }
        }
    }

    fun updateCatch(
        catch: FishingCatch,
        fishId: Long,
        baitId: Long?,
        feedId: Long?,
        tackleId: Long?,
        count: Int,
        weight: Long,
        length: Int?,
        caughtAt: Long
    ) {
        viewModelScope.launch {
            try {
                val updatedCatch = catch.copy(
                    fishId = fishId,
                    baitId = baitId,
                    feedId = feedId,
                    tackleId = tackleId,
                    count = count,
                    totalWeight = weight,
                    avgLength = length,
                    caughtAt = caughtAt
                )

                journalRepository.updateCatch(updatedCatch)

            } catch (e: Exception) {
                Log.e(
                    "EntryDetailVM",
                    "Ошибка обновления улова",
                    e
                )
            }
        }
    }
    fun saveCatchPhoto(photoUri: String) {
        viewModelScope.launch {
            try {
                val currentEntry = _entry.value
                    ?: return@launch

                val oldPhoto = currentEntry.catchPhotoUri

                val updatedEntry = currentEntry.copy(
                    catchPhotoUri = photoUri
                )

                repository.updateEntry(updatedEntry)

                _entry.value = updatedEntry

                _catchPhotos.value = listOf(photoUri)

                // Старую фотографию удаляем только после успешного сохранения
                if (!oldPhoto.isNullOrEmpty() && oldPhoto != photoUri) {
                    FileUtil.deleteFile(oldPhoto)
                }

            } catch (e: Exception) {
                Log.e(
                    "EntryDetailVM",
                    "Ошибка сохранения фото улова",
                    e
                )
            }
        }
    }


}

data class CatchWithDetails(
    @Embedded
    val catch: FishingCatch,
    val fishName: String,
    val baitName: String?,
    val feedName: String?,
    val tackleName: String?

)
