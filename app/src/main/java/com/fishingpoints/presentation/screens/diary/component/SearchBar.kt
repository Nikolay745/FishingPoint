package com.fishingpoints.presentation.screens.diary.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isActive by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = {
                    isActive = false
                },
                expanded = isActive,
                onExpandedChange = { expanded ->
                    isActive = expanded

                    if (!expanded && query.isEmpty()) {
                        onClose()
                    }
                },
                placeholder = {
                    Text("Поиск записей...")
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Поиск"
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                onClear()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Очистить"
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onClose
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Закрыть поиск"
                            )
                        }
                    }
                },
                modifier = Modifier.focusRequester(focusRequester)
            )
        },
        expanded = isActive,
        onExpandedChange = { expanded ->
            isActive = expanded

            if (!expanded && query.isEmpty()) {
                onClose()
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Быстрые фильтры",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    "Сегодня",
                    "За неделю",
                    "За месяц",
                    "С фото"
                )

                items(filters) { filter ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            when (filter) {
                                "Сегодня" -> {
                                    // Фильтр по сегодняшней дате
                                }

                                "За неделю" -> {
                                    // Фильтр за неделю
                                }

                                "За месяц" -> {
                                    // Фильтр за месяц
                                }

                                "С фото" -> {
                                    // Фильтр записей с фотографиями
                                }
                            }
                        },
                        label = {
                            Text(filter)
                        }
                    )
                }
            }
        }
    }
}
