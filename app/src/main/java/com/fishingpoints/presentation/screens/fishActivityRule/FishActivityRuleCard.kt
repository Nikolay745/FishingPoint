package com.fishingpoints.presentation.screens.fishActivityRule

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fishingpoints.data.local.entity.FishActivityRule
import com.fishingpoints.data.local.enums.RuleSource
//
//@Composable
//fun FishActivityRuleCard(
//    rule: FishActivityRule,
//    fishName: String,
//    regionName: String,
//    onEdit: () -> Unit,
//    onDelete: () -> Unit
//) {
//
//    var expanded by remember {
//        mutableStateOf(false)
//    }
//
//    val isCustom = rule.source == RuleSource.CUSTOM
//
//
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(3.dp)
//    ) {
//
//        Column(
//            modifier = Modifier.padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//
//                Text(
//                    text = "🐟 $fishName",
//                    style = MaterialTheme.typography.titleLarge,
//                    modifier = Modifier.weight(1f)
//                )
//
//                Box {
//
//                    IconButton(
//                        onClick = {
//                            expanded = true
//                        }
//                    ) {
//                        Icon(
//                            Icons.Default.MoreVert,
//                            contentDescription = "Действия"
//                        )
//                    }
//
//                    DropdownMenu(
//                        expanded = expanded,
//                        onDismissRequest = {
//                            expanded = false
//                        }
//                    ) {
//
//                    }
//                        if (isCustom) {
//                        DropdownMenuItem(
//                            text = { Text("Редактировать") },
//                            leadingIcon = {
//                                Icon(
//                                    Icons.Default.Edit,
//                                    contentDescription = null
//                                )
//                            },
//                            onClick = {
//                                expanded = false
//                                onEdit()
//                            }
//                        )
//
//                        DropdownMenuItem(
//                            text = {
//                                Text(
//                                    "Удалить",
//                                    color = MaterialTheme.colorScheme.error
//                                )
//                            },
//                            leadingIcon = {
//                                Icon(
//                                    Icons.Default.Delete,
//                                    contentDescription = null,
//                                    tint = MaterialTheme.colorScheme.error
//                                )
//                            },
//                            onClick = {
//                                expanded = false
//                                onDelete()
//                            }
//                        )
//                    } else{
//                            DropdownMenuItem(
//                                text = {
//                                    Text("Базовое правило (только просмотр)")
//                                },
//                                enabled = false,
//                                onClick = {}
//                            )
//
//                        }
//                }
//            }
//
//            HorizontalDivider()
//
//            Text("🌍 Регион: $regionName")
//
//            Text(
//                "🌡 Температура воды: " +
//                        "${rule.temperatureMin?.let { "$it°C" } ?: "без ограничения"} — " +
//                        (rule.temperatureMax?.let { "$it°C" } ?: "без ограничения")
//            )
//
//            Text(
//                "📅 Сезон: ${rule.season?.displayName ?: "Любой"}"
//            )
//
//            Text(
//                "🕒 Время суток: ${rule.timeOfDay?.displayName ?: "Любое"}"
//            )
//
//            Text(
//                text = "Активность: ${rule.activity.displayText}",
//                style = MaterialTheme.typography.titleMedium
//            )
//
//            Text(
//                "Достоверность: ${rule.confidence.displayName}"
//            )
//
//            rule.description
//                ?.takeIf { it.isNotBlank() }
//                ?.let {
//                    HorizontalDivider()
//
//                    Text(
//                        text = it,
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                }
//
//            Text(
//                text = if (rule.source == RuleSource.DEFAULT)
//                    "Базовое правило"
//                else
//                    "Пользовательское правило",
//                style = MaterialTheme.typography.labelSmall,
//                color = MaterialTheme.colorScheme.primary
//            )
//        }
//    }
//}
@Composable
fun FishActivityRuleCard(
    rule: FishActivityRule,
    fishName: String,
    regionName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    val isDefault = rule.source == RuleSource.DEFAULT

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ЗАГОЛОВОК
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "🐟",
                    fontSize = 24.sp
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    text = fishName,
                    modifier = Modifier
                        .weight(1f)
                        .widthIn(min = 0.dp),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Box {
                    IconButton(
                        onClick = {
                            expanded = true
                        }
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Действия"
                        )
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {

                        if (!isDefault) {
                            DropdownMenuItem(
                                text = {
                                    Text("Редактировать")
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    onEdit()
                                }
                            )

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Удалить",
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    expanded = false
                                    onDelete()
                                }
                            )
                        } else {
                            DropdownMenuItem(
                                text = {
                                    Text("Базовое правило")
                                },
                                enabled = false,
                                onClick = {}
                            )
                        }
                    }
                }
            }

            // МЕТКА ТИПА ПРАВИЛА
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isDefault)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = if (isDefault)
                        "🔒 Базовое правило · Только просмотр"
                    else
                        "✏️ Пользовательское правило",
                    modifier = Modifier.padding(
                        horizontal = 10.dp,
                        vertical = 6.dp
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDefault)
                        MaterialTheme.colorScheme.onSecondaryContainer
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            HorizontalDivider()

            // РЕГИОН
            RuleInfoRow(
                icon = "🌍",
                title = "Регион",
                value = regionName
            )

            // ТЕМПЕРАТУРА
            RuleInfoRow(
                icon = "🌡️",
                title = "Температура воды",
                value = buildString {
                    append(
                        rule.temperatureMin?.let { "$it °C" }
                            ?: "Без ограничения"
                    )
                    append(" — ")
                    append(
                        rule.temperatureMax?.let { "$it °C" }
                            ?: "Без ограничения"
                    )
                }
            )

            // СЕЗОН
            RuleInfoRow(
                icon = "📅",
                title = "Сезон",
                value = rule.season?.displayName ?: "Любой сезон"
            )

            // ВРЕМЯ СУТОК
            RuleInfoRow(
                icon = "🕒",
                title = "Время суток",
                value = rule.timeOfDay?.displayName ?: "Любое время"
            )

            // АКТИВНОСТЬ
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Активность питания",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = rule.activity.displayText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // ДОСТОВЕРНОСТЬ
            RuleInfoRow(
                icon = "📊",
                title = "Достоверность",
                value = rule.confidence.displayName
            )

            // ОПИСАНИЕ
            rule.description
                ?.takeIf { it.isNotBlank() }
                ?.let { description ->

                    HorizontalDivider()

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "💡 Описание правила",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
        }
    }
}

@Composable
private fun RuleInfoRow(
    icon: String,
    title: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {

        Text(
            text = icon,
            fontSize = 18.sp
        )

        Spacer(Modifier.width(8.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
