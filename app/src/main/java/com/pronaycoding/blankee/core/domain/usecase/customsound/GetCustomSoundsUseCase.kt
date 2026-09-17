package com.pronaycoding.blankee.core.domain.usecase.customsound

import com.pronaycoding.blankee.core.data.repository.CustomSoundRepository
import com.pronaycoding.blankee.core.database.entities.CustomSoundEntity
import kotlinx.coroutines.flow.Flow

class GetCustomSoundsUseCase(
    private val customSoundRepository: CustomSoundRepository,
) {
    operator fun invoke(): Flow<List<CustomSoundEntity>> = customSoundRepository.getAllCustomSounds()
}
