package com.fishingpoints.presentation.components

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun FullScreenPhotoViewer(
    photoUri: String,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var imageSize by remember { mutableStateOf(IntSize.Zero) }

    val minScale = 1f
    val maxScale = 5f
    val doubleTapScale = 2.5f

    fun sharePhoto() {
        try {
            val uri = Uri.parse(photoUri)

            val shareUri = when (uri.scheme) {

                "file" -> {
                    val file = File(uri.path ?: return)

                    if (!file.exists()) {
                        return
                    }

                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                }

                "content" -> {
                    uri
                }

                "android.resource" -> {
                    uri
                }

                else -> {
                    return
                }
            }

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, shareUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(
                    intent,
                    "Поделиться фотографией"
                )
            )

        } catch (e: Exception) {
            Log.e(
                "FullScreenPhotoViewer",
                "Ошибка при отправке изображения",
                e
            )
        }
    }



    /*
     * Максимальное допустимое перемещение картинки.
     */
    fun calculateMaxOffset(
        currentScale: Float
    ): Offset {
        if (
            containerSize.width == 0 ||
            containerSize.height == 0 ||
            imageSize.width == 0 ||
            imageSize.height == 0
        ) {
            return Offset.Zero
        }

        val scaledWidth = imageSize.width * currentScale
        val scaledHeight = imageSize.height * currentScale
        val maxX = maxOf(0f, (scaledWidth - containerSize.width) / 2f)
        val maxY = maxOf(0f, (scaledHeight - containerSize.height) / 2f)

        return Offset(
            x = maxX,
            y = maxY
        )
    }

    /*
     * Не даём фотографии уехать за пределы экрана.
     */
    fun clampOffset(
        value: Offset,
        currentScale: Float
    ): Offset {

        val maxOffset = calculateMaxOffset(currentScale)

        return Offset(
            x = value.x.coerceIn(
                -maxOffset.x,
                maxOffset.x
            ),

            y = value.y.coerceIn(
                -maxOffset.y,
                maxOffset.y
            )
        )
    }

    /*
     * Плавно меняем zoom.
     */
    fun animateZoom(
        targetScale: Float,
        targetOffset: Offset
    ) {

        val clamped =
            clampOffset(
                targetOffset,
                targetScale
            )

        scope.launch {

            val startScale = scale
            val startOffset = offset

            val animation =
                androidx.compose.animation.core.Animatable(0f)

            animation.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 250,
                    easing = FastOutSlowInEasing
                )
            ) {
                val progress = value

                scale = startScale + (targetScale - startScale) * progress

                offset =
                    Offset(
                        x = startOffset.x + (clamped.x - startOffset.x) * progress,
                        y = startOffset.y + (clamped.y - startOffset.y) * progress
                    )
            }

            scale = targetScale
            offset = clamped
        }
    }

    /*
     * Состояние pinch-to-zoom.
     */
    val transformState =
        rememberTransformableState {
                zoomChange,
                panChange,
                _ ->

            val newScale =
                (scale * zoomChange)
                    .coerceIn(
                        minScale,
                        maxScale
                    )

            val newOffset =
                if (newScale > minScale) {
                    offset + panChange
                } else {
                    Offset.Zero
                }

            scale = newScale

            offset =
                clampOffset(
                    newOffset,
                    newScale
                )
        }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .onSizeChanged {
                    containerSize = it
                }
        ) {

            // ---------------------------------------------------------
            // ФОТО
            // ---------------------------------------------------------

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(photoUri)
                    .crossfade(true)
                    .build(),

                contentDescription = "Фото в полном размере",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()

                    /*
                     * Запоминаем фактический размер
                     * composable с изображением.
                     */
                    .onGloballyPositioned { coordinates ->
                        imageSize =
                            coordinates.size
                    }

                    /*
                     * Масштабирование и перемещение.
                     */
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale

                        translationX = offset.x
                        translationY = offset.y
                    }

                    /*
                     * DOUBLE TAP
                     */
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = { tapPosition ->
                                if (scale > 1.01f) {

                                    // ---------------------------------
                                    // Вернуться к 1x
                                    // ---------------------------------
                                    animateZoom(
                                        targetScale = 1f,
                                        targetOffset = Offset.Zero
                                    )

                                } else {

                                    // ---------------------------------
                                    // Увеличить к месту нажатия
                                    // ---------------------------------

                                    val targetScale = doubleTapScale

                                    val center = Offset(
                                        x = containerSize.width / 2f,
                                        y = containerSize.height / 2f
                                    )

                                    val tapFromCenter = tapPosition - center

                                    val targetOffset = Offset(
                                        x = -tapFromCenter.x * (targetScale - 1f),
                                        y = -tapFromCenter.y * (targetScale - 1f)
                                    )

                                    animateZoom(
                                        targetScale = targetScale,
                                        targetOffset = targetOffset
                                    )
                                }
                            }
                        )
                    }

                    /*
                     * PINCH + PAN
                     */
                    .transformable(
                        state = transformState
                    )
            )

            // ---------------------------------------------------------
            // CLOSE
            // ---------------------------------------------------------

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                // Поделиться
                IconButton(
                    onClick = { sharePhoto() },
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.55f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Поделиться",
                        tint = Color.White
                    )
                }

                // Закрыть
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.55f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Закрыть",
                        tint = Color.White
                    )
                }
            }


            // ---------------------------------------------------------
            // ПОДСКАЗКА
            // ---------------------------------------------------------

            AnimatedVisibility(
                visible = scale <= 1.01f,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            ) {

                Text(
                    text = "Двойное нажатие или pinch для увеличения",
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .background(
                            color = Color.Black.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp
                        )
                )
            }
        }
    }
}

