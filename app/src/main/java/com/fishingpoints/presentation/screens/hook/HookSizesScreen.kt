package com.fishingpoints.presentation.screens.hook

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

/** * Экран таблицы размеров крючков. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HookSizesScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(text = "📏 Размеры крючков")},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.navigateUp()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 12.dp)
        ) {

            Text(
                text = "Таблица соответствия международной " +
                        "и отечественной нумерации крючков",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(
                    horizontal = 4.dp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            /* * Таблица.  */
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                val horizontalScrollState = rememberScrollState()
                val verticalScrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    HookTableHeader()

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(verticalScrollState)
                    ) {
                        hookSizes.forEach { hook ->
                            HookTableRow(hook)
                        }
                    }
                }




            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "↔ Проведите в сторону, если таблица не помещается на экране",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}


/* * Заголовок таблицы. */
@Composable
private fun HookTableHeader() {

    /* * Используем фиксированную ширину */
    val columnWidth = 100.dp

    Column {
        /*  Верхняя строка. */
        Row(
            modifier = Modifier
                .height(44.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer
                )
        ) {

            Box(
                modifier = Modifier
                    .width(columnWidth * 2)
                    .fillMaxHeight()
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Нумерация крючка",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .width(columnWidth * 2)
                    .fillMaxHeight()
                    .border(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline
                    ),
                contentAlignment = Alignment.Center
            ) {

                Text(
                    text = "Ширина крючка, мм",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }

        /* * Вторая строка заголовка. */
        Row(
            modifier = Modifier
                .height(44.dp)
                .background(
                    MaterialTheme.colorScheme.primaryContainer
                )
        ) {

            HookHeaderCell(
                text = "Международ.",
                width = columnWidth
            )

            HookHeaderCell(
                text = "Российская",
                width = columnWidth
            )

            HookHeaderCell(
                text = "Международ.",
                width = columnWidth
            )

            HookHeaderCell(
                text = "Российская",
                width = columnWidth
            )
        }
    }
}


/** * Заголовочная ячейка. */
@Composable
private fun HookHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outline
            )
            .padding(4.dp),

        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}


/** * Строка таблицы. */
@Composable
private fun HookTableRow(
    hook: HookSize
) {
    val columnWidth = 100.dp
    val backgroundColor = MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .height(42.dp)
            .background(backgroundColor)
    ) {

        HookTableCell(
            text = hook.international,
            width = columnWidth,
            bold = true
        )

        HookTableCell(
            text = hook.domestic ?: "—",
            width = columnWidth
        )

        HookTableCell(
            text = hook.internationalWidth,
            width = columnWidth
        )

        HookTableCell(
            text = hook.domesticWidth ?: "—",
            width = columnWidth
        )
    }
}


/** * Обычная ячейка таблицы. */
@Composable
private fun HookTableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    bold: Boolean = false
) {

    Box(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            .padding(
                horizontal = 4.dp,
                vertical = 2.dp
            ),

        contentAlignment = Alignment.Center
    ) {

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight =
                if (bold) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                },
            textAlign = TextAlign.Center
        )
    }
}
