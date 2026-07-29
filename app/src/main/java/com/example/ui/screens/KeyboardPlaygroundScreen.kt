package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.ai.ShinaAiEngine
import com.example.data.ShinaKeyboardRepository
import com.example.model.KeyboardMode
import com.example.model.KeyboardPreferences
import com.example.ui.components.AiAssistantDrawer
import com.example.ui.components.ClipboardDrawer
import com.example.ui.components.KeyboardLayoutView
import kotlinx.coroutines.launch

@Composable
fun KeyboardPlaygroundScreen(
    repository: ShinaKeyboardRepository,
    aiEngine: ShinaAiEngine,
    preferences: KeyboardPreferences,
    onPreferenceChange: (KeyboardPreferences) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var typedText by remember { mutableStateOf("سلام! مئے نام گلگت شینا ہن۔") }
    
    // Undo / Redo stacks
    var undoStack by remember { mutableStateOf(listOf<String>()) }
    var redoStack by remember { mutableStateOf(listOf<String>()) }

    fun updateText(newText: String) {
        if (newText != typedText) {
            undoStack = undoStack + typedText
            redoStack = emptyList()
            typedText = newText
        }
    }

    fun handleUndo() {
        if (undoStack.isNotEmpty()) {
            val last = undoStack.last()
            undoStack = undoStack.dropLast(1)
            redoStack = redoStack + typedText
            typedText = last
        }
    }

    fun handleRedo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.last()
            redoStack = redoStack.dropLast(1)
            undoStack = undoStack + typedText
            typedText = next
        }
    }

    var showClipboardDrawer by remember { mutableStateOf(false) }
    var showAiDrawer by remember { mutableStateOf(false) }
    var showVoiceModal by remember { mutableStateOf(false) }

    val clips by repository.allClips.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper Content Area (Scrollable typing canvas & IME Activation Banner)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Text Input Canvas Header with Undo/Redo & Quick Modes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Interactive Shina Typing Playground",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = { handleUndo() }, enabled = undoStack.isNotEmpty(), modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { handleRedo() }, enabled = redoStack.isNotEmpty(), modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Redo, contentDescription = "Redo", modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = typedText,
                onValueChange = { updateText(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("Type in Shina or use the AI features...") },
                trailingIcon = {
                    if (typedText.isNotEmpty()) {
                        IconButton(onClick = { updateText("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mode Selector Bar (Standard, One-Handed Left/Right, Floating, Split)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mode:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                FilterChip(
                    selected = preferences.mode == KeyboardMode.STANDARD,
                    onClick = { onPreferenceChange(preferences.copy(mode = KeyboardMode.STANDARD)) },
                    label = { Text("Standard", fontSize = 10.sp) }
                )
                FilterChip(
                    selected = preferences.mode == KeyboardMode.ONE_HAND_LEFT,
                    onClick = { onPreferenceChange(preferences.copy(mode = KeyboardMode.ONE_HAND_LEFT)) },
                    label = { Text("One-Hand ◄", fontSize = 10.sp) }
                )
                FilterChip(
                    selected = preferences.mode == KeyboardMode.ONE_HAND_RIGHT,
                    onClick = { onPreferenceChange(preferences.copy(mode = KeyboardMode.ONE_HAND_RIGHT)) },
                    label = { Text("► One-Hand", fontSize = 10.sp) }
                )
                FilterChip(
                    selected = preferences.mode == KeyboardMode.FLOATING,
                    onClick = { onPreferenceChange(preferences.copy(mode = KeyboardMode.FLOATING)) },
                    label = { Text("Floating", fontSize = 10.sp) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        scope.launch {
                            repository.addClip(typedText)
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy to Clipboard", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Clip", fontSize = 12.sp)
                }

                TextButton(
                    onClick = { showAiDrawer = true }
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI Assistant", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Assistant", fontSize = 12.sp)
                }
            }
        }

        // Drawers (Clipboard / AI / Voice)
        if (showClipboardDrawer) {
            ClipboardDrawer(
                clips = clips,
                onSelectClip = { clipText ->
                    updateText(typedText + clipText)
                    showClipboardDrawer = false
                },
                onPinClip = { id, isPinned ->
                    scope.launch { repository.setPinnedClip(id, isPinned) }
                },
                onDeleteClip = { id ->
                    scope.launch { repository.deleteClip(id) }
                },
                onClose = { showClipboardDrawer = false }
            )
        }

        if (showAiDrawer) {
            AiAssistantDrawer(
                currentText = typedText,
                onApplyText = { newText -> updateText(newText) },
                onRunAiAction = { req, onResult ->
                    scope.launch {
                        val response = aiEngine.generateAiWritingResponse(req)
                        onResult(response.resultText)
                    }
                },
                onClose = { showAiDrawer = false }
            )
        }

        if (showVoiceModal) {
            AlertDialog(
                onDismissRequest = { showVoiceModal = false },
                title = { Text("Shina Voice Recognition AI") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Listening in Gilgit Shina dialect...", fontSize = 13.sp)
                        Text("Speak clearly near the device microphone.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        updateText(typedText + " سلام! گلگت شینا زباں")
                        showVoiceModal = false
                    }) {
                        Text("Insert Spoken Text")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showVoiceModal = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Interactive Virtual Keyboard Component
        KeyboardLayoutView(
            preferences = preferences,
            currentText = typedText,
            onKeyPress = { char -> updateText(typedText + char) },
            onBackspace = { if (typedText.isNotEmpty()) updateText(typedText.dropLast(1)) },
            onSpace = {
                val newText = typedText + " "
                updateText(newText)
                if (!preferences.incognitoMode && !preferences.disableLearning) {
                    scope.launch {
                        repository.observeAndLearnFromUserText(
                            text = typedText,
                            dialect = preferences.dialect.displayName,
                            disableLearning = preferences.disableLearning
                        )
                    }
                }
            },
            onEnter = {
                val newText = typedText + "\n"
                updateText(newText)
                if (!preferences.incognitoMode && !preferences.disableLearning) {
                    scope.launch {
                        repository.observeAndLearnFromUserText(
                            text = typedText,
                            dialect = preferences.dialect.displayName,
                            disableLearning = preferences.disableLearning
                        )
                    }
                }
            },
            onSuggestionSelected = { suggestion ->
                val words = typedText.trim().split(Regex("\\s+"))
                val newText = if (words.isNotEmpty()) {
                    words.dropLast(1).joinToString(" ") + " $suggestion "
                } else {
                    "$suggestion "
                }
                updateText(newText.trimStart())
                if (!preferences.incognitoMode && !preferences.disableLearning) {
                    scope.launch {
                        repository.observeAndLearnFromUserText(
                            text = suggestion,
                            dialect = preferences.dialect.displayName,
                            disableLearning = preferences.disableLearning
                        )
                    }
                }
            },
            onPreferenceChange = onPreferenceChange,
            onOpenAiDrawer = { showAiDrawer = true },
            onOpenClipboardDrawer = { showClipboardDrawer = true },
            onOpenVoiceTyping = { showVoiceModal = true }
        )
    }
}
