package com.fishingpoints.domain.permissions

/**
 * Состояние разрешений на определение местоположения
 *
 * Используется для отслеживания статуса разрешений в UI
 * и принятия решений о том, что показывать пользователю
 *
 * @property hasPermission - есть ли у приложения разрешение
 * @property shouldShowRationale - нужно ли показать объяснение пользователю
 * @property isPermissionPermanentlyDenied - запрещено навсегда
 * @property deniedPermissions - список запрещенных разрешений (для детализации)
 */
data class LocationPermissionState(
    val hasPermission: Boolean = false,
    val shouldShowRationale: Boolean = false,
    val isPermissionPermanentlyDenied: Boolean = false,
    val deniedPermissions: List<String> = emptyList()
) {
    /**
     * Можно ли запрашивать разрешение
     */
    fun canRequestPermission(): Boolean =
        !hasPermission && !isPermissionPermanentlyDenied

    /**
     * Нужно ли показывать объяснение
     */
    fun needsRationale(): Boolean =
        !hasPermission && shouldShowRationale

    /**
     * Пользователь навсегда запретил разрешения
     */
    fun isBlocked(): Boolean =
        !hasPermission && isPermissionPermanentlyDenied

    companion object {
        /**
         * Начальное состояние (еще не проверяли)
         */
        fun initial() = LocationPermissionState(
            hasPermission = false,
            shouldShowRationale = false,
            isPermissionPermanentlyDenied = false
        )

        /**
         * Состояние "Разрешение получено"
         */
        fun granted() = LocationPermissionState(
            hasPermission = true,
            shouldShowRationale = false,
            isPermissionPermanentlyDenied = false
        )
    }
}