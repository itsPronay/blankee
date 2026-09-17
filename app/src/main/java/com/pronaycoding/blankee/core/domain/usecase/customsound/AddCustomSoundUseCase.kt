package com.pronaycoding.blankee.core.domain.usecase.customsound

import com.pronaycoding.blankee.core.data.repository.CustomSoundRepository

class AddCustomSoundUseCase(
    private val customSoundRepository: CustomSoundRepository,
) {
    suspend operator fun invoke(
        displayName: String,
        filePath: String,
    ) = customSoundRepository.addCustomSound(displayName, filePath)
}
