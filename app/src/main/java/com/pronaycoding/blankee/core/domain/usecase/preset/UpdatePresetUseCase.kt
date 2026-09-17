package com.pronaycoding.blankee.core.domain.usecase.preset

import com.pronaycoding.blankee.core.data.repository.PresetRepository
import com.pronaycoding.blankee.core.database.entities.PresetEntity

class UpdatePresetUseCase(
    private val presetRepository: PresetRepository,
) {
    suspend operator fun invoke(entity: PresetEntity) = presetRepository.updatePreset(entity)
}
