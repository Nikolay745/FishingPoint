package com.fishingpoints.presentation.screens.diary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.local.entity.FishingEntry
import com.fishingpoints.data.local.entity.LocationEntity
import com.fishingpoints.data.repository.FishingRepository
import com.fishingpoints.data.repository.LocationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class EditEntryViewModel @Inject constructor(
    private val repository: FishingRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _entry = MutableStateFlow<FishingEntry?>(null)
    val entry: StateFlow<FishingEntry?> = _entry.asStateFlow()

    private val _locationName = MutableStateFlow<String?>(null)
    val locationName: StateFlow<String?> = _locationName.asStateFlow()

    private val _allLocations = MutableStateFlow<List<LocationEntity>>(emptyList())
    val allLocations: StateFlow<List<LocationEntity>> = _allLocations.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    fun loadAllLocations() {
        viewModelScope.launch {
            try {
                locationRepository.getAllLocations().collect { locations ->
                    _allLocations.value = locations
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки мест: ${e.message}"
            }
        }
    }

    fun loadEntry(entryId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val entry = repository.getEntryById(entryId)
                _entry.value = entry

                entry?.let {
                    val location = locationRepository.getLocationById(it.locationId)
                    _locationName.value = location?.name ?: "Место не найдено"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки записи: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateEntry(
        locationId: Long,
        weather: String?,
        notes: String?,
        photoUri: String?,
        dateEnd: Date?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val currentEntry = _entry.value ?: return@launch
                val updatedEntry = currentEntry.copy(
                    locationId = locationId,
                    weather = weather,
                    notes = notes,
                    photoUri = photoUri,
                    dateEnd = dateEnd
                )

                repository.updateEntry(updatedEntry)
                _entry.value = updatedEntry
                _isSaved.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка сохранения: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSavedState() {
        _isSaved.value = false
    }

    fun deleteEntry() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val currentEntry = _entry.value ?: return@launch
                repository.deleteEntry(currentEntry)
                _isSaved.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка удаления: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}