package com.fishingpoints.presentation.screens.fishingRule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.local.entity.EquipmentEntity
import com.fishingpoints.data.repository.EquipmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class EquipmentViewModel @Inject constructor(
    private val repository: EquipmentRepository
) : ViewModel() {

    private val errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<EquipmentUiState> =
        combine(
            repository.getAllEquipment(),
            errorMessage
        ) { equipment, error ->
            EquipmentUiState(
                equipment = equipment,
                errorMessage = error
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EquipmentUiState()
        )

    init {
        viewModelScope.launch {
            repository.seedDefaultEquipment()
        }
    }

    fun addEquipment(name: String) {
        viewModelScope.launch {
            handleResult {
                repository.addEquipment(name)
            }
        }
    }

    fun updateEquipment(
        equipment: EquipmentEntity,
        newName: String
    ) {
        viewModelScope.launch {
            handleResult {
                repository.updateEquipment(equipment, newName)
            }
        }
    }

    fun toggleEquipment(
        equipment: EquipmentEntity,
        isPacked: Boolean
    ) {
        viewModelScope.launch {
            repository.toggleEquipment(equipment, isPacked)
        }
    }

    fun deleteEquipment(equipment: EquipmentEntity) {
        viewModelScope.launch {
            repository.deleteEquipment(equipment)
        }
    }

    fun resetAllEquipment() {
        viewModelScope.launch {
            repository.resetAllEquipment()
        }
    }

    fun clearError() {
        errorMessage.value = null
    }

    private suspend fun handleResult(
        action: suspend () -> Result<Unit>
    ) {
        val result = action()

        errorMessage.value = result.exceptionOrNull()?.message
    }
}

data class EquipmentUiState(
    val equipment: List<EquipmentEntity> = emptyList(),
    val errorMessage: String? = null
) {
    val packedCount: Int
        get() = equipment.count { it.isPacked }

    val totalCount: Int
        get() = equipment.size

    val progress: Float
        get() = if (totalCount == 0) {
            0f
        } else {
            packedCount.toFloat() / totalCount
        }
}
