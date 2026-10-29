package com.fishingpoints.presentation.screens.tackle

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
//import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fishingpoints.data.local.entity.Tackle

@Composable
fun TackleInfoDialog(
    tackle: Tackle,
    viewModel: TackleViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val fish by viewModel
        .getFishForTackle(tackle.id)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Закрыть")
            }},

        title = {
            Column(modifier = Modifier
                .fillMaxWidth()

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
                    text = tackle.name
                )
            }

        },

        text = {
            Column(modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Text( text = "Тип — ${tackle.type.title }" )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Описание - ${tackle.description }")
                HorizontalDivider()
                Text("Рыбы:")
                if (fish.isEmpty()) {

                    Text(
                        text = "Не указаны",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                } else {

                    fish.forEach { item ->
                        Text(
                            text = "• ${item.name}"
                        )
                    }
                }
            }
        }
        )
}
