package com.pronaycoding.blankee.core.data.repositoryImpl

import com.pronaycoding.blankee.core.data.repository.PresetRepository
import com.pronaycoding.blankee.core.database.dao.PresetDao
import com.pronaycoding.blankee.core.database.entities.PresetEntity
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of the [PresetRepository] interface.
 *
 * This class provides concrete implementations for all preset database operations.
 * It acts as a bridge between the UI/business logic layer and the Room database DAO,
 * delegating all operations to [PresetDao] while maintaining the repository abstraction.
 *
 * @param presetDao The Data Access Object used for database operations
 *
 * @see PresetRepository for the interface contract
 * @see PresetDao for lower-level database access
 */
class PresetRepositoryImpl(
    private val presetDao: PresetDao,
) : PresetRepository {
    /**
     * Observes all presets from the database.
     *
     * @return A Flow from the DAO that emits updated preset lists on database changes
     */
    override fun observePresets(): Flow<List<PresetEntity>> = presetDao.observeAll()

    /**
     * Saves a preset to the database.
     *
     * @param entity The preset to save
     * @return The ID assigned to the new preset
     */
    override suspend fun savePreset(entity: PresetEntity): Long = presetDao.insertPreset(entity)

    /**
     * Deletes a preset from the database.
     *
     * @param id The ID of the preset to delete
     */
    override suspend fun deletePreset(id: Long) = presetDao.deleteById(id)

    override suspend fun updatePreset(entity: PresetEntity) = presetDao.updatePreset(entity)
}
