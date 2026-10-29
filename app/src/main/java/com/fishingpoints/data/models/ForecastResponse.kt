package com.fishingpoints.data.models

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("list") val list: List<ForecastItem>,
    @SerializedName("city") val city: City
)

data class ForecastItem(
    @SerializedName("dt") val dt: Long,
    @SerializedName("main") val main: ForecastMain,
    @SerializedName("wind") val wind: ForecastWind,
    @SerializedName("weather") val weather: List<WeatherDescription>,
    @SerializedName("clouds")    val clouds: ForecastClouds,
    @SerializedName("dt_txt") val dtTxt: String
){
    // Добавляем вспомогательное свойство для совместимости
    val windSpeed: Double
        get() = wind.speed

    val windDeg: Int
        get() = wind.deg
}
data class ForecastMain(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("temp_min") val tempMin: Double,
    @SerializedName("temp_max") val tempMax: Double,
    @SerializedName("pressure") val pressure: Int,
    @SerializedName("humidity") val humidity: Int
)

data class ForecastWind(
    @SerializedName("speed") val speed: Double,
    @SerializedName("deg") val deg: Int
)

data class City(
    @SerializedName("name") val name: String,
    @SerializedName("country") val country: String
)

data class ForecastClouds(
    @SerializedName("all")
    val all: Int
)