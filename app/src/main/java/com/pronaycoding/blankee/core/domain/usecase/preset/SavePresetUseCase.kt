package com.pronaycoding.blankee.core.domain.usecase.preset

import com.pronaycoding.blankee.core.data.repository.PresetRepository
import com.pronaycoding.blankee.core.database.entities.PresetEntity

class SavePresetUseCase(
    private val presetRepository: PresetRepository,
) {
    suspend operator fun invoke(entity: PresetEntity): Long = presetRepository.savePreset(entity)
}
