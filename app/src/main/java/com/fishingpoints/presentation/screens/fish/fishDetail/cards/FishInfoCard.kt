package com.fishingpoints.presentation.screens.fish.fishDetail.cards

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.fishingpoints.data.local.entity.Fish
import com.fishingpoints.R
import com.fishingpoints.core.utils.FishImage

@Composable
fun FishInfoCard(fish: Fish) {
    val hasImage = !fish.imageUrl.isNullOrBlank()
    val description = fish.description?.takeIf { it.isNotBlank() }
    if (!hasImage && description == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (hasImage) {
                FishImage(
                    fish = fish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit,
                    placeholder = painterResource(R.drawable.icon_fish2),
                    error = painterResource(R.drawable.placeholder_image)
                )
            }
            description?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}