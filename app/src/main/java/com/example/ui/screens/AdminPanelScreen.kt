package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ShinaKeyboardRepository
import com.example.data.ShinaWordEntity
import com.example.data.LearnedPhraseEntity
import com.example.data.VoiceContributionEntity
import kotlinx.coroutines.launch

@Composable
fun AdminPanelScreen(
    repository: ShinaKeyboardRepository
) {
    val scope = rememberCoroutineScope()

    // Database Flows
    val totalWords by repository.totalWordsCount.collectAsState(initial = 0)
    val totalNgrams by repository.totalNgramsCount.collectAsState(initial = 0)
    val totalVoices by repository.totalVoiceCount.collectAsState(initial = 0)
    val totalLearned by repository.totalLearnedCount.collectAsState(initial = 0)
    val pendingWords by repository.pendingCommunityWords.collectAsState(initial = emptyList())
    val topWordsList by repository.topWords.collectAsState(initial = emptyList())
    val topLearnedPhrasesList by repository.topLearnedPhrases.collectAsState(initial = emptyList())
    val voiceContributionsList by repository.allVoiceContributions.collectAsState(initial = emptyList())

    // Admin Password Security State
    var adminPassword by remember { mutableStateOf("shina2026") }
    var isUnlocked by remember { mutableStateOf(false) }
    var inputPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    // Change password dialog state
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var changePasswordSuccessMsg by remember { mutableStateOf("") }

    // Dictionary Management Form State
    var newWordInput by remember { mutableStateOf("") }
    var selectedDialectForWord by remember { mutableStateOf("Gilgit") }
    var wordActionNotice by remember { mutableStateOf("") }

    // Tab state
    var selectedTab by remember { mutableIntStateOf(0) }
    val adminModules = listOf(
        "Analytics", "Dictionary Manager", "Learned Phrases",
        "Voice Samples", "Moderation Queue", "Users & Security"
    )

    // UNLOCKED ADMIN PANEL CONTENT vs PASSWORD LOCK SCREEN
    if (!isUnlocked) {
        // --- PASSWORD VERIFICATION SCREEN ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Security Lock",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Text(
                        text = "Shina AI Admin Authentication",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = "Enter master password to access full developer control panel.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = inputPassword,
                        onValueChange = {
                            inputPassword = it
                            passwordError = false
                        },
                        label = { Text("Admin Password") },
                        isError = passwordError,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle Password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (passwordError) {
                        Text(
                            text = "Incorrect admin password! Please try again.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Default: shina2026",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                        Button(
                            onClick = {
                                if (inputPassword == adminPassword) {
                                    isUnlocked = true
                                    passwordError = false
                                    inputPassword = ""
                                } else {
                                    passwordError = true
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Unlock Console", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        // --- FULL ADMIN CONSOLE UI ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top App Bar with Lock Button & Admin Status
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Console",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Zaigham's Admin Console",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Full System Control • Shina AI Engine",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }

                    IconButton(
                        onClick = { isUnlocked = false },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Console",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Navigation Tab Row
            ScrollableTabRow(selectedTabIndex = selectedTab) {
                adminModules.forEachIndexed { idx, title ->
                    Tab(
                        selected = selectedTab == idx,
                        onClick = { selectedTab = idx },
                        text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTab) {
                    0 -> { // Analytics & Overview
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MetricCard("Total Lexicon", "$totalWords words", Icons.Default.Book, Modifier.weight(1f))
                                MetricCard("Learned N-Grams", "$totalNgrams pairs", Icons.Default.Psychology, Modifier.weight(1f))
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                MetricCard("Learned Phrases", "$totalLearned custom", Icons.Default.AutoAwesome, Modifier.weight(1f))
                                MetricCard("Voice Dataset", "$totalVoices recordings", Icons.Default.Mic, Modifier.weight(1f))
                            }
                        }
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Database Status & Storage Health", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Spacer(Modifier.height(6.dp))
                                    Text("• Room DB Schema: Version 2 (Local Persistence Active)", fontSize = 12.sp)
                                    Text("• On-Device Learning Module: Active", fontSize = 12.sp)
                                    Text("• Community Moderation Queue: ${pendingWords.size} pending", fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    1 -> { // Dictionary & Word Manager (Add, Edit, Remove)
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text("Add New Word to Dictionary", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = newWordInput,
                                            onValueChange = { newWordInput = it },
                                            label = { Text("Shina Word") },
                                            modifier = Modifier.weight(1f),
                                            singleLine = true
                                        )

                                        Button(
                                            onClick = {
                                                if (newWordInput.isNotBlank()) {
                                                    val wordToAdd = newWordInput.trim()
                                                    scope.launch {
                                                        repository.addDictionaryWord(wordToAdd, selectedDialectForWord)
                                                        wordActionNotice = "Added '$wordToAdd' to Room Database!"
                                                        newWordInput = ""
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Add")
                                            Spacer(Modifier.width(4.dp))
                                            Text("Add")
                                        }
                                    }

                                    if (wordActionNotice.isNotBlank()) {
                                        Text(
                                            text = wordActionNotice,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Existing Vocabulary List ($totalWords Total)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }

                        if (topWordsList.isEmpty()) {
                            item {
                                Text("No words in dictionary.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        } else {
                            items(topWordsList) { wordItem ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = wordItem.word, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text(
                                                text = "Dialect: ${wordItem.dialect} • Frequency: ${wordItem.frequency}",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    repository.deleteWordByString(wordItem.word)
                                                    wordActionNotice = "Deleted '${wordItem.word}' from database."
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Word",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> { // Learned Phrases Manager
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("On-Device Learned Phrases", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text("Phrases automatically learned by observing typing patterns.", fontSize = 12.sp)
                                    }

                                    Button(
                                        onClick = {
                                            scope.launch {
                                                repository.clearAllLearnedPhrases()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Clear All", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        if (topLearnedPhrasesList.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No learned phrases stored yet.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(topLearnedPhrasesList) { phrase ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = phrase.phrase, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text(text = "Typed ${phrase.frequency} times • Dialect: ${phrase.dialect}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    repository.deleteLearnedPhrase(phrase.id)
                                                }
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    3 -> { // Voice Samples Manager
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text("Recorded Voice Clips Manager", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("Total dataset clips: $totalVoices audio recordings", fontSize = 12.sp)
                                }
                            }
                        }

                        if (voiceContributionsList.isEmpty()) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                    Text("No voice recordings uploaded yet.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            items(voiceContributionsList) { voice ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = voice.transcribedText.ifEmpty { "Audio Sample" }, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(text = "Dialect: ${voice.dialect} • Path: ${voice.audioPath}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        IconButton(
                                            onClick = {
                                                scope.launch {
                                                    repository.deleteVoiceContribution(voice.id)
                                                }
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    4 -> { // Moderation Queue
                        item {
                            Text("Community Moderation Queue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }

                        if (pendingWords.isEmpty()) {
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text("Queue empty! All user contributions reviewed.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        } else {
                            items(pendingWords) { word ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = word.word, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                            Text(text = "Dialect: ${word.dialect} • Contributor: ${word.contributorName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        repository.approveCommunityWord(word.id, word.word, word.dialect)
                                                    }
                                                }
                                            ) {
                                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Approve", tint = MaterialTheme.colorScheme.primary)
                                            }
                                            IconButton(
                                                onClick = {
                                                    scope.launch {
                                                        repository.deleteCommunityWord(word.id)
                                                    }
                                                }
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    5 -> { // Users & Security Settings (Change Admin Password)
                        item {
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text("Admin Security & Password Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                                    Text("Change Master Admin Password", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                                    OutlinedTextField(
                                        value = newPasswordInput,
                                        onValueChange = { newPasswordInput = it },
                                        label = { Text("New Admin Password") },
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = confirmPasswordInput,
                                        onValueChange = { confirmPasswordInput = it },
                                        label = { Text("Confirm New Password") },
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Button(
                                        onClick = {
                                            if (newPasswordInput.isNotBlank() && newPasswordInput == confirmPasswordInput) {
                                                adminPassword = newPasswordInput
                                                changePasswordSuccessMsg = "Master Admin Password updated successfully!"
                                                newPasswordInput = ""
                                                confirmPasswordInput = ""
                                            } else {
                                                changePasswordSuccessMsg = "Passwords do not match or are empty!"
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Save New Password")
                                    }

                                    if (changePasswordSuccessMsg.isNotBlank()) {
                                        Text(
                                            text = changePasswordSuccessMsg,
                                            color = if (changePasswordSuccessMsg.contains("successfully")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

