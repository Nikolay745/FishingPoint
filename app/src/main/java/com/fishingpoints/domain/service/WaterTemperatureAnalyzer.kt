package com.fishingpoints.domain.service

import javax.inject.Inject

// Анализатор температуры воды
class WaterTemperatureAnalyzer @Inject constructor() {
    fun estimate(
        airTemperature: Double
    ): Double {

        return when {
            airTemperature <= 0 -> 0.5
            airTemperature <= 5 -> airTemperature - 1.5

            airTemperature <= 10 ->
                airTemperature - 2.0

            airTemperature <= 15 ->
                airTemperature - 2.5

            airTemperature <= 20 ->
                airTemperature - 3.0

            airTemperature <= 25 ->
                airTemperature - 3.5

            airTemperature <= 30 ->
                airTemperature - 4.0

            else ->
                airTemperature - 5.0
        }.coerceIn(0.0, 30.0)
    }

    fun isOptimal(
        waterTemp: Double
    ): Boolean {
        return waterTemp in 10.0..20.0
    }
}