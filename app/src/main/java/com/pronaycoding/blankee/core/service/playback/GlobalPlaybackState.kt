package com.pronaycoding.blankee.core.service.playback

import com.pronaycoding.blankee.feature.home.SoundManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the global playback state of all sounds in the Blankee application.
 *
 * This class maintains a single source of truth for whether audio playback is currently enabled
 * across the entire app. It provides functionality to:
 * - Query the current playback state via [canPlay] StateFlow
 * - Set playback state and coordinate pause/resume of all sounds
 * - Toggle between play and pause states
 * - Track whether the current audio mix is audible (used for notification management)
 *
 * The playback state is backed by a [StateFlow] for reactive updates throughout the app.
 * When playback is disabled, all sounds are paused; when enabled, all previously playing sounds resume.
 *
 * @param soundManager Reference to [SoundManager] for coordinating pause/resume of all loaded sounds
 *
 * @see SoundManager for audio playback control
 * @see HomeViewmodel for ViewModel-level playback management
 */
class GlobalPlaybackState(
    private val soundManager: SoundManager,
) {
    private val _canPlay = MutableStateFlow(true)

    /**
     * StateFlow representing the current global playback state.
     *
     * - true: Audio playback is enabled
     * - false: Audio playback is paused (all sounds are paused)
     */
    val canPlay: StateFlow<Boolean> = _canPlay.asStateFlow()

    @Volatile
    var lastHasAudibleMix: Boolean = false

    /**
     * Sets the global playback state and updates all sounds accordingly.
     *
     * When set to true, all previously playing sounds resume.
     * When set to false, all currently playing sounds are paused and can be resumed later.
     *
     * @param play true to enable playback, false to pause all sounds
     * @see resumeAllSounds
     * @see pauseAllSounds
     */
    fun setCanPlay(play: Boolean) {
        _canPlay.value = play
        if (play) {
            soundManager.resumeAllSounds()
        } else {
            soundManager.pauseAllSounds()
        }
    }

    /**
     * Toggles the global playback state between play and pause.
     *
     * This is a convenience method that inverts the current [canPlay] value and calls [setCanPlay].
     */
    fun togglePlayPause() {
        setCanPlay(!_canPlay.value)
    }

    companion object {
        @Volatile
        var currentState: GlobalPlaybackState? = null
            private set

        internal fun setInstance(state: GlobalPlaybackState) {
            currentState = state
        }
    }
}
