package com.pronaycoding.blankee.core.ui.components

/**
 * "Enjoy Blankee" prompt dialog component.
 *
 * This composable displays a series of dialogs to users after a certain number of app launches
 * (configured via [Constants.ENJOY_PROMPT_TRIGGER_LAUNCH]).
 *
 * Dialog sequence:
 * 1. Initial prompt asking "Are you enjoying Blankee?"
 * 2. Opens GitHub repository directly if user taps "Yes"
 *
 * Features:
 * - Shown only once per app installation (tracked via preferences)
 * - Automatic dismissal with state management
 * - Opens GitHub link in external browser
 *
 * @see Constants.ENJOY_PROMPT_TRIGGER_LAUNCH for trigger threshold
 * @see PreferenceManagerRepository for preference persistence
 */

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.pronaycoding.blankee.R
import com.pronaycoding.blankee.core.common.Constants
import com.pronaycoding.blankee.core.common.util.openExternalUrl
import com.pronaycoding.blankee.core.datastore.PreferenceManagerRepositoryImpl

@Composable
fun EnjoyBlankeePrompt(shouldShow: Boolean) {
    if (!shouldShow) return

    val context = LocalContext.current
    var showEnjoyDialog by rememberSaveable { mutableStateOf(false) }
    val preferenceManager =
        remember(context) {
            PreferenceManagerRepositoryImpl(
                context,
            )
        }

    LaunchedEffect(Unit) {
        // Mark as shown as soon as we decide to show, so it won't loop on restarts.
        preferenceManager.setEnjoyPromptShown(true)
        showEnjoyDialog = true
    }

    if (showEnjoyDialog) {
        AlertDialog(
            onDismissRequest = { showEnjoyDialog = false },
            title = { Text(stringResource(R.string.enjoy_prompt_title)) },
            text = { Text(stringResource(R.string.enjoy_prompt_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showEnjoyDialog = false
                        openExternalUrl(context, Constants.GITHUB_REPO)
                    },
                ) {
                    Text(stringResource(R.string.enjoy_prompt_yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showEnjoyDialog = false }) {
                    Text(stringResource(R.string.enjoy_prompt_no))
                }
            },
        )
    }
}
