package com.fishingpoints.data.local.database.initializer.region.voronezh


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

internal class VoronezhRegionSeeder @Inject constructor(
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
                dailyWeightLimit = 5_000,
                allowOversizeFish = true,
                maxDailyNorms = 2,
                notes = """
                Регион: Воронежская область.
                Рыбохозяйственный бассейн: Азово-Черноморский.

                Общая суточная норма для рек составляет 5 кг.
                Для отдельных видов водных биоресурсов предусмотрены
                специальные ограничения и исключения.

                При пребывании на водоёме более одних суток
                допускается не более двух суточных норм.

                Необходимо соблюдать минимальные размеры рыб,
                сезонные запреты и ограничения на конкретных водоёмах.

                Источник: Правила рыболовства для Азово-Черноморского
                рыбохозяйственного бассейна, пункт 55.5.
            """.trimIndent()
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
            AllowanceData("Щука", 30, "Минимальный размер — 30 см."),
            AllowanceData("Судак", 38, "Минимальный размер — 38 см."),
            AllowanceData("Сом", 60, "Пресноводный сом. Минимальный размер — 60 см."),
            AllowanceData("Сазан", 35, "Минимальный размер — 35 см."),
            AllowanceData("Карп", 35, "Для карпа применяется норматив для сазана."),
            AllowanceData("Белый амур", 45, "Минимальный размер — 45 см."),
            AllowanceData("Толстолобик", 50, "Минимальный размер — 50 см."),
            AllowanceData("Лещ", 24, "Для других пресноводных водоёмов — 24 см.\n" +
                    "                Для реки Дон предусмотрен отдельный норматив — 28 см.\n" +
                    "                При проверке необходимо учитывать конкретный водоём."),
            AllowanceData("Голавль", 28, "Для остальных водных объектов — 28 см.\n" +
                    "                Для озёр и водохранилищ Воронежской области предусмотрено отдельное значение."),
            AllowanceData("Линь", 17, "Минимальный размер — 17 см."),
            AllowanceData("Чехонь", 24, "Минимальный размер — 24 см."),
            AllowanceData("Рыбец", 22, "Минимальный размер рыбца (сырти) — 22 см."),
            AllowanceData("Рак пресноводный", 9, "Минимальный размер — 9 см.\n" +
                    "                Также необходимо соблюдать ограничения на вылов самок, вынашивающих икру и личинок."),
            AllowanceData(
                "Карась серебряный", 0,
                notes = "Для серебряного карася предусмотрено исключение из общей суточной нормы по весу.\n" +
                        "                Значение 10 кг следует трактовать в соответствии с действующей редакцией пункта 55.5.",
                weightLimit = 10_000
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
                    minLength = allowance.minLength.takeIf { it > 0 },
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
        val pikeId = fishMap["Щука"]?.id
        val zanderId = fishMap["Судак"]?.id

        val bans = listOf(
            FishingBan(
                regionId = regionId,
                fishIds = listOfNotNull(pikeId),
                title = "Весенний запрет на вылов щуки",
                description = "Запрещён вылов щуки с 20 марта по 25 апреля.",
                location = "Водные объекты рыбохозяйственного значения Воронежской области",
                validFrom = validFrom,
                validUntil = null,
                seasonRepeatsAnnually = true,
                seasonStartMonth = 3, seasonStartDay = 20,
                seasonEndMonth = 4, seasonEndDay = 25
            ),
            FishingBan(
                regionId = regionId,
                fishIds = listOfNotNull(zanderId),
                title = "Весенний запрет на вылов судака",
                description = "Запрещён вылов судака с 15 апреля по 8 июня.",
                location = "Водные объекты рыбохозяйственного значения Воронежской области",
                validFrom = validFrom,
                validUntil = null,
                seasonRepeatsAnnually = true,
                seasonStartMonth = 4, seasonStartDay = 15,
                seasonEndMonth = 6, seasonEndDay = 8
            ),
            FishingBan(
                regionId = regionId,
                fishIds = emptyList(),
                title = "Ограничения на рыбалку на Дону и его притоках",
                description = """
                С 20 апреля по 1 июня на реке Дон со всеми его притоками, рукавами, протоками и пойменными озёрами на участке выше устья реки Иловля действуют ограничения.

                Правила предусматривают исключения для ловли с берега без использования плавательных средств определёнными разрешёнными снастями.

                Это не следует трактовать как полный запрет любой рыбалки на всей территории области. """.trimIndent(),
                location = "Река Дон и её притоки выше устья реки Иловля",
                validFrom = validFrom,
                validUntil = null,
                seasonRepeatsAnnually = true,
                seasonStartMonth = 4, seasonStartDay = 20,
                seasonEndMonth = 6, seasonEndDay = 1
            ),
            FishingBan(
                regionId = regionId,
                fishIds = emptyList(),
                title = "Запрет на ловлю на зимовальных ямах",
                description = """
                С 15 ноября по 31 марта запрещена добыча водных биоресурсов на зимовальных ямах, включённых в приложение № 1 к Правилам рыболовства.

                Запрет распространяется только на указанные в нормативном перечне участки.
            """.trimIndent(),
                location = "Зимовальные ямы Воронежской области согласно приложению № 1",
                validFrom = validFrom,
                validUntil = null,
                seasonRepeatsAnnually = true,
                seasonStartMonth = 11, seasonStartDay = 15,
                seasonEndMonth = 3, seasonEndDay = 31
            )
        )

        bans.forEach { fishingBanDao.insertBan(it) }
    }

    private companion object {
        const val REGION_NAME = "Воронежская область"
    }
}