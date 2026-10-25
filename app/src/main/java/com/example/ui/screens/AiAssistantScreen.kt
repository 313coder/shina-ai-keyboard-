package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.example.ai.ShinaAiEngine
import com.example.ai.ShinaAiTrainingEngine
import com.example.data.ShinaKeyboardRepository
import com.example.model.AiWritingRequest
import com.example.ui.components.AiCompactButtonLoader
import com.example.ui.components.AiThinkingBubble
import com.example.ui.components.AiQuantumOrbLoader
import com.example.voice.ShinaPhoneticSpeechService
import com.example.voice.ShinaPhoneticVoiceDialog
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AiAssistantScreen(
    aiEngine: ShinaAiEngine,
    repository: ShinaKeyboardRepository? = null,
    trainingEngine: ShinaAiTrainingEngine? = null,
    onOpenTrainingStudio: () -> Unit = {},
    onOpenContributions: () -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("AI Chat & Talk", "AI Voice", "AI Translate", "AI Rewrite", "AI Writing", "Training Studio", "Model Impact", "Dictionary & Vision")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        when (selectedTab) {
            0 -> AiChatTab(
                aiEngine = aiEngine,
                trainingEngine = trainingEngine,
                onOpenTrainingStudio = { selectedTab = 5 },
                onOpenContributions = { selectedTab = 6 }
            )
            1 -> AiVoiceTab(aiEngine)
            2 -> AiTranslateTab(aiEngine)
            3 -> AiRewriteTab(aiEngine)
            4 -> AiWritingTab(aiEngine)
            5 -> {
                if (repository != null && trainingEngine != null) {
                    ShinaTrainingStudioScreen(
                        repository = repository,
                        aiEngine = aiEngine,
                        trainingEngine = trainingEngine,
                        onNavigateBack = { selectedTab = 0 },
                        onOpenContributions = { selectedTab = 6 }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Training Studio Initializing...")
                    }
                }
            }
            6 -> {
                if (repository != null && trainingEngine != null) {
                    UserContributionStatsScreen(
                        repository = repository,
                        aiEngine = aiEngine,
                        trainingEngine = trainingEngine,
                        onNavigateBack = { selectedTab = 0 },
                        onNavigateToStudio = { selectedTab = 5 }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Contribution Analytics Initializing...")
                    }
                }
            }
            7 -> AiDictionaryVisionTab(aiEngine)
        }
    }
}

// ---------------- 1. AI REWRITE TAB ----------------
@Composable
fun AiRewriteTab(aiEngine: ShinaAiEngine) {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var inputText by remember { mutableStateOf("سلام! مئے نام گلگت ہن۔ مئے ملک پاکستان ہن۔") }
    var selectedTone by remember { mutableStateOf("Professional") }
    var resultText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val tones = listOf("Professional", "Friendly", "Formal", "Short", "Long", "Academic", "Social Media", "Business")

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("AI Rewrite Engine", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Transform sentence tone, length, and style seamlessly.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth().height(110.dp),
                label = { Text("Original Sentence / Text") },
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Text("Select Rewrite Tone & Style", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(tones) { tone ->
                    FilterChip(
                        selected = selectedTone == tone,
                        onClick = { selectedTone = tone },
                        label = { Text(tone) }
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (inputText.isBlank()) return@Button
                    scope.launch {
                        isLoading = true
                        val res = aiEngine.generateAiWritingResponse(
                            AiWritingRequest(
                                prompt = inputText,
                                action = "Rewrite",
                                tone = selectedTone
                            )
                        )
                        resultText = res.resultText
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && inputText.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    AiCompactButtonLoader()
                    Spacer(Modifier.width(8.dp))
                    Text("Rewriting with Grazina AI...")
                } else {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Rewrite Sentence ($selectedTone)")
                }
            }
        }

        if (resultText.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Rewritten Output ($selectedTone)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(resultText)) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Result", modifier = Modifier.size(18.dp))
                            }
                        }
                        Text(resultText, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ---------------- 2. AI TRANSLATE TAB ----------------
@Composable
fun AiTranslateTab(aiEngine: ShinaAiEngine) {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var inputText by remember { mutableStateOf("مئے نام گلگت ہن، مئے زباں شینا ہن۔") }
    var selectedLang by remember { mutableStateOf("English") }
    var translatedText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val languages = listOf("Shina", "Urdu", "English", "Balti", "Burushaski", "Khowar", "Arabic", "Chinese", "Hindi")

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Translate, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("AI Multilingual Translator", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Translate between Shina, Northern GB languages & global languages.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.fillMaxWidth().height(110.dp),
                label = { Text("Input Text (Shina / Urdu / English / any)") },
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Text("Target Language", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(languages) { lang ->
                    FilterChip(
                        selected = selectedLang == lang,
                        onClick = { selectedLang = lang },
                        label = { Text(lang) }
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (inputText.isBlank()) return@Button
                    scope.launch {
                        isLoading = true
                        val res = aiEngine.generateAiWritingResponse(
                            AiWritingRequest(
                                prompt = inputText,
                                action = "Translate",
                                targetLanguage = selectedLang
                            )
                        )
                        translatedText = res.resultText
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && inputText.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    AiCompactButtonLoader()
                    Spacer(Modifier.width(8.dp))
                    Text("Translating with Grazina AI...")
                } else {
                    Icon(Icons.Default.GTranslate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Translate to $selectedLang")
                }
            }
        }

        if (translatedText.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Translation ($selectedLang)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(translatedText)) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Translation", modifier = Modifier.size(18.dp))
                            }
                        }
                        Text(translatedText, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ---------------- 3. AI CHAT & TALK TAB ----------------
data class ChatMessage(
    val sender: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String = "Just now"
)

@Composable
fun AiChatTab(
    aiEngine: ShinaAiEngine,
    trainingEngine: ShinaAiTrainingEngine? = null,
    onOpenTrainingStudio: () -> Unit = {},
    onOpenContributions: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val listState = rememberLazyListState()

    var chatMode by remember { mutableStateOf("Text Chat") } // "Text Chat", "Voice Talk", "Offline AI"
    var selectedDialect by remember { mutableStateOf("Gilgit Shina") }
    var autoSpeakReplies by remember { mutableStateOf(true) }
    var isVoiceListening by remember { mutableStateOf(false) }
    var showShinaVoiceDialog by remember { mutableStateOf(false) }
    var lastAutoTrainFeedback by remember { mutableStateOf<String?>(null) }

    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage(
                    sender = "Shina AI",
                    text = "سلام! تو کیچو ہنو؟ مئے نام شینا اے آئی ہن۔ I am your personal Shina AI companion. You can chat with me, talk using voice, or ask any question about Shina language and Gilgit-Baltistan culture!",
                    isUser = false
                )
            )
        )
    }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Text-to-Speech Engine
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                ttsEngine?.language = Locale.ENGLISH
            }
        }
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    fun speakText(textToSpeak: String) {
        val cleaned = textToSpeak
            .replace(Regex("[\"#*✨🏔️👇🌸]"), "")
            .trim()
        ttsEngine?.speak(cleaned, TextToSpeech.QUEUE_FLUSH, null, "shina_ai_chat_response")
    }

    // Android Speech Recognition Intent Launcher with Shina Phonetic Processing
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isVoiceListening = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenMatches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenMatches?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                val phoneticResult = ShinaPhoneticSpeechService.transcribeToShinaPhonetics(spokenText, selectedDialect)
                inputText = phoneticResult.phoneticText
                Toast.makeText(context, "Shina Phonetics: ${phoneticResult.phoneticText}", Toast.LENGTH_SHORT).show()
                // Auto-train on voice input
                scope.launch {
                    trainingEngine?.autoTrainFromInteraction(
                        inputText = phoneticResult.phoneticText,
                        responseText = null,
                        dialect = selectedDialect,
                        source = "Voice Talk"
                    )
                    lastAutoTrainFeedback = "Auto-learned from voice: \"${phoneticResult.phoneticText.take(25)}\""
                }
            }
        }
    }

    fun launchVoiceRecognition() {
        showShinaVoiceDialog = true
    }

    fun sendMessage(userMsg: String) {
        if (userMsg.isBlank() || isLoading) return
        val trimmed = userMsg.trim()
        inputText = ""
        messages = messages + ChatMessage("You", trimmed, isUser = true)

        scope.launch {
            isLoading = true
            val response = aiEngine.generateAiWritingResponse(
                AiWritingRequest(
                    prompt = trimmed,
                    action = "Chat",
                    targetLanguage = selectedDialect,
                    tone = "Friendly"
                )
            )
            val aiMsg = ChatMessage("Shina AI", response.resultText, isUser = false)
            messages = messages + aiMsg
            isLoading = false
            listState.animateScrollToItem((messages.size - 1).coerceAtLeast(0))

            // Automatic Training on user chat interaction
            trainingEngine?.autoTrainFromInteraction(
                inputText = trimmed,
                responseText = response.resultText,
                dialect = selectedDialect,
                source = "AI Chat"
            )
            val wordCount = trimmed.split(Regex("\\s+")).filter { it.isNotBlank() }.size
            lastAutoTrainFeedback = "Auto-trained Shina AI: +$wordCount words & n-grams learned"

            if (autoSpeakReplies && isTtsReady) {
                speakText(response.resultText)
            }
        }
    }

    // Voice Visualizer Waves Animation
    val infiniteTransition = rememberInfiniteTransition(label = "audio_waves")
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w1"
    )
    val waveScale2 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(550, easing = LinearEasing), RepeatMode.Reverse),
        label = "w2"
    )
    val waveScale3 by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(350, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w3"
    )
    val waveScale4 by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(480, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "w4"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // --- Top Header Card: Shina AI Assistant Status & Voice Toggle ---
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Shina AI Avatar",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "'SAIPBG' Shina AI",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = "Power by Grazina",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Chatting & Talking in $selectedDialect",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Auto-Speak Toggle
                    IconButton(
                        onClick = {
                            autoSpeakReplies = !autoSpeakReplies
                            if (!autoSpeakReplies) ttsEngine?.stop()
                        }
                    ) {
                        Icon(
                            imageVector = if (autoSpeakReplies) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (autoSpeakReplies) "Auto-Speak Voice ON" else "Voice Muted",
                            tint = if (autoSpeakReplies) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
                    }

                    // Reset / Clear Chat
                    IconButton(
                        onClick = {
                            ttsEngine?.stop()
                            messages = listOf(
                                ChatMessage(
                                    sender = "Shina AI",
                                    text = "سلام! نیا گفتگو شروع کریں۔ How can I help you in Shina today?",
                                    isUser = false
                                )
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Clear Chat",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // --- Live Auto-Training Status Banner ---
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .clickable { onOpenTrainingStudio() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = lastAutoTrainFeedback ?: "Auto-Training Active: Shina AI learns from every chat & voice talk",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable { onOpenContributions() }
                    ) {
                        Text(
                            text = "My Impact →",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { onOpenTrainingStudio() }
                    ) {
                        Text(
                            text = "Data Hub →",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // --- Dialect & Mode Selector Chips ---
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            val modes = listOf("Text Chat", "Voice Talk", "Offline AI")
            items(modes) { mode ->
                FilterChip(
                    selected = chatMode == mode,
                    onClick = { chatMode = mode },
                    label = { Text(mode, fontSize = 11.sp) },
                    leadingIcon = {
                        val icon = when (mode) {
                            "Voice Talk" -> Icons.Default.Mic
                            "Offline AI" -> Icons.Default.WifiOff
                            else -> Icons.Default.Chat
                        }
                        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
                    }
                )
            }

            item {
                VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))
            }

            val dialects = listOf("Gilgit Shina", "Astore Shina", "Diamer/Chilas", "Kohistani")
            items(dialects) { dialect ->
                FilterChip(
                    selected = selectedDialect == dialect,
                    onClick = { selectedDialect = dialect },
                    label = { Text(dialect, fontSize = 11.sp) }
                )
            }
        }

        // --- Interactive Voice Talking Panel (Active when Voice Talk is selected or microphone pressed) ---
        AnimatedVisibility(visible = chatMode == "Voice Talk" || isVoiceListening) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(8.dp))
                            Text("Talking with Shina AI Voice", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        TextButton(onClick = { chatMode = "Text Chat"; isVoiceListening = false }) {
                            Text("Switch to Text", fontSize = 11.sp)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Audio Waveform Equalizer Display
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(44.dp)
                    ) {
                        listOf(waveScale1, waveScale2, waveScale3, waveScale4, waveScale2, waveScale1).forEach { scale ->
                            Box(
                                modifier = Modifier
                                    .width(6.dp)
                                    .height((36 * scale).dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(MaterialTheme.colorScheme.secondary)
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = if (isVoiceListening) "Listening to you... Speak in Shina or Urdu" else "Tap Mic or pick a spoken phrase below to talk:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    Spacer(Modifier.height(8.dp))

                    // Quick Spoken Prompts in Shina
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val spokenPhrases = listOf(
                            "سلام! تو کیچو ہنو؟",
                            "گلگت بلتستان شُو ہنو",
                            "شینا زبانہِ تاریخ کی ہن؟",
                            "How to say 'Thank you' in Shina?",
                            "شینا میں چائے کو کیا کہتے ہیں؟"
                        )
                        items(spokenPhrases) { phrase ->
                            SuggestionChip(
                                onClick = { sendMessage(phrase) },
                                label = { Text(phrase, fontSize = 11.sp) },
                                icon = { Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(13.dp)) }
                            )
                        }
                    }
                }
            }
        }

        // --- Message Conversation Stream ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                ) {
                    if (!msg.isUser) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.isUser) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (msg.isUser) 16.dp else 4.dp,
                            bottomEnd = if (msg.isUser) 4.dp else 16.dp
                        ),
                        modifier = Modifier.widthIn(max = 290.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (msg.isUser) "You" else "Shina AI ($selectedDialect)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = msg.timestamp,
                                    fontSize = 10.sp,
                                    color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Action buttons on Shina AI message: Speak aloud & Copy
                            if (!msg.isUser) {
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { speakText(msg.text) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Listen to Shina AI speak",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(msg.text))
                                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy message",
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (msg.isUser) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "User",
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    AiThinkingBubble(
                        customText = "'SAIPBG' Grazina AI is composing authentic Shina in $selectedDialect..."
                    )
                }
            }
        }

        // --- Quick Prompts Starter Chips ---
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            val quickPrompts = listOf(
                "تو کیچو ہنو؟",
                "گلگت بلتستان کے بارے میں بتاؤ",
                "شینا زبان کے 5 الفاظ سکھائیں",
                "شُو سہیلو (Good morning)",
                "شکریہ کو شینا میں کیا کہتے ہیں؟",
                "شینا محاورے (Proverbs)"
            )
            items(quickPrompts) { prompt ->
                SuggestionChip(
                    onClick = { sendMessage(prompt) },
                    label = { Text(prompt, fontSize = 11.sp) }
                )
            }
        }

        // --- Shina Phonetics Voice-to-Text Recognition Dialog ---
        if (showShinaVoiceDialog) {
            ShinaPhoneticVoiceDialog(
                initialDialect = selectedDialect,
                onDismiss = { showShinaVoiceDialog = false },
                onTextTranscribed = { transcribedShina, autoSend ->
                    inputText = transcribedShina
                    showShinaVoiceDialog = false
                    scope.launch {
                        trainingEngine?.autoTrainFromInteraction(
                            inputText = transcribedShina,
                            responseText = null,
                            dialect = selectedDialect,
                            source = "Voice Talk"
                        )
                        lastAutoTrainFeedback = "Auto-trained Shina AI from voice: \"${transcribedShina.take(25)}\""
                    }
                    if (autoSend) {
                        sendMessage(transcribedShina)
                    }
                }
            )
        }

        // --- Bottom Chat & Voice Input Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input_text_field"),
                placeholder = {
                    Text(
                        text = if (chatMode == "Voice Talk") "Speak into mic or type in Shina..." else "Ask or talk to Shina AI...",
                        fontSize = 13.sp
                    )
                },
                shape = RoundedCornerShape(26.dp),
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        // Microphone icon button in the AI chat text input field
                        IconButton(
                            onClick = { showShinaVoiceDialog = true },
                            modifier = Modifier.testTag("shina_voice_input_button")
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (isVoiceListening) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isVoiceListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                                        contentDescription = "Shina Phonetic Voice-to-Text",
                                        tint = if (isVoiceListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            )

            Spacer(Modifier.width(8.dp))

            FloatingActionButton(
                onClick = { sendMessage(inputText) },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("send_chat_message_button"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

// ---------------- 4. AI WRITING TAB ----------------
@Composable
fun AiWritingTab(aiEngine: ShinaAiEngine) {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    var selectedCategory by remember { mutableStateOf("Emails") }
    var promptInput by remember { mutableStateOf("Requesting leave for visiting Hunza and Gilgit Valley") }
    var generatedResult by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val categories = listOf("Emails", "Essays", "Captions", "Facebook Posts", "WhatsApp Messages", "SMS", "Poems", "Stories", "Assignments", "Blogs")

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Article, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text("AI Content Generator", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Generate emails, essays, social captions, poems, and stories instantly.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }
        }

        item {
            Text("Select Document / Post Type", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                label = { Text("Topic / Brief Instructions") },
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Button(
                onClick = {
                    if (promptInput.isBlank()) return@Button
                    scope.launch {
                        isLoading = true
                        val res = aiEngine.generateAiWritingResponse(
                            AiWritingRequest(
                                prompt = promptInput,
                                action = "Write",
                                contentType = selectedCategory
                            )
                        )
                        generatedResult = res.resultText
                        isLoading = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && promptInput.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    AiCompactButtonLoader()
                    Spacer(Modifier.width(8.dp))
                    Text("Generating $selectedCategory...")
                } else {
                    Icon(Icons.Default.Create, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Generate $selectedCategory")
                }
            }
        }

        if (generatedResult.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Generated Output ($selectedCategory)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            IconButton(onClick = { clipboardManager.setText(AnnotatedString(generatedResult)) }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(18.dp))
                            }
                        }
                        Text(generatedResult, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

// ---------------- 5. AI VOICE TAB ----------------
@Composable
fun AiVoiceTab(aiEngine: ShinaAiEngine) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var voiceText by remember { mutableStateOf("سلام! گلگت شینا زباں آن آواز سُن۔") }
    var selectedAccent by remember { mutableStateOf("Gilgit Shina") }
    var noiseCancellation by remember { mutableStateOf(true) }
    var offlineVoice by remember { mutableStateOf(true) }

    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Initialize TTS
            }
        }
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val accents = listOf("Gilgit Shina", "Astore Shina", "Diamer / Chilas", "Danyore", "Kohistani", "Urdu", "English Accent")

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("AI Voice & Speech Recognition Engine", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Speech-to-Text (STT) & Text-to-Speech (TTS) with dialect accent recognition & noise cancellation.", fontSize = 12.sp)
                }
            }
        }

        item {
            Text("Selected Shina Dialect / Accent Recognition", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accents) { accent ->
                    FilterChip(
                        selected = selectedAccent == accent,
                        onClick = { selectedAccent = accent },
                        label = { Text(accent) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Equalizer, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("AI Noise Cancellation Filter", fontSize = 14.sp)
                }
                Switch(checked = noiseCancellation, onCheckedChange = { noiseCancellation = it })
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Offline Voice Processing", fontSize = 14.sp)
                }
                Switch(checked = offlineVoice, onCheckedChange = { offlineVoice = it })
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isListening) "Listening in $selectedAccent..." else "Tap Microphone to Speak",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isListening = !isListening
                            if (!isListening) {
                                voiceText = "سلام! گلگت آسٹور زبانہِ بول مئے ریکارڈ ہن۔"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isListening) "Stop Speech Recognition" else "Start Speech Recognition")
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = voiceText,
                onValueChange = { voiceText = it },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                label = { Text("Recognized Speech / Text to Speak") },
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Button(
                onClick = {
                    ttsEngine?.speak(voiceText, TextToSpeech.QUEUE_FLUSH, null, "ShinaTTS")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = voiceText.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Play Text To Speech (TTS)")
            }
        }
    }
}

// ---------------- 6. AI DICTIONARY & VISION TAB ----------------
@Composable
fun AiDictionaryVisionTab(aiEngine: ShinaAiEngine) {
    val scope = rememberCoroutineScope()
    var dictQuery by remember { mutableStateOf("میلا") }
    var dictResult by remember { mutableStateOf("") }
    var isDictLoading by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("AI Shina Dictionary & Etymology", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = dictQuery,
                    onValueChange = { dictQuery = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Enter Shina word") },
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (dictQuery.isBlank()) return@Button
                        scope.launch {
                            isDictLoading = true
                            dictResult = aiEngine.queryShinaDictionary(dictQuery)
                            isDictLoading = false
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Search")
                }
            }
        }

        if (isDictLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AiQuantumOrbLoader(
                        size = 85.dp,
                        statusText = "Querying 'SAIPBG' Shina Neural Dictionary..."
                    )
                }
            }
        } else if (dictResult.isNotBlank()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Shina AI Dictionary Entry", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(dictResult, fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            Divider(Modifier.padding(vertical = 8.dp))
            Text("AI Document & Image OCR Scanner", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = null, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Extract Shina & Urdu text from documents or photos", fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = { /* Image OCR flow */ }) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Scan Document / Photo")
                    }
                }
            }
        }
    }
}
