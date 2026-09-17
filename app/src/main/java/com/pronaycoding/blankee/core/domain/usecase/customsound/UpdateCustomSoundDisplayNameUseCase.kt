package com.pronaycoding.blankee.core.domain.usecase.customsound

import com.pronaycoding.blankee.core.data.repository.CustomSoundRepository

class UpdateCustomSoundDisplayNameUseCase(
    private val customSoundRepository: CustomSoundRepository,
) {
    suspend operator fun invoke(
        id: Int,
        displayName: String,
    ) = customSoundRepository.updateCustomSoundDisplayName(id, displayName)
}
