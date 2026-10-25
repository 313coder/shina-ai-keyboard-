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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardPreferences
import com.example.model.KeyboardTheme
import com.example.model.ShinaDialect

@Composable
fun SettingsScreen(
    preferences: KeyboardPreferences,
    onPreferenceChange: (KeyboardPreferences) -> Unit,
    onOpenTrainingStudio: () -> Unit = {},
    onOpenContributions: () -> Unit = {},
    onOpenDictionary: () -> Unit = {},
    onOpenStorageManager: () -> Unit = {},
    onOpenLeaderboard: () -> Unit = {}
) {
    val context = LocalContext.current
    var expandedDialect by remember { mutableStateOf(false) }
    var privacyNotice by remember { mutableStateOf("") }
    var offlineNotice by remember { mutableStateOf("") }
    var showShortcutsDialog by remember { mutableStateOf(false) }

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
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Customize dialect, desktop layout, hardware keyboard, on-device AI self-training, voice, and privacy controls.",
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
                            text = "Tap to activate Shina AI Keyboard as your default Android system input method in WhatsApp, Messages, and Chrome.",
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
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("enable_ime_settings_btn")
                    ) {
                        Text("Enable", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Desktop & Large Screen Experience (Chromebook, DeX, Tablet, PC) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LaptopMac,
                            contentDescription = "Desktop Support",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Desktop & Large Screen Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Optimized for Chromebooks, Samsung DeX, Tablets & Desktop Monitors",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SwitchPreferenceItem(
                        title = "Adaptive Desktop Layout",
                        subtitle = "Automatically switch to side navigation rail on wide screens (>= 700dp) with zero mobile impact",
                        checked = preferences.desktopModeEnabled,
                        onCheckedChange = { onPreferenceChange(preferences.copy(desktopModeEnabled = it)) }
                    )

                    SwitchPreferenceItem(
                        title = "Hardware Keyboard Shina Transliteration",
                        subtitle = "Type Shina phonetically using standard physical laptop/desktop QWERTY keys",
                        checked = preferences.desktopPhysicalKeyboardTransliteration,
                        onCheckedChange = { onPreferenceChange(preferences.copy(desktopPhysicalKeyboardTransliteration = it)) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showShortcutsDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("view_desktop_shortcuts_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.KeyboardAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Desktop Keyboard Shortcuts Cheatsheet", fontSize = 13.sp)
                    }
                }
            }
        }

        // --- On-Device AI & Self-Training Preferences ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "AI Training",
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "On-Device AI Self-Training & Contributions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Learn Shina grammar, preserve local vocabulary, track your impact",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SwitchPreferenceItem(
                        title = "Auto-Learn from AI Interactions",
                        subtitle = "Extract new Shina vocabulary automatically when accepting AI suggestions",
                        checked = preferences.autoTrainFromAiChats,
                        onCheckedChange = { onPreferenceChange(preferences.copy(autoTrainFromAiChats = it)) }
                    )

                    SwitchPreferenceItem(
                        title = "Contribution Milestone Alerts",
                        subtitle = "Receive celebratory badges for 50+, 100+, 500+ verified words and sentences",
                        checked = preferences.contributionNotifications,
                        onCheckedChange = { onPreferenceChange(preferences.copy(contributionNotifications = it)) }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = {
                                com.example.util.ShinaHapticManager.performKeyClick(context, preferences.hapticFeedback)
                                onOpenTrainingStudio()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_open_training_studio_btn"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.ModelTraining, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Studio", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        FilledTonalButton(
                            onClick = {
                                com.example.util.ShinaHapticManager.performKeyClick(context, preferences.hapticFeedback)
                                onOpenContributions()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_open_contributions_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Insights, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Impact", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                com.example.util.ShinaHapticManager.performKeyClick(context, preferences.hapticFeedback)
                                onOpenLeaderboard()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("settings_open_leaderboard_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Leaderboard", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Voice & Speech Configuration ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(10.dp))
                        Text("Voice Typing & Speech Synthesis", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    SwitchPreferenceItem(
                        title = "Auto-Speak Shina AI Responses",
                        subtitle = "Automatically synthesize and read out Shina translations and answers",
                        checked = preferences.voiceAutoSpeakAiReplies,
                        onCheckedChange = { onPreferenceChange(preferences.copy(voiceAutoSpeakAiReplies = it)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Speech Playback Speed: ${String.format("%.2f", preferences.speechSpeed)}x", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(0.75f to "Slow (0.75x)", 1.0f to "Normal (1.0x)", 1.25f to "Fast (1.25x)").forEach { (speed, label) ->
                            FilterChip(
                                selected = preferences.speechSpeed == speed,
                                onClick = { onPreferenceChange(preferences.copy(speechSpeed = speed)) },
                                label = { Text(label, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // --- Offline AI Module ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WifiOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                        Spacer(Modifier.width(8.dp))
                        Text("Offline AI Engine (Works Without Internet)", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "On-device neural models powering Prediction, Autocorrection, Dictionary, Voice Typing, and Offline Translation.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SwitchPreferenceItem(
                        title = "On-Device Offline AI",
                        subtitle = "Use 100% local models without network calls",
                        checked = preferences.offlineAiEnabled,
                        onCheckedChange = { onPreferenceChange(preferences.copy(offlineAiEnabled = it)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Downloaded Offline Language Packs:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• Shina ↔ Urdu Neural Pack (24 MB)", fontSize = 12.sp)
                        Text("Installed", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("• Shina ↔ English Dictionary Pack (32 MB)", fontSize = 12.sp)
                        Text("Installed", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { offlineNotice = "All offline language packs are up-to-date!" },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Manage / Check Offline Packs")
                    }
                    if (offlineNotice.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(offlineNotice, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // --- Offline Searchable Dictionary Card ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Dictionary",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Offline Searchable Dictionary & Synonyms",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Real-time definitions, Roman transliterations, and clickable synonyms",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Browse authentic Shina vocabulary, search across Shina/Roman/Urdu/English, discover synonyms (مترادفات), and contribute your own community words.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenDictionary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_open_dictionary_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open Shina Offline Dictionary", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Privacy Dashboard & Controls ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
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
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { privacyNotice = "My Personal Shina Keyboard Data exported to Download/shina_user_data.json" },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Export Data", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { privacyNotice = "All local vocabulary & personal learned n-grams deleted." },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Delete Data", fontSize = 12.sp)
                        }
                    }

                    if (privacyNotice.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(privacyNotice, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // --- Storage Management & Permanent Data System ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Storage & Permanent Data Center",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "ACID SQLite persistence, JSON snapshot backups, compaction",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "All Shina vocabulary, user contributions, verified sentences, and dictionary entries are stored permanently with WAL logging and full backup/restore capability.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Automatic Cloud Backup Toggle (Firebase Firestore Sync)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "Automatic Cloud Backup",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            Text(
                                text = "Syncs local Room database with Firebase Firestore so your contributed Shina vocabulary and grammar are never lost across device changes.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                            )
                        }
                        Switch(
                            checked = preferences.automaticCloudBackup,
                            onCheckedChange = { isChecked ->
                                onPreferenceChange(preferences.copy(automaticCloudBackup = isChecked))
                            },
                            modifier = Modifier.testTag("toggle_auto_cloud_backup")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenStorageManager,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_open_storage_manager_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.SettingsBackupRestore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Manage Storage & Backups", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // --- Dialect Selection ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
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
                        OutlinedButton(
                            onClick = { expandedDialect = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
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

        // --- Appearance & System-wide Dark Mode ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("card_settings_appearance"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DarkMode,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Appearance & Display", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "High-contrast dark color scheme for low-light conditions",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SwitchPreferenceItem(
                        title = "System-wide Dark Mode",
                        subtitle = "Forces a rich, high-contrast dark color scheme across all screens, dictionary, and AI chat interface for comfortable low-light typing",
                        checked = preferences.systemWideDarkMode,
                        onCheckedChange = { isDark ->
                            com.example.util.ShinaHapticManager.performKeyClick(context, preferences.hapticFeedback)
                            onPreferenceChange(preferences.copy(systemWideDarkMode = isDark))
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Theme Palette Accent", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    KeyboardTheme.values().forEach { theme ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    com.example.util.ShinaHapticManager.performKeyClick(context, preferences.hapticFeedback)
                                    onPreferenceChange(preferences.copy(theme = theme))
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = preferences.theme == theme,
                                onClick = {
                                    com.example.util.ShinaHapticManager.performKeyClick(context, preferences.hapticFeedback)
                                    onPreferenceChange(preferences.copy(theme = theme))
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = theme.displayName, fontSize = 14.sp)
                        }
                    }
                }
            }
        }

        // --- Typing Preferences ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Typing & Suggestions", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                    Spacer(modifier = Modifier.height(8.dp))

                    SwitchPreferenceItem(
                        title = "Haptic Vibration",
                        subtitle = "Tactile feedback for keypresses, submissions, badge awards & dictionary searches",
                        checked = preferences.hapticFeedback,
                        onCheckedChange = { isHaptic ->
                            com.example.util.ShinaHapticManager.performKeyClick(context, isHaptic)
                            onPreferenceChange(preferences.copy(hapticFeedback = isHaptic))
                        }
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

    // --- Desktop Keyboard Shortcuts Cheatsheet Dialog ---
    if (showShortcutsDialog) {
        AlertDialog(
            onDismissRequest = { showShortcutsDialog = false },
            confirmButton = {
                Button(
                    onClick = { showShortcutsDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Got It")
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LaptopMac, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("Desktop Keyboard Shortcuts", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "When running Shina AI on Desktop (Chromebook, Samsung DeX, or Android PC), use these hardware keyboard shortcuts:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    ShortcutRow("Ctrl + 1", "AI Writing & Translation Suite")
                    ShortcutRow("Ctrl + 2", "Keyboard Playground & Typing")
                    ShortcutRow("Ctrl + 3", "Community Learning & Corpus")
                    ShortcutRow("Ctrl + 4", "Settings & Configuration")
                    ShortcutRow("Ctrl + 5", "About Developer & Mission")
                    ShortcutRow("Ctrl + 6", "On-Device AI Self-Training Studio")
                    ShortcutRow("Ctrl + 7", "User Contributions & Impact Stats")
                    ShortcutRow("Ctrl + K", "Admin Panel & Dataset Inspector")
                    ShortcutRow("Escape", "Back / Return to AI Suite")
                    ShortcutRow("Space / Enter", "Select & Confirm Suggestions")
                }
            }
        )
    }
}

@Composable
private fun ShortcutRow(shortcut: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = shortcut,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Text(
            text = description,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 12.dp)
        )
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
