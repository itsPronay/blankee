package com.pronaycoding.blankee

import com.pronaycoding.blankee.core.di.databaseModule
import com.pronaycoding.blankee.core.di.repositoryModule
import com.pronaycoding.blankee.core.di.serviceModule
import com.pronaycoding.blankee.core.di.useCaseModule
import com.pronaycoding.blankee.core.di.viewModelModule
import org.koin.dsl.module

val appModule = module {
    includes(
        databaseModule,
        repositoryModule,
        useCaseModule,
        viewModelModule,
        serviceModule,
    )
}
