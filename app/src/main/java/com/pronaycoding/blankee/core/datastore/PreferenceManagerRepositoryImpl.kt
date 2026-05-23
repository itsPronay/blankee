package com.pronaycoding.blankee.core.datastore

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pronaycoding.blankee.core.common.Constants.LANGUAGE_TAG_SYSTEM
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.util.Locale

/**
 * Implementation of [PreferenceManagerRepository] using Android DataStore.
 *
 * DataStore is a modern replacement for SharedPreferences, providing:
 * - Type-safe access to preferences
 * - Encryption of sensitive data
 * - Coroutine-based async API
 * - Reactive Flows for preference changes
 *
 * This implementation stores preferences in a datastore file named "blankee_preferences".
 * All preference keys and their types are defined in the companion object's Keys class.
 *
 * Thread-safety: All methods are thread-safe and can be called from any thread.
 *
 * @param context The application context used to access DataStore
 *
 * @see PreferenceManagerRepository for the interface contract
 */
class PreferenceManagerRepositoryImpl(
    private val context: Context,
) : PreferenceManagerRepository {
    private val appContext = context.applicationContext

    /**
     * Retrieves the stored theme mode using synchronous blocking.
     *
     * Uses runBlocking internally - should only be called from contexts where blocking is acceptable.
     *
     * @param defaultValue Default value if not set
     * @return The stored theme mode or defaultValue
     */
    override fun getThemeModeBlocking(defaultValue: String): String =
        runBlocking {
            appContext.dataStore.data.first()[Keys.themeMode] ?: defaultValue
        }

    /**
     * Stores the theme mode preference.
     *
     * @param mode Theme mode to store
     */
    override suspend fun setThemeMode(mode: String) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.themeMode] = mode
        }
    }

    /**
     * Retrieves the stored language tag using synchronous blocking.
     *
     * The language tag is a BCP 47 locale identifier (e.g., "en", "hi", "es-MX").
     * Uses runBlocking internally - should only be called from contexts where blocking is acceptable.
     *
     * @param defaultValue Default value if not set
     * @return The stored language tag or defaultValue
     */
    override fun getLanguageTagBlocking(defaultValue: String): String =
        runBlocking {
            appContext.dataStore.data.first()[Keys.languageTag] ?: defaultValue
        }

    /**
     * Stores the language tag preference.
     *
     * @param tag BCP 47 language tag to store
     */
    override suspend fun setLanguageTag(tag: String) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.languageTag] = tag
        }
    }

    /**
     * Atomically increments the launch count by 1 and returns the new value.
     *
     * @return The new launch count after incrementing
     */
    override suspend fun incrementLaunchCount(): Int {
        var next = 1
        appContext.dataStore.edit { prefs ->
            next = (prefs[Keys.launchCount] ?: 0) + 1
            prefs[Keys.launchCount] = next
        }
        return next
    }

    /**
     * Retrieves the current launch count.
     *
     * @return The launch count, or 0 if not yet set
     */
    override suspend fun getLaunchCount(): Int = appContext.dataStore.data.first()[Keys.launchCount] ?: 0

    /**
     * Checks if the "Enjoy Blankee" prompt has been displayed.
     *
     * @return true if shown, false otherwise
     */
    override suspend fun isEnjoyPromptShown(): Boolean = appContext.dataStore.data.first()[Keys.enjoyPromptShown] ?: false

    /**
     * Marks the "Enjoy Blankee" prompt as shown/hidden.
     *
     * @param shown true to mark as shown
     */
    override suspend fun setEnjoyPromptShown(shown: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.enjoyPromptShown] = shown
        }
    }

    /**
     * Checks if the notification permission primer has been displayed.
     *
     * @return true if shown, false otherwise
     */
    override suspend fun isNotificationPrimerShown(): Boolean = appContext.dataStore.data.first()[Keys.notificationPrimerShown] ?: false

    /**
     * Marks the notification permission primer as shown/hidden.
     *
     * @param shown true to mark as shown
     */
    override suspend fun setNotificationPrimerShown(shown: Boolean) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.notificationPrimerShown] = shown
        }
    }

    companion object {
        /**
         * Wraps a Context to apply the stored language/locale settings.
         *
         * This method creates a new Context with Configuration modified to use the stored language tag.
         * Intended for use in Activity.attachBaseContext() to ensure all resources resolve with the correct locale.
         *
         * Example usage:
         * ```kotlin
         * override fun attachBaseContext(newBase: Context) {
         *     super.attachBaseContext(PreferenceManagerRepositoryImpl.wrapContextWithStoredLanguage(newBase))
         * }
         * ```
         *
         * @param base The base Context to wrap
         * @return A new Context configured with the stored language, or the original Context if system language is selected
         */
        fun wrapContextWithStoredLanguage(base: Context): Context {
            val tag = PreferenceManagerRepositoryImpl(base).getLanguageTagBlocking(LANGUAGE_TAG_SYSTEM)
            if (tag == LANGUAGE_TAG_SYSTEM) return base

            val locale = Locale.forLanguageTag(tag)
            Locale.setDefault(locale)
            val config = Configuration(base.resources.configuration)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocales(LocaleList(locale))
            } else {
                @Suppress("DEPRECATION")
                config.locale = locale
            }
            return base.createConfigurationContext(config)
        }
    }

    /**
     * Private object containing all DataStore preference keys.
     *
     * Each key is typed and corresponds to a specific preference value.
     */
    private object Keys {
        val themeMode: Preferences.Key<String> = stringPreferencesKey("app_theme_mode")
        val languageTag: Preferences.Key<String> = stringPreferencesKey("app_language_tag")
        val launchCount: Preferences.Key<Int> = intPreferencesKey("launch_count")
        val enjoyPromptShown: Preferences.Key<Boolean> = booleanPreferencesKey("enjoy_prompt_shown")
        val notificationPrimerShown: Preferences.Key<Boolean> =
            booleanPreferencesKey("notification_primer_shown")
    }
}

private val Context.dataStore by preferencesDataStore(name = "blankee_preferences")
