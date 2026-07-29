package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiWritingRequest

@Composable
fun AiAssistantDrawer(
    currentText: String,
    onApplyText: (String) -> Unit,
    onRunAiAction: (AiWritingRequest, (String) -> Unit) -> Unit,
    onClose: () -> Unit
) {
    var selectedAction by remember { mutableStateOf("Rewrite") }
    var selectedTone by remember { mutableStateOf("Formal") }
    var aiOutput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val actions = listOf("Rewrite", "Correct Grammar", "Expand", "Shorten", "Translate Shina", "Formal Tone", "Friendly Tone", "Summarize")
    val tones = listOf("Formal", "Friendly", "Professional", "Casual")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(4.dp),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Assistant",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Shina AI Writing Assistant",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Pills
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(actions) { action ->
                    FilterChip(
                        selected = selectedAction == action,
                        onClick = {
                            selectedAction = action
                            if (currentText.isNotBlank()) {
                                isLoading = true
                                onRunAiAction(
                                    AiWritingRequest(
                                        prompt = currentText,
                                        action = action,
                                        tone = selectedTone
                                    )
                                ) { result ->
                                    aiOutput = result
                                    isLoading = false
                                }
                            }
                        },
                        label = { Text(action, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Output / Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center).size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else if (aiOutput.isBlank()) {
                    Text(
                        text = if (currentText.isBlank()) "Type or select text first, then tap an AI action above!" else "Tap an action above to generate AI output for:\n\"$currentText\"",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = aiOutput,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (aiOutput.isNotBlank()) {
                Button(
                    onClick = {
                        onApplyText(aiOutput)
                        onClose()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Insert")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Replace Text with AI Output")
                }
            }
        }
    }
}
