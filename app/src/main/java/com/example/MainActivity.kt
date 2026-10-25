package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ShinaAiEngine
import com.example.ai.ShinaAiLearningEngine
import com.example.ai.ShinaAiTrainingEngine
import com.example.data.ShinaDatabase
import com.example.data.ShinaKeyboardRepository
import com.example.model.KeyboardPreferences
import com.example.ui.screens.*
import com.example.ui.theme.ShinaTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: ShinaDatabase
    private lateinit var repository: ShinaKeyboardRepository
    private lateinit var aiEngine: ShinaAiEngine
    private lateinit var aiLearningEngine: ShinaAiLearningEngine
    private lateinit var trainingEngine: ShinaAiTrainingEngine

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = ShinaDatabase.getDatabase(applicationContext)
        repository = ShinaKeyboardRepository(database)
        aiEngine = ShinaAiEngine(database)
        aiLearningEngine = ShinaAiLearningEngine(repository, aiEngine)
        trainingEngine = ShinaAiTrainingEngine(repository, aiEngine)

        setContent {
            var preferences by remember { mutableStateOf(KeyboardPreferences()) }
            var currentScreen by remember { mutableIntStateOf(0) }
            var showSplash by remember { mutableStateOf(true) }
            var showDesktopShortcutsDialog by remember { mutableStateOf(false) }

            val focusRequester = remember { FocusRequester() }

            LaunchedEffect(showSplash) {
                if (!showSplash) {
                    try {
                        focusRequester.requestFocus()
                    } catch (e: Exception) {
                        // ignore focus errors
                    }
                }
            }

            LaunchedEffect(preferences.automaticCloudBackup) {
                if (preferences.automaticCloudBackup) {
                    val backupManager = repository.getBackupManager(applicationContext)
                    if (backupManager.auth.currentUser != null) {
                        try {
                            backupManager.syncLocalDataToFirestore()
                        } catch (e: Exception) {
                            // Non-fatal background sync
                        }
                    }
                }
            }

            ShinaTheme(
                theme = preferences.theme,
                darkTheme = preferences.systemWideDarkMode || androidx.compose.foundation.isSystemInDarkTheme()
            ) {
                if (showSplash) {
                    WelcomeSplashScreen(
                        onSplashComplete = { showSplash = false }
                    )
                } else {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(focusRequester)
                            .focusable()
                            .onKeyEvent { keyEvent ->
                                if (keyEvent.type == KeyEventType.KeyDown) {
                                    if (keyEvent.isCtrlPressed) {
                                        when (keyEvent.key) {
                                            Key.One -> { currentScreen = 0; true }
                                            Key.Two -> { currentScreen = 1; true }
                                            Key.Three -> { currentScreen = 2; true }
                                            Key.Four -> { currentScreen = 3; true }
                                            Key.Five -> { currentScreen = 4; true }
                                            Key.Six -> { currentScreen = 6; true }
                                            Key.Seven -> { currentScreen = 7; true }
                                            Key.Eight -> { currentScreen = 8; true }
                                            Key.D -> { currentScreen = 8; true }
                                            Key.S -> { currentScreen = 9; true }
                                            Key.K -> { currentScreen = 5; true }
                                            Key.L -> { currentScreen = 10; true }
                                            else -> false
                                        }
                                    } else if (keyEvent.key == Key.Escape && currentScreen != 0) {
                                        currentScreen = 0
                                        true
                                    } else false
                                } else false
                            }
                    ) {
                        val isDesktop = maxWidth >= 700.dp && preferences.desktopModeEnabled

                        BackHandler(enabled = currentScreen != 0) {
                            currentScreen = 0
                        }

                        if (isDesktop) {
                            // ==========================================
                            // DESKTOP & LARGE SCREEN LAYOUT (Chromebook / DeX / Tablet / PC)
                            // ==========================================
                            Row(modifier = Modifier.fillMaxSize()) {
                                NavigationRail(
                                    modifier = Modifier.fillMaxHeight(),
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    header = {
                                        IconButton(
                                            onClick = { showSplash = true },
                                            modifier = Modifier.padding(top = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.AutoAwesome,
                                                contentDescription = "Welcome Splash",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                ) {
                                    Spacer(modifier = Modifier.height(8.dp))

                                    NavigationRailItem(
                                        selected = currentScreen == 0,
                                        onClick = { currentScreen = 0 },
                                        icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Suite") },
                                        label = { Text("AI Suite") }
                                    )
                                    NavigationRailItem(
                                        selected = currentScreen == 1,
                                        onClick = { currentScreen = 1 },
                                        icon = { Icon(Icons.Default.Keyboard, contentDescription = "Keyboard") },
                                        label = { Text("Keyboard") }
                                    )
                                    NavigationRailItem(
                                        selected = currentScreen == 2,
                                        onClick = { currentScreen = 2 },
                                        icon = { Icon(Icons.Default.GroupWork, contentDescription = "Community") },
                                        label = { Text("Community") }
                                    )
                                    NavigationRailItem(
                                        selected = currentScreen == 3,
                                        onClick = { currentScreen = 3 },
                                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                        label = { Text("Settings") }
                                    )
                                    NavigationRailItem(
                                        selected = currentScreen == 4,
                                        onClick = { currentScreen = 4 },
                                        icon = { Icon(Icons.Default.Info, contentDescription = "About") },
                                        label = { Text("About") }
                                    )
                                    NavigationRailItem(
                                        selected = currentScreen == 8,
                                        onClick = { currentScreen = 8 },
                                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Dictionary") },
                                        label = { Text("Dictionary") }
                                    )

                                    Spacer(modifier = Modifier.weight(1f))

                                    // Quick studio & impact rail actions
                                    NavigationRailItem(
                                        selected = currentScreen == 6,
                                        onClick = { currentScreen = 6 },
                                        icon = { Icon(Icons.Default.Psychology, contentDescription = "Training") },
                                        label = { Text("Studio") }
                                    )
                                    NavigationRailItem(
                                        selected = currentScreen == 7,
                                        onClick = { currentScreen = 7 },
                                        icon = { Icon(Icons.Default.Insights, contentDescription = "Impact") },
                                        label = { Text("Impact") }
                                    )
                                    NavigationRailItem(
                                        selected = currentScreen == 10,
                                        onClick = { currentScreen = 10 },
                                        icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "Leaderboard") },
                                        label = { Text("Ranks") }
                                    )
                                    NavigationRailItem(
                                        selected = currentScreen == 5,
                                        onClick = { currentScreen = 5 },
                                        icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Admin") },
                                        label = { Text("Admin") }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    // Desktop Header Bar
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        color = MaterialTheme.colorScheme.surface,
                                        shadowElevation = 1.dp
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 24.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "'SAIPBG' Shina AI",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 20.sp,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.primaryContainer,
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text(
                                                        text = "Power by Grazina",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Surface(
                                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.LaptopMac,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(
                                                            text = "Desktop Mode Active",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                    }
                                                }
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { showDesktopShortcutsDialog = true },
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier.testTag("desktop_header_shortcuts_btn")
                                                ) {
                                                    Icon(
                                                        Icons.Default.KeyboardAlt,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text("Shortcuts (Ctrl+? / Esc)", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.TopCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .widthIn(max = 1200.dp)
                                        ) {
                                            AppScreenContent(
                                                currentScreen = currentScreen,
                                                onNavigate = { currentScreen = it },
                                                aiEngine = aiEngine,
                                                repository = repository,
                                                aiLearningEngine = aiLearningEngine,
                                                trainingEngine = trainingEngine,
                                                preferences = preferences,
                                                onPreferenceChange = { preferences = it }
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // ==========================================
                            // STANDARD MOBILE PHONE LAYOUT (Zero impact/change on phones)
                            // ==========================================
                            Scaffold(
                                topBar = {
                                    CenterAlignedTopAppBar(
                                        title = {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "'SAIPBG' Shina AI",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 17.sp
                                                )
                                                Text(
                                                    text = "Power by Grazina",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        navigationIcon = {
                                            IconButton(onClick = { showSplash = true }) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = "Replay Welcome Animation",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        },
                                        actions = {
                                            // Shina Offline Searchable Dictionary
                                            IconButton(
                                                onClick = { currentScreen = 8 },
                                                modifier = Modifier.testTag("nav_dictionary_btn")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MenuBook,
                                                    contentDescription = "Shina Offline Searchable Dictionary",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            // Shina AI Contribution & Model Progress
                                            IconButton(
                                                onClick = { currentScreen = 7 },
                                                modifier = Modifier.testTag("nav_user_contributions_btn")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Insights,
                                                    contentDescription = "My AI Contributions & Impact",
                                                    tint = MaterialTheme.colorScheme.tertiary
                                                )
                                            }
                                            // Community Leaderboard
                                            IconButton(
                                                onClick = { currentScreen = 10 },
                                                modifier = Modifier.testTag("nav_community_leaderboard_btn")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.EmojiEvents,
                                                    contentDescription = "Community Leaderboard",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            // Shina AI Self-Training Studio Button
                                            IconButton(onClick = { currentScreen = 6 }) {
                                                Icon(
                                                    imageVector = Icons.Default.Psychology,
                                                    contentDescription = "Shina AI Self-Training Hub",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            // Admin Dashboard
                                            IconButton(onClick = { currentScreen = 5 }) {
                                                Icon(
                                                    imageVector = Icons.Default.AdminPanelSettings,
                                                    contentDescription = "Admin Dashboard"
                                                )
                                            }
                                        }
                                    )
                                },
                                bottomBar = {
                                    NavigationBar {
                                        NavigationBarItem(
                                            selected = currentScreen == 0,
                                            onClick = { currentScreen = 0 },
                                            icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Suite") },
                                            label = { Text("AI Suite") }
                                        )
                                        NavigationBarItem(
                                            selected = currentScreen == 1,
                                            onClick = { currentScreen = 1 },
                                            icon = { Icon(Icons.Default.Keyboard, contentDescription = "Keyboard") },
                                            label = { Text("Keyboard") }
                                        )
                                        NavigationBarItem(
                                            selected = currentScreen == 2,
                                            onClick = { currentScreen = 2 },
                                            icon = { Icon(Icons.Default.GroupWork, contentDescription = "Community") },
                                            label = { Text("Community") }
                                        )
                                        NavigationBarItem(
                                            selected = currentScreen == 3,
                                            onClick = { currentScreen = 3 },
                                            icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                                            label = { Text("Settings") }
                                        )
                                        NavigationBarItem(
                                            selected = currentScreen == 4,
                                            onClick = { currentScreen = 4 },
                                            icon = { Icon(Icons.Default.Info, contentDescription = "About") },
                                            label = { Text("About") }
                                        )
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                ) {
                                    AppScreenContent(
                                        currentScreen = currentScreen,
                                        onNavigate = { currentScreen = it },
                                        aiEngine = aiEngine,
                                        repository = repository,
                                        aiLearningEngine = aiLearningEngine,
                                        trainingEngine = trainingEngine,
                                        preferences = preferences,
                                        onPreferenceChange = { preferences = it }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showDesktopShortcutsDialog) {
                AlertDialog(
                    onDismissRequest = { showDesktopShortcutsDialog = false },
                    confirmButton = {
                        Button(
                            onClick = { showDesktopShortcutsDialog = false },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Close")
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LaptopMac, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text("Desktop Hardware Shortcuts", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Navigate the entire Shina AI app using physical keyboard shortcuts:",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            ShortcutRowItem("Ctrl + 1", "AI Assistant Suite")
                            ShortcutRowItem("Ctrl + 2", "Keyboard Playground")
                            ShortcutRowItem("Ctrl + 3", "Community Learning & Corpus")
                            ShortcutRowItem("Ctrl + 4", "Settings & Configuration")
                            ShortcutRowItem("Ctrl + 5", "About Developer & Vision")
                            ShortcutRowItem("Ctrl + 6", "On-Device AI Self-Training Studio")
                            ShortcutRowItem("Ctrl + 7", "Contributions & Impact Stats")
                            ShortcutRowItem("Ctrl + 8 / Ctrl + D", "Shina Offline Dictionary")
                            ShortcutRowItem("Ctrl + S", "Storage & Permanent Data Center")
                            ShortcutRowItem("Ctrl + L", "Community Leaderboard & Hall of Fame")
                            ShortcutRowItem("Ctrl + K", "Admin Panel")
                            ShortcutRowItem("Escape", "Return to AI Suite (Home)")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ShortcutRowItem(shortcut: String, description: String) {
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
private fun AppScreenContent(
    currentScreen: Int,
    onNavigate: (Int) -> Unit,
    aiEngine: ShinaAiEngine,
    repository: ShinaKeyboardRepository,
    aiLearningEngine: ShinaAiLearningEngine,
    trainingEngine: ShinaAiTrainingEngine,
    preferences: KeyboardPreferences,
    onPreferenceChange: (KeyboardPreferences) -> Unit
) {
    when (currentScreen) {
        0 -> AiAssistantScreen(
            aiEngine = aiEngine,
            repository = repository,
            trainingEngine = trainingEngine,
            onOpenTrainingStudio = { onNavigate(6) },
            onOpenContributions = { onNavigate(7) }
        )
        1 -> KeyboardPlaygroundScreen(
            repository = repository,
            aiEngine = aiEngine,
            preferences = preferences,
            onPreferenceChange = onPreferenceChange,
            onOpenDictionary = { onNavigate(8) }
        )
        2 -> CommunityLearningScreen(
            repository = repository,
            aiEngine = aiEngine,
            aiLearningEngine = aiLearningEngine,
            preferences = preferences,
            onPreferenceChange = onPreferenceChange
        )
        3 -> SettingsScreen(
            preferences = preferences,
            onPreferenceChange = onPreferenceChange,
            onOpenTrainingStudio = { onNavigate(6) },
            onOpenContributions = { onNavigate(7) },
            onOpenDictionary = { onNavigate(8) },
            onOpenStorageManager = { onNavigate(9) },
            onOpenLeaderboard = { onNavigate(10) }
        )
        4 -> AboutScreen()
        5 -> AdminPanelScreen(repository = repository)
        6 -> ShinaTrainingStudioScreen(
            repository = repository,
            aiEngine = aiEngine,
            trainingEngine = trainingEngine,
            onNavigateBack = { onNavigate(0) },
            onOpenContributions = { onNavigate(7) }
        )
        7 -> UserContributionStatsScreen(
            repository = repository,
            aiEngine = aiEngine,
            trainingEngine = trainingEngine,
            onNavigateBack = { onNavigate(0) },
            onNavigateToStudio = { onNavigate(6) },
            onNavigateToLeaderboard = { onNavigate(10) }
        )
        8 -> ShinaDictionaryScreen(
            repository = repository,
            onNavigateBack = { onNavigate(0) }
        )
        9 -> StorageManagementScreen(
            repository = repository,
            onNavigateBack = { onNavigate(0) }
        )
        10 -> CommunityLeaderboardScreen(
            repository = repository,
            hapticFeedbackEnabled = preferences.hapticFeedback,
            onNavigateBack = { onNavigate(0) },
            onNavigateToContributions = { onNavigate(7) }
        )
    }
}
