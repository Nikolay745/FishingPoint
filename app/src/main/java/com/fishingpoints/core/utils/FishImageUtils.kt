package com.fishingpoints.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.presentation.components.FullScreenPhotoViewer
import java.io.File

object FishImageUtils {
    // Маппинг названий рыб на имена файлов изображений
    private val fishImageMap = mapOf(
        "Щука" to "pike",
        "Окунь" to "perch",
        "Ерш" to "ruff",
        "Карп" to "carp",
        "Карась" to "carassius_carassius",
        "Карась серебряный" to "carassiu_gibelio",
        "Налим" to "burbot",
        "Плотва" to "roach",
        "Сом" to "som",
        "Судак" to "sudak",
        "Красноперка" to "rudd",
        "Лещ" to "bream",
        "Линь" to "liny",
        "Белый амур" to "whiteamur",
        "Толстолобик" to "tolstolobik",
        "Голавль" to "chub",
        "Жерех" to "zherekh",
        "Чехонь" to "chekhon",
        "Сазан" to "sazan",
        "Рыбец" to "vimba",
        "Хариус" to "harjus",
        "Форель" to "forel",
        "Рак пресноводный" to "crayfish",
    )

    fun getImageResourceName(fishName: String): String {
        return fishImageMap[fishName] ?: "fish_default"
    }

    @SuppressLint("DiscouragedApi")
    fun getImageResourceId(context: Context, fishName: String): Int {
        val resourceName = getImageResourceName(fishName)
        return context.resources.getIdentifier(
            resourceName,
            "drawable",
            context.packageName
        ).takeIf { it != 0 } ?: context.resources.getIdentifier(
            "fish_default",
            "drawable",
            context.packageName
        )
    }

    fun getImageUri(context: Context, fishName: String): String {
        val resourceId = getImageResourceId(context, fishName)
        return "android.resource://${context.packageName}/$resourceId"
    }

    fun getDisplayImageUri(
        context: Context,
        fish: Fish
    ): String {

        val imageUrl = fish.imageUrl

        return when {

            // Нет изображения — встроенная картинка
            imageUrl.isNullOrBlank() -> {
                getImageUri(
                    context,
                    fish.name
                )
            }

            // HTTP / HTTPS
            imageUrl.startsWith("http://") ||
                    imageUrl.startsWith("https://") -> {
                imageUrl
            }

            // content://
            imageUrl.startsWith("content://") -> {
                imageUrl
            }

            // android.resource://
            imageUrl.startsWith("android.resource://") -> {
                imageUrl
            }

            // file://
            imageUrl.startsWith("file://") -> {
                imageUrl
            }

            // Абсолютный путь к файлу
            // /data/user/0/com.fishingpoints/files/fish_xxx.jpg
            imageUrl.startsWith("/") -> {

                val file = File(imageUrl)

                if (file.exists()) {
                    Uri.fromFile(file).toString()
                } else {
                    getImageUri(
                        context,
                        fish.name
                    )
                }
            }

            // Имя drawable
            !imageUrl.contains("/") &&
                    !imageUrl.contains(".") -> {

                val resourceId = context.resources.getIdentifier(
                    imageUrl,
                    "drawable",
                    context.packageName
                )

                if (resourceId != 0) {

                    "android.resource://${context.packageName}/$resourceId"

                } else {

                    getImageUri(
                        context,
                        fish.name
                    )
                }
            }

            // Неизвестный формат
            else -> {
                getImageUri(
                    context,
                    fish.name
                )
            }
        }
    }


}



@SuppressLint("LocalContextResourcesRead", "DiscouragedApi")
@Composable
fun FishImage(
    fish: Fish,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: Painter? = null,
    error: Painter? = null
) {
    val context = LocalContext.current

    var showFullScreen by remember {
        mutableStateOf(false)
    }

    val imageUrl = fish.imageUrl

    val defaultPlaceholder = placeholder ?: painterResource(
        id = context.resources.getIdentifier(
            "icon_fish2",
            "drawable",
            context.packageName
        ).takeIf { it != 0 }
            ?: android.R.drawable.ic_menu_gallery
    )

    val defaultError = error ?: painterResource(
        id = context.resources.getIdentifier(
            "placeholder_image",
            "drawable",
            context.packageName
        ).takeIf { it != 0 }
            ?: android.R.drawable.ic_menu_report_image
    )

    /*
     * ВАЖНО:
     * clickable находится снаружи всех вариантов изображения.
     * Поэтому клик работает независимо от того,
     * откуда реально загружена картинка.
     */
    Box(
        modifier = modifier
            .clickable {
                showFullScreen = true
            }
    ) {

        when {
            // ---------------------------------------------------------
            // HTTP / HTTPS
            // ---------------------------------------------------------
            !imageUrl.isNullOrBlank() &&
                    (
                            imageUrl.startsWith("http://") ||
                                    imageUrl.startsWith("https://")
                            ) -> {

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Изображение ${fish.name}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    placeholder = defaultPlaceholder,
                    error = defaultError
                )
            }

            // ---------------------------------------------------------
            // FILE
            // ---------------------------------------------------------
            !imageUrl.isNullOrBlank() &&
                    (
                            imageUrl.startsWith("file://") ||
                                    imageUrl.startsWith("/")
                            ) -> {

                val file = if (imageUrl.startsWith("file://")) {
                    imageUrl.toUri().path?.let {
                        File(it)
                    }
                } else {
                    File(imageUrl)
                }


                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(file)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Изображение ${fish.name}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    placeholder = defaultPlaceholder,
                    error = defaultError
                )
            }

            // ---------------------------------------------------------
            // CONTENT URI / ANDROID RESOURCE
            // ---------------------------------------------------------
            !imageUrl.isNullOrBlank() &&
                    (
                            imageUrl.startsWith("content://") ||
                                    imageUrl.startsWith("android.resource://")
                            ) -> {

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Изображение ${fish.name}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale,
                    placeholder = defaultPlaceholder,
                    error = defaultError
                )
            }

            // ---------------------------------------------------------
            // ИМЯ DRAWABLE
            // Например: "karp", "som", "okuny"
            // ---------------------------------------------------------
            !imageUrl.isNullOrBlank() &&
                    !imageUrl.contains("/") &&
                    !imageUrl.contains(".") -> {

                val resourceId = context.resources.getIdentifier(
                    imageUrl,
                    "drawable",
                    context.packageName
                )

                if (resourceId != 0) {

                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(resourceId)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Изображение ${fish.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = contentScale,
                        placeholder = defaultPlaceholder,
                        error = defaultError
                    )

                } else {

                    Image(
                        painter = getDefaultFishImage(
                            context,
                            fish.name
                        ),
                        contentDescription = "Изображение ${fish.name}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = contentScale
                    )
                }
            }

            // ---------------------------------------------------------
            // IMAGE URL ПУСТОЙ
            // ---------------------------------------------------------
            else -> {

                Image(
                    painter = getDefaultFishImage(
                        context,
                        fish.name
                    ),
                    contentDescription = "Изображение ${fish.name}",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
        }
    }

    // -------------------------------------------------------------
    // FULL SCREEN
    // -------------------------------------------------------------

    if (showFullScreen) {

        FullScreenPhotoViewer(
            photoUri = FishImageUtils.getDisplayImageUri(
                context = context,
                fish = fish
            ),
            onDismiss = {
                showFullScreen = false
            }
        )
    }
}


// Функция для получения изображения по умолчанию
@SuppressLint("DiscouragedApi")
@Composable
fun getDefaultFishImage(context: Context, fishName: String): Painter {
    // Пытаемся найти изображение по названию рыбы
    val resourceName = FishImageUtils.getImageResourceName(fishName)
    var resourceId = context.resources.getIdentifier(
        resourceName,
        "drawable",
        context.packageName
    )

    // Если не найдено, пробуем по названию рыбы в нижнем регистре
    if (resourceId == 0) {
        val nameLower = fishName.lowercase().replace(" ", "_")
        resourceId = context.resources.getIdentifier(
            nameLower,
            "drawable",
            context.packageName
        )
    }

    // Если все еще не найдено, используем стандартную заглушку
    return if (resourceId != 0) {
        painterResource(id = resourceId)
    } else {
        // Пытаемся найти icon_fish2 или используем стандартный
        val fallbackId = context.resources.getIdentifier(
            "icon_fish2",
            "drawable",
            context.packageName
        ).takeIf { it != 0 } ?: android.R.drawable.ic_menu_gallery

        painterResource(id = fallbackId)
    }
}







