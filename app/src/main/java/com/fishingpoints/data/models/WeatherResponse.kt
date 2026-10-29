package com.fishingpoints.data.models

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("coord") val coord: Coord? = null,
    @SerializedName("weather") val weather: List<WeatherDescription> = emptyList(),
    @SerializedName("base") val base: String? = null,
    @SerializedName("main") val main: MainWeather? = null,
    @SerializedName("visibility") val visibility: Int? = null,
    @SerializedName("wind") val wind: Wind? = null,
    @SerializedName("clouds") val clouds: Clouds? = null,
    @SerializedName("dt") val dt: Long? = null,
    @SerializedName("sys") val sys: Sys? = null,
    @SerializedName("timezone") val timezone: Int? = null,
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("cod") val cod: Int? = null,
) {
    // Вспомогательные свойства для доступа к данным
    val current: CurrentWeather
        get() = CurrentWeather(
            temp = main?.temp ?: 0.0,
            pressure = main?.pressure ?: 0,
            windSpeed = wind?.speed ?: 0.0,
            windDeg = wind?.deg ?: 0,
            clouds = clouds?.all ?: 0,
            weather = weather
        )
}

data class Coord(  val lon: Double? = null,  val lat: Double? = null)

data class MainWeather(
    val temp: Double? = null,

    @SerializedName("feels_like")
    val feelsLike: Double? = null,

    @SerializedName("temp_min")
    val tempMin: Double? = null,

    @SerializedName("temp_max")
    val tempMax: Double? = null,

    val pressure: Int? = null,
    val humidity: Int? = null
)


data class Wind(
    val speed: Double = 0.0,
    val deg: Int = 0, // град
    val gust: Double? = null // порыв ветра
)

data class Clouds(   // Облака
    val all: Int
)

data class Sys(
    val type: Int? = null,
    val id: Int? = null,
    val country: String? = null,
    val sunrise: Long? = null,
    val sunset: Long? = null
)

data class WeatherDescription(val id: Int, val main: String, val description: String, val icon: String)

data class CurrentWeather(    // Текущая погода
    val temp: Double,
    val pressure: Int,
    val windSpeed: Double,
    val windDeg: Int,
    val clouds: Int,
    val weather: List<WeatherDescription>
)

data class DailyWeather(val dt: Int, val temp: Temp, val pressure: Int, val windSpeed: Double,  // Ежедневная погода
                        val weather: List<WeatherDescription>)

data class Temp(                    // Временный
    val day: Double,
    val min: Double,
    val max: Double
)


data class FishingWeather(
    val temperature: Double? = null, // температура воздуха
    val waterTemperature: Double? = null,  // температура воды (опционально)
    val isRealWaterTemperature: Boolean,   // Это реальная температура воды
    val pressure: Int,                      // Давление
    val pressureTrend: PressureTrend,       // Тренд давления
    val windSpeed: Double,                 // скорость ветра
    val cloudiness: Int,                // облачность
    val precipitation: String,         // осадок
    val recommendation: String,         // рекомендация
    val biteRating: Double              // погода, оценивающая клев
)

enum class PressureTrend {
    RISING,    // Давление растет
    FALLING,   // Давление падает
    STABLE,     // Давление стабильно
    NODATA      // Нет данных
}