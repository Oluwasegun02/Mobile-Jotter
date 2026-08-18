package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PinLockScreen(
    isSetupMode: Boolean = false,
    onPinSuccess: () -> Unit,
    onVerifyPin: (String) -> Boolean,
    onSaveNewPin: (String) -> Boolean,
    onSkipSetup: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isConfirmingStep by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    fun triggerShake() {
        scope.launch {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 350
                    -22f at 50
                    22f at 100
                    -16f at 150
                    16f at 200
                    -8f at 250
                    8f at 300
                    0f at 350
                }
            )
        }
    }

    fun handleDigitPress(digit: String) {
        if (isSetupMode) {
            if (!isConfirmingStep) {
                if (pin.length < 4) {
                    pin += digit
                    if (pin.length == 4) {
                        isConfirmingStep = true
                        errorMessage = null
                    }
                }
            } else {
                if (confirmPin.length < 4) {
                    confirmPin += digit
                    if (confirmPin.length == 4) {
                        if (pin == confirmPin) {
                            val success = onSaveNewPin(pin)
                            if (success) {
                                onPinSuccess()
                            } else {
                                errorMessage = "Failed to save PIN"
                                triggerShake()
                            }
                        } else {
                            errorMessage = "PINs do not match. Try again."
                            confirmPin = ""
                            triggerShake()
                        }
                    }
                }
            }
        } else {
            if (pin.length < 4) {
                pin += digit
                if (pin.length == 4) {
                    val verified = onVerifyPin(pin)
                    if (verified) {
                        onPinSuccess()
                    } else {
                        errorMessage = "Incorrect PIN. Please try again."
                        pin = ""
                        triggerShake()
                    }
                }
            }
        }
    }

    fun handleBackspace() {
        if (isSetupMode && isConfirmingStep) {
            if (confirmPin.isNotEmpty()) {
                confirmPin = confirmPin.dropLast(1)
            } else {
                isConfirmingStep = false
                pin = ""
            }
        } else {
            if (pin.isNotEmpty()) {
                pin = pin.dropLast(1)
            }
        }
        errorMessage = null
    }

    val currentInputPin = if (isSetupMode && isConfirmingStep) confirmPin else pin

    Surface(
        modifier = modifier.fillMaxSize().testTag("pin_lock_screen"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Icon and Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSetupMode) Icons.Default.Security else Icons.Default.Lock,
                        contentDescription = "Lock",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = when {
                        isSetupMode && !isConfirmingStep -> "Set 4-Digit PIN"
                        isSetupMode && isConfirmingStep -> "Confirm Your PIN"
                        else -> "Unlock Jotter"
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when {
                        isSetupMode && !isConfirmingStep -> "Choose a secure PIN to protect your notes"
                        isSetupMode && isConfirmingStep -> "Re-enter your PIN to confirm"
                        else -> "Enter your 4-digit master PIN to access your notes"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // PIN indicator dots with Shake animation
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                        .padding(vertical = 12.dp)
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < currentInputPin.length
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) MaterialTheme.colorScheme.primary
                                    else Color.Transparent
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                        )
                    }
                }

                // Error message
                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }

            // Numeric Keypad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val buttonRows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "DEL")
                )

                for (row in buttonRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (key in row) {
                            when (key) {
                                "" -> {
                                    Spacer(modifier = Modifier.size(72.dp))
                                }
                                "DEL" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .clickable { handleBackspace() }
                                            .testTag("pin_delete_button"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDark) Color(0xFF1E2330)
                                                else Color(0xFFF1F3F9)
                                            )
                                            .clickable { handleDigitPress(key) }
                                            .testTag("pin_key_$key"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (onSkipSetup != null && isSetupMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onSkipSetup,
                        modifier = Modifier.testTag("pin_skip_setup_button")
                    ) {
                        Text("Skip for Now", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
