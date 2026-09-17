package com.pronaycoding.blankee.core.domain.usecase.customsound

import com.pronaycoding.blankee.core.data.repository.CustomSoundRepository

class RemoveCustomSoundUseCase(
    private val customSoundRepository: CustomSoundRepository,
) {
    suspend operator fun invoke(id: Int) = customSoundRepository.removeCustomSound(id)
}
