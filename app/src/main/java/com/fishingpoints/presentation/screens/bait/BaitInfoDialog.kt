package com.fishingpoints.presentation.screens.bait

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
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
import com.fishingpoints.data.local.entity.BaitWithFish

@Composable
fun BaitInfoDialog(
    baitWithFish: BaitWithFish,
    onDismiss: () -> Unit
) {
    val bait = baitWithFish.bait
    val fish = baitWithFish.fish

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Закрыть")
            }
        },

        title = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Article,
                        contentDescription = null
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text("Информация")
                }

                Spacer(modifier = Modifier.height(8.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    fontStyle = FontStyle.Italic,
                    text = bait.name
                )
            }
        },

        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Тип - ${bait.type.title}"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Лучший сезон - ${bait.bestSeason.title}"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    if (fish.isNotEmpty()) {
                        "Для следующих рыб: ${
                            fish.joinToString(", ") { it.name }
                        }"
                    } else {
                        "Для следующих рыб: не указано"
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Описание ")

                Text(
                    "    ${ bait.description
                            .takeIf { it.isNotBlank() }
                            ?: "Нет описания"
                    }"
                )

            }
        }
    )
}
