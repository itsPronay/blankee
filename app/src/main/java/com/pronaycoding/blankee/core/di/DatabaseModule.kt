package com.pronaycoding.blankee.core.di

import com.pronaycoding.blankee.core.database.BlankeeDatabase
import com.pronaycoding.blankee.core.database.dao.CustomSoundDao
import com.pronaycoding.blankee.core.database.dao.PresetDao
import org.koin.dsl.module

val databaseModule = module {
    single { BlankeeDatabase.getDatabase(get()) }
    single<CustomSoundDao> { get<BlankeeDatabase>().customSoundDao() }
    single<PresetDao> { get<BlankeeDatabase>().presetDao() }
}
