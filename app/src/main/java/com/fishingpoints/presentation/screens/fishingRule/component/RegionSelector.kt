package com.fishingpoints.presentation.screens.fishingRule.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fishingpoints.data.local.entity.Region


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegionSelector(
    regions: List<Region>,
    selectedRegionId: Long?,
    isDetectingRegion: Boolean,
    onRegionSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = when {
        isDetectingRegion -> "Определяем регион..."
        else -> {
            regions
                .find { it.id == selectedRegionId }
                ?.name
                ?: "Выберите регион"
        }
    }


    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = { },
            readOnly = true,
            label = { Text("Регион") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor(
                    type = ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                    enabled = true
                )
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            regions.forEach { region ->
                DropdownMenuItem(
                    text = { Text(region.name) },
                    onClick = {
                        onRegionSelected(region.id)
                        expanded = false
                    }
                )
            }
        }
    }
}
