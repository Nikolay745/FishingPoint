package com.fishingpoints.core.network

import com.fishingpoints.data.models.ForecastResponse
import com.fishingpoints.data.models.WeatherResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<WeatherResponse>

    // прогноз погоды
    @GET("forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<ForecastResponse>
}