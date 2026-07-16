package com.pronaycoding.blankee.core.ui.components

/**
 * Custom sound card component for user-uploaded sounds.
 *
 * This file contains composable functions for displaying and controlling custom sounds
 * uploaded by the user. Features:
 * - Display custom sound with music icon
 * - Volume control slider for individual custom sounds
 * - Delete button with confirmation dialog
 * - Play/pause state tracking
 * - Visual feedback for playback status
 *
 * Custom sounds are distinct from built-in sounds and can be removed by the user.
 *
 * @see com.pronaycoding.blankee.core.model.CardItems.CustomCardItem for custom sound model
 * @see androidx.compose.material3.Slider for volume control
 */

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pronaycoding.blankee.R
import com.pronaycoding.blankee.feature.home.HomeViewmodel
import org.koin.androidx.compose.koinViewModel

@Composable
fun CustomSoundCard(
    modifier: Modifier,
    cardNumber: Int,
    soundId: Int,
    displayName: String,
    playOrPause: Boolean,
    onDeleteClick: (Int) -> Unit,
    viewModel: HomeViewmodel = koinViewModel(),
) {
    val context = LocalContext.current
    val customVolumes by viewModel.customVolumes.collectAsStateWithLifecycle()
    val volume = customVolumes[soundId] ?: 0f
    var localVolume by remember(soundId) { mutableFloatStateOf(volume) }
    var isDragging by remember(soundId) { mutableStateOf(false) }
    LaunchedEffect(volume, soundId) {
        if (!isDragging) localVolume = volume
    }
    val displayVolume = localVolume
    val shouldPlaySound = displayVolume > 0f
    val shouldUseActiveColor = shouldPlaySound && playOrPause
    var showDeleteConfirmDialog by rememberSaveable(soundId) { mutableStateOf(false) }
    val interactionSource = remember(soundId) { MutableInteractionSource() }

    Column(
        modifier = modifier.padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Icon(
                modifier =
                    Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                            shape = CircleShape,
                        ).clickable { showDeleteConfirmDialog = true },
                imageVector = Icons.Filled.Remove,
                contentDescription = stringResource(R.string.delete_sound),
                tint = MaterialTheme.colorScheme.error,
            )

            Box(
                contentAlignment = Alignment.TopEnd,
                modifier =
                    Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                                viewModel.onCustomCardClick(soundId)
                            },
                        ).background(
                            if (shouldUseActiveColor) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            } else {
                                Color.Gray.copy(alpha = 0.1f)
                            },
                        ),
            ) {
                Icon(
                    imageVector = Icons.Filled.LibraryMusic,
                    contentDescription = stringResource(R.string.content_desc_sound_icon),
                    modifier =
                        Modifier
                            .size(40.dp)
                            .align(Alignment.Center),
                    tint =
                        if (shouldUseActiveColor) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Gray.copy(
                                .5f,
                            )
                        },
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = displayName,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(12.dp))

        Slider(
            colors =
                SliderDefaults.colors(
                    thumbColor =
                        when (playOrPause) {
                            true -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            else -> Color.Gray.copy(.5f)
                        },
                    activeTrackColor =
                        if (playOrPause) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            Color.Gray.copy(
                                .5f,
                            )
                        },
                    inactiveTrackColor = Color.Gray.copy(.5f),
                    disabledThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    disabledActiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                    disabledInactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                ),
            value = displayVolume,
            onValueChange = { newValue ->
                if (playOrPause) {
                    isDragging = true
                    localVolume = newValue
                    viewModel.previewCustomSoundVolume(soundId, newValue)
                }
            },
            onValueChangeFinished = {
                if (playOrPause) {
                    viewModel.setCustomSoundVolume(soundId, localVolume)
                }
                isDragging = false
                if (!playOrPause) {
                    Toast
                        .makeText(
                            context,
                            context.getString(R.string.cant_play_sound_on_pause),
                            Toast.LENGTH_SHORT,
                        ).show()
                }
            },
            valueRange = 0f..1f,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(6.dp),
        )
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text(text = stringResource(R.string.delete_sound_dialog_title)) },
            text = {
                Text(
                    text =
                        stringResource(
                            R.string.delete_sound_dialog_message,
                            displayName,
                        ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick(soundId)
                        showDeleteConfirmDialog = false
                    },
                ) {
                    Text(text = stringResource(R.string.delete_sound_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(text = stringResource(R.string.dialog_cancel))
                }
            },
        )
    }
}
