package com.example.ui.screens

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ShinaAiEngine
import com.example.model.AiWritingRequest
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AiAssistantScreen(
    aiEngine: ShinaAiEngine
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("AI Rewrite", "AI Translate", "AI Chat", "AI Writing", "AI Voice", "Dictionary & Vision")

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
            0 -> AiRewriteTab(aiEngine)
            1 -> AiTranslateTab(aiEngine)
            2 -> AiChatTab(aiEngine)
            3 -> AiWritingTab(aiEngine)
            4 -> AiVoiceTab(aiEngine)
            5 -> AiDictionaryVisionTab(aiEngine)
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
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Rewriting...")
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
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Translating...")
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

// ---------------- 3. AI CHAT TAB (ChatGPT Style) ----------------
data class ChatMessage(val sender: String, val text: String, val isUser: Boolean, val timestamp: String = "Just now")

@Composable
fun AiChatTab(aiEngine: ShinaAiEngine) {
    val scope = rememberCoroutineScope()
    var chatMode by remember { mutableStateOf("Text Chat") } // Text Chat, Voice Chat, Image Chat, Doc Chat, Offline AI
    var messages by remember {
        mutableStateOf(
            listOf(
                ChatMessage("Shina AI", "سلام! مئے نام شینا اے آئی اسسٹنٹ ہن۔ how can I assist you today in Shina, Urdu, or English?", isUser = false)
            )
        )
    }
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        // Mode selector
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            val modes = listOf("Text Chat", "Voice Chat", "Image Chat", "Document Chat", "Offline AI Chat")
            items(modes) { mode ->
                FilterChip(
                    selected = chatMode == mode,
                    onClick = { chatMode = mode },
                    label = { Text(mode, fontSize = 12.sp) },
                    leadingIcon = {
                        val icon = when (mode) {
                            "Voice Chat" -> Icons.Default.Mic
                            "Image Chat" -> Icons.Default.Image
                            "Document Chat" -> Icons.Default.Description
                            "Offline AI Chat" -> Icons.Default.WifiOff
                            else -> Icons.Default.Chat
                        }
                        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }

        // Message List
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { msg ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (msg.isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (msg.isUser) "You" else "Shina AI Assistant",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = if (msg.isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            if (isLoading) {
                item {
                    Text("Shina AI is thinking...", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text(if (chatMode == "Voice Chat") "Speak or type prompt..." else "Ask AI anything...") },
                shape = RoundedCornerShape(24.dp)
            )
            Spacer(Modifier.width(6.dp))
            FloatingActionButton(
                onClick = {
                    if (inputText.isBlank()) return@FloatingActionButton
                    val userMsg = inputText
                    inputText = ""
                    messages = messages + ChatMessage("You", userMsg, isUser = true)
                    scope.launch {
                        isLoading = true
                        val res = aiEngine.generateAiWritingResponse(
                            AiWritingRequest(
                                prompt = userMsg,
                                action = "Chat",
                                tone = "Friendly"
                            )
                        )
                        messages = messages + ChatMessage("Shina AI", res.resultText, isUser = false)
                        isLoading = false
                    }
                },
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send")
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
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
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
                CircularProgressIndicator()
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
