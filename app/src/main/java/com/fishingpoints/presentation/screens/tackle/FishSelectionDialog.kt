package com.fishingpoints.presentation.screens.tackle

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fishingpoints.data.local.entity.Fish

@Composable
fun FishSelectionDialog(
    fish: List<Fish>,
    selectedFishIds: Set<Long>,
    onDismiss: () -> Unit,
    onSelectionChanged: (Set<Long>) -> Unit
) {
    var selected by remember(selectedFishIds) {
        mutableStateOf(selectedFishIds)
    }

    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text("Выбор рыб")
        },

        text = {

            if (fish.isEmpty()) {

                Text(
                    text = "Список рыб пуст",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            } else {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {

                    items(
                        items = fish,
                        key = { it.id }
                    ) { item ->

                        val checked = item.id in selected

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {

                                    selected =
                                        if (checked) {
                                            selected - item.id
                                        } else {
                                            selected + item.id
                                        }

                                }
                                .padding(vertical = 4.dp),

                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->

                                    selected =
                                        if (isChecked) {
                                            selected + item.id
                                        } else {
                                            selected - item.id
                                        }
                                }
                            )

                            Text(
                                text = item.name,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }
            }
        },

        confirmButton = {

            TextButton(
                onClick = {
                    onSelectionChanged(selected)
                    onDismiss()
                }
            ) {
                Text("Готово")
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {
                Text("Отмена")
            }
        }
    )
}
