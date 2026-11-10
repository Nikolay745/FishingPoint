package com.fishingpoints.presentation.screens.main.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fishingpoints.data.local.enums.FeedingActivity
import com.fishingpoints.data.local.enums.RuleConfidence

@Composable
fun ActivityBadge(
    activity: FeedingActivity,
    confidence: RuleConfidence? = null
) {
    val containerColor = when (activity) {
        FeedingActivity.VERY_HIGH ->
            Color(0xFFE8F5E9)

        FeedingActivity.HIGH ->
            Color(0xFFC8E6C9)

        FeedingActivity.MEDIUM ->
            Color(0xFFFFF3CD)

        FeedingActivity.LOW ->
            Color(0xFFFFE0B2)

        FeedingActivity.NONE ->
            Color(0xFFE0E0E0)
    }

    val contentColor = when (activity) {
        FeedingActivity.VERY_HIGH,
        FeedingActivity.HIGH ->
            Color(0xFF1B5E20)

        FeedingActivity.MEDIUM ->
            Color(0xFF795548)

        FeedingActivity.LOW ->
            Color(0xFFE65100)

        FeedingActivity.NONE ->
            Color.DarkGray
    }

    Surface(
        color = containerColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 8.dp
            )
        ) {
            Text(
                text = "${activity.icon} Активность: ${activity.displayName}",
                color = contentColor,
                style = MaterialTheme.typography.labelLarge
            )

            confidence?.let {
                Text(
                    text = when (it) {
                        RuleConfidence.HIGH -> "Достоверность: высокая"
                        RuleConfidence.MEDIUM -> "Достоверность: средняя"
                        RuleConfidence.LOW -> "Достоверность: низкая"
                    },
                    color = contentColor,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
