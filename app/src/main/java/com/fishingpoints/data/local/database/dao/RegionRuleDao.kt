package com.fishingpoints.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.fishingpoints.data.local.entity.RegionRule
import kotlinx.coroutines.flow.Flow

@Dao
interface RegionRuleDao {

    // =====================================
    // ПОЛУЧЕНИЕ ПРАВИЛ НА ДАТУ
    // =====================================

    @Query(
        """
        SELECT * FROM region_rules
        WHERE regionId = :regionId
          AND validFrom <= :date
          AND (
              validUntil IS NULL
              OR validUntil >= :date
          )
        ORDER BY validFrom DESC, id DESC
        """
    )
    fun getRegionRulesForDate(
        regionId: Long,
        date: String
    ): Flow<List<RegionRule>>


    // =====================================
    // ПОЛНАЯ ИСТОРИЯ РЕГИОНА
    // =====================================

    @Query(
        """
        SELECT * FROM region_rules
        WHERE regionId = :regionId
        ORDER BY ruleGroupId, validFrom DESC, id DESC
        """
    )
    fun getAllRegionRules(
        regionId: Long
    ): Flow<List<RegionRule>>


    // =====================================
    // ИСТОРИЯ ОДНОГО ПРАВИЛА
    // =====================================

    @Query(
        """
        SELECT * FROM region_rules
        WHERE regionId = :regionId
          AND ruleGroupId = :ruleGroupId
        ORDER BY validFrom DESC, id DESC
        """
    )
    fun getRuleHistory(
        regionId: Long,
        ruleGroupId: Long
    ): Flow<List<RegionRule>>


    // =====================================
    // ПОИСК ПЕРЕСЕЧЕНИЙ
    // =====================================

    @Query(
        """
        SELECT * FROM region_rules
        WHERE regionId = :regionId
          AND id != :excludeId
          AND validFrom <= COALESCE(:validUntil, '9999-12-31')
          AND COALESCE(validUntil, '9999-12-31') >= :validFrom
        ORDER BY validFrom DESC
        """
    )
    suspend fun getOverlappingRules(
        regionId: Long,
        validFrom: String,
        validUntil: String?,
        excludeId: Long
    ): List<RegionRule>


    // =====================================
    // ПЕРЕСЕЧЕНИЯ ВНУТРИ ГРУППЫ
    // =====================================

    @Query(
        """
        SELECT * FROM region_rules
        WHERE regionId = :regionId
          AND ruleGroupId = :ruleGroupId
          AND id != :excludeId
          AND validFrom <= COALESCE(:validUntil, '9999-12-31')
          AND COALESCE(validUntil, '9999-12-31') >= :validFrom
        """
    )
    suspend fun getOverlappingRevisions(
        regionId: Long,
        ruleGroupId: Long,
        validFrom: String,
        validUntil: String?,
        excludeId: Long
    ): List<RegionRule>


    // =====================================
    // ДОБАВЛЕНИЕ РЕДАКЦИИ
    // =====================================

    @Insert
    suspend fun insertRuleRevision(
        rule: RegionRule
    ): Long


    // =====================================
    // ОБНОВЛЕНИЕ РЕДАКЦИИ
    // =====================================

    @Update
    suspend fun updateRuleRevision(
        rule: RegionRule
    )


    // =====================================
    // УДАЛЕНИЕ РЕДАКЦИИ
    // =====================================

    @Delete
    suspend fun deleteRuleRevision(
        rule: RegionRule
    )


    // =====================================
    // УДАЛЕНИЕ ВСЕЙ ГРУППЫ
    // =====================================

    @Query(
        """
        DELETE FROM region_rules
        WHERE regionId = :regionId
          AND ruleGroupId = :ruleGroupId
        """
    )
    suspend fun deleteRuleGroup(
        regionId: Long,
        ruleGroupId: Long
    )


    // =====================================
    // КОЛИЧЕСТВО ПРАВИЛ
    // =====================================

    @Query(
        """
        SELECT COUNT(*) FROM region_rules
        WHERE regionId = :regionId
        """
    )
    suspend fun getRulesCount(
        regionId: Long
    ): Int

    @Transaction
    suspend fun createNewRule(
        rule: RegionRule
    ): Long {

        require(rule.validUntil == null ||
                !rule.validUntil.isBefore(rule.validFrom)) {
            "Дата окончания раньше даты начала"
        }

        // Вставляем первую редакцию.
        val id = insertRuleRevision(
            rule.copy(
                id = 0,
                ruleGroupId = 0
            )
        )

        // Привязываем её к собственной группе.
        updateRuleRevision(
            rule.copy(
                id = id,
                ruleGroupId = id
            )
        )

        return id
    }
    @Transaction
    suspend fun createNewRevision(
        previousRule: RegionRule,
        newRule: RegionRule
    ): Long {

        require(previousRule.ruleGroupId > 0L) {
            "Некорректный идентификатор группы"
        }

        require(previousRule.regionId == newRule.regionId) {
            "Регион редакции не совпадает"
        }

        require(
            newRule.validUntil == null ||
                    !newRule.validUntil.isBefore(newRule.validFrom)
        ) {
            "Дата окончания раньше даты начала"
        }

        require(newRule.validFrom.isAfter(previousRule.validFrom)) {
            "Новая редакция должна начинаться позже предыдущей"
        }

        // 1. Находим предыдущую редакцию.
        val previousRevision = getPreviousRevision(
            regionId = previousRule.regionId,
            ruleGroupId = previousRule.ruleGroupId,
            newValidFrom = newRule.validFrom.toString()
        ) ?: throw IllegalStateException(
            "Предыдущая редакция не найдена"
        )

        // 2. Проверяем пересечения с другими редакциями группы.
        val overlaps = getOverlappingRevisions(
            regionId = newRule.regionId,
            ruleGroupId = previousRule.ruleGroupId,
            validFrom = newRule.validFrom.toString(),
            validUntil = newRule.validUntil?.toString(),
            excludeId = previousRevision.id
        )

        require(overlaps.isEmpty()) {
            "Новая редакция пересекается с другой редакцией"
        }

        // 3. Автоматически закрываем предыдущую редакцию.
        val closingDate = newRule.validFrom.minusDays(1)

        if (
            previousRevision.validUntil == null ||
            !previousRevision.validUntil.isBefore(newRule.validFrom)
        ) {
            closeRevision(
                id = previousRevision.id,
                validUntil = closingDate.toString()
            )
        }

        // 4. Создаём новую редакцию.
        return insertRuleRevision(
            newRule.copy(
                id = 0L,
                ruleGroupId = previousRule.ruleGroupId
            )
        )
    }

    @Query(
        """
    SELECT * FROM region_rules
    WHERE regionId = :regionId
      AND ruleGroupId = :ruleGroupId
      AND validFrom < :newValidFrom
    ORDER BY validFrom DESC, id DESC
    LIMIT 1
    """
    )
    suspend fun getPreviousRevision(
        regionId: Long,
        ruleGroupId: Long,
        newValidFrom: String
    ): RegionRule?


    @Query(
        """
    UPDATE region_rules
    SET validUntil = :validUntil
    WHERE id = :id
    """
    )
    suspend fun closeRevision(
        id: Long,
        validUntil: String
    )






}
