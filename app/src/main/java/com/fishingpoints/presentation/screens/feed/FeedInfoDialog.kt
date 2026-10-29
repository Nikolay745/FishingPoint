package com.fishingpoints.presentation.screens.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.fishingpoints.data.local.entity.Feed


@Composable
fun FeedInfoDialog (
    feed: Feed?,
    onDismiss: () -> Unit
) {

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Закрыть")
            }
        },

        title = {
            Column(modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())

            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically

                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Article,
                        contentDescription = null
                    )

                    Text(
                        " Информация"
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Text( modifier = Modifier
                    .fillMaxWidth(),
                    fontStyle = FontStyle.Italic,
                    text = feed?.name ?: "Название не указано"
                )
            }

        },

        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Описание - ${feed?.description ?: "Нет описания"}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Лучший сезон - ${feed?.bestSeason?.title ?: "Не указан"}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Для следующих рыб: ${feed?.bestForFish ?: "Не указано"}")
            }
        }
    )
}