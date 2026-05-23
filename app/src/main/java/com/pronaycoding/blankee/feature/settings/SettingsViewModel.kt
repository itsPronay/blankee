package com.pronaycoding.blankee.feature.settings

/**
 * ViewModel for the Settings screen managing user preferences.
 *
 * Responsibilities:
 * - Theme preference management (light/dark/system)
 * - Language/locale preference management
 *
 * State exposed:
 * - `selectedTheme`: Current theme mode
 * - `selectedLanguage`: Current language tag (BCP 47)
 * - `themeChoices`: Available theme options
 * - `languageChoices`: Available language options
 *
 * Theme changes require Activity recreation to apply new theme/language.
 *
 * @see PreferenceManagerRepository for preference persistence
 */

import androidx.lifecycle.ViewModel
import com.pronaycoding.blankee.R
import com.pronaycoding.blankee.core.common.Constants
import com.pronaycoding.blankee.core.datastore.PreferenceManagerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking

class SettingsViewModel(
    private val prefManager: PreferenceManagerRepository,
) : ViewModel() {
    val themeChoices =
        listOf(
            ThemeChoice(Constants.MODE_LIGHT, R.string.theme_light),
            ThemeChoice(Constants.MODE_DARK, R.string.theme_dark),
            ThemeChoice(Constants.MODE_SYSTEM, R.string.theme_system),
        )

    val languageChoices =
        listOf(
            LanguageChoice(Constants.LANGUAGE_TAG_SYSTEM, R.string.language_system),
            LanguageChoice(Constants.LANGUAGE_TAG_ENGLISH, R.string.language_english),
            LanguageChoice(Constants.LANGUAGE_TAG_CHINESE, R.string.language_chinese),
            LanguageChoice(Constants.LANGUAGE_TAG_HINDI, R.string.language_hindi),
            LanguageChoice(Constants.LANGUAGE_TAG_SPANISH, R.string.language_spanish),
            LanguageChoice(Constants.LANGUAGE_TAG_KOREAN, R.string.language_korean),
            LanguageChoice(Constants.LANGUAGE_TAG_ARABIC, R.string.language_arabic),
            LanguageChoice(Constants.LANGUAGE_TAG_FRENCH, R.string.language_french),
            LanguageChoice(Constants.LANGUAGE_TAG_BENGALI, R.string.language_bengali),
            LanguageChoice(Constants.LANGUAGE_TAG_PORTUGUESE, R.string.language_portuguese),
            LanguageChoice(Constants.LANGUAGE_TAG_TAMIL, R.string.language_tamil),
            LanguageChoice(Constants.LANGUAGE_TAG_URDU, R.string.language_urdu),
            LanguageChoice(Constants.LANGUAGE_TAG_INDONESIAN, R.string.language_indonesian),
            LanguageChoice(Constants.LANGUAGE_TAG_JAPANESE, R.string.language_japanese),
            LanguageChoice(Constants.LANGUAGE_TAG_RUSSIAN, R.string.language_russian),
            LanguageChoice(Constants.LANGUAGE_TAG_GERMAN, R.string.language_german),
            LanguageChoice(Constants.LANGUAGE_TAG_TURKISH, R.string.language_turkish),
            LanguageChoice(Constants.LANGUAGE_TAG_VIETNAMESE, R.string.language_vietnamese),
            LanguageChoice(Constants.LANGUAGE_TAG_THAI, R.string.language_thai),
            LanguageChoice(Constants.LANGUAGE_TAG_PERSIAN, R.string.language_persian),
            LanguageChoice(Constants.LANGUAGE_TAG_PUNJABI, R.string.language_punjabi),
        )

    private val _selectedTheme =
        MutableStateFlow(prefManager.getThemeModeBlocking(Constants.MODE_DARK))
    val selectedTheme: StateFlow<String> = _selectedTheme.asStateFlow()

    private val _selectedLanguage =
        MutableStateFlow(
            prefManager.getLanguageTagBlocking(Constants.LANGUAGE_TAG_SYSTEM),
        )
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()

    fun updateTheme(mode: String) {
        if (_selectedTheme.value == mode) return
        _selectedTheme.value = mode
        runBlocking {
            prefManager.setThemeMode(mode)
        }
    }

    fun updateLanguage(tag: String) {
        if (_selectedLanguage.value == tag) return
        _selectedLanguage.value = tag
        runBlocking {
            prefManager.setLanguageTag(tag)
        }
    }
}

data class ThemeChoice(
    val mode: String,
    val labelRes: Int,
)

data class LanguageChoice(
    val tag: String,
    val labelRes: Int,
)
