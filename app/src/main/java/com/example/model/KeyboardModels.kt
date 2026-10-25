package com.example.model

enum class KeyboardLayoutType {
    SHINA_DEFAULT,
    SHINA_PLUS,
    SYMBOLS,
    ROMAN_SHINA,
    EMOJI,
    GIF
}

enum class KeyboardMode {
    STANDARD,
    ONE_HAND_LEFT,
    ONE_HAND_RIGHT,
    FLOATING,
    SPLIT
}

enum class ShinaDialect(val displayName: String, val region: String) {
    GILGIT("Gilgit", "Gilgit Valley"),
    DANYORE("Danyore", "Danyore / Oshikhandass"),
    ASTORE("Astore", "Astore Valley"),
    DIAMER("Diamer", "Diamer / Chilas"),
    GUPIS("Gupis", "Gupis / Yasin"),
    PUNIAL("Punial", "Punial / Gahkuch"),
    CHILAS("Chilas", "Chilas City"),
    KOHISTAN("Kohistan", "Shina Kohistan")
}

enum class KeyboardTheme(val displayName: String) {
    EMERALD_GILGIT("Emerald Gilgit"),
    KARAKORAM_SUNSET("Karakoram Sunset"),
    INDIGO_SLATE("Indigo Slate"),
    MINIMAL_DARK("Minimal Dark"),
    GOLD_SILK("Gold Silk")
}

enum class KeyboardHeight(val scale: Float, val label: String) {
    COMPACT(0.85f, "Compact"),
    NORMAL(1.0f, "Normal"),
    TALL(1.15f, "Tall"),
    EXTRA_TALL(1.30f, "Extra Tall")
}

data class KeyModel(
    val label: String,
    val primaryCode: String = label,
    val longPressVariants: List<String> = emptyList(),
    val flexWeight: Float = 1.0f,
    val isSpecial: Boolean = false,
    val iconResId: Int? = null
)

data class KeyboardPreferences(
    val selectedLayout: KeyboardLayoutType = KeyboardLayoutType.SHINA_DEFAULT,
    val mode: KeyboardMode = KeyboardMode.STANDARD,
    val dialect: ShinaDialect = ShinaDialect.GILGIT,
    val theme: KeyboardTheme = KeyboardTheme.EMERALD_GILGIT,
    val height: KeyboardHeight = KeyboardHeight.NORMAL,
    val showNumberRow: Boolean = true,
    val hapticFeedback: Boolean = true,
    val typingSound: Boolean = true,
    val swipeTyping: Boolean = true,
    val autoCapitalization: Boolean = true,
    val autoCorrect: Boolean = true,
    val nextWordPrediction: Boolean = true,
    val autoSpace: Boolean = true,
    val doubleSpacePeriod: Boolean = true,
    val smartPunctuation: Boolean = true,
    val incognitoMode: Boolean = false,
    val allowAnonymousUpload: Boolean = true,
    val allowVoiceContribution: Boolean = true,
    val disableLearning: Boolean = false,
    val disableVoiceCollection: Boolean = false,
    val disableCloudAi: Boolean = false,
    val offlineAiEnabled: Boolean = true,
    val desktopModeEnabled: Boolean = true,
    val desktopSideRailNavigation: Boolean = true,
    val desktopPhysicalKeyboardTransliteration: Boolean = true,
    val autoTrainFromAiChats: Boolean = true,
    val contributionNotifications: Boolean = true,
    val voiceAutoSpeakAiReplies: Boolean = false,
    val speechSpeed: Float = 1.0f,
    val autoExportBackupWeekly: Boolean = false,
    val automaticCloudBackup: Boolean = true,
    val systemWideDarkMode: Boolean = false
)

data class AiWritingRequest(
    val prompt: String,
    val action: String, // Rewrite, Translate, Chat, Write, Voice, Vision
    val tone: String = "Formal", // Professional, Friendly, Formal, Short, Long, Academic, Social Media, Business, Poetic
    val targetLanguage: String = "Shina", // Shina, Urdu, English, Balti, Burushaski, Khowar, Arabic, Chinese, Hindi
    val contentType: String = "General" // Emails, Essays, Captions, Facebook Posts, WhatsApp Messages, SMS, Poems, Stories, Assignments, Blogs
)

data class AiWritingResponse(
    val resultText: String,
    val explanation: String = ""
)
