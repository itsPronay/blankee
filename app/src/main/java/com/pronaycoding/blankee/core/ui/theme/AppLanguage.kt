package com.pronaycoding.blankee.core.ui.theme

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.pronaycoding.blankee.core.common.Constants.LANGUAGE_TAG_SYSTEM
import com.pronaycoding.blankee.core.datastore.PreferenceManagerRepositoryImpl
import java.util.Locale

/**
 * Provides the user-selected language to [content] by observing the stored language tag,
 * so changing it re-resolves strings (and layout direction) in place without recreating the activity.
 */
@Composable
fun AppLanguageProvider(content: @Composable () -> Unit) {
    val activityContext = LocalContext.current
    val preferenceManager = remember(activityContext) { PreferenceManagerRepositoryImpl(activityContext) }
    val tag by remember(preferenceManager) {
        preferenceManager.languageTagFlow(LANGUAGE_TAG_SYSTEM)
    }.collectAsState(
        initial = remember(preferenceManager) { preferenceManager.getLanguageTagBlocking(LANGUAGE_TAG_SYSTEM) },
    )

    val localized =
        remember(activityContext, tag) {
            val locale =
                if (tag == LANGUAGE_TAG_SYSTEM) {
                    Resources.getSystem().configuration.locales[0]
                } else {
                    Locale.forLanguageTag(tag)
                }
            Locale.setDefault(locale)
            val config = Configuration(activityContext.resources.configuration)
            config.setLocales(LocaleList(locale))
            config.setLayoutDirection(locale)
            LocalizedContext(activityContext, activityContext.createConfigurationContext(config).resources, config)
        }

    CompositionLocalProvider(
        LocalContext provides localized,
        LocalConfiguration provides localized.configuration,
        LocalLayoutDirection provides
            if (localized.configuration.layoutDirection == Configuration.SCREENLAYOUT_LAYOUTDIR_RTL) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            },
        content = content,
    )
}

/** Keeps the activity reachable through the wrapper chain while serving localized resources. */
private class LocalizedContext(
    base: Context,
    private val localizedResources: Resources,
    val configuration: Configuration,
) : ContextWrapper(base) {
    override fun getResources(): Resources = localizedResources
}
