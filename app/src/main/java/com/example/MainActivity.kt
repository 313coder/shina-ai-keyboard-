package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ShinaAiEngine
import com.example.ai.ShinaAiLearningEngine
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

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        database = ShinaDatabase.getDatabase(applicationContext)
        repository = ShinaKeyboardRepository(database)
        aiEngine = ShinaAiEngine(database)
        aiLearningEngine = ShinaAiLearningEngine(repository, aiEngine)

        setContent {
            var preferences by remember { mutableStateOf(KeyboardPreferences()) }
            var currentScreen by remember { mutableIntStateOf(0) }
            var showSplash by remember { mutableStateOf(true) }

            ShinaTheme(theme = preferences.theme) {
                if (showSplash) {
                    WelcomeSplashScreen(
                        onSplashComplete = { showSplash = false }
                    )
                } else {
                    Scaffold(
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = "Shina AI Keyboard",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
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
                                    IconButton(onClick = { currentScreen = 4 }) { // Admin Dashboard
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
                                    icon = { Icon(Icons.Default.Keyboard, contentDescription = "Keyboard") },
                                    label = { Text("Keyboard") }
                                )
                                NavigationBarItem(
                                    selected = currentScreen == 1,
                                    onClick = { currentScreen = 1 },
                                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "AI Suite") },
                                    label = { Text("AI Suite") }
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
                                    selected = currentScreen == 5,
                                    onClick = { currentScreen = 5 },
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
                            when (currentScreen) {
                                0 -> KeyboardPlaygroundScreen(
                                    repository = repository,
                                    aiEngine = aiEngine,
                                    preferences = preferences,
                                    onPreferenceChange = { preferences = it }
                                )
                                1 -> AiAssistantScreen(aiEngine = aiEngine)
                                2 -> CommunityLearningScreen(
                                    repository = repository,
                                    aiEngine = aiEngine,
                                    aiLearningEngine = aiLearningEngine,
                                    preferences = preferences,
                                    onPreferenceChange = { preferences = it }
                                )
                                3 -> SettingsScreen(
                                    preferences = preferences,
                                    onPreferenceChange = { preferences = it }
                                )
                                4 -> AdminPanelScreen(repository = repository)
                                5 -> AboutScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}
