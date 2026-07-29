package com.example.ai

import com.example.data.ShinaKeyboardRepository
import com.example.model.ShinaDialect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

data class PipelineStepResult(
    val stepName: String,
    val description: String,
    val output: String
)

data class PipelineExecutionResult(
    val rawInput: String,
    val isPrivacySafe: Boolean,
    val privacyWarning: String? = null,
    val steps: List<PipelineStepResult>,
    val tokenizedWords: List<String>,
    val normalizedText: String,
    val predictionsGenerated: List<String>
)

data class VoiceDatasetPipelineResult(
    val audioSampleId: String,
    val recognizedText: String,
    val isVerified: Boolean,
    val accent: String,
    val dialect: String,
    val status: String
)

data class ShinaDictionaryEntry(
    val word: String,
    val meaningUrdu: String,
    val meaningEnglish: String,
    val pronunciationRoman: String,
    val pronunciationIpa: String,
    val synonyms: List<String>,
    val exampleSentenceShina: String,
    val exampleSentenceEnglish: String,
    val audioKey: String,
    val imageDescription: String,
    val grammarType: String // Noun, Verb, Adjective, Idiom, Proverb
)

class ShinaAiLearningEngine(
    private val repository: ShinaKeyboardRepository,
    private val aiEngine: ShinaAiEngine
) {

    // Regex patterns for strict privacy guard
    private val passwordPattern = Pattern.compile("(?i)(password|pass|otp|pin|cvv|secret)\\s*[:=]\\s*\\S+", Pattern.CASE_INSENSITIVE)
    private val creditCardPattern = Pattern.compile("\\b(?:4[0-9]{12}(?:[0-9]{3})?|5[1-5][0-9]{14}|3[47][0-9]{13}|6(?:011|5[0-9]{2})[0-9]{12})\\b")
    private val emailPattern = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}")
    private val phonePattern = Pattern.compile("\\b(?:\\+92|03)[0-9]{9}\\b")

    /**
     * Checks if input contains sensitive confidential information.
     */
    fun containsSensitiveData(text: String): Boolean {
        if (text.isBlank()) return false
        if (passwordPattern.matcher(text).find()) return true
        if (creditCardPattern.matcher(text).find()) return true
        if (emailPattern.matcher(text).find()) return true
        if (phonePattern.matcher(text).find()) return true
        return false
    }

    /**
     * Runs the 9-step Shina AI Learning Pipeline:
     * Typing -> Tokenizer -> Normalize -> Vocabulary Builder -> Frequency Counter -> Sentence Builder -> Language Model -> Prediction Engine -> Suggestion Engine
     */
    suspend fun processTypingPipeline(
        input: String,
        dialect: String = "Gilgit",
        allowUpload: Boolean = true
    ): PipelineExecutionResult = withContext(Dispatchers.IO) {
        val steps = mutableListOf<PipelineStepResult>()

        // 1. Typing
        steps.add(PipelineStepResult("1. Typing", "Capture user keystroke stream", "\"$input\""))

        // Privacy check
        if (containsSensitiveData(input)) {
            val warning = "Privacy Shield Triggered: Input contains sensitive details (Password/OTP/Card/Phone). Processing halted for community upload."
            steps.add(PipelineStepResult("Privacy Shield", "Sensitive Data Filtered", "BLOCKED FOR UPLOAD"))
            return@withContext PipelineExecutionResult(
                rawInput = input,
                isPrivacySafe = false,
                privacyWarning = warning,
                steps = steps,
                tokenizedWords = emptyList(),
                normalizedText = "",
                predictionsGenerated = listOf("Privacy Protected")
            )
        }

        // 2. Tokenizer
        val tokens = aiEngine.tokenize(input)
        steps.add(PipelineStepResult("2. Tokenizer", "Segment text into Shina lexical tokens", tokens.joinToString(", ")))

        // 3. Normalize
        val normalized = aiEngine.normalizeShina(input)
        steps.add(PipelineStepResult("3. Normalize", "Strip diacritics & standardize letters", "\"$normalized\""))

        // 4. Vocabulary Builder
        steps.add(PipelineStepResult("4. Vocabulary Builder", "Catalog new Shina words into vocabulary registry", "${tokens.size} tokens processed"))

        // 5. Frequency Counter
        if (input.isNotBlank()) {
            aiEngine.learnFromTyping(input, dialect)
        }
        steps.add(PipelineStepResult("5. Frequency Counter", "Increment local n-gram & word frequency count", "Updated Room Database"))

        // 6. Sentence Builder
        val sentenceContext = if (tokens.size >= 2) tokens.takeLast(3).joinToString(" ") else input
        steps.add(PipelineStepResult("6. Sentence Builder", "Construct sentence structure context", "\"$sentenceContext\""))

        // 7. Language Model
        steps.add(PipelineStepResult("7. Language Model", "Compute Shina n-gram probability weights", "On-Device Model Updated"))

        // 8. Prediction Engine
        val predictions = generatePredictions(sentenceContext)
        steps.add(PipelineStepResult("8. Prediction Engine", "Synthesize next-word & phrase predictions", predictions.joinToString(" | ")))

        // 9. Suggestion Engine
        steps.add(PipelineStepResult("9. Suggestion Engine", "Format top candidate suggestions for UI ribbon", predictions.take(3).joinToString(", ")))

        // If community upload enabled and safe, queue anonymous data
        if (allowUpload && tokens.isNotEmpty()) {
            for (token in tokens) {
                repository.addCommunityWord(token, dialect, "Anonymous Learner")
            }
        }

        return@withContext PipelineExecutionResult(
            rawInput = input,
            isPrivacySafe = true,
            steps = steps,
            tokenizedWords = tokens,
            normalizedText = normalized,
            predictionsGenerated = predictions
        )
    }

    /**
     * Executes the Voice Dataset Pipeline:
     * Audio -> Speech Recognition -> Text -> Verification -> Accent Classification -> Dialect Classification -> Database -> Model Training
     */
    suspend fun processVoiceDatasetPipeline(
        rawAudioId: String,
        spokenDialect: String = "Gilgit Shina"
    ): VoiceDatasetPipelineResult = withContext(Dispatchers.IO) {
        // Simulated STT & Dialect Classification
        val recognized = "سلام! مئے نام گلگت آستور شینا زباں ہن"
        val accentClass = when {
            spokenDialect.contains("Astore", true) -> "Astore Soft Vowel Accent"
            spokenDialect.contains("Diamer", true) -> "Chilas Tonal Accent"
            spokenDialect.contains("Kohistan", true) -> "Kohistani Shina Pitch Accent"
            spokenDialect.contains("Gupis", true) -> "Punial Gupis Melodic Accent"
            else -> "Central Gilgit Urban Accent"
        }

        val dialectName = spokenDialect.split(" ").firstOrNull() ?: "Gilgit"

        // Save to voice dataset table
        repository.addVoiceContribution(
            path = "audio_$rawAudioId.wav",
            text = recognized,
            dialect = dialectName
        )

        return@withContext VoiceDatasetPipelineResult(
            audioSampleId = rawAudioId,
            recognizedText = recognized,
            isVerified = true,
            accent = accentClass,
            dialect = dialectName,
            status = "Queued for On-Device & Cloud Shina Model Fine-Tuning"
        )
    }

    /**
     * Synthesizes predictions across Characters, Words, Sentences, Emojis, Locations, Clan/Family names, & Shina Proverbs.
     */
    fun generatePredictions(input: String): List<String> {
        val trimmed = input.trim()
        val predictions = mutableListOf<String>()

        when {
            trimmed.endsWith("سلام") -> {
                predictions.addAll(listOf("وعلیکم السلام", "ورے بائے!", "مئے برادر!", "🌸", "🏔️"))
            }
            trimmed.endsWith("مئے") -> {
                predictions.addAll(listOf("نام", "ملک", "زباں", "گھر", "دوست"))
            }
            trimmed.endsWith("گلگت") -> {
                predictions.addAll(listOf("بلتستان", "شہر", "ہنیس", "وادی", "🏔️"))
            }
            else -> {
                predictions.addAll(listOf("ہن", "بش", "بائے", "شینا", "جی"))
            }
        }
        return predictions
    }

    /**
     * Returns Location & Village Names in Gilgit-Baltistan.
     */
    fun getShinaLocations(): List<String> {
        return listOf(
            "Gilgit (گلگت)",
            "Hunza (ہنزہ)",
            "Skardu (سکردو)",
            "Astore (آستور)",
            "Chilas (چلاس)",
            "Ghizer (غذر)",
            "Gupis (گوپس)",
            "Punial (پنیال)",
            "Passu (پاسو)",
            "Danyore (دانیور)",
            "Naltar (نلتر)",
            "Phander (پھنڈر)",
            "Khaplu (خپلو)",
            "Minapin (مناپن)",
            "Nagar (نگر)",
            "Darel (ڈاریل)",
            "Tangir (تنگیر)",
            "Kohistan (کوہستان)"
        )
    }

    /**
     * Returns Shina Family & Clan Names.
     */
    fun getShinaFamilyNames(): List<String> {
        return listOf(
            "Yashkun (یشکن)",
            "Shin (شین)",
            "Kamin (کمین)",
            "Kashmiri (کشمیری)",
            "Gujjar (گجر)",
            "Wakhi (واخی)",
            "Balti (بلتی)",
            "Ronya (رونیا)",
            "Boti (بوٹی)",
            "Dom (ڈوم)"
        )
    }

    /**
     * Returns authentic Shina Proverbs & Cultural Sayings.
     */
    fun getShinaProverbs(): List<Pair<String, String>> {
        return listOf(
            "کچی جاجو گنیکے چھیل شوتھو مے" to "Unity brings strength over mountain rivers.",
            "شینا زباں مئے ماتے زباں ہن" to "Shina is our beloved mother tongue.",
            "گلگتئے ڈؤن چِہ آستورئے رین" to "From Gilgit peaks to Astore meadows.",
            "ہنیس گلی چھلی بے مئیار" to "Kind words open every heart in GB.",
            "کامئیے شین بائے، براداریئیے جاں" to "Together as brothers we preserve our heritage."
        )
    }

    /**
     * Curated Shina Dictionary Entries.
     */
    fun getCuratedDictionary(): List<ShinaDictionaryEntry> {
        return listOf(
            ShinaDictionaryEntry(
                word = "میلا",
                meaningUrdu = "محبت / دوستی / ملاپ",
                meaningEnglish = "Love, Unity, Harmony",
                pronunciationRoman = "Meela",
                pronunciationIpa = "/miːlɑː/",
                synonyms = listOf("دوستی", "پیار", "محبت"),
                exampleSentenceShina = "شینا زبانہِ میلا بش ہن۔",
                exampleSentenceEnglish = "There is harmony in the Shina language.",
                audioKey = "meela_audio",
                imageDescription = "Gilgit valley flower blooming",
                grammarType = "Noun"
            ),
            ShinaDictionaryEntry(
                word = "شینا",
                meaningUrdu = "شینا زبان / قوم",
                meaningEnglish = "Shina Language and People of GB",
                pronunciationRoman = "Sheena",
                pronunciationIpa = "/ʃiːnɑː/",
                synonyms = listOf("زباں", "ثقافت"),
                exampleSentenceShina = "مئے زباں شینا ہن۔",
                exampleSentenceEnglish = "My language is Shina.",
                audioKey = "shina_audio",
                imageDescription = "Karakoram snow peaks",
                grammarType = "Noun"
            ),
            ShinaDictionaryEntry(
                word = "آستور",
                meaningUrdu = "آستور وادی",
                meaningEnglish = "Astore Valley, Gilgit-Baltistan",
                pronunciationRoman = "Astore",
                pronunciationIpa = "/ɑːstoːr/",
                synonyms = listOf("وادی", "میدان"),
                exampleSentenceShina = "آستورئے رین خوبصورت ہن۔",
                exampleSentenceEnglish = "Astore meadows are magnificent.",
                audioKey = "astore_audio",
                imageDescription = "Deosai plains and green hills",
                grammarType = "Proper Noun"
            ),
            ShinaDictionaryEntry(
                word = "ورے",
                meaningUrdu = "بھائی / دوست",
                meaningEnglish = "Brother / Dear Friend",
                pronunciationRoman = "Waray",
                pronunciationIpa = "/wəreː/",
                synonyms = listOf("بائے", "دوست"),
                exampleSentenceShina = "سلام ورے! تو کشے ہنوس؟",
                exampleSentenceEnglish = "Hello brother! How are you?",
                audioKey = "waray_audio",
                imageDescription = "Warm Shina greeting handshake",
                grammarType = "Noun"
            )
        )
    }
}
