package com.pronaycoding.blankee.feature.home

/**
 * Home screen composable for the Blankee application.
 *
 * The main feature screen displaying:
 * - Sound categories (Nature, Travel, Interiors, Noise)
 * - Built-in ambient sounds with individual volume controls
 * - Custom user-uploaded sounds
 * - Sound selection and mixing interface
 * - Integration with playback controls (play/pause, reset, timer)
 *
 * Features:
 * - LazyColumn layout for efficient scrolling through sounds
 * - Real-time volume adjustments
 * - Custom sound upload capability
 * - Preset management (save/load/delete)
 * - Top app bar with media controls
 *
 * State management via [HomeViewmodel]:
 * - Built-in sound volumes
 * - Custom sound volumes
 * - Playback state
 * - Preset management
 * - Sleep timer
 *
 * @see HomeViewmodel for screen state and business logic
 * @see BlankeeTopAppBar for media controls
 * @see PrettyCardView for sound card UI
 * @see CustomSoundCard for custom sound card UI
 */

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButtonDefaults.filledTonalIconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pronaycoding.blankee.R
import com.pronaycoding.blankee.core.database.entities.CustomSoundEntity
import com.pronaycoding.blankee.core.database.entities.PresetEntity
import com.pronaycoding.blankee.core.model.CardItems
import com.pronaycoding.blankee.core.ui.components.BlankeeTopAppBar
import com.pronaycoding.blankee.core.ui.components.CustomSoundCard
import com.pronaycoding.blankee.core.ui.components.PrettyCardView
import com.pronaycoding.blankee.core.ui.components.TitleCardView
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeScreenRoute(
    navigateToSettings: () -> Unit,
    viewmodel: HomeViewmodel = koinViewModel(),
) {
    val canPlaySound by viewmodel.canPlay.collectAsStateWithLifecycle()
    val customSounds by viewmodel.customSounds.collectAsStateWithLifecycle()
    val sleepTimerRemainingMillis by viewmodel.sleepTimerRemainingMillis.collectAsStateWithLifecycle()
    val canPlay by viewmodel.canPlay.collectAsStateWithLifecycle()
//    val canPlay by viewModel.canPlay.collectAsStateWithLifecycle()
    val presets by viewmodel.presets.collectAsStateWithLifecycle()
    val builtinVolumes by viewmodel.builtinVolumes.collectAsStateWithLifecycle()
    val customVolumes by viewmodel.customVolumes.collectAsStateWithLifecycle()

    HomeScreen(
        canPlay = canPlay,
        presets = presets,
        builtinVolumes = builtinVolumes,
        customVolumes = customVolumes,
        navigateToSettings = navigateToSettings,
        canPlaySound = canPlaySound,
        savePreset = viewmodel::savePreset,
        customSounds = customSounds,
        startSleepTimer = viewmodel::startSleepTimer,
        applyPreset = viewmodel::applyPreset,
        onAddCustomSound = { displayName, filePath ->
            viewmodel.addCustomSound(
                displayName,
                filePath,
            )
        },
        handlePlayPause = viewmodel::handlePlayPause,
        onDeleteCustomSound = { soundId -> viewmodel.removeCustomSound(soundId) },
        sleepTimerRemainingMillis = sleepTimerRemainingMillis,
        onCancelSleepTimer = { viewmodel.cancelSleepTimer() },
        resetAllSounds = viewmodel::resetAllSounds,
        deletePreset = viewmodel::deletePreset,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeScreen(
    canPlay: Boolean,
    savePreset: (String) -> Unit,
    presets: List<PresetEntity>,
    builtinVolumes: Map<Int, Float>,
    customVolumes: Map<Int, Float>,
    navigateToSettings: () -> Unit,
    startSleepTimer: (Long) -> Unit,
    applyPreset: (PresetEntity) -> Unit,
    canPlaySound: Boolean,
    deletePreset: (Long) -> Unit,
    customSounds: List<CustomSoundEntity> = emptyList(),
    resetAllSounds: () -> Unit,
    handlePlayPause: (Boolean) -> Unit,
    onAddCustomSound: (String, String) -> Unit = { _, _ -> },
    onDeleteCustomSound: (Int) -> Unit = {},
    sleepTimerRemainingMillis: Long? = null,
    onCancelSleepTimer: () -> Unit = {},
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showCancelTimerDialog by rememberSaveable { mutableStateOf(false) }
    var showTimerDialog by rememberSaveable { mutableStateOf(false) }

    val btnColors =
        IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(.1f),
            contentColor = MaterialTheme.colorScheme.primaryContainer,
        )

    var showDropdown by rememberSaveable { mutableStateOf(false) }
    var showSavePresetDialog by rememberSaveable { mutableStateOf(false) }
    var presetNameInput by rememberSaveable { mutableStateOf("") }
//    var showTimerDialog by rememberSaveable { mutableStateOf(false) }
    var selectedTimerOption by rememberSaveable { mutableIntStateOf(5) }
    var customTimerInput by rememberSaveable { mutableStateOf("") }
    var presetPendingDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }
    var presetPendingDeleteName by rememberSaveable { mutableStateOf("") }
    var presetClicked by rememberSaveable { mutableStateOf(false) }

    val canSavePreset =
        remember(builtinVolumes, customVolumes) {
            val until = getCardList().size - 1
            builtinVolumes.any { (i, v) -> i in 0 until until && v > 0f } ||
                customVolumes.any { (_, v) -> v > 0f }
        }

    PresetClicked(
        context = context,
        dropdownEnabled = presetClicked,
        applyPreset = applyPreset,
        presets = presets,
        setDropdownFalse = { presetClicked = false },
        setPresentPendingDeleteId = { presetPendingDeleteId = it },
        setPresetPendingDeleteName = { presetPendingDeleteName = it },
        canSavePreset = canSavePreset,
        savePreset = savePreset,
    )

    DeletePresetDialog(
        presetPendingDeleteName = presetPendingDeleteName,
        presetPendingDeleteId = presetPendingDeleteId,
        deletePreset = deletePreset,
        setPresetPendingDeleteId = { presetPendingDeleteId = it },
    )

    TimerDialog(
        context = context,
        showTimerDialog = showTimerDialog,
        sleepTimerRemainingMillis = sleepTimerRemainingMillis,
        setTimerDialogFalse = { showTimerDialog = false },
        startSleepTimer = startSleepTimer,
    )

    val audioPickerLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent(),
        ) { uri: Uri? ->
            uri?.let {
                val defaultName =
                    context.getString(
                        R.string.custom_sound_default_name,
                        customSounds.size + 1,
                    )
                val displayName = defaultName
                val filePath = it.toString()
                if (filePath.isNotEmpty()) {
                    onAddCustomSound(displayName, filePath)
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.sound_added, displayName),
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.could_not_add_sound),
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            }
        }

    Scaffold(
        bottomBar = {
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .height(60.dp),
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            presetClicked = true
                        },
                        colors = btnColors,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = stringResource(R.string.content_desc_more_options),
                            modifier = Modifier.size(18.dp),
                        )
                    }

                    FilledTonalIconButton(
                        onClick = {
                            resetAllSounds()
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
                        )
                    }

                    FilledTonalIconButton(
                        modifier =
                            Modifier
                                .size(60.dp),
                        onClick = { handlePlayPause(!canPlay) },
                        shape = CircleShape,
                        colors =
                            filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.error.copy(.1f),
                                contentColor = MaterialTheme.colorScheme.error,
                            ),
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
                            modifier = Modifier.size(42.dp),
                        )
                    }

                    FilledTonalIconButton(
                        onClick = { showTimerDialog = true },
                        shape = CircleShape,
                        colors =
                            filledTonalIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(.1f),
                            ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = stringResource(R.string.menu_timer),
                            tint = MaterialTheme.colorScheme.primaryContainer,
                        )
                    }

                    FilledTonalIconButton(
                        onClick = {
                            navigateToSettings()
                        },
                        colors = btnColors,
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.content_desc_more_options),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        },
    ) {
        Column(
            modifier =
                Modifier
                    .padding(it)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                content = {
                    // responsible for sleep timer countdown showing on UI
                    if (sleepTimerRemainingMillis != null) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            SleepTimerActiveCard(
                                sleepTimerRemainingMillis = sleepTimerRemainingMillis,
                                setShowCancelTimerDialogTrue = {
                                    showCancelTimerDialog = true
                                },
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        CustomCard {
                            SoundGrid(
                                items = homeGridCardItems(),
                                canPlaySound = canPlaySound,
                            )
                        }
                    }

                    if (customSounds.isNotEmpty()) {
                        item {
                            TitleCardView(stringResource(R.string.category_custom))
                            CustomCard {
                                val chunks = customSounds.chunked(2)
                                chunks.forEach { chunk ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        chunk.forEach { customSound ->
                                            CustomSoundCard(
                                                modifier = Modifier.weight(1f),
                                                soundId = customSound.id,
                                                displayName = customSound.displayName,
                                                playOrPause = canPlaySound,
                                                onDeleteClick = onDeleteCustomSound,
                                            )
                                        }
                                        // Fill empty slot if odd number
                                        if (chunk.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Button(
                                onClick = { audioPickerLauncher.launch("audio/*") },
                                modifier =
                                    Modifier
//                                    .fillMaxWidth()
                                        .height(56.dp)
                                        .align(Alignment.Center),
                                shape = RoundedCornerShape(16.dp),
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.background,
                                    ),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = stringResource(R.string.content_desc_add_custom_sound),
                                    modifier = Modifier.size(24.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    stringResource(R.string.add_custom_sound),
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                },
            )
        }
    }

    if (showCancelTimerDialog) {
        AlertDialog(
            onDismissRequest = { showCancelTimerDialog = false },
            title = { Text(stringResource(R.string.timer_cancel_confirm_title)) },
            text = { Text(stringResource(R.string.timer_cancel_confirm_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancelSleepTimer()
                        showCancelTimerDialog = false
                    },
                ) {
                    Text(stringResource(R.string.timer_cancel))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelTimerDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }
}

private fun formatTimer(remainingMillis: Long): String {
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

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun SoundGrid(
    items: List<CardItems>,
    canPlaySound: Boolean,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val itemMinWidth = 140.dp
        val columns = ((maxWidth - 8.dp) / (itemMinWidth + 8.dp)).toInt().coerceAtLeast(1).coerceAtMost(5)

        Column {
            val chunks = items.chunked(columns)
            chunks.forEach { chunk ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    chunk.forEach { cardItem ->
                        PrettyCardView(
                            modifier = Modifier.weight(1f),
                            index = getCardList().indexOf(cardItem),
                            cardItem = cardItem,
                            playOrPause = canPlaySound,
                        )
                    }
                    repeat(columns - chunk.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

fun getCardList(): List<CardItems> =
    listOf(
        CardItems.Rain,
        CardItems.Wind,
        CardItems.Storm,
        CardItems.Wave,
        CardItems.Stream,
        CardItems.Birds,
        CardItems.SummerNight,
        CardItems.Train,
        CardItems.Boat,
        CardItems.City,
        CardItems.CoffeeShop,
        CardItems.FirePlace,
        CardItems.BusyRestaurant,
        CardItems.PinkNoise,
        CardItems.WhiteNoise,
    )

private fun homeGridCardItems(): List<CardItems> = getCardList()

@Composable
fun CustomCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
fun PresetClicked(
    dropdownEnabled: Boolean,
    applyPreset: (PresetEntity) -> Unit,
    presets: List<PresetEntity>,
    setDropdownFalse: () -> Unit,
    setPresentPendingDeleteId: (Long) -> Unit,
    setPresetPendingDeleteName: (String) -> Unit,
    canSavePreset: Boolean,
    savePreset: (String) -> Unit,
    context: Context,
) {
    var presetNameInput by rememberSaveable { mutableStateOf("") }
    var showSavePresetDialog by rememberSaveable { mutableStateOf(false) }

    DropdownMenu(
        expanded = dropdownEnabled,
        onDismissRequest = setDropdownFalse,
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.presets_save_mix)) },
            onClick = {
                setDropdownFalse()
                if (!canSavePreset) {
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.preset_nothing_to_save),
                            Toast.LENGTH_SHORT,
                        ).show()
                } else {
                    presetNameInput = ""
                    showSavePresetDialog = true
                }
            },
        )
        HorizontalDivider()
        if (presets.isEmpty()) {
            DropdownMenuItem(
                text = {
                    Text(
                        stringResource(R.string.presets_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                onClick = { },
                enabled = false,
            )
        } else {
            presets.forEach { preset ->
                DropdownMenuItem(
                    text = {
                        Text(
                            preset.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        setDropdownFalse()
                        applyPreset(preset)
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                setDropdownFalse()
                                setPresentPendingDeleteId(preset.id)
                                setPresetPendingDeleteName(preset.name)
                            },
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                tint = MaterialTheme.colorScheme.error,
                                contentDescription = stringResource(R.string.preset_delete_desc),
                            )
                        }
                    },
                )
            }
        }
    }

    if (showSavePresetDialog) {
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text(stringResource(R.string.save_preset_dialog_title)) },
            text = {
                OutlinedTextField(
                    supportingText = { Text(stringResource(R.string.preset_name_hint)) },
                    value = presetNameInput,
                    onValueChange = { presetNameInput = it },
                    label = { Text(stringResource(R.string.preset_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        savePreset(presetNameInput)
                        presetNameInput = ""
                        showSavePresetDialog = false
                    },
                ) {
                    Text(stringResource(R.string.preset_dialog_save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSavePresetDialog = false }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }
}

@Composable
fun DeletePresetDialog(
    modifier: Modifier = Modifier,
    presetPendingDeleteName: String,
    presetPendingDeleteId: Long?,
    deletePreset: (Long) -> Unit,
    setPresetPendingDeleteId: (Long?) -> Unit,
) {
    if (presetPendingDeleteId != null) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = { setPresetPendingDeleteId(null) },
            title = { Text(stringResource(R.string.preset_delete_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.preset_delete_confirm_message,
                        presetPendingDeleteName,
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deletePreset(presetPendingDeleteId!!)
                        setPresetPendingDeleteId(null)
                    },
                ) {
                    Text(stringResource(R.string.delete_sound_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { setPresetPendingDeleteId.invoke(null) }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }
}

@Composable
fun TimerDialog(
    context: Context,
    showTimerDialog: Boolean,
    sleepTimerRemainingMillis: Long?,
    startSleepTimer: (Long) -> Unit,
    setTimerDialogFalse: () -> Unit,
) {
    var selectedTimerOption by rememberSaveable { mutableIntStateOf(5) }
    var customTimerInput by rememberSaveable { mutableStateOf("") }

    if (showTimerDialog) {
        AlertDialog(
            onDismissRequest = { setTimerDialogFalse() },
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
                        startSleepTimer(selectedMinutes * 60_000L)
                        Toast
                            .makeText(
                                context,
                                context.getString(R.string.timer_set, selectedMinutes),
                                Toast.LENGTH_SHORT,
                            ).show()
                        setTimerDialogFalse.invoke()
                    },
                ) {
                    Text(stringResource(R.string.timer_start))
                }
            },
            dismissButton = {
                TextButton(onClick = { setTimerDialogFalse() }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            },
        )
    }
}

@Composable
fun SleepTimerActiveCard(
    sleepTimerRemainingMillis: Long,
    setShowCancelTimerDialogTrue: () -> Unit,
) {
    CustomCard {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text =
                    stringResource(
                        R.string.timer_countdown_label,
                        formatTimer(sleepTimerRemainingMillis),
                    ),
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = { setShowCancelTimerDialogTrue.invoke() }) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.timer_cancel),
                )
            }
        }
    }
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
