package com.pronaycoding.blankee.core.di

import com.pronaycoding.blankee.core.data.repository.CustomSoundRepository
import com.pronaycoding.blankee.core.data.repository.PresetRepository
import com.pronaycoding.blankee.core.data.repositoryImpl.CustomSoundRepositoryImpl
import com.pronaycoding.blankee.core.data.repositoryImpl.PresetRepositoryImpl
import com.pronaycoding.blankee.core.datastore.PreferenceManagerRepository
import com.pronaycoding.blankee.core.datastore.PreferenceManagerRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoryModule = module {
    singleOf(::PreferenceManagerRepositoryImpl) bind PreferenceManagerRepository::class
    singleOf(::PresetRepositoryImpl) bind PresetRepository::class
    singleOf(::CustomSoundRepositoryImpl) bind CustomSoundRepository::class
}
