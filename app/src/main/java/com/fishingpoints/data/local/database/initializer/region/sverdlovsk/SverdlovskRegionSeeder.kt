package com.fishingpoints.data.local.database.initializer.region.sverdlovsk


import com.fishingpoints.data.local.database.dao.DailyAllowanceDao
import com.fishingpoints.data.local.database.dao.FishDao
import com.fishingpoints.data.local.database.dao.FishingBanDao
import com.fishingpoints.data.local.database.dao.RegionDao
import com.fishingpoints.data.local.database.dao.RegionRuleDao
import com.fishingpoints.data.local.database.initializer.core.fishByName
import com.fishingpoints.data.local.database.initializer.core.getOrCreateRegion
import com.fishingpoints.data.local.database.initializer.core.warnMissingAllowance
import com.fishingpoints.data.local.entity.DailyAllowance
import com.fishingpoints.data.local.entity.FishingBan
import com.fishingpoints.data.local.entity.RegionRule
import java.time.LocalDate
import javax.inject.Inject

internal class SverdlovskRegionSeeder @Inject constructor(
    private val regionDao: RegionDao,
    private val regionRuleDao: RegionRuleDao,
    private val dailyAllowanceDao: DailyAllowanceDao,
    private val fishingBanDao: FishingBanDao,
    private val fishDao: FishDao
) {
    suspend fun seedIfEmpty() {
        val region = regionDao.getOrCreateRegion(REGION_NAME)

        if (regionRuleDao.getRulesCount(region.id) == 0) insertRules(region.id)
        if (dailyAllowanceDao.getAllowancesCount(region.id) == 0) insertAllowances(region.id)
        if (fishingBanDao.getBansCount(region.id) == 0) insertBans(region.id)
    }

    private suspend fun insertRules(regionId: Long) {
        regionRuleDao.insertRule(
            RegionRule(
                regionId = regionId,
                validFrom = LocalDate.of(2026, 1, 1),
                validUntil = null,
                dailyWeightLimit = 10_000,
                allowOversizeFish = true,
                maxDailyNorms = 2,
                notes = """Правила любительского рыболовства
                для Свердловской области.

                Общая суточная норма вылова:
                не более 10 кг для одного гражданина.

                Для сибирского хариуса установлена отдельная
                суточная норма: 3 кг или 15 экземпляров.

                Для судака, щуки, налима, сазана, леща и язя
                действует суммарная норма 5 кг.

                Для карася, плотвы и окуня действует
                суммарная норма 10 кг.

                Общая суточная норма для видов, перечисленных
                в Правилах, составляет не более 10 кг или
                один экземпляр, если его вес превышает 10 кг.

                Если гражданин находится на водоёме более суток,
                допускается добыча и хранение улова в пределах
                двух суточных норм согласно Правилам рыболовства.
"""
            )
        )
    }


private suspend fun insertAllowances(regionId: Long) {
    val validFrom = LocalDate.of(2026, 1, 1)
    val fishMap = fishDao.fishByName()

    data class AllowanceData(
        val fishName: String,
        val minLength: Int,
        val notes: String? = null,
        val weightLimit: Int? = null,
        val countLimit: Int? = null
    )

    val allowances = listOf(
        AllowanceData(
            fishName = "Щука",
            minLength = 30,
            weightLimit = null,
            notes = "Входит в суммарную суточную норму 5 кг для группы: судак, щука, налим, сазан, лещ и язь."
        ),
        AllowanceData(
            fishName = "Судак",
            minLength = 35,
            notes = "Входит в суммарную суточную норму 5 кг для группы: судак, щука, налим, сазан, лещ и язь."
        ),
        AllowanceData(
            fishName = "Сазан",
            minLength = 30,
            notes = "Входит в суммарную суточную норму 5 кг для группы: судак, щука, налим, сазан, лещ и язь."
        ),
        AllowanceData(
            fishName = "Карп",
            minLength = 30,
            notes = "Минимальный размер 30 см. Суточная норма учитывается в соответствии с применимыми Правилами рыболовства."
        ),
        AllowanceData(
            fishName = "Лещ",
            minLength = 23,
            notes = "Входит в суммарную суточную норму 5 кг для группы: судак, щука, налим, сазан, лещ и язь."
        ),
        AllowanceData(
            fishName = "Язь",
            minLength = 23,
            notes = "Входит в суммарную суточную норму 5 кг для группы: судак, щука, налим, сазан, лещ и язь."
        ),
        AllowanceData(
            fishName = "Сиг",
            minLength = 40,
            notes = "Минимальный разрешённый размер — 40 см. Проверь наличие специальных ограничений на конкретном водоёме."
        ),
        AllowanceData(
            fishName = "Рипус",
            minLength = 21,
            notes = "Минимальный разрешённый размер — 21 см. Проверь наличие специальных ограничений на конкретном водоёме."
        ),
        AllowanceData(
            fishName = "Пелядь",
            minLength = 26,
            notes = "Минимальный разрешённый размер — 26 см."
        ),
        AllowanceData(
            fishName = "Хариус сибирский",
            minLength = 26,
            weightLimit = 3_000,
            countLimit = 15,
            notes = "Суточная норма: 3 кг или 15 экземпляров."
        )
    )

    allowances.forEach { allowance ->
        val fish = fishMap[allowance.fishName]

        if (fish == null) {
            warnMissingAllowance(allowance.fishName)
            return@forEach
        }

        dailyAllowanceDao.insertAllowance(
            DailyAllowance(
                fishId = fish.id,
                regionId = regionId,
                validFrom = validFrom,
                validUntil = null,
                minLength = allowance.minLength,
                countLimit = allowance.countLimit,
                weightLimit = allowance.weightLimit,
                notes = allowance.notes
            )
        )
    }
}


    private suspend fun insertBans(regionId: Long) {
        val validFrom = LocalDate.of(2026, 1, 1)
        val fishMap = fishDao.fishByName()
        val sigId = fishMap["Сиг"]?.id
        val ripusId = fishMap["Рипус"]?.id
        val hariusId = fishMap["Хариус сибирский"]?.id   // ИСПРАВЛЕНО

        val bans = listOf(

            FishingBan(
                regionId = regionId,
                fishIds = emptyList(),
                title = "Весенний запрет на рыбалку на отдельных водоёмах",
                description = """
                С 25 апреля по 15 июня запрещена добыча водных биоресурсов на перечисленных в Правилах рыболовства водоёмах, 
                включая Черноисточинское, Верхне-Макаровское, Нижне-Тагильское, Белоярское, Волчихинское и Исетское водохранилища, озёра Таватуй, Балтым и Мелкое, а также реку Исеть от истока до подпора  Верх-Исетского водохранилища и реку Решётку.

                Предусмотрено исключение для ловли с берега одной донной или поплавочной удочкой либо спиннингом с общим количеством крючков не более двух у одного гражданина.

                Перед рыбалкой необходимо проверить точное местоположение и действующую редакцию правил. """.trimIndent(),
                location = """
                Отдельные водохранилища, озёра и участки рек Свердловской области согласно пункту 17.1  """.trimIndent(),
                validFrom = validFrom,
                validUntil = null,
                seasonRepeatsAnnually = true,
                seasonStartMonth = 4,
                seasonStartDay = 25,
                seasonEndMonth = 6,
                seasonEndDay = 15
            ),

            FishingBan(
                regionId = regionId,
                fishIds = emptyList(),
                title = "Весенний запрет на отдельных водоёмах второй группы",
                description = """
                С 15 мая по 15 июня запрещена добыча водных биоресурсов в перечисленных в Правилах рыболовства водохранилищах, включая Артинское, Нижне-Сергинское,
                Верх-Нейвинское, Староуткинское, Верхне-Туринское, Режевское, Ревдинское и другие указанные водоёмы, а также в озёрах Шитовское, Шарташ, Чусовское и Дикое.

                Предусмотрено исключение для ловли с берега разрешёнными снастями с общим количеством крючков не более двух у одного гражданина.
            """.trimIndent(),
                location = "Водоёмы Свердловской области согласно пункту 17.1",
                validFrom = validFrom,
                validUntil = null,
                seasonRepeatsAnnually = true,
                seasonStartMonth = 5,
                seasonStartDay = 15,
                seasonEndMonth = 6,
                seasonEndDay = 15
            ),

            FishingBan(
                regionId = regionId,
                fishIds = emptyList(),
                title = "Весенний запрет на остальных водоёмах",
                description = """
                С 1 мая по 30 мая запрещена добыча водных биоресурсов на других водных объектах рыбохозяйственного значения Свердловской области.

                Исключения для отдельных разрешённых снастей и ловли с берега необходимо применять  в соответствии с действующими Правилами рыболовства.
            """.trimIndent(),
                location = "Другие водные объекты Свердловской области",
                validFrom = validFrom,
                validUntil = null,
                seasonRepeatsAnnually = true,
                seasonStartMonth = 5,
                seasonStartDay = 1,
                seasonEndMonth = 5,
                seasonEndDay = 30
            ),

            FishingBan(
                regionId = regionId,
                fishIds = emptyList(),
                title = "Весенний запрет на реке Лозьва",
                description = """
                С 1 мая по 15 июня действует запрет на добычу водных биоресурсов на реке Лозьва от истоков до устья реки Большая Умпия.

                Необходимо учитывать предусмотренные правилами исключения и точные границы участка. """.trimIndent(),
                location = "Река Лозьва от истоков до устья реки Большая Умпия",
                validFrom = validFrom,
                validUntil = null,
                seasonRepeatsAnnually = true,
                seasonStartMonth = 5,
                seasonStartDay = 1,
                seasonEndMonth = 6,
                seasonEndDay = 15
            ),

            FishingBan(
                regionId = regionId,
                fishIds = emptyList(),
                title = "Запрет на ловлю на зимовальных ямах",
                description = """
                С 1 ноября по 1 мая запрещена добыча водных биоресурсов на зимовальных ямах, включённых в приложение № 1 к Правилам рыболовства
                для Западно-Сибирского рыбохозяйственного бассейна.

                Запрет действует на указанных в нормативном перечне участках, а не автоматически на всех водоёмах региона.
            """.trimIndent(),
                location = "Зимовальные ямы согласно приложению № 1",
                validFrom = validFrom,
                validUntil = null,
                seasonRepeatsAnnually = true,
                seasonStartMonth = 11,
                seasonStartDay = 1,
                seasonEndMonth = 5,
                seasonEndDay = 1
            ),

            FishingBan(
                regionId = regionId,
                fishIds = listOfNotNull(sigId),
                title = "Сезонный запрет на вылов сига на озере Таватуй",
                description = """
                Запрещён вылов сига с 20 сентября по 20 ноября. Ограничение распространяется на озеро Таватуй.
            """.trimIndent(),
                location = "Озеро Таватуй",
                validFrom = validFrom,
                validUntil = null,
                seasonRepeatsAnnually = true,
                seasonStartMonth = 9,
                seasonStartDay = 20,
                seasonEndMonth = 11,
                seasonEndDay = 20
            ),

            FishingBan(
                regionId = regionId,
                fishIds = listOfNotNull(hariusId),
                title = "Сезонный запрет на вылов сибирского хариуса",
                description = """
                Запрещён вылов сибирского хариуса с 20 октября по 15 ноября.
                Ограничение действует повсеместно в пределах Свердловской области. """.trimIndent(),
                location = "Водные объекты Свердловской области",
                validFrom = validFrom,
                validUntil = null,
                seasonRepeatsAnnually = true,
                seasonStartMonth = 10,
                seasonStartDay = 20,
                seasonEndMonth = 11,
                seasonEndDay = 15
            ),

            FishingBan(
                regionId = regionId,
                fishIds = listOfNotNull(ripusId),
                title = "Сезонный запрет на вылов рипуса",
                description = """
                Запрещён вылов рипуса с 15 октября по 10 декабря в Нижне-Качканарском водохранилище на реке Выя.""".trimIndent(),
                location = "Нижне-Качканарское водохранилище на реке Выя",
                validFrom = validFrom,
                validUntil = null,
                seasonRepeatsAnnually = true,
                seasonStartMonth = 10,
                seasonStartDay = 15,
                seasonEndMonth = 12,
                seasonEndDay = 10
            ),

            FishingBan(
                regionId = regionId,
                fishIds = listOfNotNull(
                    fishMap["Осётр сибирский"]?.id,
                    fishMap["Осетр сибирский"]?.id,
                    fishMap["Стерлядь"]?.id,
                    fishMap["Нельма"]?.id,
                    fishMap["Таймень"]?.id,
                    fishMap["Хариус европейский"]?.id,
                    fishMap["Хариус сибирский"]?.id,
                    fishMap["Обыкновенный подкаменщик"]?.id
                ).distinct(),
                title = "Запрещённые для вылова виды рыб",
                description = """
                Правила рыболовства устанавливают запрет на добычу отдельных видов водных биоресурсов, включая
                осетра сибирского, стерлядь, нельму, тайменя, европейского хариуса и обыкновенного подкаменщика.

                Запрещённые виды необходимо незамедлительно выпускать в естественную среду обитания с наименьшими повреждениями.
            """.trimIndent(),
                location = "Водные объекты Свердловской области",
                validFrom = validFrom,
                validUntil = null,
                seasonRepeatsAnnually = false,
                seasonStartMonth = null,
                seasonStartDay = null,
                seasonEndMonth = null,
                seasonEndDay = null
            ),


        )

        bans.forEach { fishingBanDao.insertBan(it) }
    }

    private companion object {
        const val REGION_NAME = "Свердловская область"
    }
}