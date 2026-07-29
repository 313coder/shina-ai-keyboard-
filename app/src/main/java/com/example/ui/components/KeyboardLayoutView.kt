package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*

val SHINA_UNIQUE_CHARS = setOf(
    "ݜ", "ڇ", "څ", "ڙ", "ݩ", "ݬ", "ݿ", "ۄ",
    "پھ", "تھ", "ٹھ", "جھ", "چھ", "دھ", "ڈھ", "کھ", "گھ",
    "ċ", "č", "ǰ", "ṣ", "ẓ", "ṭ", "ṛ", "ŋ", "rr"
)

@Composable
fun KeyboardLayoutView(
    preferences: KeyboardPreferences,
    currentText: String,
    onKeyPress: (String) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onEnter: () -> Unit,
    onSuggestionSelected: (String) -> Unit,
    onPreferenceChange: (KeyboardPreferences) -> Unit,
    onOpenAiDrawer: () -> Unit = {},
    onOpenClipboardDrawer: () -> Unit = {},
    onOpenVoiceTyping: () -> Unit = {}
) {
    val view = LocalView.current
    var currentLayout by remember { mutableStateOf(preferences.selectedLayout) }
    var activePopupKey by remember { mutableStateOf<KeyModel?>(null) }

    fun triggerHaptic() {
        if (preferences.hapticFeedback) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    val rows = when (currentLayout) {
        KeyboardLayoutType.SHINA_DEFAULT -> LayoutDefinitions.SHINA_DEFAULT_ROWS
        KeyboardLayoutType.SHINA_PLUS -> LayoutDefinitions.SHINA_PLUS_ROWS
        KeyboardLayoutType.SYMBOLS -> LayoutDefinitions.SYMBOLS_ROWS
        KeyboardLayoutType.ROMAN_SHINA -> LayoutDefinitions.ROMAN_SHINA_ROWS
        KeyboardLayoutType.EMOJI -> LayoutDefinitions.EMOJI_ROWS
        KeyboardLayoutType.GIF -> LayoutDefinitions.GIF_ROWS
    }

    // Suggestions derived from last typed word
    val lastWord = currentText.trim().split(Regex("\\s+")).lastOrNull() ?: ""
    val suggestions = remember(lastWord) {
        if (lastWord.isBlank()) {
            listOf("سلام", "شکریلا", "میلا", "آستور", "گلگت")
        } else {
            LayoutDefinitions.INITIAL_SHINA_VOCABULARY
                .filter { it.startsWith(lastWord, ignoreCase = true) }
                .take(3)
                .ifEmpty { listOf(lastWord, "${lastWord}ے", "${lastWord}و") }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        // --- 1. SHINA AI KEYBOARD TOOLBAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Shina AI Brand & Dialect Indicator Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(end = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "🏔️ SHINA AI",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "• ${preferences.dialect.displayName}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (preferences.incognitoMode) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = "Incognito Active",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Right: Quick Action Buttons (AI Assistant, Voice, Clipboard)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        triggerHaptic()
                        onOpenAiDrawer()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Writing Assistant",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        triggerHaptic()
                        onOpenVoiceTyping()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Typing",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = {
                        triggerHaptic()
                        onOpenClipboardDrawer()
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Clipboard Manager",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // --- 2. LAYOUT SWITCHER CHIPS (SHINA STANDARD, SHINA PLUS, ROMAN, 123, EMOJI, GIF) ---
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val layoutItems = listOf(
                KeyboardLayoutType.SHINA_DEFAULT to "شینا (Standard)",
                KeyboardLayoutType.SHINA_PLUS to "شینا پلس (Plus)",
                KeyboardLayoutType.ROMAN_SHINA to "Roman Shina",
                KeyboardLayoutType.SYMBOLS to "123 Symbols",
                KeyboardLayoutType.EMOJI to "😃 Emoji",
                KeyboardLayoutType.GIF to "🖼️ GIF"
            )

            items(layoutItems) { (type, label) ->
                val isSelected = currentLayout == type
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        triggerHaptic()
                        currentLayout = type
                    },
                    label = {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        // --- 3. SUGGESTION BAR ---
        SuggestionBar(
            suggestions = suggestions,
            onSuggestionClick = { selected ->
                triggerHaptic()
                onSuggestionSelected(selected)
            }
        )

        Spacer(modifier = Modifier.height(2.dp))

        // --- 4. NUMBER ROW (IF ENABLED) ---
        if (preferences.showNumberRow) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0").forEach { num ->
                    KeyButton(
                        label = num,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            triggerHaptic()
                            onKeyPress(num)
                        }
                    )
                }
            }
        }

        // --- 5. KEYBOARD ROWS ---
        rows.forEach { rowKeys ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowKeys.forEach { key ->
                    val isShinaUnique = SHINA_UNIQUE_CHARS.contains(key.label)
                    KeyButton(
                        label = key.label,
                        modifier = Modifier.weight(key.flexWeight),
                        hasLongPress = key.longPressVariants.isNotEmpty(),
                        isShinaUnique = isShinaUnique,
                        onClick = {
                            triggerHaptic()
                            onKeyPress(key.primaryCode)
                        },
                        onLongPress = {
                            if (key.longPressVariants.isNotEmpty()) {
                                triggerHaptic()
                                activePopupKey = key
                            }
                        }
                    )
                }
            }
        }

        // --- 6. BOTTOM ACTION ROW ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quick Layout Switcher Button
            KeyButton(
                label = if (currentLayout == KeyboardLayoutType.SYMBOLS) "آب‌پ" else "?123",
                isSpecial = true,
                modifier = Modifier.weight(1.5f),
                onClick = {
                    triggerHaptic()
                    currentLayout = if (currentLayout == KeyboardLayoutType.SYMBOLS) KeyboardLayoutType.SHINA_DEFAULT else KeyboardLayoutType.SYMBOLS
                }
            )

            // Spacebar with swipe gesture cursor control & Shina AI Branding
            Box(
                modifier = Modifier
                    .weight(4.5f)
                    .height(42.dp)
                    .padding(horizontal = 2.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        triggerHaptic()
                        onSpace()
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            if (dragAmount.x > 30) {
                                triggerHaptic()
                            } else if (dragAmount.x < -30) {
                                triggerHaptic()
                            }
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = when (currentLayout) {
                            KeyboardLayoutType.SHINA_DEFAULT -> "🏔️ شینا AI (Shina)"
                            KeyboardLayoutType.SHINA_PLUS -> "✨ شینا پلس"
                            KeyboardLayoutType.ROMAN_SHINA -> "🔤 Roman Shina"
                            KeyboardLayoutType.SYMBOLS -> "Space"
                            KeyboardLayoutType.EMOJI -> "Emoji Picker"
                            KeyboardLayoutType.GIF -> "GIF Search"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Backspace Key
            KeyButton(
                label = "⌫",
                isSpecial = true,
                modifier = Modifier.weight(1.5f),
                onClick = {
                    triggerHaptic()
                    onBackspace()
                }
            )

            // Enter Key
            KeyButton(
                label = "↵",
                isSpecial = true,
                isPrimaryAction = true,
                modifier = Modifier.weight(1.5f),
                onClick = {
                    triggerHaptic()
                    onEnter()
                }
            )
        }
    }

    // --- POPUP DIALOG FOR LONG-PRESS VARIANTS ---
    if (activePopupKey != null) {
        AlertDialog(
            onDismissRequest = { activePopupKey = null },
            confirmButton = {},
            title = {
                Text(
                    text = "Select Shina Character Variant for '${activePopupKey?.label}'",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    activePopupKey?.longPressVariants?.forEach { variant ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable {
                                    triggerHaptic()
                                    onKeyPress(variant)
                                    activePopupKey = null
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = variant,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        )
    }
}

@Composable
fun KeyButton(
    label: String,
    modifier: Modifier = Modifier,
    isSpecial: Boolean = false,
    isPrimaryAction: Boolean = false,
    hasLongPress: Boolean = false,
    isShinaUnique: Boolean = false,
    onClick: () -> Unit = {},
    onLongPress: () -> Unit = {}
) {
    val keyBgColor = when {
        isPrimaryAction -> MaterialTheme.colorScheme.primary
        isSpecial -> MaterialTheme.colorScheme.surfaceVariant
        isShinaUnique -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
        else -> MaterialTheme.colorScheme.surface
    }

    val keyTextColor = when {
        isPrimaryAction -> MaterialTheme.colorScheme.onPrimary
        isSpecial -> MaterialTheme.colorScheme.onSurfaceVariant
        isShinaUnique -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        modifier = modifier
            .height(42.dp)
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = { if (hasLongPress) onLongPress() }
                )
            },
        color = keyBgColor,
        shadowElevation = 1.5.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                fontSize = if (label.length > 2) 12.sp else 18.sp,
                fontWeight = if (isSpecial || isPrimaryAction || isShinaUnique) FontWeight.Bold else FontWeight.Medium,
                color = keyTextColor,
                textAlign = TextAlign.Center
            )

            // Long press indicator dot
            if (hasLongPress) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(3.dp)
                        .size(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }

            // Highlight badge for unique Shina characters (e.g. ݜ, ڇ, ۄ, ݩ, etc.)
            if (isShinaUnique) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .size(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}
