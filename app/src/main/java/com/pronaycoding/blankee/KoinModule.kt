package com.pronaycoding.blankee

import com.pronaycoding.blankee.core.data.repository.CustomSoundRepository
import com.pronaycoding.blankee.core.data.repository.PresetRepository
import com.pronaycoding.blankee.core.data.repositoryImpl.CustomSoundRepositoryImpl
import com.pronaycoding.blankee.core.data.repositoryImpl.PresetRepositoryImpl
import com.pronaycoding.blankee.core.database.BlankeeDatabase
import com.pronaycoding.blankee.core.database.dao.CustomSoundDao
import com.pronaycoding.blankee.core.database.dao.PresetDao
import com.pronaycoding.blankee.core.datastore.PreferenceManagerRepository
import com.pronaycoding.blankee.core.datastore.PreferenceManagerRepositoryImpl
import com.pronaycoding.blankee.core.service.playback.GlobalPlaybackState
import com.pronaycoding.blankee.core.service.playback.MediaPlaybackNotifications
import com.pronaycoding.blankee.feature.home.HomeViewmodel
import com.pronaycoding.blankee.feature.home.SoundManager
import com.pronaycoding.blankee.feature.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

/**
 * Koin Dependency Injection Module for the Blankee application.
 *
 * This object configures and provides all dependencies for the app using Koin,
 * a lightweight Kotlin dependency injection framework.
 *
 * Module Organization:
 * - **Repository Module**: Data access layers (repositories and preference manager)
 * - **ViewModel Module**: Lifecycle-aware screen state management
 * - **Database Module**: Room database and Data Access Objects (DAOs)
 * - **Helper Classes Module**: Services and managers (audio, playback, notifications)
 *
 * All modules are combined in [allModules] which is loaded during app initialization.
 *
 * @see org.koin.core.module.Module for Koin module documentation
 * @see allModules for the complete combined module
 */
object KoinModule {
    /**
     * Repository Module - Data Access Layer.
     *
     * Provides singleton instances of repository implementations:
     * - [PreferenceManagerRepository]: App preferences and settings
     * - [PresetRepository]: Preset persistence operations
     * - [CustomSoundRepository]: Custom sound file management
     *
     * Each repository is bound to its interface for dependency injection.
     */
    private val repositoryModule =
        module {
            singleOf(::PreferenceManagerRepositoryImpl) bind PreferenceManagerRepository::class
            singleOf(::PresetRepositoryImpl) bind PresetRepository::class
            singleOf(::CustomSoundRepositoryImpl) bind CustomSoundRepository::class
        }

    /**
     * ViewModel Module - UI State Management.
     *
     * Provides ViewModels for screens:
     * - [HomeViewmodel]: Home screen state (sounds, presets, playback)
     * - [SettingsViewModel]: Settings screen state (theme, language)
     *
     * ViewModels are created with viewModelOf() which automatically handles
     * lifecycle awareness and dependency injection.
     */
    private val viewmodelModule =
        module {
            viewModelOf(::HomeViewmodel)
            viewModelOf(::SettingsViewModel)
        }

    /**
     * Database Module - Persistence Layer.
     *
     * Provides:
     * - [BlankeeDatabase]: Singleton Room database instance
     * - [CustomSoundDao]: Custom sound database access
     * - [PresetDao]: Preset database access
     *
     * DAOs are extracted from the database singleton to avoid creating multiple instances.
     */
    private val databaseModule =
        module {
            single { BlankeeDatabase.getDatabase(get()) }
            single<CustomSoundDao> { get<BlankeeDatabase>().customSoundDao() }
            single<PresetDao> { get<BlankeeDatabase>().presetDao() }
        }

    /**
     * Helper Classes Module - Services and Managers.
     *
     * Provides singleton instances of core services:
     * - [SoundManager]: Audio playback control and MediaPlayer management
     * - [GlobalPlaybackState]: Global play/pause state and coordination
     * - [MediaPlaybackNotifications]: System notification management for playback
     *
     * These services are singletons to maintain consistent state across the app.
     */
    private val helperClassModules =
        module {
            single { SoundManager(get()) }
            single {
                GlobalPlaybackState(get()).apply {
                    GlobalPlaybackState.setInstance(this)
                }
            }
            single { MediaPlaybackNotifications(get()) }
        }

    /**
     * Complete Koin Module including all sub-modules.
     *
     * This module combines all dependency configurations and is passed to Koin during
     * application initialization (in App.kt). It includes:
     * - Repository implementations
     * - ViewModels
     * - Database and DAOs
     * - Services and managers
     *
     * @see repositoryModule
     * @see viewmodelModule
     * @see databaseModule
     * @see helperClassModules
     */
    val allModules =
        module {
            includes(
                repositoryModule,
                viewmodelModule,
                databaseModule,
                helperClassModules,
            )
        }
}
