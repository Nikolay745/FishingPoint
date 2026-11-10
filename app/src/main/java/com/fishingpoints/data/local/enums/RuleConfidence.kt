package com.fishingpoints.data.local.enums

enum class RuleConfidence(val displayName: String) {
    HIGH("Высокая"),
    MEDIUM("Средняя"),
    LOW("Низкая")
}


enum class RuleSource {
    DEFAULT,
    CUSTOM
}
