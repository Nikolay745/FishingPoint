package com.fishingpoints.presentation.screens.regions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.local.entity.Region
import com.fishingpoints.data.repository.RegionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegionViewModel @Inject constructor(
    private val repository: RegionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegionUiState())
    val uiState = _uiState.asStateFlow()

    init {
        observeRegions()
    }

    private fun observeRegions() {
        viewModelScope.launch {
            repository.getAllRegions()
                .collect { regions ->
                    _uiState.update {
                        it.copy(
                            regions = regions
                        )
                    }
                }
        }
    }

    fun addRegion(name: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    error = null
                )
            }

            when (repository.insert(Region(name = name))) {

                RegionSaveResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveCompleted = true
                        )
                    }
                }

                RegionSaveResult.DuplicateName -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = "Регион с таким названием уже существует"
                        )
                    }
                }

                RegionSaveResult.EmptyName -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = "Введите название региона"
                        )
                    }
                }
            }
        }
    }

    fun updateRegion(region: Region) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    error = null
                )
            }

            when (repository.update(region)) {

                RegionSaveResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveCompleted = true
                        )
                    }
                }

                RegionSaveResult.DuplicateName -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = "Регион с таким названием уже существует"
                        )
                    }
                }

                RegionSaveResult.EmptyName -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = "Введите название региона"
                        )
                    }
                }
            }
        }
    }

    fun clearSaveCompleted() {
        _uiState.update {
            it.copy(
                saveCompleted = false
            )
        }
    }


    fun clearError() {
        _uiState.update {
            it.copy(
                error = null
            )
        }
    }

    fun deleteRegion(region: Region) {
        viewModelScope.launch {
            repository.delete(region)
        }
    }
}


data class RegionUiState(
    val regions: List<Region> = emptyList(),
    val isSaving: Boolean = false,
    val saveCompleted: Boolean = false,
    val error: String? = null
)

sealed interface RegionSaveResult {
    data object Success : RegionSaveResult
    data object DuplicateName : RegionSaveResult
    data object EmptyName : RegionSaveResult
}

