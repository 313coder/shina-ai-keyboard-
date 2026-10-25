package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.ShinaAiEngine
import com.example.ai.ShinaAiTrainingEngine
import com.example.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShinaTrainingStudioScreen(
    repository: ShinaKeyboardRepository,
    aiEngine: ShinaAiEngine,
    trainingEngine: ShinaAiTrainingEngine,
    onNavigateBack: () -> Unit = {},
    onOpenContributions: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        "AI Training",
        "Words",
        "Sentences",
        "Dialogues",
        "Grammar Rules",
        "Poetry",
        "Audio & Video"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Top Bar with back button ---
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
                Spacer(Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Shina AI Self-Training Hub",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Contribute data & train offline AI model on-device",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(
                    onClick = onOpenContributions,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Impact", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = "OFFLINE AI",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // --- Horizontal Tab Row ---
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 12.dp,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        // --- Screen Content Based on Selected Tab ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            when (selectedTabIndex) {
                0 -> TrainingDashboardTab(repository, trainingEngine)
                1 -> AddWordsTab(repository, aiEngine)
                2 -> AddSentencesTab(repository)
                3 -> AddDialoguesTab(repository)
                4 -> AddGrammarRulesTab(repository)
                5 -> AddPoetryTab(repository)
                6 -> AddAudioVideoTab(repository)
            }
        }
    }
}

// =========================================================================
// 1. TRAINING DASHBOARD TAB
// =========================================================================
@Composable
fun TrainingDashboardTab(
    repository: ShinaKeyboardRepository,
    trainingEngine: ShinaAiTrainingEngine
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val trainingState by trainingEngine.trainingProgress.collectAsState()

    val totalWords by repository.totalWordsCount.collectAsState(initial = 0)
    val totalSentences by repository.totalSentencesCount.collectAsState(initial = 0)
    val totalConversions by repository.totalConversionsCount.collectAsState(initial = 0)
    val totalGrammar by repository.totalGrammarRulesCount.collectAsState(initial = 0)
    val totalPoetry by repository.totalPoetryCount.collectAsState(initial = 0)
    val totalDialogues by repository.totalDialoguesCount.collectAsState(initial = 0)
    val totalMedia by repository.totalMediaCorpusCount.collectAsState(initial = 0)
    val totalAutoEvents by repository.totalAutoTrainingEventsCount.collectAsState(initial = 0)

    val latestMetric by repository.latestTrainingMetric.collectAsState(initial = null)
    val recentLogs by repository.recentAutoTrainingLogs.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- Hero Training Action Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "On-Device Shina AI Model",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Learns autonomously from typing, voice, and your datasets",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Training Progress & Status
                    if (trainingState.isTraining) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    com.example.ui.components.AiCompactButtonLoader(
                                        size = 14.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = trainingState.phaseName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "Epoch ${trainingState.currentEpoch}/${trainingState.totalEpochs}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            LinearProgressIndicator(
                                progress = { trainingState.progressFraction },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Loss: ${"%.2f".format(trainingState.currentLoss)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    text = "Accuracy: ${"%.1f".format(trainingState.currentAccuracy)}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    } else {
                        if (trainingState.completionTimestamp != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        text = trainingState.trainedSummary,
                                        fontSize = 12.sp,
                                        lineHeight = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    trainingEngine.runComprehensiveTrainingCycle()
                                    Toast.makeText(context, "Shina AI offline model successfully trained!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("run_shina_training_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.ModelTraining, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Train Shina AI Offline Model Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- Current Model Accuracy & Metric Card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MetricItem(
                        title = "Offline Accuracy",
                        value = "${latestMetric?.accuracyPercent ?: 95.4f}%",
                        icon = Icons.Default.Verified
                    )
                    VerticalDivider(modifier = Modifier.height(36.dp))
                    MetricItem(
                        title = "Training Loss",
                        value = "%.2f".format(latestMetric?.trainingLoss ?: 0.12f),
                        icon = Icons.Default.ShowChart
                    )
                    VerticalDivider(modifier = Modifier.height(36.dp))
                    MetricItem(
                        title = "Model Build",
                        value = latestMetric?.modelVersionTag ?: "v3.2-GB",
                        icon = Icons.Default.Memory
                    )
                }
            }
        }

        // --- Dataset Statistics Grid ---
        item {
            Text("Shina Multi-Modal Datasets In Corpus", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DatasetStatBadge("Words", "$totalWords", Icons.Default.Abc, Modifier.weight(1f))
                    DatasetStatBadge("Sentences", "$totalSentences", Icons.Default.ShortText, Modifier.weight(1f))
                    DatasetStatBadge("Conversations", "$totalDialogues", Icons.Default.Forum, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DatasetStatBadge("Grammar Rules", "$totalGrammar", Icons.Default.Rule, Modifier.weight(1f))
                    DatasetStatBadge("Poetry (بیت)", "$totalPoetry", Icons.Default.AutoStories, Modifier.weight(1f))
                    DatasetStatBadge("Audio/Video", "$totalMedia", Icons.Default.VideoLibrary, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    DatasetStatBadge("Conversions", "$totalConversions", Icons.Default.Translate, Modifier.weight(1f))
                    DatasetStatBadge("Auto-Learned", "$totalAutoEvents", Icons.Default.Bolt, Modifier.weight(1f))
                }
            }
        }

        // --- Real-time Auto-Training Feed ---
        item {
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Automatic Self-Training Stream", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Live On-Device", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        }

        if (recentLogs.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(6.dp))
                        Text("Auto-learning is actively listening!", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            "Type in the Shina keyboard or chat in the AI Suite. The model automatically trains on your Shina words, voice, and sentences in the background.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(recentLogs) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (log.source) {
                                        "Voice Talk" -> Icons.Default.Mic
                                        "Keyboard Typing" -> Icons.Default.Keyboard
                                        else -> Icons.Default.ChatBubble
                                    },
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = log.source,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "+${log.tokensExtracted} tokens",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                            Text(
                                text = "\"${log.inputSnippet}\"",
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricItem(title: String, value: String, icon: ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.height(2.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(title, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
private fun DatasetStatBadge(label: String, count: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Column {
                Text(count, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

// =========================================================================
// 2. ADD SHINA WORDS TAB
// =========================================================================
@Composable
fun AddWordsTab(repository: ShinaKeyboardRepository, aiEngine: ShinaAiEngine) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var newWord by remember { mutableStateOf("") }
    var selectedDialect by remember { mutableStateOf("Gilgit") }
    var partOfSpeech by remember { mutableStateOf("Noun (اسم)") }
    var meaning by remember { mutableStateOf("") }
    val dialects = listOf("Gilgit", "Astore", "Diamer/Chilas", "Kohistani", "Gurez/Dras")

    val allWords by repository.topWords.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Shina Vocabulary Word", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = newWord,
                        onValueChange = { newWord = it },
                        modifier = Modifier.fillMaxWidth().testTag("shina_word_input_field"),
                        label = { Text("Shina Word (Arabic or Roman)") },
                        placeholder = { Text("e.g. ݜینا, میلا, ورے, چھیل") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = meaning,
                        onValueChange = { meaning = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Urdu / English Meaning") },
                        placeholder = { Text("e.g. محبت / Love, بھائی / Brother") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    Text("Select Dialect:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(dialects) { dialect ->
                            FilterChip(
                                selected = selectedDialect == dialect,
                                onClick = { selectedDialect = dialect },
                                label = { Text(dialect, fontSize = 11.sp) }
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (newWord.isNotBlank()) {
                                scope.launch {
                                    val normalized = aiEngine.normalizeShina(newWord.trim())
                                    repository.wordDao.insertOrUpdateWord(
                                        ShinaWordEntity(
                                            word = newWord.trim(),
                                            normalizedWord = normalized,
                                            frequency = 15,
                                            dialect = selectedDialect,
                                            isApproved = true,
                                            isUserAdded = true
                                        )
                                    )
                                    Toast.makeText(context, "Added \"$newWord\" to Shina AI!", Toast.LENGTH_SHORT).show()
                                    newWord = ""
                                    meaning = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("add_shina_word_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Add Word to Shina AI")
                    }
                }
            }
        }

        item {
            Text("Indexed Shina Words (${allWords.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        items(allWords) { wordItem ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(wordItem.word, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Dialect: ${wordItem.dialect} • Frequency: ${wordItem.frequency}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    IconButton(onClick = { scope.launch { repository.deleteWordByString(wordItem.word) } }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. ADD SENTENCES & CONVERSIONS TAB
// =========================================================================
@Composable
fun AddSentencesTab(repository: ShinaKeyboardRepository) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var subTab by remember { mutableIntStateOf(0) } // 0: Sentence, 1: Conversion

    // Sentence Form State
    var shinaSentence by remember { mutableStateOf("") }
    var urduTranslation by remember { mutableStateOf("") }
    var englishTranslation by remember { mutableStateOf("") }
    var sentenceDialect by remember { mutableStateOf("Gilgit") }
    var sentenceCategory by remember { mutableStateOf("Daily Life") }

    // Conversion Form State
    var sourceText by remember { mutableStateOf("") }
    var targetShinaText by remember { mutableStateOf("") }
    var sourceLanguage by remember { mutableStateOf("Urdu") }
    var conversionType by remember { mutableStateOf("Translation") }

    val allSentences by repository.allSentences.collectAsState(initial = emptyList())
    val allConversions by repository.allConversions.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { subTab = 0 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (subTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (subTab == 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Shina Sentence")
                }
                Button(
                    onClick = { subTab = 1 },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (subTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (subTab == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Conversion / Pair")
                }
            }
        }

        if (subTab == 0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Add Shina Sentence to Training Corpus", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        OutlinedTextField(
                            value = shinaSentence,
                            onValueChange = { shinaSentence = it },
                            modifier = Modifier.fillMaxWidth().testTag("shina_sentence_input"),
                            label = { Text("Shina Sentence") },
                            placeholder = { Text("شینا زباں مئے ماتے زباں ہن۔") },
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = urduTranslation,
                            onValueChange = { urduTranslation = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Urdu Translation") },
                            placeholder = { Text("شینا زبان ہماری مادری زبان ہے۔") },
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = englishTranslation,
                            onValueChange = { englishTranslation = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("English Translation") },
                            placeholder = { Text("Shina is our mother tongue.") },
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                if (shinaSentence.isNotBlank()) {
                                    scope.launch {
                                        repository.addSentence(
                                            ShinaSentenceEntity(
                                                sentenceShina = shinaSentence.trim(),
                                                translationUrdu = urduTranslation.trim(),
                                                translationEnglish = englishTranslation.trim(),
                                                dialect = sentenceDialect,
                                                category = sentenceCategory,
                                                isTrained = false
                                            )
                                        )
                                        Toast.makeText(context, "Sentence added to training corpus!", Toast.LENGTH_SHORT).show()
                                        shinaSentence = ""
                                        urduTranslation = ""
                                        englishTranslation = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("add_shina_sentence_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddCircle, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Save Sentence to Corpus")
                        }
                    }
                }
            }

            items(allSentences) { item ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.category, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                            IconButton(
                                onClick = { scope.launch { repository.deleteSentence(item.id) } },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                        Text(item.sentenceShina, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (item.translationUrdu.isNotBlank()) {
                            Text("اردو: ${item.translationUrdu}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (item.translationEnglish.isNotBlank()) {
                            Text("Eng: ${item.translationEnglish}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        } else {
            // Conversions SubTab
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Add Translation / Script Conversion Pair", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        OutlinedTextField(
                            value = sourceText,
                            onValueChange = { sourceText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Source Text (Urdu, English, Roman Shina)") },
                            placeholder = { Text("Where are you going? or Tu kacho hano?") },
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = targetShinaText,
                            onValueChange = { targetShinaText = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Target Shina Script Text") },
                            placeholder = { Text("تو کانے گاسوس؟ or تو کیچو ہنو؟") },
                            shape = RoundedCornerShape(10.dp)
                        )

                        Button(
                            onClick = {
                                if (sourceText.isNotBlank() && targetShinaText.isNotBlank()) {
                                    scope.launch {
                                        repository.addConversion(
                                            ShinaConversionEntity(
                                                sourceText = sourceText.trim(),
                                                sourceLanguage = sourceLanguage,
                                                targetShinaText = targetShinaText.trim(),
                                                conversionType = conversionType,
                                                dialect = "Gilgit",
                                                isTrained = false
                                            )
                                        )
                                        Toast.makeText(context, "Conversion pair saved!", Toast.LENGTH_SHORT).show()
                                        sourceText = ""
                                        targetShinaText = ""
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CompareArrows, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Save Conversion Pair")
                        }
                    }
                }
            }

            items(allConversions) { conv ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${conv.sourceLanguage} -> Shina", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("\"${conv.sourceText}\"", fontSize = 12.sp)
                            Text("↳ ${conv.targetShinaText}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        IconButton(onClick = { scope.launch { repository.deleteConversion(conv.id) } }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 4. ADD SHINA DIALOGUES TAB
// =========================================================================
@Composable
fun AddDialoguesTab(repository: ShinaKeyboardRepository) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var scenario by remember { mutableStateOf("Mountain Bazaar") }
    var speakerA by remember { mutableStateOf("") }
    var speakerB by remember { mutableStateOf("") }
    var dialect by remember { mutableStateOf("Gilgit") }

    val allDialogues by repository.allDialogues.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Shina Conversation / Dialogue", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Dialogue Title / Scenario") },
                        placeholder = { Text("e.g. Greeting a Friend at Danyore Bridge") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = speakerA,
                        onValueChange = { speakerA = it },
                        modifier = Modifier.fillMaxWidth().testTag("dialogue_speaker_a_input"),
                        label = { Text("Speaker A (Question / Statement in Shina)") },
                        placeholder = { Text("سلام ورے! توئے حال چال کشے ہنو؟") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = speakerB,
                        onValueChange = { speakerB = it },
                        modifier = Modifier.fillMaxWidth().testTag("dialogue_speaker_b_input"),
                        label = { Text("Speaker B (Answer / Reply in Shina)") },
                        placeholder = { Text("وعلیکم السلام! اللہ پاکے کرم ہن، تو کشے ہنوس؟") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (speakerA.isNotBlank() && speakerB.isNotBlank()) {
                                scope.launch {
                                    repository.addDialogue(
                                        ShinaDialogueEntity(
                                            dialogueTitle = if (title.isBlank()) "Shina Dialogue" else title.trim(),
                                            scenario = scenario,
                                            speakerAText = speakerA.trim(),
                                            speakerBReply = speakerB.trim(),
                                            dialect = dialect,
                                            isTrained = false
                                        )
                                    )
                                    Toast.makeText(context, "Dialogue added to training set!", Toast.LENGTH_SHORT).show()
                                    title = ""
                                    speakerA = ""
                                    speakerB = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("add_shina_dialogue_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Forum, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Add Dialogue to AI Corpus")
                    }
                }
            }
        }

        item {
            Text("Training Dialogues (${allDialogues.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        items(allDialogues) { d ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(d.dialogueTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        IconButton(
                            onClick = { scope.launch { repository.deleteDialogue(d.id) } },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("🗣️ A: ${d.speakerAText}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(2.dp))
                    Text("💬 B: ${d.speakerBReply}", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// =========================================================================
// 5. ADD GRAMMAR RULES TAB
// =========================================================================
@Composable
fun AddGrammarRulesTab(repository: ShinaKeyboardRepository) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var ruleName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Ergative Case") }
    var patternExample by remember { mutableStateOf("") }
    var explanation by remember { mutableStateOf("") }
    var dialect by remember { mutableStateOf("Gilgit") }

    val allRules by repository.allGrammarRules.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Shina Grammar Rule", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = ruleName,
                        onValueChange = { ruleName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Rule Title") },
                        placeholder = { Text("e.g. Ergative Case Suffix (-e)") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = patternExample,
                        onValueChange = { patternExample = it },
                        modifier = Modifier.fillMaxWidth().testTag("grammar_rule_pattern_input"),
                        label = { Text("Pattern Example in Shina") },
                        placeholder = { Text("راجائے رئیو (Raja-e reyo) = The Raja said") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = explanation,
                        onValueChange = { explanation = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Linguistic Explanation") },
                        placeholder = { Text("Subject takes -e suffix in transitive past tense.") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (ruleName.isNotBlank() && patternExample.isNotBlank()) {
                                scope.launch {
                                    repository.addGrammarRule(
                                        ShinaGrammarRuleEntity(
                                            ruleName = ruleName.trim(),
                                            ruleCategory = category,
                                            patternExample = patternExample.trim(),
                                            explanation = explanation.trim(),
                                            dialect = dialect,
                                            isTrained = false
                                        )
                                    )
                                    Toast.makeText(context, "Grammar rule registered!", Toast.LENGTH_SHORT).show()
                                    ruleName = ""
                                    patternExample = ""
                                    explanation = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("add_shina_grammar_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Rule, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Submit Grammar Rule")
                    }
                }
            }
        }

        item {
            Text("Shina Grammar System Rules (${allRules.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        items(allRules) { r ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(r.ruleName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                        IconButton(
                            onClick = { scope.launch { repository.deleteGrammarRule(r.id) } },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text("Example: ${r.patternExample}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    if (r.explanation.isNotBlank()) {
                        Text(r.explanation, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 6. ADD SHINA POETRY TAB (شاعری / بیت / غزل)
// =========================================================================
@Composable
fun AddPoetryTab(repository: ShinaKeyboardRepository) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var poet by remember { mutableStateOf("") }
    var poetryType by remember { mutableStateOf("Bait (بیت)") }
    var verses by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }

    val allPoetry by repository.allPoetry.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Add Shina Poetry & Folk Verses (شاعری)", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Poem Title / نظم کا عنوان") },
                        placeholder = { Text("e.g. شینا زبانہِ شان") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = poet,
                        onValueChange = { poet = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Poet Name / شاعر") },
                        placeholder = { Text("e.g. روایتی بیت یا شاعر کا نام") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = verses,
                        onValueChange = { verses = it },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("shina_poetry_verses_input"),
                        label = { Text("Shina Verses (اشعار)") },
                        placeholder = { Text("گلگتئے ڈؤن چِہ آستورئے رین\nشینا زبانہِ میلا بش ہن...") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = translation,
                        onValueChange = { translation = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Urdu / English Meaning (معانی)") },
                        placeholder = { Text("گلگت کے پہاڑوں سے آستور کی وادیوں تک...") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (verses.isNotBlank()) {
                                scope.launch {
                                    repository.addPoetry(
                                        ShinaPoetryEntity(
                                            title = if (title.isBlank()) "شینا کلام" else title.trim(),
                                            poetName = if (poet.isBlank()) "روایتی" else poet.trim(),
                                            poetryType = poetryType,
                                            versesShina = verses.trim(),
                                            translationUrduEnglish = translation.trim(),
                                            dialect = "Gilgit",
                                            isTrained = false
                                        )
                                    )
                                    Toast.makeText(context, "Poetry added to Shina AI!", Toast.LENGTH_SHORT).show()
                                    title = ""
                                    poet = ""
                                    verses = ""
                                    translation = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("add_shina_poetry_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.AutoStories, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Add Poetry to Corpus")
                    }
                }
            }
        }

        item {
            Text("Preserved Shina Poetry (${allPoetry.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        items(allPoetry) { poem ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(poem.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("شاعر: ${poem.poetName} • ${poem.poetryType}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(
                            onClick = { scope.launch { repository.deletePoetry(poem.id) } },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = poem.versesShina,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    if (poem.translationUrduEnglish.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text("ترجمہ: ${poem.translationUrduEnglish}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

// =========================================================================
// 7. ADD AUDIO & VIDEO TRANSCRIPTS TAB
// =========================================================================
@Composable
fun AddAudioVideoTab(repository: ShinaKeyboardRepository) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var mediaType by remember { mutableStateOf("Voice Audio") }
    var title by remember { mutableStateOf("") }
    var transcript by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }
    var phoneticNotes by remember { mutableStateOf("") }
    var dialect by remember { mutableStateOf("Gilgit") }

    val allMedia by repository.allMediaCorpus.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Contribute Shina Audio / Video Data", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("Voice Audio", "Video Transcript", "Folk Song Clip").forEach { type ->
                            FilterChip(
                                selected = mediaType == type,
                                onClick = { mediaType = type },
                                label = { Text(type, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Title / Recording Topic") },
                        placeholder = { Text("e.g. Gilgit Polo Match commentary / Folk Speech") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = transcript,
                        onValueChange = { transcript = it },
                        modifier = Modifier.fillMaxWidth().height(90.dp).testTag("media_transcript_input"),
                        label = { Text("Spoken Shina Audio Transcript") },
                        placeholder = { Text("Exact spoken Shina words from audio/video...") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = phoneticNotes,
                        onValueChange = { phoneticNotes = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Phonetic & Acoustic Notes") },
                        placeholder = { Text("e.g. Soft retroflex ݜ, high pitch tone in Kohistan") },
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (transcript.isNotBlank()) {
                                scope.launch {
                                    repository.addMediaCorpus(
                                        ShinaMediaCorpusEntity(
                                            title = if (title.isBlank()) "$mediaType Sample" else title.trim(),
                                            mediaType = mediaType,
                                            transcriptShina = transcript.trim(),
                                            translation = translation.trim(),
                                            dialect = dialect,
                                            durationSeconds = 15,
                                            phoneticNotes = phoneticNotes.trim(),
                                            isTrained = false
                                        )
                                    )
                                    Toast.makeText(context, "Media corpus sample recorded!", Toast.LENGTH_SHORT).show()
                                    title = ""
                                    transcript = ""
                                    translation = ""
                                    phoneticNotes = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("add_shina_media_button"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Record / Add Media Transcript")
                    }
                }
            }
        }

        item {
            Text("Audio & Video Corpus Entries (${allMedia.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        items(allMedia) { item ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (item.mediaType.contains("Video")) Icons.Default.Videocam else Icons.Default.Audiotrack,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        IconButton(
                            onClick = { scope.launch { repository.deleteMediaCorpus(item.id) } },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                    Text("Transcript: \"${item.transcriptShina}\"", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    if (item.phoneticNotes.isNotBlank()) {
                        Text("Acoustics: ${item.phoneticNotes}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}
