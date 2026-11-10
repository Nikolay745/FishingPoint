package com.fishingpoints.presentation.screens.fishActivityRule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.local.entity.FishActivityRule
import com.fishingpoints.data.local.enums.RuleSource
import com.fishingpoints.data.repository.FishActivityRuleRepository
import com.fishingpoints.data.repository.FishRepository
import com.fishingpoints.data.repository.RegionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class FishActivityRuleViewModel @Inject constructor(
    private val ruleRepository: FishActivityRuleRepository,
    fishRepository: FishRepository,
    regionRepository: RegionRepository
) : ViewModel() {

    val rules = ruleRepository.getAllRules()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val fishList = fishRepository.getAllFish()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val regionList = regionRepository.getAllRegions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun saveRule(rule: FishActivityRule) {
        viewModelScope.launch {
            try {
                val customRule = rule.copy(
                    source = RuleSource.CUSTOM
                )

                if (customRule.id == 0L) {
                    ruleRepository.insertRule(customRule)
                } else {
                    ruleRepository.updateRule(customRule)
                }

                _error.value = null

            } catch (e: CancellationException) {
                throw e

            } catch (e: Exception) {
                _error.value = e.message
                    ?: "Не удалось сохранить правило"
            }
        }
    }



    fun clearError() {
        _error.value = null
    }

    fun getFishName(fishId: Long): String {
        return fishList.value
            .find { it.id == fishId }
            ?.name ?: "Неизвестная рыба"
    }

    fun getRegionName(regionId: Long?): String {
        if (regionId == null) return "Все регионы"

        return regionList.value
            .find { it.id == regionId }
            ?.name ?: "Неизвестный регион"
    }

    fun deleteRule(rule: FishActivityRule) {

        if (rule.source == RuleSource.DEFAULT) {
            _error.value = "Базовые правила нельзя удалить"
            return
        }

        viewModelScope.launch {
            try {
                ruleRepository.deleteRule(rule)
                _error.value = null
            } catch (_: Exception) {
                _error.value = "Не удалось удалить правило"
            }
        }
    }

}
