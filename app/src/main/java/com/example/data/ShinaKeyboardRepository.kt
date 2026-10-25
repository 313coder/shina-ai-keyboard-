package com.example.data

import com.example.model.LayoutDefinitions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ShinaKeyboardRepository(
    private val database: ShinaDatabase
) {
    val wordDao = database.shinaWordDao()
    val ngramDao = database.ngramDao()
    val clipboardDao = database.clipboardDao()
    val voiceDao = database.voiceContributionDao()
    val communityDao = database.communityWordDao()
    val learnedPhraseDao = database.learnedPhraseDao()
    val trainingDao = database.trainingDataDao()
    val dictionaryDao = database.dictionaryDao()
    val historicalLearningDao = database.historicalLearningDao()
    val storageManager = ShinaStorageManager(database)

    init {
        // Seed initial Shina vocabulary and baseline linguistic corpus asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            val count = wordDao.getTotalWordCount().first()
            if (count == 0) {
                for (word in LayoutDefinitions.INITIAL_SHINA_VOCABULARY) {
                    wordDao.insertOrUpdateWord(
                        ShinaWordEntity(
                            word = word,
                            normalizedWord = word,
                            frequency = 10,
                            dialect = "Gilgit",
                            isApproved = true
                        )
                    )
                }
            }

            // Seed initial Shina dictionary if empty
            val dictCount = dictionaryDao.getTotalDictionaryCount().first()
            if (dictCount == 0) {
                dictionaryDao.insertEntries(DictionarySeedData.INITIAL_ENTRIES)
            }

            // Seed initial Shina sentences if empty
            val sentenceCount = trainingDao.getTotalSentenceCount().first()
            if (sentenceCount == 0) {
                seedInitialSentences()
            }

            // Seed initial Shina grammar rules if empty
            val grammarCount = trainingDao.getTotalGrammarRuleCount().first()
            if (grammarCount == 0) {
                seedInitialGrammarRules()
            }

            // Seed initial Shina poetry if empty
            val poetryCount = trainingDao.getTotalPoetryCount().first()
            if (poetryCount == 0) {
                seedInitialPoetry()
            }

            // Seed initial Shina dialogues if empty
            val dialogueCount = trainingDao.getTotalDialogueCount().first()
            if (dialogueCount == 0) {
                seedInitialDialogues()
            }

            // Seed initial Shina conversions if empty
            val conversionCount = trainingDao.getTotalConversionCount().first()
            if (conversionCount == 0) {
                seedInitialConversions()
            }
        }
    }

    fun getSuggestions(prefix: String): Flow<List<ShinaWordEntity>> {
        return wordDao.getSuggestions(prefix, limit = 5)
    }

    fun getNextWordPredictions(prevWord: String): Flow<List<String>> {
        return ngramDao.getNextWordPredictions(prevWord, limit = 3)
    }

    val allClips: Flow<List<ClipboardEntity>> = clipboardDao.getAllClips()

    suspend fun addClip(text: String) {
        if (text.isNotBlank()) {
            clipboardDao.insertClip(ClipboardEntity(text = text))
        }
    }

    suspend fun deleteClip(id: Long) = clipboardDao.deleteClip(id)

    suspend fun setPinnedClip(id: Long, isPinned: Boolean) = clipboardDao.setPinned(id, isPinned)

    suspend fun addVoiceContribution(path: String, text: String, dialect: String) {
        voiceDao.insertVoiceContribution(
            VoiceContributionEntity(
                audioPath = path,
                transcribedText = text,
                dialect = dialect
            )
        )
    }

    suspend fun addCommunityWord(word: String, dialect: String, contributor: String) {
        communityDao.insertCommunityWord(
            CommunityWordEntity(
                word = word,
                dialect = dialect,
                contributorName = contributor
            )
        )
    }

    val pendingCommunityWords: Flow<List<CommunityWordEntity>> = communityDao.getAllCommunityWords()

    suspend fun approveCommunityWord(id: Long, word: String, dialect: String) {
        communityDao.updateStatus(id, "Approved")
        wordDao.insertOrUpdateWord(
            ShinaWordEntity(
                word = word,
                normalizedWord = word,
                frequency = 5,
                dialect = dialect,
                isApproved = true
            )
        )
    }

    suspend fun rejectCommunityWord(id: Long) {
        communityDao.updateStatus(id, "Rejected")
    }

    val totalWordsCount: Flow<Int> = wordDao.getTotalWordCount()
    val userAddedWordsCount: Flow<Int> = wordDao.getUserAddedWordCount()
    val userAddedWords: Flow<List<ShinaWordEntity>> = wordDao.getUserAddedWords()
    val approvedCommunityWordsCount: Flow<Int> = communityDao.getApprovedCommunityWordCount()
    val totalCommunityWordsCount: Flow<Int> = communityDao.getTotalCommunityWordCount()
    val totalNgramsCount: Flow<Int> = ngramDao.getTotalNgrams()
    val totalVoiceCount: Flow<Int> = voiceDao.getTotalVoiceCount()
    val totalLearnedCount: Flow<Int> = learnedPhraseDao.getTotalLearnedCount()

    val topWords: Flow<List<ShinaWordEntity>> = wordDao.getTopWords()
    val topLearnedPhrases: Flow<List<LearnedPhraseEntity>> = learnedPhraseDao.getTopLearnedPhrases()
    val allVoiceContributions: Flow<List<VoiceContributionEntity>> = voiceDao.getAllVoiceContributions()

    suspend fun addDictionaryWord(word: String, dialect: String = "Gilgit") {
        if (word.isNotBlank()) {
            wordDao.insertOrUpdateWord(
                ShinaWordEntity(
                    word = word.trim(),
                    normalizedWord = word.trim().lowercase(),
                    frequency = 10,
                    dialect = dialect,
                    isApproved = true,
                    isUserAdded = true
                )
            )
        }
    }

    // --- Rich Offline Searchable Dictionary ---
    fun getAllDictionaryEntries(): Flow<List<ShinaDictionaryEntryEntity>> = dictionaryDao.getAllEntries()

    fun searchDictionary(query: String): Flow<List<ShinaDictionaryEntryEntity>> =
        if (query.isBlank()) dictionaryDao.getAllEntries() else dictionaryDao.searchEntries(query.trim())

    fun searchFilteredDictionary(query: String, dialect: String, partOfSpeech: String): Flow<List<ShinaDictionaryEntryEntity>> =
        dictionaryDao.searchFilteredEntries(query.trim(), dialect, partOfSpeech)

    val totalDictionaryCount: Flow<Int> = dictionaryDao.getTotalDictionaryCount()
    val userContributedDictionaryCount: Flow<Int> = dictionaryDao.getUserContributedCount()
    val userContributedDictionaryEntries: Flow<List<ShinaDictionaryEntryEntity>> = dictionaryDao.getUserContributedEntries()

    suspend fun getDictionaryEntryByWord(word: String): ShinaDictionaryEntryEntity? =
        dictionaryDao.getEntryByWord(word.trim())

    suspend fun addDictionaryEntry(
        wordShina: String,
        romanized: String = "",
        partOfSpeech: String = "Noun",
        definitionUrdu: String,
        definitionEnglish: String = "",
        synonyms: String = "",
        exampleSentenceShina: String = "",
        exampleTranslation: String = "",
        dialect: String = "Gilgit",
        contributorName: String = "You (Offline Contributor)"
    ): Long {
        val entry = ShinaDictionaryEntryEntity(
            wordShina = wordShina.trim(),
            romanized = romanized.trim(),
            partOfSpeech = partOfSpeech,
            definitionUrdu = definitionUrdu.trim(),
            definitionEnglish = definitionEnglish.trim(),
            synonyms = synonyms.trim(),
            exampleSentenceShina = exampleSentenceShina.trim(),
            exampleTranslation = exampleTranslation.trim(),
            dialect = dialect,
            contributorName = contributorName,
            isUserContributed = true,
            isVerified = true
        )
        val id = dictionaryDao.insertEntry(entry)

        // Also register into keyboard vocabulary
        wordDao.insertOrUpdateWord(
            ShinaWordEntity(
                word = wordShina.trim(),
                normalizedWord = wordShina.trim().lowercase(),
                frequency = 12,
                dialect = dialect,
                isApproved = true,
                isUserAdded = true
            )
        )

        // Log auto-training event
        trainingDao.insertAutoTrainingLog(
            ShinaAutoTrainingLogEntity(
                source = "Dictionary Contribution",
                inputSnippet = "$wordShina: $definitionUrdu",
                tokensExtracted = 1,
                ngramsLearned = if (synonyms.isNotBlank()) synonyms.split(",").size else 1,
                dialect = dialect
            )
        )

        return id
    }

    suspend fun deleteDictionaryEntry(id: Long) = dictionaryDao.deleteEntry(id)

    suspend fun deleteWordByString(word: String) = wordDao.deleteWord(word)
    suspend fun deleteVoiceContribution(id: Long) = voiceDao.deleteVoiceContribution(id)
    suspend fun deleteCommunityWord(id: Long) = communityDao.deleteCommunityWord(id)
    suspend fun deleteLearnedPhrase(id: Long) = learnedPhraseDao.deleteLearnedPhrase(id)
    suspend fun clearAllLearnedPhrases() = learnedPhraseDao.clearAllLearnedPhrases()

    // --- Storage & Permanent Persistence Management ---
    suspend fun getStorageStats(context: android.content.Context) = storageManager.getStorageStats(context)
    suspend fun verifyDatabaseIntegrity() = storageManager.verifyDatabaseIntegrity()
    suspend fun compactAndVacuumDatabase() = storageManager.compactAndVacuumDatabase()
    suspend fun createBackup(context: android.content.Context, note: String = "") = storageManager.createBackup(context, note)
    suspend fun restoreFromBackupJson(json: String) = storageManager.restoreFromBackupJson(json)
    suspend fun exportLinguisticDataset(context: android.content.Context) = storageManager.exportContributedLinguisticDataset(context)
    suspend fun importLinguisticDataset(context: android.content.Context, jsonString: String) = storageManager.importLinguisticDataset(context, jsonString)
    suspend fun clearTemporaryCache(context: android.content.Context) = storageManager.clearTemporaryCache(context)
    fun getAvailableBackups(context: android.content.Context) = storageManager.getAvailableBackups(context)

    private var _backupManager: ShinaFirestoreBackupManager? = null
    fun getBackupManager(context: android.content.Context): ShinaFirestoreBackupManager {
        if (_backupManager == null) {
            _backupManager = ShinaFirestoreBackupManager(context.applicationContext, this)
        }
        return _backupManager!!
    }

    fun getLearnedPhrases(prefix: String): Flow<List<String>> {
        return learnedPhraseDao.getLearnedPhraseSuggestions(prefix, limit = 5)
    }

    /**
     * On-Device Learning: Observes common user words and phrases typed by the user,
     * storing them into the local Room database to improve autocorrect and suggestions over time.
     */
    suspend fun observeAndLearnFromUserText(
        text: String,
        dialect: String = "Gilgit",
        disableLearning: Boolean = false
    ) {
        if (disableLearning || text.isBlank()) return

        val words = text.trim().split(Regex("\\s+")).filter { it.length >= 2 }
        if (words.isEmpty()) return

        // 1. Learn individual words
        for (w in words) {
            val normalized = w.lowercase()
            val existing = wordDao.getWord(w)
            if (existing != null) {
                wordDao.incrementFrequency(w)
            } else {
                wordDao.insertOrUpdateWord(
                    ShinaWordEntity(
                        word = w,
                        normalizedWord = normalized,
                        frequency = 1,
                        dialect = dialect,
                        isApproved = true,
                        isUserAdded = true
                    )
                )
            }
        }

        // 2. Learn N-Grams (consecutive word pairs)
        for (i in 0 until words.size - 1) {
            val w1 = words[i]
            val w2 = words[i + 1]
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

        // 3. Learn multi-word custom phrases (2-4 words)
        if (words.size in 2..4) {
            val phrase = words.joinToString(" ")
            val existingPhrase = learnedPhraseDao.getPhrase(phrase)
            if (existingPhrase != null) {
                learnedPhraseDao.incrementPhraseFrequency(phrase)
            } else {
                learnedPhraseDao.insertOrUpdatePhrase(
                    LearnedPhraseEntity(
                        phrase = phrase,
                        frequency = 1,
                        dialect = dialect
                    )
                )
            }
        }

        // 4. Log auto-training event from keyboard typing
        logAutoTraining(
            source = "Keyboard Typing",
            snippet = text.trim(),
            tokens = words.size,
            ngrams = (words.size - 1).coerceAtLeast(0),
            dialect = dialect
        )
    }

    // --- Training Datasets Flows and Methods ---
    val allSentences: Flow<List<ShinaSentenceEntity>> = trainingDao.getAllSentences()
    val totalSentencesCount: Flow<Int> = trainingDao.getTotalSentenceCount()
    val verifiedSentencesCount: Flow<Int> = trainingDao.getVerifiedSentenceCount()
    val pendingSentencesCount: Flow<Int> = trainingDao.getPendingSentenceCount()
    val pendingSentences: Flow<List<ShinaSentenceEntity>> = trainingDao.getPendingSentences()
    val verifiedSentences: Flow<List<ShinaSentenceEntity>> = trainingDao.getVerifiedSentences()
    suspend fun addSentence(sentence: ShinaSentenceEntity) = trainingDao.insertSentence(sentence)
    suspend fun deleteSentence(id: Long) = trainingDao.deleteSentence(id)
    suspend fun verifySentence(id: Long) = trainingDao.setSentenceVerified(id, true)
    suspend fun unverifySentence(id: Long) = trainingDao.setSentenceVerified(id, false)

    val allConversions: Flow<List<ShinaConversionEntity>> = trainingDao.getAllConversions()
    val totalConversionsCount: Flow<Int> = trainingDao.getTotalConversionCount()
    suspend fun addConversion(conversion: ShinaConversionEntity) = trainingDao.insertConversion(conversion)
    suspend fun deleteConversion(id: Long) = trainingDao.deleteConversion(id)

    val allGrammarRules: Flow<List<ShinaGrammarRuleEntity>> = trainingDao.getAllGrammarRules()
    val totalGrammarRulesCount: Flow<Int> = trainingDao.getTotalGrammarRuleCount()
    suspend fun addGrammarRule(rule: ShinaGrammarRuleEntity) = trainingDao.insertGrammarRule(rule)
    suspend fun deleteGrammarRule(id: Long) = trainingDao.deleteGrammarRule(id)

    val allPoetry: Flow<List<ShinaPoetryEntity>> = trainingDao.getAllPoetry()
    val totalPoetryCount: Flow<Int> = trainingDao.getTotalPoetryCount()
    suspend fun addPoetry(poetry: ShinaPoetryEntity) = trainingDao.insertPoetry(poetry)
    suspend fun deletePoetry(id: Long) = trainingDao.deletePoetry(id)

    val allDialogues: Flow<List<ShinaDialogueEntity>> = trainingDao.getAllDialogues()
    val totalDialoguesCount: Flow<Int> = trainingDao.getTotalDialogueCount()
    suspend fun addDialogue(dialogue: ShinaDialogueEntity) = trainingDao.insertDialogue(dialogue)
    suspend fun deleteDialogue(id: Long) = trainingDao.deleteDialogue(id)

    val allMediaCorpus: Flow<List<ShinaMediaCorpusEntity>> = trainingDao.getAllMediaCorpus()
    val totalMediaCorpusCount: Flow<Int> = trainingDao.getTotalMediaCount()
    suspend fun addMediaCorpus(media: ShinaMediaCorpusEntity) = trainingDao.insertMediaCorpus(media)
    suspend fun deleteMediaCorpus(id: Long) = trainingDao.deleteMediaCorpus(id)

    val latestTrainingMetric: Flow<ShinaModelTrainingMetricEntity?> = trainingDao.getLatestTrainingMetric()
    val recentTrainingMetrics: Flow<List<ShinaModelTrainingMetricEntity>> = trainingDao.getRecentTrainingMetrics()
    val recentAutoTrainingLogs: Flow<List<ShinaAutoTrainingLogEntity>> = trainingDao.getRecentAutoTrainingLogs()
    val totalAutoTrainingEventsCount: Flow<Int> = trainingDao.getTotalAutoTrainingEventsCount()

    suspend fun logAutoTraining(source: String, snippet: String, tokens: Int, ngrams: Int, dialect: String) {
        trainingDao.insertAutoTrainingLog(
            ShinaAutoTrainingLogEntity(
                source = source,
                inputSnippet = snippet.take(120),
                tokensExtracted = tokens,
                ngramsLearned = ngrams,
                dialect = dialect
            )
        )
    }

    private suspend fun seedInitialSentences() {
        val initial = listOf(
            ShinaSentenceEntity(
                sentenceShina = "شینا زباں مئے ماتے زباں ہن۔",
                translationUrdu = "شینا زبان ہماری مادری زبان ہے۔",
                translationEnglish = "Shina language is our beloved mother tongue.",
                dialect = "Gilgit",
                category = "General"
            ),
            ShinaSentenceEntity(
                sentenceShina = "سلام ورے! تو کشے ہنوس؟",
                translationUrdu = "سلام بھائی! آپ کیسے ہیں؟",
                translationEnglish = "Hello brother! How are you?",
                dialect = "Gilgit",
                category = "Greetings"
            ),
            ShinaSentenceEntity(
                sentenceShina = "مئے نام علی ہن اور مئے گلگت مئے ہنس۔",
                translationUrdu = "میرا نام علی ہے اور میں گلگت میں رہتا ہوں۔",
                translationEnglish = "My name is Ali and I live in Gilgit.",
                dialect = "Gilgit",
                category = "Daily Life"
            ),
            ShinaSentenceEntity(
                sentenceShina = "آستورئے رین مئیار بش ہن۔",
                translationUrdu = "آستور کی وادیاں بہت خوبصورت ہیں۔",
                translationEnglish = "The meadows of Astore are exceptionally beautiful.",
                dialect = "Astore",
                category = "Folklore",
                isVerified = true
            ),
            ShinaSentenceEntity(
                sentenceShina = "کچی جاجو گنیکے چھیل شوتھو مے۔",
                translationUrdu = "اتحاد و اتفاق میں سب سے بڑی طاقت ہے۔",
                translationEnglish = "Unity brings strength over mountain streams.",
                dialect = "Diamer/Chilas",
                category = "Proverbs",
                isVerified = true
            ),
            // Unverified candidate sentences ready for user verification
            ShinaSentenceEntity(
                sentenceShina = "توئے ڈیو کے شو ہن؟",
                translationUrdu = "کیا آپ کی آنکھیں ٹھیک ہیں؟",
                translationEnglish = "Are your eyes feeling better?",
                dialect = "Gilgit",
                category = "Health & Well-being",
                isVerified = false
            ),
            ShinaSentenceEntity(
                sentenceShina = "مئے چِھل پیمپٹ پیلوس۔",
                translationUrdu = "میں نے تازہ پانی پیا۔",
                translationEnglish = "I drank fresh mountain spring water.",
                dialect = "Astore",
                category = "Daily Life",
                isVerified = false
            ),
            ShinaSentenceEntity(
                sentenceShina = "گلگتئیے مئیار بازارر نئیں چیزے ملن۔",
                translationUrdu = "گلگت کے خوبصورت بازار میں نئی اشیاء دستیاب ہیں۔",
                translationEnglish = "New items are available in Gilgit's lovely bazaar.",
                dialect = "Gilgit",
                category = "Market",
                isVerified = false
            )
        )
        for (s in initial) {
            trainingDao.insertSentence(s)
        }
    }

    private suspend fun seedInitialGrammarRules() {
        val rules = listOf(
            ShinaGrammarRuleEntity(
                ruleName = "Ergative Case Suffix (-e / -i)",
                ruleCategory = "Ergative Case",
                patternExample = "راجائے رئیو (Raja-e reyo) = راجہ نے کہا",
                explanation = "In transitive past tense sentences, the subject takes the ergative agent suffix '-e' (masculine) or '-i' (feminine).",
                dialect = "Gilgit"
            ),
            ShinaGrammarRuleEntity(
                ruleName = "Retroflex Consonants (ݜ, ݗ, ڇ, ڙ, ݨ, ݪ)",
                ruleCategory = "Retroflex Phonology",
                patternExample = "ݜینا (Ṣhina), ݗو (Kho / Eating), ڇیل (Chil / Water)",
                explanation = "Shina possesses unique retroflex sibilants and affricates distinguished by unique dot annotations in the Arabic-Shina script.",
                dialect = "All Dialects"
            ),
            ShinaGrammarRuleEntity(
                ruleName = "SOV Word Order (Subject - Object - Verb)",
                ruleCategory = "Syntax",
                patternExample = "مئے (S) چا (O) پیلوس (V) = میں نے چائے پی",
                explanation = "Standard Shina declarative sentences strictly place the verb at the sentence terminal boundary.",
                dialect = "Gilgit"
            ),
            ShinaGrammarRuleEntity(
                ruleName = "Past Tense Gender Agreement",
                ruleCategory = "Verb Tense",
                patternExample = "مئے بئے گاو (he went) vs مئے بئے گئی (she went)",
                explanation = "Verbal predicates inflect for masculine singular (-o/-u) and feminine singular (-i).",
                dialect = "Astore"
            )
        )
        for (r in rules) {
            trainingDao.insertGrammarRule(r)
        }
    }

    private suspend fun seedInitialPoetry() {
        val poetryList = listOf(
            ShinaPoetryEntity(
                title = "شینا زبانہِ شان (The Glory of Shina)",
                poetName = "استاد شینا (Traditional Bard)",
                poetryType = "Bait (بیت)",
                versesShina = "گلگتئے ڈؤن چِہ آستورئے رین\nشینا زبانہِ میلا بش ہن\nہنیس گلی چھلی بے مئیار\nکامئیے شین بائے، براداریئیے جاں",
                translationUrduEnglish = "گلگت کی پہاڑیوں سے آستور کے سبزہ زاروں تک، شینا زبان کا میٹھا بول گونجتا ہے۔",
                dialect = "Gilgit"
            ),
            ShinaPoetryEntity(
                title = "چلاسئے ہوائیں (Breeze of Chilas)",
                poetName = "فولکلور چلاس",
                poetryType = "Maye (مئے)",
                versesShina = "دیامرئے سندھو چھل چھل گائے\nشینا دلاں مئے موتی لٹائے\nورے مئے جاں، مئے شینا وطن",
                translationUrduEnglish = "دریائے سندھ کے کنارے گونجتے ہوئے نغمے، شینا دلوں کے جذبات۔",
                dialect = "Diamer/Chilas"
            )
        )
        for (p in poetryList) {
            trainingDao.insertPoetry(p)
        }
    }

    private suspend fun seedInitialDialogues() {
        val dialogues = listOf(
            ShinaDialogueEntity(
                dialogueTitle = "Traditional Mountain Bazaar Greeting",
                scenario = "Mountain Bazaar",
                speakerAText = "سلام ورے! توئے حال چال کشے ہنو؟ (Peace brother! How is your health and wellbeing?)",
                speakerBReply = "وعلیکم السلام! اللہ پاکے کرم ہن، تو کشے ہنوس؟ بال بچے شُو ہن؟ (Peace! By God's grace, all is well. How are you and your family?)",
                dialect = "Gilgit"
            ),
            ShinaDialogueEntity(
                dialogueTitle = "Ordering Local Tea & Food",
                scenario = "Tea House / Hotel",
                speakerAText = "ورے! ایک کپ چا اور گرم روئی بے دیو۔ (Brother! Give us a cup of tea and hot bread.)",
                speakerBReply = "جی بائے، فوراً ہانیر شُو چا گھنتے اوتھوس۔ (Yes brother, immediately bringing delicious tea.)",
                dialect = "Gilgit"
            )
        )
        for (d in dialogues) {
            trainingDao.insertDialogue(d)
        }
    }

    private suspend fun seedInitialConversions() {
        val conversions = listOf(
            ShinaConversionEntity(
                sourceText = "آپ کا نام کیا ہے؟",
                sourceLanguage = "Urdu",
                targetShinaText = "توئے نام کی ہن؟",
                conversionType = "Translation",
                dialect = "Gilgit"
            ),
            ShinaConversionEntity(
                sourceText = "Where are you going?",
                sourceLanguage = "English",
                targetShinaText = "تو کانے گاسوس؟",
                conversionType = "Translation",
                dialect = "Gilgit"
            ),
            ShinaConversionEntity(
                sourceText = "Tu kacho hano?",
                sourceLanguage = "Roman Shina",
                targetShinaText = "تو کیچو ہنو؟",
                conversionType = "Roman-to-Arabic-Script",
                dialect = "Gilgit"
            )
        )
        for (c in conversions) {
            trainingDao.insertConversion(c)
        }
    }

    // ==========================================
    // Shina Dictionary CRUD Operations
    // ==========================================

    suspend fun insertDictionaryEntry(entry: ShinaDictionaryEntryEntity): Long {
        return dictionaryDao.insertEntry(entry)
    }

    suspend fun insertDictionaryEntries(entries: List<ShinaDictionaryEntryEntity>) {
        dictionaryDao.insertEntries(entries)
    }

    suspend fun updateDictionaryEntry(entry: ShinaDictionaryEntryEntity) {
        dictionaryDao.updateEntry(entry)
    }

    // ==========================================
    // Historical Learning Data CRUD Operations
    // ==========================================

    suspend fun logHistoricalLearning(
        source: String,
        snippet: String,
        tokensExtracted: Int,
        ngramsLearned: Int,
        dialect: String = "Gilgit"
    ): Long {
        val log = ShinaAutoTrainingLogEntity(
            source = source,
            inputSnippet = snippet.take(100),
            tokensExtracted = tokensExtracted,
            ngramsLearned = ngramsLearned,
            dialect = dialect,
            timestamp = System.currentTimeMillis()
        )
        return historicalLearningDao.insertLog(log)
    }

    fun getAllHistoricalLogs(): Flow<List<ShinaAutoTrainingLogEntity>> {
        return historicalLearningDao.getAllHistoricalLogs()
    }

    suspend fun deleteHistoricalLog(id: Long) {
        historicalLearningDao.deleteLog(id)
    }

    suspend fun clearHistoricalLogs() {
        historicalLearningDao.clearAllHistoricalLogs()
    }

    suspend fun deleteHistoricalLogsOlderThan(cutoffTimestamp: Long): Int {
        return historicalLearningDao.deleteLogsOlderThan(cutoffTimestamp)
    }

    suspend fun recordTrainingMetric(
        modelVersionTag: String,
        accuracyPercent: Float,
        totalWordsTrained: Int,
        totalSentencesTrained: Int,
        loss: Float = 0.12f
    ): Long {
        val metric = ShinaModelTrainingMetricEntity(
            modelVersionTag = modelVersionTag,
            accuracyPercent = accuracyPercent,
            totalWordsTrained = totalWordsTrained,
            totalSentencesTrained = totalSentencesTrained,
            trainingLoss = loss,
            cycleTimestamp = System.currentTimeMillis()
        )
        return historicalLearningDao.insertMetric(metric)
    }

    fun getHistoricalMetrics(): Flow<List<ShinaModelTrainingMetricEntity>> {
        return historicalLearningDao.getAllHistoricalMetrics()
    }
}

