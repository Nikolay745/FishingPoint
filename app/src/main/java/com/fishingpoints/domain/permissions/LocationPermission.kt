package com.fishingpoints.domain.permissions


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Hook для управления разрешениями на определение местоположения
 *
 * Инкапсулирует всю логику работы с разрешениями:
 * - Проверка статуса
 * - Запрос разрешений
 * - Обработка результатов
 * - Отслеживание состояния
 *
 * @param onPermissionGranted - колбэк при получении разрешения
 * @param onPermissionDenied - колбэк при отказе в разрешении
 * @param onShowRationale - колбэк, когда нужно показать объяснение
 * @param autoRequest - автоматически запрашивать разрешение при первом запуске
 * @return LocationPermissionState - текущее состояние разрешений
 *
 * Пример использования:
 * ```
 * @Composable
 * fun MainScreen() {
 *     val permissionState = rememberLocationPermission(
 *         onPermissionGranted = { viewModel.start() },
 *         onPermissionDenied = { viewModel.onPermissionDenied() }
 *     )
 *
 *     if (!permissionState.hasPermission) {
 *         LocationPermissionScreen(permissionState)
 *     } else {
 *         MainContent()
 *     }
 * }
 * ```
 */

@Composable
fun rememberLocationPermissionState(
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {},
    onShowRationale: (() -> Unit)? = null,
    autoRequest: Boolean = true
): PermissionStateWithAction {

    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionState by remember {
        mutableStateOf(LocationPermissionState.initial())
    }

    var wasPermissionGranted by remember {
        mutableStateOf(false)
    }

    var hasRequestedPermission by rememberSaveable {
        mutableStateOf(false)
    }

    var hasShownRationale by rememberSaveable {
        mutableStateOf(false)
    }

    fun checkPermissionStatus(): LocationPermissionState {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            return LocationPermissionState.granted()
        }

        val shouldShowRationale = activity?.let { act ->
            ActivityCompat.shouldShowRequestPermissionRationale(
                act,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) || ActivityCompat.shouldShowRequestPermissionRationale(
                act,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } ?: false

        return LocationPermissionState(
            hasPermission = false,
            shouldShowRationale = shouldShowRationale,
            isPermissionPermanentlyDenied =
                hasRequestedPermission && !shouldShowRationale
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->

        val fineGranted =
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

        val coarseGranted =
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineGranted || coarseGranted) {
            permissionState = LocationPermissionState.granted()
            wasPermissionGranted = true
            hasShownRationale = false

            onPermissionGranted()
        } else {
            val currentState = checkPermissionStatus()

            val deniedPermissions = permissions
                .filterValues { !it }
                .keys
                .toList()

            permissionState = currentState.copy(
                deniedPermissions = deniedPermissions
            )

            wasPermissionGranted = false

            if (
                currentState.shouldShowRationale &&
                onShowRationale != null &&
                !hasShownRationale
            ) {
                hasShownRationale = true
                onShowRationale()
            }
        }

    }

    val openAppSettings: () -> Unit = {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
    }


    val requestPermission: () -> Unit = {
        if (activity != null) {
            val currentState = checkPermissionStatus()

            permissionState = currentState

            if (currentState.isPermissionPermanentlyDenied) {
                openAppSettings()
            } else {
                hasRequestedPermission = true

                launcher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }



    LaunchedEffect(autoRequest) {
        val currentState = checkPermissionStatus()

        permissionState = currentState
        wasPermissionGranted = currentState.hasPermission

        when {
            currentState.hasPermission -> {
                onPermissionGranted()
            }

            autoRequest &&
                    !currentState.isPermissionPermanentlyDenied -> {
                requestPermission()
            }

            else -> {
                // Не переключаем приложение на локацию по умолчанию.
                // Пользователь остаётся на экране разрешений.
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val currentState = checkPermissionStatus()

                permissionState = currentState

                val isPermissionGrantedNow =
                    currentState.hasPermission

                if (
                    isPermissionGrantedNow &&
                    !wasPermissionGranted
                ) {
                    onPermissionGranted()
                }

                wasPermissionGranted = isPermissionGrantedNow
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    return PermissionStateWithAction(
        state = permissionState,
        requestPermission = requestPermission,
        openAppSettings = openAppSettings
    )
}



/**
 * Состояние разрешений с возможностью действий
 */
data class PermissionStateWithAction(
    val state: LocationPermissionState,
    val requestPermission: () -> Unit,
    val openAppSettings: () -> Unit
) {
    // Делегируем все поля из state
    val hasPermission: Boolean get() = state.hasPermission
    val shouldShowRationale: Boolean get() = state.shouldShowRationale
    val isPermissionPermanentlyDenied: Boolean get() = state.isPermissionPermanentlyDenied
    val deniedPermissions: List<String> get() = state.deniedPermissions
}