package com.pronaycoding.blankee.feature.home

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.pronaycoding.blankee.core.model.CardItems
import java.io.File

/**
 * Manages audio playback for both built-in and custom sounds in the Blankee application.
 *
 * Every sound is its own looping [ExoPlayer], so any number of sounds can be mixed. Players use
 * media [AudioAttributes] and do not request audio focus individually (otherwise mixed players would
 * steal focus from each other and pause one another).
 *
 * Built-in players are created lazily on first play to avoid allocating a decoder per bundled sound.
 *
 * Thread-safety: This class must be used from the main/UI thread (ExoPlayer's application thread).
 *
 * @param context The application context used for resource access and player creation
 *
 * @see GlobalPlaybackState for overall app playback state management
 */
class SoundManager(
    private val context: Context,
) {
    private val builtInSources = mutableMapOf<Int, Int>()
    private val mediaPlayers = mutableMapOf<Int, ExoPlayer>()
    private val customMediaPlayers = mutableMapOf<Int, ExoPlayer>()
    private var currentlyPlayingSounds: MutableList<Int> = mutableListOf()

    private val audioAttributes =
        AudioAttributes
            .Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

    private fun newPlayer(
        uri: Uri,
        label: String,
    ): ExoPlayer =
        ExoPlayer
            .Builder(context)
            // Audio focus is intentionally off: with many mixed players each one would otherwise
            // take focus from the others.
            .setAudioAttributes(audioAttributes, false)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ONE
                addListener(
                    object : Player.Listener {
                        override fun onPlayerError(error: PlaybackException) {
                            Log.e("SoundManager", "Playback error for $label", error)
                        }
                    },
                )
                setMediaItem(MediaItem.fromUri(uri))
                prepare()
            }

    private fun ExoPlayer.isActive(): Boolean = playWhenReady && playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED

    private fun ExoPlayer.stopAndRewind() {
        playWhenReady = false
        seekTo(0)
    }

    /**
     * Registers all built-in sounds from [getCardList] by position. Players are created on first play.
     */
    fun loadSounds() {
        getCardList().forEachIndexed { index: Int, item: CardItems ->
            Log.d("SoundManager", "Registering sound: $index")
            builtInSources[index] = item.audioSource
        }
    }

    private fun builtInPlayer(soundIndex: Int): ExoPlayer? =
        mediaPlayers[soundIndex] ?: builtInSources[soundIndex]?.let { resId ->
            newPlayer(Uri.parse("android.resource://${context.packageName}/$resId"), "built-in $soundIndex")
                .also { mediaPlayers[soundIndex] = it }
        }

    /**
     * Loads a custom sound from file path or content URI
     */
    fun loadCustomSound(
        soundId: Int,
        filePath: String,
    ): Boolean {
        return try {
            Log.d("SoundManager", "Loading custom sound: $soundId from path: $filePath")
            val uri =
                if (filePath.startsWith("content://") || filePath.startsWith("file://")) {
                    Uri.parse(filePath)
                } else {
                    val file = File(filePath)
                    if (!file.exists()) {
                        Log.e("SoundManager", "Custom sound file does not exist: $filePath")
                        return false
                    }
                    Uri.fromFile(file)
                }
            customMediaPlayers.remove(soundId)?.release()
            customMediaPlayers[soundId] = newPlayer(uri, "custom $soundId")
            Log.d("SoundManager", "Successfully loaded custom sound: $soundId")
            true
        } catch (e: Exception) {
            Log.e("SoundManager", "Error loading custom sound: $soundId - ${e.message}", e)
            false
        }
    }

    /**
     * Unloads a custom sound and releases resources
     */
    fun unloadCustomSound(soundId: Int) {
        customMediaPlayers.remove(soundId)?.let {
            try {
                it.release()
                Log.d("SoundManager", "Unloaded custom sound: $soundId")
            } catch (e: Exception) {
                Log.e("SoundManager", "Error unloading custom sound: $soundId", e)
            }
        }
    }

    /**
     * Checks if any sound is playing.
     */
    fun isAnySoundPlaying(): Boolean = mediaPlayers.values.any { it.isActive() } || customMediaPlayers.values.any { it.isActive() }

    /**
     * Plays a sound if it's not already playing.
     */
    fun playSound(
        soundIndex: Int,
        volume: Float,
    ) {
        Log.d("SoundManager", "Play sound called for index: $soundIndex")
        builtInPlayer(soundIndex)?.let { player ->
            player.volume = volume
            if (!player.isActive()) {
                player.playWhenReady = true
                Log.d("SoundManager", "Playing sound at index: $soundIndex")
            }
        } ?: Log.e("SoundManager", "Player not found for index: $soundIndex")
    }

    fun isBuiltInPlaying(soundIndex: Int): Boolean = mediaPlayers[soundIndex]?.isActive() == true

    /**
     * Plays a custom sound from file
     */
    fun playCustomSound(
        soundId: Int,
        volume: Float,
    ) {
        Log.d("SoundManager", "Play custom sound called for id: $soundId")
        customMediaPlayers[soundId]?.let { player ->
            player.volume = volume
            if (!player.isActive()) {
                player.playWhenReady = true
                Log.d("SoundManager", "Playing custom sound: $soundId")
            }
        } ?: Log.e("SoundManager", "Custom player not found for id: $soundId")
    }

    fun isCustomSoundPlaying(soundId: Int): Boolean = customMediaPlayers[soundId]?.isActive() == true

    /**
     * Adjusts the volume of a currently playing sound.
     */
    fun controlSound(
        soundIndex: Int,
        volume: Float,
    ) {
        mediaPlayers[soundIndex]?.let {
            it.volume = volume
        } ?: Log.e("SoundManager", "Cannot control sound at index: $soundIndex, not found.")
    }

    /**
     * Adjusts the volume of a custom sound.
     */
    fun controlCustomSound(
        soundId: Int,
        volume: Float,
    ) {
        customMediaPlayers[soundId]?.let {
            it.volume = volume
        } ?: Log.e("SoundManager", "Cannot control custom sound: $soundId, not found.")
    }

    /**
     * Stops a currently playing sound.
     */
    fun stopSound(soundIndex: Int) {
        mediaPlayers[soundIndex]?.let {
            if (it.isActive()) {
                it.stopAndRewind()
                Log.d("SoundManager", "Stopped sound at index: $soundIndex")
            }
        } ?: Log.e("SoundManager", "Cannot stop sound at index: $soundIndex, not found.")
    }

    /**
     * Stops a custom sound.
     */
    fun stopCustomSound(soundId: Int) {
        customMediaPlayers[soundId]?.let {
            if (it.isActive()) {
                it.stopAndRewind()
                Log.d("SoundManager", "Stopped custom sound: $soundId")
            }
        } ?: Log.e("SoundManager", "Cannot stop custom sound: $soundId, not found.")
    }

    fun pauseAllSounds() {
        mediaPlayers.entries.forEach { (index, player) ->
            if (player.isActive()) {
                player.playWhenReady = false
                currentlyPlayingSounds.add(index)
                Log.d("SoundManager", "Paused sound at index: $index")
            }
        }
        customMediaPlayers.entries.forEach { (id, player) ->
            if (player.isActive()) {
                player.playWhenReady = false
                currentlyPlayingSounds.add(id)
                Log.d("SoundManager", "Paused custom sound: $id")
            }
        }
        Log.d("SoundManager", "All sounds paused.")
    }

    /**
     * Resumes all paused sounds.
     */
    fun resumeAllSounds() {
        mediaPlayers.entries.forEach { (index, player) ->
            if (currentlyPlayingSounds.contains(index)) {
                player.playWhenReady = true
            }
        }
        customMediaPlayers.entries.forEach { (id, player) ->
            if (currentlyPlayingSounds.contains(id)) {
                player.playWhenReady = true
            }
        }
        currentlyPlayingSounds = mutableListOf()
    }

    /**
     * Stops all currently playing sounds and clears active sound records.
     */
    fun stopAllSounds() {
        mediaPlayers.values.forEach { if (it.isActive()) it.stopAndRewind() }
        customMediaPlayers.values.forEach { if (it.isActive()) it.stopAndRewind() }
        currentlyPlayingSounds.clear()
        Log.d("SoundManager", "All sounds stopped.")
    }

    /**
     * Releases all players.
     */
    fun release() {
        mediaPlayers.values.forEach { it.release() }
        customMediaPlayers.values.forEach { it.release() }
        mediaPlayers.clear()
        customMediaPlayers.clear()
        Log.d("SoundManager", "SoundManager resources released.")
    }
}
