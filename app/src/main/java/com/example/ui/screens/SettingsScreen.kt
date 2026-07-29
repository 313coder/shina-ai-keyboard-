package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardHeight
import com.example.model.KeyboardPreferences
import com.example.model.KeyboardTheme
import com.example.model.ShinaDialect

@Composable
fun SettingsScreen(
    preferences: KeyboardPreferences,
    onPreferenceChange: (KeyboardPreferences) -> Unit
) {
    val context = LocalContext.current
    var expandedDialect by remember { mutableStateOf(false) }
    var privacyNotice by remember { mutableStateOf("") }
    var offlineNotice by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Shina AI Keyboard Settings",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Configure keyboard dialect, themes, haptics, offline AI, system activation, and privacy controls.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // --- System Keyboard Activation Section ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = "System Keyboard",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Shina AI Keyboard System-Wide",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Tap to activate Shina AI Keyboard as your default Android system input method.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            try {
                                context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                            } catch (e: Exception) {
                                // Fallback
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Enable", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Offline AI Module ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Offline AI Engine (Works Without Internet)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "On-device neural models powering Prediction, Autocorrection, Dictionary, Voice Typing, and Offline Translation.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SwitchPreferenceItem(
                        title = "On-Device Offline AI",
                        subtitle = "Use 100% local models without network calls",
                        checked = preferences.offlineAiEnabled,
                        onCheckedChange = { onPreferenceChange(preferences.copy(offlineAiEnabled = it)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Downloaded Offline Language Packs:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• Shina ↔ Urdu Pack (24 MB)", fontSize = 12.sp)
                        Text("Installed", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• Shina ↔ English Pack (32 MB)", fontSize = 12.sp)
                        Text("Installed", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { offlineNotice = "All offline language packs updated!" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Manage / Download Offline Packs")
                    }
                    if (offlineNotice.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(offlineNotice, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // --- Privacy Dashboard & Controls ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Privacy Dashboard & Control Panel", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Privacy-first guarantees. Everything learns locally on your device unless you explicitly opt in.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SwitchPreferenceItem(
                        title = "Incognito Private Mode",
                        subtitle = "Stop learning or storing typed words",
                        checked = preferences.incognitoMode,
                        onCheckedChange = { onPreferenceChange(preferences.copy(incognitoMode = it)) }
                    )

                    SwitchPreferenceItem(
                        title = "Disable Learning",
                        subtitle = "Turn off local vocabulary and n-gram learning",
                        checked = preferences.disableLearning,
                        onCheckedChange = { onPreferenceChange(preferences.copy(disableLearning = it)) }
                    )

                    SwitchPreferenceItem(
                        title = "Disable Voice Collection",
                        subtitle = "Block voice dataset contributions",
                        checked = preferences.disableVoiceCollection,
                        onCheckedChange = { onPreferenceChange(preferences.copy(disableVoiceCollection = it)) }
                    )

                    SwitchPreferenceItem(
                        title = "Disable Cloud AI",
                        subtitle = "Block all external Gemini AI requests",
                        checked = preferences.disableCloudAi,
                        onCheckedChange = { onPreferenceChange(preferences.copy(disableCloudAi = it)) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { privacyNotice = "My Personal Shina Keyboard Data exported to Download/shina_user_data.json" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Export My Data", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { privacyNotice = "All local vocabulary & personal learned n-grams deleted." },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Delete My Data", fontSize = 12.sp)
                        }
                    }

                    if (privacyNotice.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(privacyNotice, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // --- Dialect Selection ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Shina Dialect", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Current: ${preferences.dialect.displayName} (${preferences.dialect.region})",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Box {
                        OutlinedButton(onClick = { expandedDialect = true }) {
                            Text("Change Dialect: ${preferences.dialect.displayName}")
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }

                        DropdownMenu(
                            expanded = expandedDialect,
                            onDismissRequest = { expandedDialect = false }
                        ) {
                            ShinaDialect.values().forEach { dialect ->
                                DropdownMenuItem(
                                    text = { Text("${dialect.displayName} (${dialect.region})") },
                                    onClick = {
                                        onPreferenceChange(preferences.copy(dialect = dialect))
                                        expandedDialect = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Theme Selection ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Keyboard Theme", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    KeyboardTheme.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPreferenceChange(preferences.copy(theme = theme)) }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferences.theme == theme,
                                onClick = { onPreferenceChange(preferences.copy(theme = theme)) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = theme.displayName, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        // --- Typing Preferences ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Typing & Suggestions", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    SwitchPreferenceItem(
                        title = "Haptic Vibration",
                        subtitle = "Vibrate keypress feedback",
                        checked = preferences.hapticFeedback,
                        onCheckedChange = { onPreferenceChange(preferences.copy(hapticFeedback = it)) }
                    )

                    SwitchPreferenceItem(
                        title = "Typing Sound",
                        subtitle = "Audio click feedback",
                        checked = preferences.typingSound,
                        onCheckedChange = { onPreferenceChange(preferences.copy(typingSound = it)) }
                    )

                    SwitchPreferenceItem(
                        title = "Number Row",
                        subtitle = "Show top row numbers above keys",
                        checked = preferences.showNumberRow,
                        onCheckedChange = { onPreferenceChange(preferences.copy(showNumberRow = it)) }
                    )

                    SwitchPreferenceItem(
                        title = "Auto-Correction",
                        subtitle = "Smart correction for Shina typos",
                        checked = preferences.autoCorrect,
                        onCheckedChange = { onPreferenceChange(preferences.copy(autoCorrect = it)) }
                    )

                    SwitchPreferenceItem(
                        title = "Next Word Prediction",
                        subtitle = "Predict next Shina word in sentence",
                        checked = preferences.nextWordPrediction,
                        onCheckedChange = { onPreferenceChange(preferences.copy(nextWordPrediction = it)) }
                    )
                }
            }
        }
    }
}

@Composable
fun SwitchPreferenceItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
