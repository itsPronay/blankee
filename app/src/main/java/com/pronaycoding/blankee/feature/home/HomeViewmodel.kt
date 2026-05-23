package com.pronaycoding.blankee.feature.home

/**
 * ViewModel for the Home screen managing all audio playback and preset logic.
 *
 * Responsibilities:
 * - Load and manage built-in sounds
 * - Load and manage custom user-uploaded sounds
 * - Control sound volume levels (both built-in and custom)
 * - Manage global playback state (play/pause/reset)
 * - Save and load sound presets
 * - Manage sleep timer functionality
 * - Coordinate with SoundManager, GlobalPlaybackState, and repositories
 *
 * State flows exposed:
 * - `canPlay`: Global playback state
 * - `customSounds`: List of user-uploaded custom sounds
 * - `builtinVolumes`: Current volume levels for built-in sounds
 * - `customVolumes`: Current volume levels for custom sounds
 * - `presets`: Saved sound presets
 * - `sleepTimerRemainingMillis`: Sleep timer countdown
 *
 * @see SoundManager for audio playback control
 * @see GlobalPlaybackState for global playback coordination
 * @see PresetRepositoryImpl for preset persistence
 * @see CustomSoundRepositoryImpl for custom sound management
 */

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pronaycoding.blankee.R
import com.pronaycoding.blankee.core.common.PresetJson
import com.pronaycoding.blankee.core.data.repositoryImpl.CustomSoundRepositoryImpl
import com.pronaycoding.blankee.core.data.repositoryImpl.PresetRepositoryImpl
import com.pronaycoding.blankee.core.database.entities.CustomSoundEntity
import com.pronaycoding.blankee.core.database.entities.PresetEntity
import com.pronaycoding.blankee.core.service.playback.GlobalPlaybackState
import com.pronaycoding.blankee.core.service.playback.MediaPlaybackNotifications
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class HomeViewmodel(
    private val soundManager: SoundManager,
    private val customSoundRepository: CustomSoundRepositoryImpl,
    private val presetRepository: PresetRepositoryImpl,
    private val globalPlaybackState: GlobalPlaybackState,
    private val mediaPlaybackNotifications: MediaPlaybackNotifications,
    private val context: Context,
) : ViewModel() {
    val canPlay: StateFlow<Boolean> = globalPlaybackState.canPlay

    private val _customSounds: MutableStateFlow<List<CustomSoundEntity>> = MutableStateFlow(emptyList())
    val customSounds: StateFlow<List<CustomSoundEntity>> = _customSounds.asStateFlow()

    private val _builtinVolumes: MutableStateFlow<Map<Int, Float>> = MutableStateFlow(emptyMap())
    val builtinVolumes: StateFlow<Map<Int, Float>> = _builtinVolumes.asStateFlow()

    private val _customVolumes: MutableStateFlow<Map<Int, Float>> = MutableStateFlow(emptyMap())
    val customVolumes: StateFlow<Map<Int, Float>> = _customVolumes.asStateFlow()

    val presets: StateFlow<List<PresetEntity>> =
        presetRepository
            .observePresets()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val loadedCustomSoundIds: MutableSet<Int> = mutableSetOf()
    private var customSoundsCollectionJob: Job? = null
    private var sleepTimerJob: Job? = null

    private val _sleepTimerRemainingMillis: MutableStateFlow<Long?> = MutableStateFlow(null)
    val sleepTimerRemainingMillis: StateFlow<Long?> = _sleepTimerRemainingMillis.asStateFlow()

    init {
        soundManager.loadSounds()
        startCustomSoundsCollection()
        // Always start from a clean playback state on app launch/re-entry.
        resetAllSounds()
    }

    private fun builtInPresetIndexEndExclusive(): Int = getCardList().size - 1

    private fun startCustomSoundsCollection() {
        if (customSoundsCollectionJob?.isActive == true) return
        customSoundsCollectionJob =
            viewModelScope.launch {
                customSoundRepository.getAllCustomSounds().collect { sounds ->
                    _customSounds.value = sounds

                    sounds.forEach { sound ->
                        if (!loadedCustomSoundIds.contains(sound.id)) {
                            val persistentPath = getPersistentFilePath(sound.id, sound.filePath)

                            Log.d("HomeViewmodel", "Loading custom sound from: $persistentPath")
                            soundManager.loadCustomSound(sound.id, persistentPath)
                            loadedCustomSoundIds.add(sound.id)
                            Log.d("HomeViewmodel", "Loaded new custom sound: ${sound.displayName}")
                        }
                    }

                    val soundIds = sounds.map { it.id }.toSet()
                    loadedCustomSoundIds.forEach { loadedId ->
                        if (!soundIds.contains(loadedId)) {
                            soundManager.unloadCustomSound(loadedId)
                            Log.d("HomeViewmodel", "Unloaded deleted custom sound: $loadedId")
                        }
                    }
                    loadedCustomSoundIds.retainAll(soundIds)
                    _customVolumes.update { current ->
                        current.filterKeys { it in soundIds }
                    }
                    syncPlaybackNotification()
                }
            }
    }

    private fun stopCustomSoundsCollection() {
        customSoundsCollectionJob?.cancel()
        customSoundsCollectionJob = null
        loadedCustomSoundIds.toList().forEach { id ->
            soundManager.unloadCustomSound(id)
        }
        loadedCustomSoundIds.clear()
        _customSounds.value = emptyList()
        _customVolumes.value = emptyMap()
        syncPlaybackNotification()
    }

    private fun hasAudibleMix(): Boolean {
        val end = builtInPresetIndexEndExclusive()
        val builtIn = _builtinVolumes.value.any { (i, v) -> i in 0 until end && v > 0f }
        val custom = _customVolumes.value.any { (_, v) -> v > 0f }
        return builtIn || custom
    }

    private fun syncPlaybackNotification() {
        globalPlaybackState.lastHasAudibleMix = hasAudibleMix()
        mediaPlaybackNotifications.requestSync(
            globalPlaybackState.canPlay.value,
            globalPlaybackState.lastHasAudibleMix,
        )
    }

    private fun getPersistentFilePath(
        soundId: Int,
        originalPath: String,
    ): String {
        val customSoundsDir = File(context.filesDir, "custom_sounds")
        if (!customSoundsDir.exists()) {
            customSoundsDir.mkdirs()
        }

        val persistentFile = File(customSoundsDir, "sound_$soundId.mp3")

        if (!persistentFile.exists()) {
            try {
                Log.d("HomeViewmodel", "Copying sound from $originalPath to ${persistentFile.absolutePath}")
                val inputStream =
                    if (originalPath.startsWith("content://")) {
                        context.contentResolver.openInputStream(originalPath.toUri())
                    } else {
                        File(originalPath).inputStream()
                    }

                inputStream?.use { input ->
                    persistentFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                Log.d("HomeViewmodel", "Successfully copied sound to persistent storage")
            } catch (e: Exception) {
                Log.e("HomeViewmodel", "Error copying sound file: ${e.message}", e)
                return originalPath
            }
        }

        return persistentFile.absolutePath
    }

    fun handlePlayPause(canPlay: Boolean) {
        globalPlaybackState.setCanPlay(canPlay)
        syncPlaybackNotification()
    }

    fun resetAllSounds() {
        soundManager.stopAllSounds()
        _builtinVolumes.value = emptyMap()
        _customVolumes.value = emptyMap()
        globalPlaybackState.setCanPlay(true)
        syncPlaybackNotification()
    }

    fun setBuiltinVolume(
        index: Int,
        volume: Float,
    ) {
        _builtinVolumes.update { current ->
            current.toMutableMap().apply { put(index, volume) }
        }
        if (!globalPlaybackState.canPlay.value) return
        if (volume <= 0f) {
            soundManager.stopSound(index)
        } else {
            if (soundManager.isBuiltInPlaying(index)) {
                soundManager.controlSound(index, volume)
            } else {
                soundManager.playSound(index, volume)
            }
        }
        syncPlaybackNotification()
    }

    /**
     * Updates built-in sound audio only while dragging a slider. Avoids updating [builtinVolumes]
     * and notification sync on every frame (those caused heavy recomposition and service churn).
     */
    fun previewBuiltinVolume(
        index: Int,
        volume: Float,
    ) {
        if (!globalPlaybackState.canPlay.value) return
        if (volume <= 0f) {
            soundManager.stopSound(index)
        } else {
            if (soundManager.isBuiltInPlaying(index)) {
                soundManager.controlSound(index, volume)
            } else {
                soundManager.playSound(index, volume)
            }
        }
    }

    fun onBuiltInCardClick(index: Int) {
        val isCurrentlyActive = (_builtinVolumes.value[index] ?: 0f) > 0f
        if (!globalPlaybackState.canPlay.value) {
            globalPlaybackState.setCanPlay(true)
            if (!isCurrentlyActive) {
                setBuiltinVolume(index, 1f)
            }
            return
        }
        val targetVolume = if (isCurrentlyActive) 0f else 1f
        setBuiltinVolume(index, targetVolume)
    }

    fun setCustomSoundVolume(
        soundId: Int,
        volume: Float,
    ) {
        _customVolumes.update { current ->
            current.toMutableMap().apply { put(soundId, volume) }
        }
        if (!globalPlaybackState.canPlay.value) return
        if (volume <= 0f) {
            soundManager.stopCustomSound(soundId)
        } else {
            if (soundManager.isCustomSoundPlaying(soundId)) {
                soundManager.controlCustomSound(soundId, volume)
            } else {
                soundManager.playCustomSound(soundId, volume)
            }
        }
        syncPlaybackNotification()
    }

    /** Same as [previewBuiltinVolume] for custom sounds. */
    fun previewCustomSoundVolume(
        soundId: Int,
        volume: Float,
    ) {
        if (!globalPlaybackState.canPlay.value) return
        if (volume <= 0f) {
            soundManager.stopCustomSound(soundId)
        } else {
            if (soundManager.isCustomSoundPlaying(soundId)) {
                soundManager.controlCustomSound(soundId, volume)
            } else {
                soundManager.playCustomSound(soundId, volume)
            }
        }
    }

    fun onCustomCardClick(soundId: Int) {
        val isCurrentlyActive = (_customVolumes.value[soundId] ?: 0f) > 0f
        if (!globalPlaybackState.canPlay.value) {
            globalPlaybackState.setCanPlay(true)
            if (!isCurrentlyActive) {
                setCustomSoundVolume(soundId, 1f)
            }
            return
        }
        val targetVolume = if (isCurrentlyActive) 0f else 1f
        setCustomSoundVolume(soundId, targetVolume)
    }

    fun stopSound(int: Int) {
        soundManager.stopSound(int)
    }

    fun stopCustomSound(soundId: Int) {
        soundManager.stopCustomSound(soundId)
    }

    fun playSound(
        int: Int,
        volume: Float,
    ) {
        if (globalPlaybackState.canPlay.value) {
            soundManager.playSound(int, volume)
        }
    }

    fun playCustomSound(
        soundId: Int,
        volume: Float,
    ) {
        if (globalPlaybackState.canPlay.value) {
            soundManager.playCustomSound(soundId, volume)
        }
    }

    fun addCustomSound(
        displayName: String,
        filePath: String,
    ) {
        viewModelScope.launch {
            customSoundRepository.addCustomSound(displayName, filePath)
        }
    }

    fun removeCustomSound(soundId: Int) {
        viewModelScope.launch {
            soundManager.unloadCustomSound(soundId)
            customSoundRepository.removeCustomSound(soundId)
            _customVolumes.update { it - soundId }
            syncPlaybackNotification()
        }
    }

    fun renameCustomSound(
        soundId: Int,
        newDisplayName: String,
    ) {
        val sanitizedName = newDisplayName.trim()
        if (sanitizedName.isEmpty()) return
        viewModelScope.launch {
            customSoundRepository.updateCustomSoundDisplayName(soundId, sanitizedName)
        }
    }

    fun snapshotForNewPreset(): Pair<Map<Int, Float>, Map<Int, Float>> {
        val end = builtInPresetIndexEndExclusive()
        val builtIn =
            _builtinVolumes.value.filter { (idx, vol) ->
                idx in 0 until end && vol > 0f
            }
        val custom =
            _customVolumes.value.filter { (_, vol) -> vol > 0f }

        return builtIn to custom
    }

    fun savePreset(name: String) {
        val (builtIn, custom) = snapshotForNewPreset()
        if (builtIn.isEmpty() && custom.isEmpty()) return
        viewModelScope.launch {
            presetRepository.savePreset(
                PresetEntity(
                    name = name.trim().ifEmpty { context.getString(R.string.preset_untitled) },
                    builtInVolumesJson = PresetJson.mapToJson(builtIn),
                    customVolumesJson = PresetJson.mapToJson(custom),
                ),
            )
            Toast
                .makeText(
                    context,
                    context.getString(R.string.preset_saved),
                    Toast.LENGTH_SHORT,
                ).show()
        }
    }

    fun applyPreset(preset: PresetEntity) {
        val endExclusive = builtInPresetIndexEndExclusive()
        val builtIn =
            PresetJson
                .jsonToMap(preset.builtInVolumesJson)
                .filterKeys { it in 0 until endExclusive }
        val custom = PresetJson.jsonToMap(preset.customVolumesJson)

        soundManager.stopAllSounds()

        _builtinVolumes.value = builtIn
        _customVolumes.value = custom
        globalPlaybackState.setCanPlay(true)

        builtIn.forEach { (index, volume) ->
            if (volume > 0f) {
                soundManager.playSound(index, volume)
            }
        }
        custom.forEach { (soundId, volume) ->
            if (volume > 0f && loadedCustomSoundIds.contains(soundId)) {
                soundManager.playCustomSound(soundId, volume)
            }
        }
        syncPlaybackNotification()
    }

    fun deletePreset(id: Long) {
        viewModelScope.launch {
            presetRepository.deletePreset(id)
        }
    }

    fun startSleepTimer(durationMillis: Long) {
        if (durationMillis <= 0L) return
        sleepTimerJob?.cancel()
        _sleepTimerRemainingMillis.value = durationMillis
        sleepTimerJob =
            viewModelScope.launch {
                val endTimeMillis = System.currentTimeMillis() + durationMillis
                while (true) {
                    val remaining = (endTimeMillis - System.currentTimeMillis()).coerceAtLeast(0L)
                    _sleepTimerRemainingMillis.value = remaining
                    if (remaining <= 0L) break
                    delay(1000L)
                }
                _sleepTimerRemainingMillis.value = null
                resetAllSounds()
                Toast
                    .makeText(
                        context,
                        context.getString(R.string.sleep_timer_finished),
                        Toast.LENGTH_SHORT,
                    ).show()
            }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerRemainingMillis.value = null
    }

    override fun onCleared() {
        sleepTimerJob?.cancel()
        customSoundsCollectionJob?.cancel()
        super.onCleared()
    }
}
