package com.fishingpoints.data.repository

import com.fishingpoints.data.local.database.dao.EquipmentDao
import com.fishingpoints.data.local.entity.EquipmentEntity
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class EquipmentRepository @Inject constructor(
    private val equipmentDao: EquipmentDao
) {

    fun getAllEquipment(): Flow<List<EquipmentEntity>> {
        return equipmentDao.getAllEquipment()
    }

    suspend fun addEquipment(name: String): Result<Unit> {
        val normalizedName = name.trim()

        if (normalizedName.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Введите название предмета")
            )
        }

        if (equipmentDao.countByName(normalizedName) > 0) {
            return Result.failure(
                IllegalArgumentException("Такой предмет уже существует")
            )
        }

        equipmentDao.insertEquipment(
            EquipmentEntity(name = normalizedName)
        )

        return Result.success(Unit)
    }

    suspend fun updateEquipment(
        equipment: EquipmentEntity,
        newName: String
    ): Result<Unit> {
        val normalizedName = newName.trim()

        if (normalizedName.isBlank()) {
            return Result.failure(
                IllegalArgumentException("Введите название предмета")
            )
        }

        if (
            equipmentDao.countByNameExcludingId(
                normalizedName,
                equipment.id
            ) > 0
        ) {
            return Result.failure(
                IllegalArgumentException("Такой предмет уже существует")
            )
        }

        equipmentDao.updateEquipment(
            equipment.copy(name = normalizedName)
        )

        return Result.success(Unit)
    }

    suspend fun toggleEquipment(
        equipment: EquipmentEntity,
        isPacked: Boolean
    ) {
        equipmentDao.updateEquipment(
            equipment.copy(isPacked = isPacked)
        )
    }

    suspend fun deleteEquipment(
        equipment: EquipmentEntity
    ) {
        equipmentDao.deleteEquipment(equipment)
    }

    suspend fun resetAllEquipment() {
        equipmentDao.resetAllEquipment()
    }

    suspend fun seedDefaultEquipment() {
        if (equipmentDao.getEquipmentCount() > 0) return

        val defaults = listOf(
            "Удочка или спиннинг",
            "Леска",
            "Крючки",
            "Поплавки, грузила",
            "Запасные снасти",
            "Приманки/прикормки",
            "Насадки/наживки",
            "Садок и подсак",
            "Ножницы или кусачки",
            "Коробка для снастей",
            "Поляризационные очки",
            "Аптечка",
            "Вода",
            "Перекус",
            "Защита от солнца и насекомых"
        )

        defaults.forEach { name ->
            equipmentDao.insertEquipment(
                EquipmentEntity(name = name)
            )
        }
    }
}
