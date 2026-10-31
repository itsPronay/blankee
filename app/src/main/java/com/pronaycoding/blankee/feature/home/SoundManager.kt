package com.pronaycoding.blankee.feature.home

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import java.io.File

/**
 * Manages audio playback for both built-in and custom sounds in the Blankee application.
 *
 * Uses Media3 [ExoPlayer] with [Player.REPEAT_MODE_ONE] for smooth looping
 * (MediaPlayer.isLooping restarts the decoder and leaves an audible gap).
 *
 * This class handles:
 * - Built-in sounds (e.g., rain, birds, waves) loaded from app resources
 * - Custom sounds uploaded by users from device storage
 *
 * Key responsibilities:
 * - Loading and unloading audio files
 * - Playing, pausing, stopping, and controlling volume of sounds
 * - Looping playback for ambient/sleep sounds
 * - Tracking playing sounds for pause/resume functionality
 * - Proper resource cleanup and error handling
 *
 * Thread-safety: ExoPlayer instances must be used from the thread they were created on.
 * Use this class from the main/UI thread only.
 *
 * @param context The application context used for resource access and player creation
 *
 * @see GlobalPlaybackState for overall app playback state management
 */
class SoundManager(
    private val context: Context,
) {
    private val mediaPlayers = mutableMapOf<Int, ExoPlayer>()
    private val customMediaPlayers = mutableMapOf<Int, ExoPlayer>()

    // Players are created lazily on first play: building an ExoPlayer is expensive (own playback
    // thread, decoder), and creating all of them at launch blocked the main thread.
    private val customSoundUris = mutableMapOf<Int, Uri>()

    // Separate sets so a built-in index and a custom id with the same number never collide.
    private val pausedBuiltInSounds = mutableSetOf<Int>()
    private val pausedCustomSounds = mutableSetOf<Int>()

    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * True when the player is set to play and has not failed.
     * Uses playWhenReady instead of isPlaying so a sound that is still buffering
     * right after play() is still treated as "playing".
     */
    private val ExoPlayer.isActive: Boolean
        get() = playWhenReady && playbackState != Player.STATE_IDLE

    /**
     * Creates an ExoPlayer that loops [uri] forever and does NOT take audio focus,
     * so multiple sounds can play at the same time.
     */
    private fun createLoopingPlayer(
        uri: Uri,
        label: String,
        onError: () -> Unit = {},
    ): ExoPlayer {
        val player =
            ExoPlayer
                .Builder(context)
                .setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    false, // handleAudioFocus = false, otherwise sounds pause each other
                ).build()

        player.addListener(
            object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.e(TAG, "Playback error for $label: ${error.message}", error)
                    // Post so we never release a player from inside its own callback.
                    mainHandler.post { onError() }
                }
            },
        )

        player.setMediaItem(MediaItem.fromUri(uri))
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.volume = 1f
        player.prepare()
        return player
    }

    /**
     * Returns the player for a built-in sound, creating it on first use.
     *
     * @see getCardList for the list of available built-in sounds
     */
    @androidx.annotation.OptIn(UnstableApi::class)
    private fun builtInPlayer(index: Int): ExoPlayer? {
        mediaPlayers[index]?.let { return it }
        val item = getCardList().getOrNull(index) ?: return null
        return try {
            val uri = RawResourceDataSource.buildRawResourceUri(item.audioSource)
            createLoopingPlayer(uri, "built-in $index") {
                mediaPlayers.remove(index)?.release()
            }.also { mediaPlayers[index] = it }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sound: $index - ${e.message}", e)
            null
        }
    }

    private fun customPlayer(soundId: Int): ExoPlayer? {
        customMediaPlayers[soundId]?.let { return it }
        val uri = customSoundUris[soundId] ?: return null
        return try {
            createLoopingPlayer(uri, "custom $soundId") {
                customMediaPlayers.remove(soundId)?.release()
            }.also { customMediaPlayers[soundId] = it }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading custom sound: $soundId - ${e.message}", e)
            null
        }
    }

    /**
     * Loads a custom sound from a file path, file:// URI or content:// URI.
     *
     * Returns false if the file path does not exist or the player cannot be created.
     * Decoding errors (e.g. corrupt file) happen asynchronously; in that case the
     * sound is logged and automatically unloaded.
     */
    fun loadCustomSound(
        soundId: Int,
        filePath: String,
    ): Boolean {
        return try {
            Log.d(TAG, "Loading custom sound: $soundId from path: $filePath")

            val uri =
                when {
                    filePath.startsWith("content://") || filePath.startsWith("file://") -> {
                        Uri.parse(filePath)
                    }

                    else -> {
                        val file = File(filePath)
                        if (!file.exists()) {
                            Log.e(TAG, "Custom sound file does not exist: $filePath")
                            return false
                        }
                        Uri.fromFile(file)
                    }
                }

            // Already registered (e.g. the UI was recreated while it is playing): keep the live player.
            if (customSoundUris[soundId] == uri && customMediaPlayers.containsKey(soundId)) return true

            customMediaPlayers.remove(soundId)?.release() // avoid leaks if reloaded
            customSoundUris[soundId] = uri
            Log.d(TAG, "Registered custom sound: $soundId")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error loading custom sound: $soundId - ${e.message}", e)
            false
        }
    }

    /** Volumes of built-in sounds that are playing, or paused by [pauseAllSounds]. */
    fun activeBuiltInVolumes(): Map<Int, Float> =
        mediaPlayers
            .filter { (index, player) -> (player.isActive || index in pausedBuiltInSounds) && player.volume > 0f }
            .mapValues { it.value.volume }

    /** Volumes of custom sounds that are playing, or paused by [pauseAllSounds]. */
    fun activeCustomVolumes(): Map<Int, Float> =
        customMediaPlayers
            .filter { (id, player) -> (player.isActive || id in pausedCustomSounds) && player.volume > 0f }
            .mapValues { it.value.volume }

    /**
     * Unloads a custom sound and releases resources.
     */
    fun unloadCustomSound(soundId: Int) {
        pausedCustomSounds.remove(soundId)
        customSoundUris.remove(soundId)
        customMediaPlayers.remove(soundId)?.let {
            try {
                it.release()
                Log.d(TAG, "Unloaded custom sound: $soundId")
            } catch (e: Exception) {
                Log.e(TAG, "Error unloading custom sound: $soundId", e)
            }
        }
    }

    /**
     * Checks if any sound is playing.
     */
    fun isAnySoundPlaying(): Boolean = mediaPlayers.values.any { it.isActive } || customMediaPlayers.values.any { it.isActive }

    /**
     * Plays a built-in sound. If it is already playing, only the volume is updated.
     */
    fun playSound(
        soundIndex: Int,
        volume: Float,
    ) {
        Log.d(TAG, "Play sound called for index: $soundIndex")
        builtInPlayer(soundIndex)?.let { player ->
            player.volume = volume.coerceIn(0f, 1f)
            if (!player.isActive) {
                player.play()
                Log.d(TAG, "Playing sound at index: $soundIndex")
            } else {
                Log.d(TAG, "Sound at index: $soundIndex already playing; volume updated.")
            }
        } ?: Log.e(TAG, "Player not found for index: $soundIndex")
    }

    fun isBuiltInPlaying(soundIndex: Int): Boolean = mediaPlayers[soundIndex]?.isActive == true

    /**
     * Plays a custom sound. If it is already playing, only the volume is updated.
     */
    fun playCustomSound(
        soundId: Int,
        volume: Float,
    ) {
        Log.d(TAG, "Play custom sound called for id: $soundId")
        customPlayer(soundId)?.let { player ->
            player.volume = volume.coerceIn(0f, 1f)
            if (!player.isActive) {
                player.play()
                Log.d(TAG, "Playing custom sound: $soundId")
            } else {
                Log.d(TAG, "Custom sound: $soundId already playing; volume updated.")
            }
        } ?: Log.e(TAG, "Custom player not found for id: $soundId")
    }

    fun isCustomSoundPlaying(soundId: Int): Boolean = customMediaPlayers[soundId]?.isActive == true

    /**
     * Adjusts the volume of a built-in sound.
     */
    fun controlSound(
        soundIndex: Int,
        volume: Float,
    ) {
        mediaPlayers[soundIndex]?.let {
            it.volume = volume.coerceIn(0f, 1f)
        }
    }

    /**
     * Adjusts the volume of a custom sound.
     */
    fun controlCustomSound(
        soundId: Int,
        volume: Float,
    ) {
        customMediaPlayers[soundId]?.let {
            it.volume = volume.coerceIn(0f, 1f)
        }
    }

    /**
     * Stops a built-in sound and rewinds it to the start.
     */
    fun stopSound(soundIndex: Int) {
        pausedBuiltInSounds.remove(soundIndex)
        mediaPlayers[soundIndex]?.let {
            it.pause()
            it.seekTo(0)
            Log.d(TAG, "Stopped sound at index: $soundIndex")
        }
    }

    /**
     * Stops a custom sound and rewinds it to the start.
     */
    fun stopCustomSound(soundId: Int) {
        pausedCustomSounds.remove(soundId)
        customMediaPlayers[soundId]?.let {
            it.pause()
            it.seekTo(0)
            Log.d(TAG, "Stopped custom sound: $soundId")
        }
    }

    /**
     * Pauses everything that is currently playing and remembers it for [resumeAllSounds].
     */
    fun pauseAllSounds() {
        mediaPlayers.entries.forEach { (index, player) ->
            if (player.isActive) {
                player.pause()
                pausedBuiltInSounds.add(index)
                Log.d(TAG, "Paused sound at index: $index")
            }
        }
        customMediaPlayers.entries.forEach { (id, player) ->
            if (player.isActive) {
                player.pause()
                pausedCustomSounds.add(id)
                Log.d(TAG, "Paused custom sound: $id")
            }
        }
        Log.d(TAG, "All sounds paused.")
    }

    /**
     * Resumes all sounds that were paused by [pauseAllSounds].
     */
    fun resumeAllSounds() {
        pausedBuiltInSounds.forEach { index -> mediaPlayers[index]?.play() }
        pausedCustomSounds.forEach { id -> customMediaPlayers[id]?.play() }
        pausedBuiltInSounds.clear()
        pausedCustomSounds.clear()
    }

    /**
     * Stops all sounds, rewinds them, and clears the paused records.
     */
    fun stopAllSounds() {
        mediaPlayers.values.forEach {
            it.pause()
            it.seekTo(0)
        }
        customMediaPlayers.values.forEach {
            it.pause()
            it.seekTo(0)
        }
        pausedBuiltInSounds.clear()
        pausedCustomSounds.clear()
        Log.d(TAG, "All sounds stopped.")
    }

    /**
     * Releases all players. Call when the manager is no longer needed.
     */
    fun release() {
        mainHandler.removeCallbacksAndMessages(null)
        mediaPlayers.values.forEach { it.release() }
        customMediaPlayers.values.forEach { it.release() }
        mediaPlayers.clear()
        customMediaPlayers.clear()
        customSoundUris.clear()
        pausedBuiltInSounds.clear()
        pausedCustomSounds.clear()
        Log.d(TAG, "SoundManager resources released.")
    }

    private companion object {
        const val TAG = "SoundManager"
    }
}
