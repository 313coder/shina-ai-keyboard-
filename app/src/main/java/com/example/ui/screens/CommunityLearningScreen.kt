package com.example.ui.screens

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.PipelineExecutionResult
import com.example.ai.ShinaAiEngine
import com.example.ai.ShinaAiLearningEngine
import com.example.ai.VoiceDatasetPipelineResult
import com.example.data.ShinaKeyboardRepository
import com.example.model.KeyboardPreferences
import kotlinx.coroutines.launch

@Composable
fun CommunityLearningScreen(
    repository: ShinaKeyboardRepository,
    aiEngine: ShinaAiEngine,
    aiLearningEngine: ShinaAiLearningEngine,
    preferences: KeyboardPreferences,
    onPreferenceChange: (KeyboardPreferences) -> Unit
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    val sections = listOf(
        "AI Pipeline Simulator",
        "Community Learning & Privacy",
        "Voice Dataset Engine",
        "AI Prediction Explorer",
        "Shina Dictionary"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScrollableTabRow(
            selectedTabIndex = selectedSection,
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            sections.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSection == index,
                    onClick = { selectedSection = index },
                    text = { Text(title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        when (selectedSection) {
            0 -> AiPipelineSimulatorSection(aiLearningEngine, preferences)
            1 -> CommunityLearningPrivacySection(repository, preferences, onPreferenceChange)
            2 -> VoiceDatasetEngineSection(aiLearningEngine, repository)
            3 -> AiPredictionExplorerSection(aiLearningEngine)
            4 -> ShinaDictionarySection(aiLearningEngine)
        }
    }
}

// ---------------- 1. AI PIPELINE SIMULATOR ----------------
@Composable
fun AiPipelineSimulatorSection(
    aiLearningEngine: ShinaAiLearningEngine,
    preferences: KeyboardPreferences
) {
    val scope = rememberCoroutineScope()
    var inputSentence by remember { mutableStateOf("سلام! مئے نام گلگت شینا ہن") }
    var pipelineResult by remember { mutableStateOf<PipelineExecutionResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val pipelineStepsList = listOf(
        "Typing", "Tokenizer", "Normalize", "Vocabulary Builder",
        "Frequency Counter", "Sentence Builder", "Language Model",
        "Prediction Engine", "Suggestion Engine"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("On-Device Continuous AI Learning Pipeline", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Every typed Shina word seamlessly updates local n-gram frequencies and prediction weights.",
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Pipeline Visual Step Nodes
        item {
            Text("9-Step Shina AI Learning Architecture", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(pipelineStepsList.size) { index ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${index + 1}. ${pipelineStepsList[index]}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            if (index < pipelineStepsList.size - 1) {
                                Spacer(Modifier.width(4.dp))
                                Text("↓", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = inputSentence,
                onValueChange = { inputSentence = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Simulate Typed Text Stream") },
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            Button(
                onClick = {
                    scope.launch {
                        isProcessing = true
                        pipelineResult = aiLearningEngine.processTypingPipeline(
                            input = inputSentence,
                            dialect = preferences.dialect.displayName,
                            allowUpload = preferences.allowAnonymousUpload
                        )
                        isProcessing = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing && inputSentence.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text("Executing Pipeline...")
                } else {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Run 9-Step AI Learning Pipeline")
                }
            }
        }

        // Pipeline Results Display
        pipelineResult?.let { res ->
            item {
                if (!res.isPrivacySafe) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(8.dp))
                                Text("Privacy Shield Active", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(res.privacyWarning ?: "Sensitive details detected.", fontSize = 12.sp)
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Pipeline Execution Logs", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(Modifier.height(8.dp))
                            res.steps.forEach { step ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(step.stepName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                        Text(step.description, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(step.output, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                                Divider(Modifier.padding(vertical = 2.dp), color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                            }
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 2. COMMUNITY LEARNING & PRIVACY ----------------
@Composable
fun CommunityLearningPrivacySection(
    repository: ShinaKeyboardRepository,
    preferences: KeyboardPreferences,
    onPreferenceChange: (KeyboardPreferences) -> Unit
) {
    val scope = rememberCoroutineScope()
    var wordInput by remember { mutableStateOf("") }
    var selectedDialect by remember { mutableStateOf(preferences.dialect.displayName) }
    var contributorName by remember { mutableStateOf("") }
    var submitMsg by remember { mutableStateOf("") }

    var anonymousWordsOpt by remember { mutableStateOf(true) }
    var anonymousSentencesOpt by remember { mutableStateOf(true) }
    var anonymousGrammarOpt by remember { mutableStateOf(true) }
    var anonymousVoiceOpt by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Community Learning & Privacy Shield", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Help preserve & digitize Shina. Contributions occur strictly with explicit consent and automatic privacy filtering.",
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Privacy Guarantee Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Strict Privacy Filter (Never Uploaded)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("❌ Passwords & PINs", fontSize = 12.sp)
                        Text("❌ OTP Verification Codes", fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("❌ Credit Cards / CVV", fontSize = 12.sp)
                        Text("❌ Private Chats & Emails", fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("❌ Phone Numbers", fontSize = 12.sp)
                        Text("❌ Secure Field Content", fontSize = 12.sp)
                    }
                }
            }
        }

        // Main Opt-In Toggle
        item {
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Help Improve Shina AI", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Allow anonymous Shina vocabulary & grammar data sharing", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = preferences.allowAnonymousUpload,
                            onCheckedChange = { onPreferenceChange(preferences.copy(allowAnonymousUpload = it)) }
                        )
                    }

                    if (preferences.allowAnonymousUpload) {
                        Spacer(Modifier.height(10.dp))
                        Divider()
                        Spacer(Modifier.height(10.dp))
                        Text("Allowed Anonymous Contributions:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = anonymousWordsOpt, onCheckedChange = { anonymousWordsOpt = it })
                            Text("Anonymous Words", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = anonymousSentencesOpt, onCheckedChange = { anonymousSentencesOpt = it })
                            Text("Anonymous Sentences", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = anonymousGrammarOpt, onCheckedChange = { anonymousGrammarOpt = it })
                            Text("Anonymous Grammar", fontSize = 12.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = anonymousVoiceOpt, onCheckedChange = { anonymousVoiceOpt = it })
                            Text("Anonymous Voice Samples", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Direct Word Submission
        item {
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Submit Word / Dialect Term to Dataset", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = wordInput,
                        onValueChange = { wordInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Shina Word") }
                    )
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = contributorName,
                        onValueChange = { contributorName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Contributor Name (Optional)") }
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (wordInput.isNotBlank()) {
                                scope.launch {
                                    repository.addCommunityWord(
                                        word = wordInput.trim(),
                                        dialect = selectedDialect,
                                        contributor = contributorName.ifBlank { "Anonymous" }
                                    )
                                    wordInput = ""
                                    submitMsg = "Submitted for admin verification queue!"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Submit Word")
                    }
                    if (submitMsg.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(submitMsg, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// ---------------- 3. VOICE DATASET ENGINE ----------------
@Composable
fun VoiceDatasetEngineSection(
    aiLearningEngine: ShinaAiLearningEngine,
    repository: ShinaKeyboardRepository
) {
    val scope = rememberCoroutineScope()
    var selectedDialect by remember { mutableStateOf("Gilgit Shina") }
    var isRecording by remember { mutableStateOf(false) }
    var pipelineResult by remember { mutableStateOf<VoiceDatasetPipelineResult?>(null) }

    val dialects = listOf(
        "Gilgit Shina", "Astore Shina", "Diamer / Chilas", "Darel Shina",
        "Tangir Shina", "Gupis Shina", "Punial Shina", "Kohistan Shina", "+ Custom Dialect"
    )

    val voicePipelineSteps = listOf(
        "Audio", "Speech Recognition", "Text", "Verification",
        "Accent Classification", "Dialect Classification", "Database", "Model Training"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GraphicEq, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Voice Dataset & Accent Classification Engine", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Collects & classifies speech samples across Gilgit, Astore, Diamer, Darel, Tangir, Gupis, Punial, and Kohistan dialects.",
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Voice Pipeline Nodes
        item {
            Text("Voice Processing Pipeline", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(voicePipelineSteps) { step ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Text(
                            text = step,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        item {
            Text("Select Dialect / Region Accent", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dialects) { dialect ->
                    FilterChip(
                        selected = selectedDialect == dialect,
                        onClick = { selectedDialect = dialect },
                        label = { Text(dialect) }
                    )
                }
            }
        }

        item {
            Card(shape = RoundedCornerShape(12.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.GraphicEq else Icons.Default.Mic,
                        contentDescription = null,
                        tint = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isRecording) "Recording sample in $selectedDialect..." else "Tap to record voice sample",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            isRecording = !isRecording
                            if (!isRecording) {
                                scope.launch {
                                    pipelineResult = aiLearningEngine.processVoiceDatasetPipeline(
                                        rawAudioId = System.currentTimeMillis().toString(),
                                        spokenDialect = selectedDialect
                                    )
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(if (isRecording) "Stop & Run Voice Pipeline" else "Start Recording Sample")
                    }
                }
            }
        }

        pipelineResult?.let { res ->
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Voice Dataset Pipeline Result", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Recognized Text: ${res.recognizedText}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Accent Classifier: ${res.accent}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        Text("Dialect Tag: ${res.dialect}", fontSize = 12.sp)
                        Text("Pipeline Status: ${res.status}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ---------------- 4. AI PREDICTION EXPLORER ----------------
@Composable
fun AiPredictionExplorerSection(aiLearningEngine: ShinaAiLearningEngine) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val categories = listOf("Locations", "Family & Clans", "Shina Proverbs", "Predictions")

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(Modifier.width(8.dp))
                        Text("AI Prediction & Cultural Knowledge Engine", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Predicts next character, next word, next sentence, emojis, location names, village names, family names, and Shina proverbs.",
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                categories.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title) }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                item {
                    Text("Gilgit-Baltistan Location & Village Names", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                items(aiLearningEngine.getShinaLocations()) { loc ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Text(loc, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            1 -> {
                item {
                    Text("Shina Family, Clan & Tribes Registry", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                items(aiLearningEngine.getShinaFamilyNames()) { tribe ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(Modifier.width(8.dp))
                            Text(tribe, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            2 -> {
                item {
                    Text("Shina Proverbs & Cultural Sayings (شینا متل)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                items(aiLearningEngine.getShinaProverbs()) { (proverb, translation) ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(proverb, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(translation, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            3 -> {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Next Character / Word / Emoji Predictions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(Modifier.height(8.dp))
                            Text("Input Context: \"سلام مئے\"", fontSize = 13.sp)
                            Spacer(Modifier.height(4.dp))
                            Text("Next Word Predictions: نام | ملک | زباں | دوست", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(4.dp))
                            Text("Emoji Predictions: 🏔️ | 🌸 | 🇵🇰 | ❤️ | ✨", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// ---------------- 5. SHINA DICTIONARY ----------------
@Composable
fun ShinaDictionarySection(aiLearningEngine: ShinaAiLearningEngine) {
    val context = LocalContext.current
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { }
        ttsEngine = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val entries = aiLearningEngine.getCuratedDictionary()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                        Spacer(Modifier.width(8.dp))
                        Text("Shina Etymology & Lexicon Dictionary", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Includes meanings, phonetic IPA, Roman Shina, synonyms, example sentences, audio pronunciation, and grammar types.",
                        fontSize = 12.sp
                    )
                }
            }
        }

        items(entries) { entry ->
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
                        Column {
                            Text(entry.word, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("${entry.pronunciationRoman} • IPA: ${entry.pronunciationIpa}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { ttsEngine?.speak(entry.word, TextToSpeech.QUEUE_FLUSH, null, "ShinaDict") }) {
                            Icon(Icons.Default.VolumeUp, contentDescription = "Audio Pronunciation")
                        }
                    }

                    Spacer(Modifier.height(6.dp))
                    Text("Urdu Meaning: ${entry.meaningUrdu}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text("English Meaning: ${entry.meaningEnglish}", fontSize = 13.sp)
                    Text("Grammar: ${entry.grammarType}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)

                    Spacer(Modifier.height(6.dp))
                    Text("Synonyms: ${entry.synonyms.joinToString(", ")}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Example: ${entry.exampleSentenceShina}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Translation: ${entry.exampleSentenceEnglish}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
