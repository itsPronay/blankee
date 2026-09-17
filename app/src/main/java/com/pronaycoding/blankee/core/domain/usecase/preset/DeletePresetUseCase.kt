package com.pronaycoding.blankee.core.domain.usecase.preset

import com.pronaycoding.blankee.core.data.repository.PresetRepository

class DeletePresetUseCase(
    private val presetRepository: PresetRepository,
) {
    suspend operator fun invoke(id: Long) = presetRepository.deletePreset(id)
}
