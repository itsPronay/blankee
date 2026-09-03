package com.pronaycoding.blankee.core.model

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pronaycoding.blankee.R

/**
 * Sealed class representing available sounds in the Blankee app.
 *
 * This sealed class defines all built-in ambient sounds and provides a framework for custom user-uploaded sounds.
 * Sounds are organized into categories (Nature, Travel, Interiors, Noise, Custom) and each includes:
 * - A localized title (from string resources)
 * - An icon for UI display
 * - An audio resource file (for built-in sounds)
 * - Category information for UI grouping
 *
 * Built-in sounds are defined as data objects (singletons), while custom user sounds are represented
 * as data class instances of [CustomCardItem].
 *
 * @property titleResId String resource ID for the sound's localized name
 * @property icon Drawable resource ID for the sound's icon
 * @property audioSource Raw audio resource ID (only for built-in sounds)
 * @property type Category name (e.g., "Nature", "Travel", "Custom") for grouping
 * @property firstInType Boolean indicating if this is the first sound in its category (used for UI section headers)
 * @property customSoundId Unique ID for custom sounds (null for built-in sounds)
 * @property filePath File path or URI to audio file (only for custom sounds)
 *
 * @see CustomCardItem for custom user-uploaded sound representation
 * @see SoundManager for audio playback
 */
sealed class CardItems(
    @StringRes val titleResId: Int,
    var icon: Int,
    val audioSource: Int,
    val type: String = "",
    val firstInType: Boolean = false,
    val customSoundId: Int? = null,
    val filePath: String? = null,
) {
    /**
     * Returns the localized title string for this sound.
     *
     * For custom sounds, returns the user-provided display name.
     * For built-in sounds, loads the localized string from resources.
     *
     * @return The localized title string
     */
    @Composable
    fun localizedTitle(): String =
        when (this) {
            is CustomCardItem -> displayName
            else -> stringResource(titleResId)
        }

    /**
     * Rain sound - Nature category.
     * Gentle rainfall sounds for a calm, natural atmosphere.
     */
    data object Rain : CardItems(
        titleResId = R.string.sound_rain,
        icon = R.drawable.rain,
        audioSource = R.raw.rain,
        type = "Nature",
        firstInType = true,
    )

    /**
     * Summer Night sound - Nature category.
     * Evening outdoor ambience with summer insects and breeze.
     */
    data object SummerNight : CardItems(
        titleResId = R.string.sound_summer_night,
        icon = R.drawable.moon,
        audioSource = R.raw.summer_night,
        type = "Nature",
    )

    /**
     * Wind sound - Nature category.
     * Gentle wind and rustling leaves for outdoor ambience.
     */
    data object Wind : CardItems(
        titleResId = R.string.sound_wind,
        icon = R.drawable.wind,
        audioSource = R.raw.wind,
        type = "Nature",
    )

    /**
     * Ocean Wave sound - Nature category.
     * Soothing waves crashing on shore.
     */
    data object Wave : CardItems(
        titleResId = R.string.sound_wave,
        icon = R.drawable.wave,
        audioSource = R.raw.waves,
        type = "Nature",
    )

    /**
     * Stream sound - Nature category.
     * Gentle flowing water and babbling brook.
     */
    data object Stream : CardItems(
        titleResId = R.string.sound_stream,
        icon = R.drawable.stream,
        audioSource = R.raw.stream,
        type = "Nature",
    )

    /**
     * Thunder Storm sound - Nature category.
     * Thunderstorm with rain and distant lightning.
     */
    data object Storm : CardItems(
        titleResId = R.string.sound_storm,
        icon = R.drawable.storm,
        audioSource = R.raw.storm,
        type = "Nature",
    )

    /**
     * Bird Chirping sound - Nature category.
     * Morning birds and forest ambience.
     */
    data object Birds : CardItems(
        titleResId = R.string.sound_birds,
        icon = R.drawable.birds,
        audioSource = R.raw.birds,
        type = "Nature",
    )

    /**
     * Train sound - Travel category.
     * Moving train with rhythmic wheel sounds.
     */
    data object Train : CardItems(
        titleResId = R.string.sound_train,
        icon = R.drawable.train,
        audioSource = R.raw.train,
        type = "Travel",
        firstInType = true,
    )

    /**
     * Boat/Sailing sound - Travel category.
     * Boat engine and water ambience.
     */
    data object Boat : CardItems(
        titleResId = R.string.sound_boat,
        icon = R.drawable.sailboat,
        audioSource = R.raw.boat,
        type = "Travel",
    )

    /**
     * City Ambience sound - Travel category.
     * Urban background with traffic and city sounds.
     */
    data object City : CardItems(
        titleResId = R.string.sound_city,
        icon = R.drawable.city,
        audioSource = R.raw.city,
        type = "Travel",
    )

    /**
     * Coffee Shop sound - Interiors category.
     * Cozy coffee shop ambience with background chatter.
     */
    data object CoffeeShop : CardItems(
        titleResId = R.string.sound_coffee_shop,
        icon = R.drawable.coffee,
        audioSource = R.raw.coffee_shop,
        type = "Interiors",
        firstInType = true,
    )

    /**
     * Fireplace sound - Interiors category.
     * Crackling fireplace for warmth and comfort.
     */
    data object FirePlace : CardItems(
        titleResId = R.string.sound_fireplace,
        icon = R.drawable.fireplace,
        audioSource = R.raw.fireplace,
        type = "Interiors",
    )

    /**
     * Busy Restaurant sound - Interiors category.
     * Lively restaurant ambience with background noise.
     */
    data object BusyRestaurant : CardItems(
        titleResId = R.string.sound_busy_restaurant,
        icon = R.drawable.food_delivery,
        audioSource = R.raw.indoor_busy_restaurant,
        type = "Interiors",
    )

    /**
     * Pink Noise sound - Noise category.
     * Pink noise (deeper than white noise) for masking and focus.
     */
    data object PinkNoise : CardItems(
        titleResId = R.string.sound_pink_noise,
        icon = R.drawable.pink_noise,
        audioSource = R.raw.pink_noise,
        type = "Noise",
        firstInType = true,
    )

    /**
     * White Noise sound - Noise category.
     * Classic white noise for sleep and masking ambient sounds.
     */
    data object WhiteNoise : CardItems(
        titleResId = R.string.sound_white_noise,
        icon = R.drawable.white_noise,
        audioSource = R.raw.white_noise,
        type = "Noise",
    )

    /**
     * Custom sounds category marker.
     * Not a real sound, used as a placeholder for the Custom category.
     */
    data object Custom : CardItems(
        titleResId = R.string.empty_string,
        icon = R.drawable.city,
        audioSource = R.raw.city,
        type = "Custom",
    )

    /**
     * Represents a custom user-uploaded sound file.
     *
     * Custom sounds are audio files imported by users, extending the built-in sound library.
     * Each custom sound is tracked by a unique ID and its file path for persistent playback.
     *
     * @property id Unique database ID for this custom sound
     * @property displayName User-provided name for the custom sound
     * @property soundFilePath File path or content URI to the audio file
     *
     * @see CustomSoundRepository for custom sound management
     */
    data class CustomCardItem(
        val id: Int,
        val displayName: String,
        val soundFilePath: String,
    ) : CardItems(
            titleResId = R.string.empty_string,
            icon = R.drawable.white_noise,
            audioSource = R.raw.white_noise,
            type = "Custom",
            customSoundId = id,
            filePath = soundFilePath,
        )
}
