package com.pronaycoding.blankee.core.di

import com.pronaycoding.blankee.feature.home.HomeViewmodel
import com.pronaycoding.blankee.feature.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val viewModelModule = module {
    viewModelOf(::HomeViewmodel)
    viewModelOf(::SettingsViewModel)
}
