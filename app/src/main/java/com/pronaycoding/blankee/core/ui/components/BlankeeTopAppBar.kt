package com.pronaycoding.blankee.core.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pronaycoding.blankee.R
import com.pronaycoding.blankee.feature.home.HomeViewmodel
import com.pronaycoding.blankee.feature.home.getCardList
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlankeeTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigateToSettings: () -> Unit,
) {
    val viewModel: HomeViewmodel = koinViewModel()
    val context = LocalContext.current
    val canPlay by viewModel.canPlay.collectAsStateWithLifecycle()
    val presets by viewModel.presets.collectAsStateWithLifecycle()
    val builtinVolumes by viewModel.builtinVolumes.collectAsStateWithLifecycle()
    val customVolumes by viewModel.customVolumes.collectAsStateWithLifecycle()
    val sleepTimerRemainingMillis by viewModel.sleepTimerRemainingMillis.collectAsStateWithLifecycle()

    val canSavePreset =
        remember(builtinVolumes, customVolumes) {
            val until = getCardList().size
            builtinVolumes.any { (i, v) -> i in 0 until until && v > 0f } ||
                customVolumes.any { (_, v) -> v > 0f }
        }

    var showDropdown by rememberSaveable { mutableStateOf(false) }
    var showSavePresetDialog by rememberSaveable { mutableStateOf(false) }
    var presetNameInput by rememberSaveable { mutableStateOf("") }
    var showTimerDialog by rememberSaveable { mutableStateOf(false) }
    var selectedTimerOption by rememberSaveable { mutableIntStateOf(5) }
    var customTimerInput by rememberSaveable { mutableStateOf("") }
    var presetPendingDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var presetPendingDeleteName by rememberSaveable { mutableStateOf("") }

//    if (showSavePresetDialog) {
//        AlertDialog(
//            onDismissRequest = { showSavePresetDialog = false },
//            title = { Text(stringResource(R.string.save_preset_dialog_title)) },
//            text = {
//                OutlinedTextField(
//                    value = presetNameInput,
//                    onValueChange = { presetNameInput = it },
//                    label = { Text(stringResource(R.string.preset_name_label)) },
//                    singleLine = true,
//                    modifier = Modifier.fillMaxWidth()
//                )
//            },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        viewModel.savePreset(presetNameInput)
//                        presetNameInput = ""
//                        showSavePresetDialog = false
//                    }
//                ) {
//                    Text(stringResource(R.string.preset_dialog_save))
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { showSavePresetDialog = false }) {
//                    Text(stringResource(R.string.dialog_cancel))
//                }
//            }
//        )
//    }

    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { showTimerDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint =
                            if (sleepTimerRemainingMillis != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(stringResource(R.string.timer_dialog_title))
                }
            },
            text = {
                Column {
                    if (sleepTimerRemainingMillis != null) {
                        Text(
                            text =
                                stringResource(
                                    R.string.timer_countdown_label,
                                    formatRemainingTimerLabel(sleepTimerRemainingMillis ?: 0L),
                                ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 10.dp),
                        )
                    }

                    val timerOptions = listOf(1, 5, 10, 15, 30, -1)
                    timerOptions.forEach { option ->
                        val isSelected = selectedTimerOption == option
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        color =
                                            if (isSelected) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                            },
                                        shape = RoundedCornerShape(12.dp),
                                    ).clickable { selectedTimerOption = option }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedTimerOption = option },
                            )
                            Text(
                                text =
                                    if (option == -1) {
                                        stringResource(R.string.timer_custom)
                                    } else {
                                        stringResource(R.string.timer_minutes_format, option)
                                    },
                            )
                        }
                    }
                    if (selectedTimerOption == -1) {
                        OutlinedTextField(
                            value = customTimerInput,
                            onValueChange = { customTimerInput = it.filter(Char::isDigit) },
                            label = { Text(stringResource(R.string.timer_custom_label)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedMinutes =
                            if (selectedTimerOption == -1) {
                                customTimerInput.toLongOrNull()
                            } else {
                                selectedTimerOption.toLong()
                            }
                        if (selectedMinutes == null || selectedMinutes <= 0L) {
                            Toast
                                .makeText(
                                    context,
                                    context.getString(R.string.timer_invalid_input),
                                    Toast.LENGTH_SHORT,
                                ).show()
                            return@TextButton
                        }
                        viewModel.startSleepTimer(selectedMinutes * 60_000L)
                        Toast
                            .makeText(
                                context,
                                context.getString(R.string.timer_set, selectedMinutes),
                                Toast.LENGTH_SHORT,
                            ).show()
                        showTimerDialog = false
                    },
                ) {
                    Text(stringResource(R.string.timer_start))
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimerDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }

//    if (presetPendingDeleteId != null) {
//        AlertDialog(
//            onDismissRequest = { presetPendingDeleteId = null },
//            title = { Text(stringResource(R.string.preset_delete_confirm_title)) },
//            text = {
//                Text(
//                    stringResource(
//                        R.string.preset_delete_confirm_message,
//                        presetPendingDeleteName
//                    )
//                )
//            },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        viewModel.deletePreset(presetPendingDeleteId!!)
//                        presetPendingDeleteId = null
//                    }
//                ) {
//                    Text(stringResource(R.string.delete_sound_confirm))
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { presetPendingDeleteId = null }) {
//                    Text(stringResource(R.string.dialog_cancel))
//                }
//            }
//        )
//    }

    TopAppBar(
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                text = stringResource(R.string.app_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        actions = {
            Row {
                val btnColors =
                    IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(.1f),
                        contentColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                FilledTonalIconButton(
                    onClick = {
                        viewModel.resetAllSounds()
                        Toast
                            .makeText(
                                context,
                                context.getString(R.string.sounds_reset),
                                Toast.LENGTH_SHORT,
                            ).show()
                    },
                    colors = btnColors,
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.menu_reset),
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                FilledTonalIconButton(
                    onClick = { viewModel.handlePlayPause(!canPlay) },
                    shape = CircleShape,
                    colors = btnColors,
                ) {
                    Icon(
                        imageVector =
                            when (canPlay) {
                                true -> Icons.Default.Pause
                                false -> Icons.Default.PlayArrow
                            },
                        contentDescription =
                            if (canPlay) {
                                stringResource(R.string.menu_pause)
                            } else {
                                stringResource(R.string.menu_play)
                            },
                        modifier = Modifier.size(22.dp),
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                FilledTonalIconButton(
                    onClick = { showTimerDialog = true },
                    shape = CircleShape,
                    colors =
                        IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor =
                                if (sleepTimerRemainingMillis != null) {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer.copy(.1f)
                                },
                            contentColor =
                                if (sleepTimerRemainingMillis != null) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                },
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = stringResource(R.string.menu_timer),
                        tint =
                            if (sleepTimerRemainingMillis != null) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            },
                        modifier = Modifier.size(20.dp),
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                FilledTonalIconButton(
                    onClick = { showDropdown = true },
                    shape = CircleShape,
                    colors = btnColors,
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.content_desc_more_options),
                        modifier = Modifier.size(20.dp),
                    )
                    if (showDropdown) {
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_settings)) },
                                onClick = {
                                    showDropdown = false
                                    navigateToSettings()
                                },
                            )
                            HorizontalDivider()
//                            DropdownMenuItem(
//                                text = { Text(stringResource(R.string.presets_save_mix)) },
//                                onClick = {
//                                    showDropdown = false
//                                    if (!canSavePreset) {
//                                        Toast.makeText(
//                                            context,
//                                            context.getString(R.string.preset_nothing_to_save),
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    } else {
//                                        presetNameInput = ""
//                                        showSavePresetDialog = true
//                                    }
//                                }
//                            )
//                            HorizontalDivider()
//                            if (presets.isEmpty()) {
//                                DropdownMenuItem(
//                                    text = {
//                                        Text(
//                                            stringResource(R.string.presets_empty),
//                                            style = MaterialTheme.typography.bodySmall,
//                                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                                        )
//                                    },
//                                    onClick = { },
//                                    enabled = false
//                                )
//                            } else {
//                                presets.forEach { preset ->
//                                    DropdownMenuItem(
//                                        text = {
//                                            Text(
//                                                preset.name,
//                                                maxLines = 1,
//                                                overflow = TextOverflow.Ellipsis
//                                            )
//                                        },
//                                        onClick = {
//                                            showDropdown = false
//                                            viewModel.applyPreset(preset)
//                                        },
//                                        trailingIcon = {
//                                            IconButton(
//                                                onClick = {
//                                                    showDropdown = false
//                                                    presetPendingDeleteId = preset.id
//                                                    presetPendingDeleteName = preset.name
//                                                }
//                                            ) {
//                                                Icon(
//                                                    imageVector = Icons.Outlined.Delete,
//                                                    contentDescription = stringResource(R.string.preset_delete_desc)
//                                                )
//                                            }
//                                        }
//                                    )
//                                }
//                            }
                        }
                    }
                }
            }
        },
    )
}

private fun formatRemainingTimerLabel(remainingMillis: Long): String {
    val totalSeconds = (remainingMillis / 1000L).coerceAtLeast(0L)
    val hours = totalSeconds / 3600L
    val minutes = (totalSeconds % 3600L) / 60L
    val seconds = totalSeconds % 60L
    return if (hours > 0L) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
