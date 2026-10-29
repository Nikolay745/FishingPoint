package com.fishingpoints.data.local.database.initializer.catalog

import com.fishingpoints.data.local.database.dao.FishDao
import com.fishingpoints.data.local.database.dao.TackleDao

import com.fishingpoints.data.local.database.initializer.core.fishByName
import com.fishingpoints.data.local.database.initializer.core.warnMissingFish
import com.fishingpoints.data.local.entity.Tackle
import com.fishingpoints.data.local.entity.TackleFishCrossRef
import com.fishingpoints.data.local.enums.TackleType
import javax.inject.Inject

internal class TackleSeeder @Inject constructor(
    private val tackleDao: TackleDao,
    private val fishDao: FishDao
) {
    suspend fun seedIfEmpty() {
        if (tackleDao.getTackleCount() != 0) return

        val fishMap = fishDao.fishByName()

        TackleData.all.forEach { initialTackle ->
            val tackleId = tackleDao.insertTackle(initialTackle.tackle)

            val crossRefs = initialTackle.fishNames.mapNotNull { fishName ->
                val fish = fishMap[fishName]
                if (fish == null) {
                    warnMissingFish(fishName, "снасти '${initialTackle.tackle.name}'")
                    null
                } else {
                    TackleFishCrossRef(tackleId = tackleId, fishId = fish.id)
                }
            }

            if (crossRefs.isNotEmpty()) {
                tackleDao.insertTackleFishCrossRefs(crossRefs)
            }
        }
    }
}


internal data class InitialTackle(
    val tackle: Tackle,
    val fishNames: List<String>
)

internal object TackleData {
    val all: List<InitialTackle> = listOf(
        InitialTackle(
            tackle = Tackle(
                name = "Поплавочная удочка",
                type = TackleType.COMPLETE,
                description = "Поплавочная удочка — это снасть для рыбалки с поплавком, который показывает поклёвку.\n" +
                        "\n" +
                        "Обычно состоит из:\n" +
                        "    удилища;\n" +
                        "    лески;\n" +
                        "    поплавка;\n" +
                        "    грузила;\n" +
                        "    крючка.\n"
            ),
            fishNames = listOf(
                "Плотва",
                "Карась",
                "Лещ",
                "Красноперка"
            )
        ),

        InitialTackle(
            tackle = Tackle(
                name = "Болонская удочка",
                type = TackleType.COMPLETE,
                description = "По сути, разновидность поплавочной, но с пропускными кольцами и катушкой. " +
                        "Это позволяет делать дальние забросы и комфортно ловить на течении — с берега или с лодки."
            ),
            fishNames = listOf(
                "Плотва",
                "Карась",
                "Лещ",
                "Красноперка"
            )
        ),

        InitialTackle(
            tackle = Tackle(
                name = "\uD83C\uDFA3 Матчевая удочка",
                type = TackleType.COMPLETE,
                description = "Матчевая удочка — это удилище для поплавочной ловли, " +
                        "в первую очередь для дальних забросов. \n" +
                        "\n" +
                        "Для реки хороший универсальный вариант:\n" +
                        "    Длина: 3,9–4,2 м\n" +
                        "    Тест: примерно 5–20 г\n" +
                        "    Строй: быстрый или средне-быстрый\n" +
                        "    Катушка: 2500–3000\n" +
                        "    Леска: 0,14–0,18 мм\n" +
                        "    Поплавок: вагглер, обычно 6–15 г"
            ),
            fishNames = listOf(
                "Лещ",
                "Плотва",
                "Карась",
                "Карп",
                "Сазан"
            )
        ),

        InitialTackle(
            tackle = Tackle(
                name = "Фидерная удочка",
                type = TackleType.COMPLETE,
                description = "Фидерная удочка — это удилище для донной ловли с кормушкой " +
                        "Мягкая вершинка хорошо показывает даже лёгкую поклёвку. " +
                        "Подходит для донной ловли на водоёмах с тихим или средним течением"
            ),
            fishNames = listOf(
                "Лещ",
                "Плотва",
                "Карась",
                "Карп",
                "Сазан",
                "Голавль"
            )
        ),

        InitialTackle(
            tackle = Tackle(
                name = "Спиннинг",
                type = TackleType.COMPLETE,
                description = "Спиннинг состоит из нескольких частей:\n" +
                        "\n" +
                        "Удилище (спиннинговое удилище): Длинное, гибкое, с пропускными кольцами. Имеет тест по весу приманок и разрывной нагрузке лески.\n" +
                        "\n" +
                        "Катушка: Обычно безынерционная (с вращающейся шпулей), реже — мультипликаторная или инерционная.\n" +
                        "\n" +
                        "Леска или шнур: Чаще всего плетеный шнур (для чувствительности) или монофильная леска.\n" +
                        "\n" +
                        "Приманка: Искусственная приманка (блесна, воблер, джиг, spinnerbait и т.д.), которая имитирует рыбку, насекомое или другое существо.\n" +
                        "\n" +
                        "Грузила, поводки, крючки: Вспомогательные элементы."
            ),
            fishNames = listOf(
                "Голавль",
                "Жерех",
                "Окунь",
                "Судак",
                "Форель",
                "Щука"

            )
        ),

        InitialTackle(
            tackle = Tackle(
                name = "Нахлыстовая снасть",
                type = TackleType.COMPLETE,
                description = "Более специфичный вариант: " +
                        "на конце лески — «мушка» (имитация насекомого), а для заброса нужен " +
                        "специальный тяжёлый шнур."
            ),
            fishNames = listOf(
                "Хариус",
                "Форель",
            )
        ),

        InitialTackle(
            tackle = Tackle(
                name = "Зимняя удочка",
                type = TackleType.COMPLETE,
                description = "Компактная снасть для подлёдной рыбалки. " +
                        "Рыболов опускает приманку (мормышку, блесну, балансир) в лунку и периодически подергивает удочкой."
            ),
            fishNames = listOf(
                "Окунь",
                "Плотва",
                "Карась",
                "Лещ"
            )
        ),

        InitialTackle(
            tackle = Tackle(
                name = "Поплавок Вагглер",
                type = TackleType.FLOAT,
                description = "Вагглер (Waggler) — это специальный поплавок для матчевой удочки.\n" +
                        "\n" +
                        "Он отличается тем, что крепится к леске только нижней частью. Благодаря этому его удобно далеко забрасывать.\n" +
                        "Какие бывают\n" +
                        "\n" +
                        "    Прямой вагглер — для спокойной воды и небольшой дистанции.\n" +
                        "\n" +
                        "    Вагглер с утолщённым телом — лучше летит и устойчивее на волне.\n" +
                        "\n" +
                        "    Loaded / огруженный вагглер — внутри уже есть собственный вес, поэтому его можно далеко забрасывать с относительно лёгкой оснасткой.\n" +
                        "\n" +
                        "    Slider (скользящий вагглер) — используется на большой глубине, когда глубина больше длины удилища.\n" +
                        "\n" +
                        "Для реки и первого матчевого комплекта я бы начал с огруженного вагглера 8–12 г."
            ),
            fishNames = listOf(
                "Лещ",
                "Плотва",
                "Карась",
                "Карп",
                "Сазан",
                "Голавль"
            )
        ),
    )
}