package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.ScreenDestination
import com.example.ui.components.PinLockDialog
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.CalendarJourneyScreen
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.TrashScreen
import com.example.ui.theme.JotterTheme
import com.example.viewmodel.JotterViewModel
import com.example.viewmodel.NoteViewModel
import com.example.viewmodel.NoteViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = application
            val jotterViewModel: NoteViewModel = viewModel(
                factory = NoteViewModelFactory(app)
            )

            val isDarkModePref by jotterViewModel.isDarkMode.collectAsState()
            val currentScreen by jotterViewModel.currentScreen.collectAsState()
            val noteToUnlock by jotterViewModel.pinDialogNoteToUnlock.collectAsState()
            val isSettingUpPin by jotterViewModel.isSettingUpPin.collectAsState()
            val isAppLocked by jotterViewModel.isAppLocked.collectAsState()

            var showSettingsDialog by remember { mutableStateOf(false) }

            val useDarkTheme = isDarkModePref ?: isSystemInDarkTheme()

            JotterTheme(darkTheme = useDarkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    if (isAppLocked) {
                        // App Startup PIN Lock Screen
                        PinLockScreen(
                            isSetupMode = false,
                            onPinSuccess = {},
                            onVerifyPin = { pin -> jotterViewModel.unlockAppWithPin(pin) },
                            onSaveNewPin = { pin -> jotterViewModel.setupMasterPin(pin) }
                        )
                    } else {
                        // Screen Navigation with fade transitions
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                ScreenDestination.HOME -> {
                                    HomeScreen(
                                        viewModel = jotterViewModel,
                                        onOpenSettings = { showSettingsDialog = true }
                                    )
                                }
                                ScreenDestination.EDITOR -> {
                                    EditorScreen(viewModel = jotterViewModel)
                                }
                                ScreenDestination.CALENDAR_JOURNEY -> {
                                    CalendarJourneyScreen(viewModel = jotterViewModel)
                                }
                                ScreenDestination.ARCHIVE -> {
                                    ArchiveScreen(viewModel = jotterViewModel)
                                }
                                ScreenDestination.TRASH -> {
                                    TrashScreen(viewModel = jotterViewModel)
                                }
                                ScreenDestination.SETTINGS -> {
                                    HomeScreen(
                                        viewModel = jotterViewModel,
                                        onOpenSettings = { showSettingsDialog = true }
                                    )
                                }
                            }
                        }

                        // PIN unlock prompt for locked individual notes
                        noteToUnlock?.let { note ->
                            PinLockDialog(
                                isSetupMode = false,
                                onPinEntered = { enteredPin ->
                                    jotterViewModel.onPinEnteredForNote(enteredPin)
                                },
                                onDismiss = { jotterViewModel.dismissPinDialog() }
                            )
                        }

                        // Master PIN setup dialog
                        if (isSettingUpPin) {
                            PinLockDialog(
                                isSetupMode = true,
                                onPinEntered = { newPin ->
                                    jotterViewModel.onPinSetupCompleted(newPin)
                                },
                                onDismiss = { jotterViewModel.dismissPinDialog() }
                            )
                        }

                        // Settings Dialog
                        if (showSettingsDialog) {
                            SettingsDialog(
                                viewModel = jotterViewModel,
                                onDismiss = { showSettingsDialog = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
