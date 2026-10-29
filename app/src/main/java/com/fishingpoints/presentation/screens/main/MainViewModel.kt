package com.fishingpoints.presentation.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fishingpoints.core.common.DEFAULT_LAT
import com.fishingpoints.core.common.DEFAULT_LOCATION_NAME
import com.fishingpoints.core.common.DEFAULT_LON
import com.fishingpoints.core.common.DEFAULT_REGION
import com.fishingpoints.core.common.DEFAULT_TIMEZONE
import com.fishingpoints.core.location.GeocoderService
import com.fishingpoints.core.location.LocationProvider
import com.fishingpoints.core.location.LocationResult
import com.fishingpoints.core.network.FishingForecast
import com.fishingpoints.core.network.FishingService
import com.fishingpoints.data.local.entity.LocationEntity
import com.fishingpoints.data.models.FishingWeather
import com.fishingpoints.data.repository.FishRepository
import com.fishingpoints.data.repository.WeatherRepository
import com.fishingpoints.domain.lunar.LunarCalendar
import com.fishingpoints.domain.lunar.LunarInfo
import com.fishingpoints.domain.recommendation.bait.BaitRecommendationService
import com.fishingpoints.domain.recommendation.season.SeasonRecommendationService
import com.fishingpoints.domain.recommendation.tackle.TackleRecommendationService
import com.fishingpoints.domain.recommendation.tips.PersonalizedTipService
import com.fishingpoints.domain.service.FishingAnalyticsService
import com.fishingpoints.domain.service.TipsGenerator
import com.fishingpoints.domain.utils.FishingCalendar
import com.fishingpoints.presentation.screens.main.model.BestFishingTime
import com.fishingpoints.presentation.screens.main.model.FishForecastService
import com.fishingpoints.presentation.screens.settings.TemperatureUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject


/** * Главная ViewModel приложения. *
 * Отвечает за:
 * - Управление состоянием экрана
 * - Получение данных о погоде и прогнозе клева
 * - Обработку пользовательских действий
 *
 * @property weatherRepository - репозиторий для работы с погодой
 * @property fishingAnalyticsService - сервис анализа погоды для рыбалки
 * @property fishRepository - репозиторий для работы с рыбами
 * @property locationProvider - провайдер местоположения
 * @property geocoderService - сервис геодекодирования
 * @property tipsGenerator - генератор советов
 * @property seasonRecommendationService - сервис рекомендаций по сезонам
 * @property tackleRecommendationService - сервис рекомендаций по снастям
 * @property baitRecommendation - сервис рекомендаций по наживке
 * @property personalizedTipService - сервис персонализированных советов
 * @property lunarCalendar - календарь лунных фаз
 * @property fishingCalendar - календарь рыболовных сезонов
 * @property clock - часы для работы с временем
 * @property fishingService - сервис прогноза рыбалки
 */


@HiltViewModel
class MainViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val fishingAnalyticsService: FishingAnalyticsService,
    private val fishRepository: FishRepository,
    private val locationProvider: LocationProvider,
    private val geocoderService: GeocoderService,
    private val tipsGenerator: TipsGenerator,
    private val seasonRecommendationService: SeasonRecommendationService,
    private val tackleRecommendationService: TackleRecommendationService,
    private val baitRecommendation: BaitRecommendationService,
    private val personalizedTipService: PersonalizedTipService,
    private val lunarCalendar: LunarCalendar,
    private val fishingCalendar: FishingCalendar,
    private val clock: Clock,
    private val fishingService: FishingService,
    private val fishForecastService: FishForecastService
) : ViewModel() {

    // ============================================================
    // STATE
    // ============================================================

    private val _uiState = MutableStateFlow(MainUiState())

    val uiState: StateFlow<MainUiState> =
        _uiState.asStateFlow()

    private val _temperatureUnit = MutableStateFlow(
        weatherRepository.getTemperatureUnit()
    )

    val temperatureUnit: StateFlow<TemperatureUnit> =
        _temperatureUnit.asStateFlow()

    // ============================================================
    // FORECAST JOB MANAGEMENT
    // ============================================================

    /**
     * Единственная корутина, управляющая загрузкой
     * и пересчётом прогноза.
     */
    private var forecastJob: Job? = null

    /**
     * Идентификатор последнего запроса.
     * Нужен для защиты от обновления UI устаревшими
     * результатами расчёта.
     */
    private var forecastRequestId = 0L

    // ============================================================
    // INITIALIZATION
    // ============================================================

    init {
        updateLunarInfo()
    }

    /**
     * Запускает загрузку или пересчёт прогноза.
     * Отменяет предыдущую операцию и обрабатывает ошибки.
     */
    private fun launchForecastOperation(
        block: suspend (Long) -> Unit
    ) {
        val requestId = ++forecastRequestId

        forecastJob?.cancel()

        forecastJob = viewModelScope.launch {
            try {
                block(requestId)

            } catch (e: CancellationException) {
                // Отмена корутины не является ошибкой.
                throw e

            } catch (e: Exception) {

                // Не позволяем устаревшему запросу
                // перезаписать состояние нового запроса.
                if (requestId == forecastRequestId) {

                    val userMessage = getUserFriendlyErrorMessage(e)

                    updateState {
                        copy(
                            appState = AppState.Error(userMessage),
                            isFishingForecastLoading = false,
                            fishingForecastError = userMessage
                        )
                    }

                    // Здесь можно добавить логирование исходной ошибки.
                    // Например, Log.e("MainViewModel", "Ошибка загрузки прогноза", e)
                }
            }
        }
    }

    /**
     * Преобразует техническую ошибку в понятное сообщение.
     */
    private fun getUserFriendlyErrorMessage(
        exception: Exception
    ): String {

        // Проверяем не только само исключение,
        // но и вложенные причины ошибки.
        var cause: Throwable? = exception

        while (cause != null) {

            when (cause) {

                is java.net.SocketTimeoutException -> {
                    return "Не удалось получить данные о погоде: " +
                            "истекло время ожидания ответа.\n\n" +
                            "Возможные причины:\n" +
                            "• Медленное или нестабильное интернет-соединение.\n" +
                            "• Сервис погоды временно не отвечает.\n\n" +
                            "Проверьте интернет и попробуйте ещё раз."
                }

                is java.net.UnknownHostException -> {
                    return "Не удалось подключиться к сервису погоды.\n\n" +
                            "Проверьте подключение к интернету, " +
                            "Wi-Fi или мобильную сеть и повторите попытку."
                }

                is java.net.ConnectException -> {
                    return "Не удалось установить соединение с сервисом погоды.\n\n" +
                            "Возможно, сервис временно недоступен. " +
                            "Попробуйте ещё раз через несколько минут."
                }
            }

            cause = cause.cause
        }

        // Дополнительная проверка текстовых сообщений.
        val error = (
                exception.message.orEmpty() + " " +
                        exception.javaClass.simpleName
                ).lowercase(java.util.Locale.ROOT)

        return when {

            "timeout" in error ||
                    "timed out" in error -> {
                "Сервис погоды не ответил вовремя.\n\n" +
                        "Проверьте подключение к интернету " +
                        "и попробуйте загрузить прогноз ещё раз."
            }

            "429" in error -> {
                "Сервис погоды получил слишком много запросов.\n\n" +
                        "Подождите немного и повторите попытку."
            }

            "401" in error || "403" in error -> {
                "Не удалось получить данные от сервиса погоды.\n\n" +
                        "Возможно, возникла проблема с доступом к сервису. " +
                        "Попробуйте позже."
            }

            "500" in error ||
                    "502" in error ||
                    "503" in error ||
                    "504" in error -> {
                "Сервис погоды временно недоступен.\n\n" +
                        "Попробуйте повторить запрос через несколько минут."
            }

            exception is java.io.IOException -> {
                "Не удалось загрузить данные о погоде.\n\n" +
                        "Возможно, соединение прервалось. " +
                        "Проверьте интернет и попробуйте ещё раз."
            }

            else -> {
                "Не удалось загрузить данные о погоде " +
                        "и сформировать прогноз клёва.\n\n" +
                        "Возможно, произошла временная техническая ошибка. " +
                        "Попробуйте повторить запрос."
            }
        }
    }


    /** Проверяет, что текущая операция ещё актуальна.
     * Вызывается после длительных операций и перед
     * обновлением UI.
     */
    private suspend fun ensureCurrentRequest(
        requestId: Long
    ) {
        currentCoroutineContext().ensureActive()

        if (requestId != forecastRequestId) {
            throw CancellationException(
                "Запрос отменён: запущена новая операция"
            )
        }
    }

    // ============================================================
    // PUBLIC METHODS
    // ============================================================
    /** Первоначальный запуск приложения.*/
    fun start() {
        // Не запускаем первоначальную загрузку повторно,
        // если приложение уже загружает данные или готово к работе.
        when (_uiState.value.appState) {
            AppState.LoadingLocation,
            AppState.LoadingWeather,
            AppState.Ready -> return

            AppState.Idle,
            is AppState.Error -> Unit
        }

        launchForecastOperation { requestId ->
            getCurrentLocation(requestId)
        }
    }

    /** Обработка отказа в разрешении на геолокацию.
     * Используется локация по умолчанию. */
    fun onPermissionDenied() {
        launchForecastOperation { requestId ->
            val location = createDefaultLocation()

            ensureCurrentRequest(requestId)

            updateState {
                copy(
                    location = location,
                    locationName = location.name,
                    regionName = DEFAULT_REGION,
                    isUsingDefaultLocation = true,
                    canUseDefaultLocation = true
                )
            }

            loadFullForecast(
                location = location,
                requestId = requestId
            )
        }
    }

    /** Продолжение работы приложения со стандартной локацией.*/
    fun useDefaultLocation() {
        launchForecastOperation { requestId ->
            val location = createDefaultLocation()
            ensureCurrentRequest(requestId)

            updateState {
                copy(
                    location = location,
                    locationName = location.name,
                    regionName = DEFAULT_REGION,
                    isUsingDefaultLocation = true,
                    canUseDefaultLocation = true
                )
            }

            loadFullForecast(
                location = location,
                requestId = requestId
            )
        }
    }


    /** Полное обновление прогноза. */
    fun refreshForecast() {

        val location = _uiState.value.location
            ?: createDefaultLocation()

        launchForecastOperation { requestId ->

            loadFullForecast(
                location = location,
                requestId = requestId
            )
        }
    }

    /** Включение или отключение использования реальной температуры воды. */
    fun setUseRealWaterTemperature(
        enabled: Boolean
    ) {

        // Сохраняем настройку.
        weatherRepository.setUseRealWaterTemperature(enabled)

        // Предыдущий расчёт будет отменён.
        launchForecastOperation { requestId ->

            refreshWithCurrentWeather(
                enabled = enabled,
                requestId = requestId
            )
        }
    }

    /** Обновление единицы измерения температуры. */
    fun refreshTemperatureUnit() {
        _temperatureUnit.value =
            weatherRepository.getTemperatureUnit()
    }

    // ============================================================
    // LOCATION
    // ============================================================
    /** Создание локации по умолчанию. */
    private fun createDefaultLocation(): LocationEntity {
        return LocationEntity(
            name = DEFAULT_LOCATION_NAME,
            regionId = null,
            lat = DEFAULT_LAT,
            lon = DEFAULT_LON,
            zoneId = ZoneId.of(DEFAULT_TIMEZONE)
        )
    }

    /** Получение текущей локации устройства. */
    private suspend fun getCurrentLocation(
        requestId: Long
    ) {
        ensureCurrentRequest(requestId)
        setAppState(AppState.LoadingLocation)
        val result = locationProvider.getCurrentLocation()
        ensureCurrentRequest(requestId)

        when (result) {
            is LocationResult.Success -> {
                handleLocationSuccess(
                    result = result,
                    requestId = requestId
                )
            }
            else -> {
                handleLocationFailure(requestId)
            }
        }
    }

    /**
     * Обработка успешного получения координат.
     */
    private suspend fun handleLocationSuccess(
        result: LocationResult.Success,
        requestId: Long
    ) {
        ensureCurrentRequest(requestId)

        val address = runCatching {
            geocoderService.getAddress(
                result.latitude,
                result.longitude
            )
        }.getOrNull() ?: "Текущее местоположение"

        ensureCurrentRequest(requestId)

        val region = runCatching {
            geocoderService.getRegion(
                result.latitude,
                result.longitude
            )
        }.getOrNull()

        ensureCurrentRequest(requestId)

        val location = LocationEntity(
            name = address,
            regionId = null,
            lat = result.latitude,
            lon = result.longitude,
            zoneId = ZoneId.systemDefault()
        )

        updateState {
            copy(
                location = location,
                locationName = location.name,
                regionName = region,
                isUsingDefaultLocation = false,
                canUseDefaultLocation = false
            )
        }

        loadFullForecast(
            location = location,
            requestId = requestId
        )
    }

    /**
     * Обработка ошибки получения координат.
     */
    private suspend fun handleLocationFailure(
        requestId: Long
    ) {
        ensureCurrentRequest(requestId)
        val location = createDefaultLocation()

        updateState {
            copy(
                location = location,
                locationName = location.name,
                regionName = DEFAULT_REGION,
                isUsingDefaultLocation = true
            )
        }

        loadFullForecast(
            location = location,
            requestId = requestId
        )
    }

    // ============================================================
    // FULL FORECAST
    // ============================================================
    /**
     * Полная загрузка и расчёт прогноза.
     *
     * Последовательность:
     * 1. Получение текущей погоды.
     * 2. Получение прогноза погоды.
     * 3. Анализ погодных условий.
     * 4. Расчёт прогноза рыбалки.
     * 5. Обновление состояния UI.
     */
    private suspend fun loadFullForecast(
        location: LocationEntity,
        requestId: Long
    ) {
        ensureCurrentRequest(requestId)
        setAppState(AppState.LoadingWeather)

        updateState {
            copy(
                isFishingForecastLoading = true,
                fishingForecastError = null
            )
        }

        // 1. Текущая погода.
        val current = weatherRepository.getCurrentWeather(
            location.lat,
            location.lon
        )

        ensureCurrentRequest(requestId)

        // 2. Прогноз погоды.
        val weatherForecast = weatherRepository.getForecast(
            location.lat,
            location.lon
        )

        ensureCurrentRequest(requestId)

        // 3. Текущие дата и время в часовом поясе локации.
        val now = fishingCalendar.now(location)
        val localDate = now.toLocalDate()

        // 4. Настройки температуры воды.
        val useRealTemp = weatherRepository.isUsingRealWaterTemperature()
        val realWaterTemp = weatherRepository.getRealWaterTemperature()
        ensureCurrentRequest(requestId)

        // 5. Анализ погоды для рыбалки.
        val fishingWeather =
            fishingAnalyticsService.analyzeFishingWeather(
                currentResponse = current,
                forecastResponse = weatherForecast,
                useRealWaterTemp = useRealTemp,
                realWaterTemp = realWaterTemp,
                date = localDate
            )
        ensureCurrentRequest(requestId)

        val allTimes = fishingService.getAllFishingTimes(
            location = location,
            date = localDate,
            biteRating = fishingWeather.biteRating,
            weather = fishingWeather,
            forecast = weatherForecast
        )

        // 6. Прогноз клёва.
        val fishingForecast =
            fishingService.getFishingForecast(
                location = location,
                date = localDate,
                biteRating = fishingWeather.biteRating,
                weather = fishingWeather,
                forecast = weatherForecast
            )
        ensureCurrentRequest(requestId)

        // 7. Пересчёт данных для UI.
        recalculateFromWeather(
            location = location,
            fishingWeather = fishingWeather,
            fishingForecast = fishingForecast,
            allTimes = allTimes,
            requestId = requestId
        )
    }

    // ============================================================
    // RECALCULATE USING CACHED WEATHER
    // ============================================================
    /** Пересчёт прогноза при изменении настройки температуры воды.
     * Использует кэшированные погодные данные. При отсутствии кэша выполняет полную загрузку. */
    private suspend fun refreshWithCurrentWeather(
        enabled: Boolean,
        requestId: Long
    ) {
        ensureCurrentRequest(requestId)
        val current = weatherRepository.getCachedCurrentWeather()
        val forecast = weatherRepository.getCachedForecast()

        ensureCurrentRequest(requestId)

        // При отсутствии кэша загружаем данные заново.
        if (current == null || forecast == null) {
            val location = _uiState.value.location
                ?: createDefaultLocation()
            loadFullForecast(
                location = location,
                requestId = requestId
            )
            return
        }

        val location = _uiState.value.location
            ?: createDefaultLocation()

        // Дата в часовом поясе локации.
        val now = fishingCalendar.now(location)
        val localDate = now.toLocalDate()

        // Пересчёт погодных условий.
        val fishingWeather =
            fishingAnalyticsService.analyzeFishingWeather(
                currentResponse = current,
                forecastResponse = forecast,
                useRealWaterTemp = enabled,
                realWaterTemp =
                    weatherRepository.getRealWaterTemperature(),
                date = localDate
            )

        ensureCurrentRequest(requestId)

        // Пересчёт прогноза рыбалки.
        val fishingForecast =
            fishingService.getFishingForecast(
                location = location,
                date = localDate,
                biteRating = fishingWeather.biteRating,
                weather = fishingWeather,
                forecast = forecast
            )

        ensureCurrentRequest(requestId)

        // Обновляем все зависимые данные.
        val allTimes = fishingService.getAllFishingTimes(
            location = location,
            date = localDate,
            biteRating = fishingWeather.biteRating,
            weather = fishingWeather,
            forecast = forecast
        )
        recalculateFromWeather(
            location = location,
            fishingWeather = fishingWeather,
            fishingForecast = fishingForecast,

            allTimes = allTimes,
            requestId = requestId
        )
    }

    // ============================================================
    // RECALCULATE UI DATA
    // ============================================================
    /** Пересчитывает лунную информацию, советы, рекомендованные виды рыб и состояние UI. */
    private suspend fun recalculateFromWeather(
        location: LocationEntity,
        fishingWeather: FishingWeather,
        fishingForecast: FishingForecast,
        allTimes: List<BestFishingTime>,
        requestId: Long
    ) {

        ensureCurrentRequest(requestId)

        // Текущие дата и время.
        val now = fishingCalendar.now(location)
        val localDate = now.toLocalDate()

        // 1. Лунная информация.
        val lunarInfo = lunarCalendar.getLunarInfo(localDate)
        ensureCurrentRequest(requestId)

        // 2. Дневные рекомендации.
        val dailyTips = buildDailyTips(
            weather = fishingWeather,
            now = now,
            lunarInfo = lunarInfo
        )

        ensureCurrentRequest(requestId)

        val season = fishingCalendar.getSeason(localDate)
        val timeOfDay = fishingCalendar.getTimeOfDay(
            location = location,
            dateTime = now
        )

        val fish = fishRepository.getRecommendedFish(
            season = season,
            timeOfDay = timeOfDay,
            weather = fishingWeather,
            lunarInfo = lunarInfo
        )

        ensureCurrentRequest(requestId)

        // 5. Рассчитываем индивидуальный прогноз для каждой рыбы.
        val fishForecasts = fish.map { fishItem ->
            fishForecastService.calculate(
                fish = fishItem,
                weather = fishingWeather,
                season = season,
                timeOfDay = timeOfDay,
                location = location,
                date = localDate,
                allTimes = allTimes
            )
        }.sortedByDescending { it.rating }

        ensureCurrentRequest(requestId)

        // 4. Обновление UI.
        updateState {
            copy(
                weather = fishingWeather,
                fishingForecast = fishingForecast,
                lunarInfo = lunarInfo,
                dailyTips = dailyTips,
                recommendedFish = fish,
                fishForecasts = fishForecasts,
                allFishingTimes = allTimes,
                isFishingForecastLoading = false,
                fishingForecastError = null,
                appState = AppState.Ready
            )
        }
    }

    // ============================================================
    // DAILY TIPS
    // ============================================================
    /**     * Построение дневных рекомендаций.     */
    private fun buildDailyTips(
        weather: FishingWeather,
        now: ZonedDateTime,
        lunarInfo: LunarInfo
    ): DailyTips {

        return DailyTips(
            timeRecommendation = tipsGenerator.getTimeOfDayRecommendation(now.hour),
            seasonRecommendation = seasonRecommendationService.getSeasonRecommendation(now.month),
            tackleRecommendations = tackleRecommendationService.getTackleRecommendations(weather),
            baitRecommendations = baitRecommendation.getBaitRecommendations(weather),
            groundbaitRecommendation = tipsGenerator.getGroundbaitRecommendations(weather),
            attractantRecommendation = tipsGenerator.getAttractantRecommendations(weather),
            tipOfTheDay = personalizedTipService.getPersonalizedTip(weather, lunarInfo)
        )
    }

    // ============================================================
    // LUNAR INFORMATION
    // ============================================================
    /**     * Обновление лунной информации при инициализации.     */
    private fun updateLunarInfo() {
        val today = LocalDate.now(clock)

        updateState {
            copy(
                lunarInfo = lunarCalendar.getLunarInfo(today)
            )
        }
    }

    // ============================================================
    // STATE HELPERS
    // ============================================================
    /**     * Установка состояния приложения.     */
    private fun setAppState(state: AppState) {
        updateState {
            copy(appState = state)
        }
    }

    /**     * Безопасное обновление StateFlow.     */
    private inline fun updateState(
        block: MainUiState.() -> MainUiState
    ) {
        _uiState.update { currentState ->
            currentState.block()
        }
    }
}

/**  * Дневные советы  */
data class DailyTips(
    val timeRecommendation: String = "",
    val seasonRecommendation: String = "",
    val tackleRecommendations: List<String> = emptyList(),
    val baitRecommendations: List<String> = emptyList(),
    val groundbaitRecommendation: String = "",
    val attractantRecommendation: String = "",
    val tipOfTheDay: String = ""
)