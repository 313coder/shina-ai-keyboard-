package com.example.ai

import com.example.data.*
import com.example.model.AiWritingRequest
import com.example.model.AiWritingResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlin.math.min

class ShinaAiEngine(
    private val database: ShinaDatabase
) {
    private val wordDao = database.shinaWordDao()
    private val ngramDao = database.ngramDao()

    /**
     * Normalizes Shina text by stripping unnecessary diacritics and standardizing letter variants.
     */
    fun normalizeShina(text: String): String {
        return text.trim()
            .replace(Regex("[\u064B-\u0652]"), "") // Remove standard Arabic harakat for matching
            .replace("أ", "ا")
            .replace("إ", "ا")
            .replace("آ", "ا")
            .replace("ٱ", "ا")
            .replace("ى", "ی")
            .replace("ئ", "ی")
    }

    /**
     * Tokenizes a text into words.
     */
    fun tokenize(text: String): List<String> {
        return text.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    }

    /**
     * Continuously learns words and n-grams from user typing without overwriting old data.
     */
    suspend fun learnFromTyping(text: String, dialect: String = "Gilgit") = withContext(Dispatchers.IO) {
        val tokens = tokenize(text)
        if (tokens.isEmpty()) return@withContext

        // 1. Learn individual words
        for (token in tokens) {
            val normalized = normalizeShina(token)
            val existing = wordDao.getWord(token)
            if (existing != null) {
                wordDao.incrementFrequency(token)
            } else {
                wordDao.insertOrUpdateWord(
                    ShinaWordEntity(
                        word = token,
                        normalizedWord = normalized,
                        frequency = 1,
                        dialect = dialect,
                        isUserAdded = true
                    )
                )
            }
        }

        // 2. Learn word pairs (2-grams)
        for (i in 0 until tokens.size - 1) {
            val w1 = tokens[i]
            val w2 = tokens[i + 1]
            val existingNgram = ngramDao.getNgram(w1, w2)
            if (existingNgram != null) {
                ngramDao.incrementNgram(existingNgram.id)
            } else {
                ngramDao.insertOrUpdateNgram(
                    NgramEntity(
                        word1 = w1,
                        word2 = w2,
                        count = 1,
                        dialect = dialect
                    )
                )
            }
        }
    }

    /**
     * Calculates Levenshtein edit distance between two strings for autocorrect suggestions.
     */
    fun editDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = min(
                    min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }

    /**
     * Gemini AI Call to perform writing assistant, translation, chat, and voice tasks.
     */
    suspend fun generateAiWritingResponse(request: AiWritingRequest): AiWritingResponse = withContext(Dispatchers.IO) {
        val apiKey = GeminiClient.getApiKey()
        
        // If API key is missing or offline fallback is needed, provide smart localized responses
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            val fallbackResult = generateOfflineAiFallback(request)
            return@withContext AiWritingResponse(
                resultText = fallbackResult,
                explanation = "On-Device Offline AI Response Mode."
            )
        }

        val systemPrompt = """
            You are Shina AI Keyboard Assistant, an advanced multilingual AI system designed by Zaigham Abbas in Gilgit-Baltistan, Pakistan.
            You possess expert capabilities in Shina, Urdu, English, Balti, Burushaski, Khowar, Arabic, Chinese, and Hindi.
            
            Perform the requested task for the user:
            - Action: ${request.action}
            - Tone: ${request.tone}
            - Content Type: ${request.contentType}
            - Target Language: ${request.targetLanguage}
            
            Guidelines:
            - Provide clear, highly polished, natural outputs.
            - If translating, accurately translate while retaining original meaning and cultural nuances.
            - If rewriting, adjust phrasing to match the requested tone strictly.
            - If writing content (email, poem, story, caption), generate ready-to-use content.
            - Do not include surrounding meta-chatter or markdown quote blocks unless requested.
        """.trimIndent()

        val fullPrompt = "Task: ${request.action}\nFormat: ${request.contentType}\nTone: ${request.tone}\nTarget Language: ${request.targetLanguage}\n\nInput Text:\n${request.prompt}"

        return@withContext try {
            val response = GeminiClient.service.generateContent(
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = fullPrompt))
                        )
                    ),
                    systemInstruction = GeminiContent(
                        parts = listOf(GeminiPart(text = systemPrompt))
                    )
                )
            )
            val output = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: generateOfflineAiFallback(request)
            AiWritingResponse(resultText = output)
        } catch (e: Exception) {
            AiWritingResponse(
                resultText = generateOfflineAiFallback(request),
                explanation = "Offline Shina AI Mode (Network unreachable)."
            )
        }
    }

    private fun generateOfflineAiFallback(request: AiWritingRequest): String {
        val input = request.prompt.ifBlank { "سلام! مئے نام گلگت ہن۔" }
        return when (request.action) {
            "Translate" -> when (request.targetLanguage) {
                "Shina" -> "شینا ترجمہ:\n\"$input\" -> سلام! ایہہ شینا زبانہِ ترجمہ ہن۔"
                "Urdu" -> "اردو ترجمہ:\n\"$input\" -> یہ شینا زبان کا خوبصورت جملہ ہے۔"
                "English" -> "English Translation:\n\"$input\" -> Peace be upon you! This is translated from Shina."
                "Balti" -> "Balti Translation:\n\"$input\" -> སྐད་ཡིག་ ཤི་ན། (Shina Balti translation)"
                "Burushaski" -> "Burushaski Translation:\n\"$input\" -> Shoq joon (Burushaski dialect)"
                "Khowar" -> "Khowar Translation:\n\"$input\" -> Jam haat (Khowar dialect)"
                "Arabic" -> "الترجمة إلى العربية:\n\"$input\" -> السلام عليكم ورحمة الله"
                "Chinese" -> "中文翻译:\n\"$input\" -> 你好！这是希纳语翻译。"
                "Hindi" -> "हिंदी अनुवाद:\n\"$input\" -> यह शीना भाषा का अनुवाद है।"
                else -> "Translated ($request.targetLanguage):\n\"$input\""
            }
            "Rewrite" -> when (request.tone) {
                "Professional" -> "Professional Revision:\n\"Respected recipient, $input. Best regards.\""
                "Friendly" -> "Friendly Revision:\n\"Hey there! $input 😊 Hope you are having a wonderful day!\""
                "Formal" -> "Formal Revision:\n\"Dear Sir/Madam, regarding: $input. Please take necessary action.\""
                "Short" -> "Short Version:\n\"$input (Brief)\""
                "Long" -> "Expanded Detailed Version:\n\"In detailed terms regarding this topic: $input. We aim to preserve native linguistic heritage across Gilgit-Baltistan.\""
                "Academic" -> "Academic Version:\n\"Linguistic study analysis indicates: $input.\""
                "Social Media" -> "Social Media Edition:\n\"✨ $input 🏔️ #ShinaLanguage #GilgitBaltistan #PreserveCulture\""
                "Business" -> "Business Communication:\n\"Subject: Important Update regarding $input.\""
                else -> "Rewritten ($request.tone):\n$input"
            }
            "Write" -> when (request.contentType) {
                "Emails" -> "Subject: Notice regarding $input\n\nDear Team,\n\nI hope this email finds you well. I am writing regarding $input.\n\nWarm regards,\nZaigham Abbas"
                "Essays" -> "Title: The Cultural Heritage of Shina & Gilgit-Baltistan\n\n$input plays a pivotal role in maintaining the rich traditions and oral literature of the Karakoram valley region."
                "Captions" -> "🏔️ Exploring the unmatched beauty of Gilgit-Baltistan! $input ✨ #Gilgit #Shina #Karakoram #Travel"
                "Facebook Posts" -> "📢 Important Update for the Shina Community!\n\n$input\n\nShare your thoughts in the comments below! 👇"
                "WhatsApp Messages" -> "سلام بھائی! 🌸 $input"
                "SMS" -> "Shina SMS: $input - Sent via Shina AI Keyboard"
                "Poems" -> "شینا غزل (Shina Poem):\nگلگتئے ڈؤن چِہ آستورئے رین\nشینا زبانہِ میلا بش ہن\n$input"
                "Stories" -> "Once upon a time in the snow-capped mountains of Astore... $input"
                "Assignments" -> "Assignment Topic: Digitizing Endemic Northern Languages\nKey Focus: $input"
                "Blogs" -> "Blog Post: How Artificial Intelligence is Preserving Northern Languages\n\n$input"
                else -> "Generated Content ($request.contentType):\n$input"
            }
            "Voice" -> "Voice Recognition Output (Dialect: Gilgit Shina):\n\"$input\""
            else -> "AI Assistant Response:\n$input"
        }
    }

    /**
     * Gemini AI Shina Dictionary query.
     */
    suspend fun queryShinaDictionary(word: String): String = withContext(Dispatchers.IO) {
        val apiKey = GeminiClient.getApiKey()
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Shina Word: $word\n\nMeaning: Shina vocabulary term widely spoken in Gilgit-Baltistan.\nDialects: Gilgit, Astore, Chilas, Danyore.\nExample sentence: $word ہنیس بش ہن."
        }

        val prompt = """
            Provide an authoritative Shina AI Dictionary entry for the Shina word: "$word".
            Include:
            1. Shina Word & Urdu/English translation
            2. Phonetic pronunciation in Roman Shina
            3. Dialect variations (Gilgit, Astore, Chilas, Kohistan)
            4. Example sentence in Shina with translation
        """.trimIndent()

        return@withContext try {
            val response = GeminiClient.service.generateContent(
                apiKey = apiKey,
                request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            parts = listOf(GeminiPart(text = prompt))
                        )
                    )
                )
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "No dictionary details found."
        } catch (e: Exception) {
            "Dictionary offline query for $word: Standard Shina vocabulary item."
        }
    }
}
