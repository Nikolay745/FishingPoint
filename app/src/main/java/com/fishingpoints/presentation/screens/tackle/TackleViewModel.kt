package com.fishingpoints.presentation.screens.tackle

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.data.local.entity.Tackle
import com.fishingpoints.data.repository.TackleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TackleViewModel @Inject constructor(
    private val repository: TackleRepository
) : ViewModel() {

    val tackles: StateFlow<List<Tackle>> = repository.tackles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // Канал для ошибок (дубликат и т.д.)
    private val _errorEvent = Channel<String>()
    val errorEvent = _errorEvent.receiveAsFlow()

    // Канал для сигнала «успех» — чтобы закрыть диалог только при успехе
    private val _successEvent = Channel<Unit>()
    val successEvent = _successEvent.receiveAsFlow()

    fun saveTackle(
        tackle: Tackle,
        fishIds: List<Long>
    ) {
        viewModelScope.launch {
            val trimmedName = tackle.name.trim()

            val existing = repository.getTacklesByName(trimmedName)

            if (tackle.id == 0L) {

                if (existing != null) {
                    _errorEvent.send(
                        "Орудие лова «$trimmedName» уже существует"
                    )
                    return@launch
                }

            } else {

                if (existing != null && existing.id != tackle.id) {
                    _errorEvent.send(
                        "Орудие лова «$trimmedName» уже существует"
                    )
                    return@launch
                }
            }

            try {

                val tackleId = if (tackle.id == 0L) {

                    repository.insertTackle(
                        tackle.copy(name = trimmedName)
                    )

                } else {

                    repository.updateTackle(
                        tackle.copy(name = trimmedName)
                    )

                    tackle.id
                }

                // При редактировании удаляем старые связи
                repository.deleteFishForTackle(tackleId)

                // Сохраняем новые
                repository.saveTackleFish(
                    tackleId = tackleId,
                    fishIds = fishIds
                )

                _successEvent.send(Unit)

            } catch (e: SQLiteConstraintException) {

                _errorEvent.send(
                    "Орудие лова «$trimmedName» уже существует"
                )

            } catch (e: Exception) {

                _errorEvent.send(
                    "Ошибка сохранения: ${e.message}"
                )
            }
        }
    }


    fun deleteTackle(tackle: Tackle) {
        viewModelScope.launch {
            repository.deleteTackle(tackle)
        }
    }

    fun getFishForTackle(tackleId: Long): Flow<List<Fish>> {
        return repository.getFishForTackle(tackleId)
    }

    fun getFishIdsForTackle(
        tackleId: Long
    ): Flow<Set<Long>> {

        return repository
            .getFishForTackle(tackleId)
            .map { fishList ->
                fishList
                    .map { it.id }
                    .toSet()
            }
    }


}

