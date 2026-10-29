package com.fishingpoints.presentation.screens.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.data.local.entity.LocationEntity
import com.fishingpoints.data.repository.LocationRepository
import com.fishingpoints.data.repository.RegionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject
import kotlin.collections.emptyList


@HiltViewModel
class LocationViewModel @Inject constructor(
    private val repository: LocationRepository,
    regionRepository: RegionRepository
) : ViewModel() {

    val locations = repository.getAllLocations()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val regions = regionRepository.getAllRegions() // Добавьте получение списка регионов
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun getRegionName(regionId: Long?): String {
        return regions.value.find { it.id == regionId }?.name ?: "Неизвестный регион"
    }
    fun addLocation(name: String, lat: Double, lon: Double, regionId: Long) {
        viewModelScope.launch {
            repository.insert(
                LocationEntity(
                    name = name,
                    lat = lat,
                    lon = lon,
                    regionId = regionId,
                    zoneId = ZoneId.systemDefault()
                )
            )
        }
    }

    fun updateLocation(location: LocationEntity) {
        viewModelScope.launch {
            repository.update(location)
        }
    }

    fun deleteLocation(location: LocationEntity) {
        viewModelScope.launch {
            repository.delete(location)
        }
    }
}