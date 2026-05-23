package com.pronaycoding.blankee.core.datastore

/**
 * Repository interface for managing app preferences and settings.
 *
 * This interface provides access to user preferences stored in DataStore, including:
 * - Theme mode (light/dark/system)
 * - Language/locale settings
 * - App usage tracking (launch count)
 * - UI prompt display state
 *
 * The implementation uses Android DataStore as the backing storage for type-safe, encrypted preference management.
 *
 * @see PreferenceManagerRepositoryImpl for the concrete implementation
 */
interface PreferenceManagerRepository {
    /**
     * Retrieves the current theme mode synchronously (blocking operation).
     *
     * Should only be used when synchronous access is necessary, such as in Activity.onCreate().
     * Prefer [PreferenceManagerRepositoryImpl.premiumUnlockedFlow] for reactive theme changes.
     *
     * @param defaultValue The value to return if theme mode has not been set
     * @return The stored theme mode (e.g., "light", "dark", "system")
     */
    fun getThemeModeBlocking(defaultValue: String): String

    /**
     * Sets the theme mode preference.
     *
     * @param mode The theme mode to set (e.g., "light", "dark", "system")
     */
    suspend fun setThemeMode(mode: String)

    /**
     * Retrieves the current language tag synchronously (blocking operation).
     *
     * The stored value is a BCP 47 language tag (e.g., "en", "hi", "es").
     * Special value: use Constants.LANGUAGE_TAG_SYSTEM for system language.
     *
     * Should only be used when synchronous access is necessary, such as in Activity.attachBaseContext().
     *
     * @param defaultValue The value to return if language tag has not been set
     * @return The stored BCP 47 language tag
     */
    fun getLanguageTagBlocking(defaultValue: String): String

    /**
     * Sets the language tag preference.
     *
     * @param tag The BCP 47 language tag to set
     */
    suspend fun setLanguageTag(tag: String)

    /**
     * Increments and returns the app launch count.
     *
     * This counter tracks how many times the app has been launched, useful for
     * showing prompts or analytics after N launches.
     *
     * @return The new launch count after incrementing
     */
    suspend fun incrementLaunchCount(): Int

    /**
     * Retrieves the current app launch count.
     *
     * @return The number of times the app has been launched
     */
    suspend fun getLaunchCount(): Int

    /**
     * Checks if the "Enjoy Blankee" prompt has been shown to the user.
     *
     * @return true if the prompt has been shown, false otherwise
     */
    suspend fun isEnjoyPromptShown(): Boolean

    /**
     * Sets the state of whether the "Enjoy Blankee" prompt has been shown.
     *
     * @param shown true to mark the prompt as shown
     */
    suspend fun setEnjoyPromptShown(shown: Boolean)

    /**
     * Checks if the notification permission primer has been shown to the user.
     *
     * @return true if the primer has been shown, false otherwise
     */
    suspend fun isNotificationPrimerShown(): Boolean

    /**
     * Sets the state of whether the notification permission primer has been shown.
     *
     * @param shown true to mark the primer as shown
     */
    suspend fun setNotificationPrimerShown(shown: Boolean)
}
