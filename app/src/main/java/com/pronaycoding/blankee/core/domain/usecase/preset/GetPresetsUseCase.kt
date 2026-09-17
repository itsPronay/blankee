package com.pronaycoding.blankee.core.domain.usecase.preset

import com.pronaycoding.blankee.core.data.repository.PresetRepository
import com.pronaycoding.blankee.core.database.entities.PresetEntity
import kotlinx.coroutines.flow.Flow

class GetPresetsUseCase(
    private val presetRepository: PresetRepository,
) {
    operator fun invoke(): Flow<List<PresetEntity>> = presetRepository.observePresets()
}
