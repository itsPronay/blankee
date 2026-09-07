package com.pronaycoding.blankee.feature.settings

/**
 * Settings screen composable for the Blankee application.
 *
 * Displays app preferences and configuration options:
 * - Theme selection (Light/Dark/System)
 * - Language selection (English, Hindi, Bengali, Spanish, System)
 * - About section (app info, attribution, links)
 * - Privacy & Legal (privacy policy, open source attribution)
 *
 * Features:
 * - Segmented buttons for theme selection
 * - Single choice button row for language selection
 * - External links to privacy policy and GitHub
 * - Scrollable layout for all options
 *
 * State management via [SettingsViewModel]:
 * - Theme preference
 * - Language preference
 *
 * @see SettingsViewModel for screen state and business logic
 * @see PreferenceManagerRepository for preference persistence
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pronaycoding.blankee.R
import com.pronaycoding.blankee.core.common.Constants
import com.pronaycoding.blankee.core.common.util.findActivity
import com.pronaycoding.blankee.core.common.util.openExternalUrl
import com.pronaycoding.blankee.core.common.util.shareApp
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreenRoute(
    onBackPressed: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val selectedTheme by viewModel.selectedTheme.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    SettingsScreen(
        selectedTheme = selectedTheme,
        selectedLanguage = selectedLanguage,
        themeChoices = viewModel.themeChoices,
        languageChoices = viewModel.languageChoices,
        onBackPressed = onBackPressed,
        updateTheme = viewModel::updateTheme,
        updateLanguage = viewModel::updateLanguage,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    selectedTheme: String,
    selectedLanguage: String,
    themeChoices: List<ThemeChoice>,
    languageChoices: List<LanguageChoice>,
    updateTheme: (String) -> Unit,
    updateLanguage: (String) -> Unit,
    onBackPressed: () -> Unit,
) {
    val context = LocalContext.current
    val scheme = MaterialTheme.colorScheme

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = scheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = scheme.surface,
                        scrolledContainerColor = scheme.surface,
                    ),
            )
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.settings_section_app_preferences),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors =
                    CardDefaults.cardColors(
                        containerColor = scheme.surfaceContainerLow,
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    SettingHeading(
                        icon = Icons.Default.Palette,
                        title = stringResource(R.string.settings_theme),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    )
                    Text(
                        text = stringResource(R.string.settings_theme_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp).padding(bottom = 16.dp),
                    )

                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    ) {
                        themeChoices.forEachIndexed { index, choice ->
                            val selected = selectedTheme == choice.mode
                            SegmentedButton(
                                selected = selected,
                                onClick = {
                                    if (selectedTheme == choice.mode) return@SegmentedButton
                                    updateTheme(choice.mode)
                                    context.findActivity()?.recreate()
                                },
                                shape =
                                    SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = themeChoices.size,
                                    ),
                                colors =
                                    SegmentedButtonDefaults.colors(
                                        activeContainerColor = scheme.primaryContainer,
                                        activeContentColor = scheme.onPrimaryContainer,
                                        inactiveContainerColor = scheme.surfaceContainerHighest,
                                        inactiveContentColor = scheme.onSurface,
                                    ),
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                ) {
                                    Icon(
                                        imageVector =
                                            when (choice.mode) {
                                                Constants.MODE_LIGHT -> Icons.Outlined.LightMode
                                                Constants.MODE_DARK -> Icons.Outlined.DarkMode
                                                else -> Icons.Outlined.BrightnessAuto
                                            },
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp),
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
                                    Text(
                                        text = stringResource(choice.labelRes),
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 20.dp),
                        color = scheme.outlineVariant,
                    )

                    SettingHeading(
                        icon = Icons.Default.Language,
                        title = stringResource(R.string.settings_language),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                    )
                    Text(
                        text = stringResource(R.string.settings_language_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp).padding(top = 4.dp, bottom = 14.dp),
                    )

                    var expanded by remember { mutableStateOf(false) }
                    val currentLanguageLabel = stringResource(
                        languageChoices.find { it.tag == selectedLanguage }?.labelRes
                            ?: R.string.language_system
                    )

                    Surface(
                        onClick = { expanded = true },
                        shape = MaterialTheme.shapes.medium,
                        color = scheme.surfaceContainer,
                        modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 15.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = currentLanguageLabel,
                                style = MaterialTheme.typography.bodyLarge,
                                color = scheme.onSurface,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = scheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f),
                    ) {
                        languageChoices.forEach { choice ->
                            val label = stringResource(choice.labelRes)
                            val isSelected = selectedLanguage == choice.tag
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                        color = if (isSelected) scheme.primary else scheme.onSurface,
                                    )
                                },
                                onClick = {
                                    if (!isSelected) {
                                        updateLanguage(choice.tag)
                                        context.findActivity()?.recreate()
                                    }
                                    expanded = false
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = scheme.primary,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                } else null,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.settings_section_others),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.primary,
                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp, top = 4.dp),
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge,
                colors =
                    CardDefaults.cardColors(
                        containerColor = scheme.surfaceContainerLow,
                    ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            ) {
                SettingActionRow(
                    icon = Icons.Default.Share,
                    title = stringResource(R.string.share_app_title),
                    subtitle = stringResource(R.string.share_app_hint),
                    onClick = { shareApp(context) },
                )

                HorizontalDivider(color = scheme.outlineVariant)

                SettingActionRow(
                    icon = Icons.Default.BugReport,
                    title = stringResource(R.string.settings_report_bug_feature),
                    subtitle = stringResource(R.string.settings_report_bug_feature_hint),
                    trailingIcon = Icons.AutoMirrored.Filled.OpenInNew,
                    onClick = { openExternalUrl(context, Constants.GITHUB_REPO) },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            AttributionFooter(
                onOpenRafael = { openExternalUrl(context, Constants.RAFAEL_MARDOJAI_GITHUB) },
                onOpenPronay = { openExternalUrl(context, Constants.PRONAY_GITHUB) },
            )
        }
    }
}

@Composable
private fun SettingHeading(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = scheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = scheme.onSurface,
        )
    }
}

@Composable
private fun SettingActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailingIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowForward,
) {
    val scheme = MaterialTheme.colorScheme

    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = scheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun AttributionFooter(
    onOpenRafael: () -> Unit,
    onOpenPronay: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme

    Spacer(modifier = Modifier.height(12.dp))

    val linkStyle =
        SpanStyle(
            color = scheme.primary,
            fontWeight = FontWeight.SemiBold,
        )

    val inspired =
        AnnotatedString
            .Builder()
            .apply {
                append("Inspired by ")
                pushStringAnnotation(tag = "rafael", annotation = "rafael")
                withStyle(linkStyle) { append("Rafael Mardojai") }
                pop()
                append("'s Blanket")
            }.toAnnotatedString()

    ClickableText(
        text = inspired,
        style =
            MaterialTheme.typography.bodySmall.copy(
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            ),
        modifier = Modifier.fillMaxWidth(),
        onClick = { offset ->
            inspired
                .getStringAnnotations(tag = "rafael", start = offset, end = offset)
                .firstOrNull()
                ?.let { onOpenRafael() }
        },
    )

    val madeBy =
        AnnotatedString
            .Builder()
            .apply {
                append("Made with ❤️ by ")
                pushStringAnnotation(tag = "pronay", annotation = "pronay")
                withStyle(linkStyle) { append("Pronay") }
                pop()
            }.toAnnotatedString()

    ClickableText(
        text = madeBy,
        style =
            MaterialTheme.typography.bodySmall.copy(
                color = scheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            ),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 8.dp),
        onClick = { offset ->
            madeBy
                .getStringAnnotations(tag = "pronay", start = offset, end = offset)
                .firstOrNull()
                ?.let { onOpenPronay() }
        },
    )
}
