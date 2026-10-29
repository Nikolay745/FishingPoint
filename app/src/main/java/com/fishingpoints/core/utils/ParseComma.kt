package com.fishingpoints.core.utils

fun parseComma(input: String): Double? {
    return input
        .replace(',', '.')   // заменяем запятую на точку
        .toDoubleOrNull()
}