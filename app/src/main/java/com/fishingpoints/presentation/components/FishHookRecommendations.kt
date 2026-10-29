package com.fishingpoints.presentation.components

data class FishHookRecommendation(
    val fishName: String,
    val minHook: String,
    val recommendedHook: String,
    val maxHook: String,
    val note: String
)

val fishHookRecommendations = listOf(

    FishHookRecommendation(
        fishName = "Карась",
        minHook = "14",
        recommendedHook = "12",
        maxHook = "8",
        note = "Для крупного карася можно использовать №8–10"
    ),

    FishHookRecommendation(
        fishName = "Карп",
        minHook = "8",
        recommendedHook = "6",
        maxHook = "2",
        note = "Размер зависит от бойла и размера рыбы"
    ),

    FishHookRecommendation(
        fishName = "Голавль",
        minHook = "10",
        recommendedHook = "8",
        maxHook = "6",
        note = "Размер зависит от приманки"
    ),

    FishHookRecommendation(
        fishName = "Сазан",
        minHook = "8",
        recommendedHook = "6",
        maxHook = "2",
        note = "Для крупного сазана предпочтительны усиленные крючки"
    ),

    FishHookRecommendation(
        fishName = "Лещ",
        minHook = "14",
        recommendedHook = "10",
        maxHook = "6",
        note = "№10–12 подходят для большинства ситуаций"
    ),

    FishHookRecommendation(
        fishName = "Плотва",
        minHook = "18",
        recommendedHook = "16",
        maxHook = "12",
        note = "Для крупной плотвы можно увеличить размер"
    ),

    FishHookRecommendation(
        fishName = "Красноперка",
        minHook = "18",
        recommendedHook = "16",
        maxHook = "12",
        note = "Размер зависит от размера насадки"
    ),

    FishHookRecommendation(
        fishName = "Линь",
        minHook = "14",
        recommendedHook = "10",
        maxHook = "8",
        note = "Для крупного линя лучше использовать усиленный крючок"
    ),

    FishHookRecommendation(
        fishName = "Белый амур",
        minHook = "8",
        recommendedHook = "6",
        maxHook = "2",
        note = "Размер зависит от растительной насадки"
    ),

    FishHookRecommendation(
        fishName = "Толстолобик",
        minHook = "8",
        recommendedHook = "6",
        maxHook = "2",
        note = "Размер зависит от оснастки"
    ),

    FishHookRecommendation(
        fishName = "Чехонь",
        minHook = "16",
        recommendedHook = "12",
        maxHook = "10",
        note = "Для небольшой насадки используйте меньший крючок"
    ),

    FishHookRecommendation(
        fishName = "Рыбец",
        minHook = "14",
        recommendedHook = "10",
        maxHook = "8",
        note = "Размер зависит от насадки"
    ),

    FishHookRecommendation(
        fishName = "Налим",
        minHook = "4",
        recommendedHook = "2",
        maxHook = "1/0",
        note = "Для живца и крупной наживки нужен мощный крючок"
    ),

    FishHookRecommendation(
        fishName = "Сом",
        minHook = "2",
        recommendedHook = "2/0",
        maxHook = "4/0",
        note = "Для трофейного сома могут использоваться ещё крупнее"
    ),

    FishHookRecommendation(
        fishName = "Судак",
        minHook = "4",
        recommendedHook = "2",
        maxHook = "1/0",
        note = "Размер зависит от джиговой приманки или живца"
    ),

    FishHookRecommendation(
        fishName = "Жерех",
        minHook = "6",
        recommendedHook = "4",
        maxHook = "2",
        note = "При спиннинге размер выбирается под приманку"
    ),

    FishHookRecommendation(
        fishName = "Окунь",
        minHook = "14",
        recommendedHook = "10",
        maxHook = "6",
        note = "Для микроджига используются меньшие размеры"
    ),

    FishHookRecommendation(
        fishName = "Щука",
        minHook = "2",
        recommendedHook = "1/0",
        maxHook = "3/0",
        note = "Размер тройника выбирается прежде всего под приманку"
    ),

    FishHookRecommendation(
        fishName = "Ёрш",
        minHook = "18",
        recommendedHook = "16",
        maxHook = "12",
        note = "Для мелкого ерша лучше использовать №16–18"
    )
)
