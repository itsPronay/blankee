package com.pronaycoding.blankee

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.pronaycoding.blankee.core.common.Constants
import com.pronaycoding.blankee.core.datastore.PreferenceManagerRepositoryImpl
import com.pronaycoding.blankee.core.ui.components.EnjoyBlankeePrompt
import com.pronaycoding.blankee.core.ui.theme.BlankeeAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    private val shouldShowEnjoyPromptFlow = MutableStateFlow(false)
    private val showNotificationPrimerFlow = MutableStateFlow(false)

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(PreferenceManagerRepositoryImpl.wrapContextWithStoredLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferenceManager =
            PreferenceManagerRepositoryImpl(
                this,
            )
        lifecycleScope.launch {
            val launchCount = preferenceManager.incrementLaunchCount()
            val shouldShowEnjoyPrompt =
                launchCount == Constants.ENJOY_PROMPT_TRIGGER_LAUNCH &&
                    !preferenceManager.isEnjoyPromptShown()
            shouldShowEnjoyPromptFlow.value = shouldShowEnjoyPrompt
        }

        lifecycleScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !preferenceManager.isNotificationPrimerShown()
            ) {
                showNotificationPrimerFlow.value = true
            }
        }

        enableEdgeToEdge(
            statusBarStyle =
                SystemBarStyle.dark(
                    scrim = android.graphics.Color.TRANSPARENT,
                ),
        )

        setContent {
            BlankeeAppTheme {
                Surface {
                    val shouldShowEnjoyPrompt =
                        shouldShowEnjoyPromptFlow.collectAsStateWithLifecycle().value
                    val showNotificationPrimer =
                        showNotificationPrimerFlow.collectAsStateWithLifecycle().value
                    Navigation()
                    EnjoyBlankeePrompt(shouldShow = shouldShowEnjoyPrompt)

                    if (showNotificationPrimer) {
                        AlertDialog(
                            onDismissRequest = { },
                            title = { Text(getString(R.string.notification_primer_title)) },
                            text = { Text(getString(R.string.notification_primer_message)) },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showNotificationPrimerFlow.value = false
                                        lifecycleScope.launch {
                                            preferenceManager.setNotificationPrimerShown(true)
                                        }
                                        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    },
                                ) {
                                    Text(getString(R.string.notification_primer_grant))
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        showNotificationPrimerFlow.value = false
                                        lifecycleScope.launch {
                                            preferenceManager.setNotificationPrimerShown(true)
                                        }
                                    },
                                ) {
                                    Text(getString(R.string.notification_primer_continue_anyway), color = MaterialTheme.colorScheme.error)
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
