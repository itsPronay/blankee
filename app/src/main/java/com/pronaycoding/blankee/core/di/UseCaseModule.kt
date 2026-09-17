package com.pronaycoding.blankee.core.di

import com.pronaycoding.blankee.core.domain.usecase.customsound.AddCustomSoundUseCase
import com.pronaycoding.blankee.core.domain.usecase.customsound.GetCustomSoundsUseCase
import com.pronaycoding.blankee.core.domain.usecase.customsound.RemoveCustomSoundUseCase
import com.pronaycoding.blankee.core.domain.usecase.customsound.UpdateCustomSoundDisplayNameUseCase
import com.pronaycoding.blankee.core.domain.usecase.preset.DeletePresetUseCase
import com.pronaycoding.blankee.core.domain.usecase.preset.GetPresetsUseCase
import com.pronaycoding.blankee.core.domain.usecase.preset.SavePresetUseCase
import com.pronaycoding.blankee.core.domain.usecase.preset.UpdatePresetUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val useCaseModule = module {
    factoryOf(::GetPresetsUseCase)
    factoryOf(::SavePresetUseCase)
    factoryOf(::DeletePresetUseCase)
    factoryOf(::UpdatePresetUseCase)
    factoryOf(::GetCustomSoundsUseCase)
    factoryOf(::AddCustomSoundUseCase)
    factoryOf(::RemoveCustomSoundUseCase)
    factoryOf(::UpdateCustomSoundDisplayNameUseCase)
}
