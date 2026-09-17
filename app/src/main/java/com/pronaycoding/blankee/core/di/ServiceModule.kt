package com.pronaycoding.blankee.core.di

import com.pronaycoding.blankee.core.service.playback.GlobalPlaybackState
import com.pronaycoding.blankee.core.service.playback.MediaPlaybackNotifications
import com.pronaycoding.blankee.feature.home.SoundManager
import org.koin.dsl.module

val serviceModule = module {
    single { SoundManager(get()) }
    single {
        GlobalPlaybackState(get()).apply {
            GlobalPlaybackState.setInstance(this)
        }
    }
    single { MediaPlaybackNotifications(get()) }
}
