package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class StorageStats(
    val totalDbSizeBytes: Long,
    val walSizeBytes: Long,
    val totalCacheSizeBytes: Long,
    val wordsCount: Int,
    val userAddedWordsCount: Int,
    val dictionaryCount: Int,
    val userDictionaryCount: Int,
    val sentencesCount: Int,
    val verifiedSentencesCount: Int,
    val ngramsCount: Int,
    val phrasesCount: Int,
    val communityCount: Int,
    val voiceCount: Int,
    val clipsCount: Int,
    val trainingLogsCount: Int,
    val databasePath: String,
    val isWalEnabled: Boolean,
    val schemaVersion: Int,
    val lastBackupTimestamp: Long?
) {
    fun formattedTotalSize(): String = formatBytes(totalDbSizeBytes + walSizeBytes)
    fun formattedDbSize(): String = formatBytes(totalDbSizeBytes)
    fun formattedWalSize(): String = formatBytes(walSizeBytes)
    fun formattedCacheSize(): String = formatBytes(totalCacheSizeBytes)

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format(Locale.US, "%.1f KB", bytes / 1024.0)
            else -> String.format(Locale.US, "%.2f MB", bytes / (1024.0 * 1024.0))
        }
    }
}

data class BackupResult(
    val success: Boolean,
    val filePath: String,
    val fileSizeFormatted: String,
    val itemsCount: Int,
    val message: String
)

data class RestoreResult(
    val success: Boolean,
    val wordsRestored: Int,
    val dictionaryRestored: Int,
    val sentencesRestored: Int,
    val phrasesRestored: Int,
    val message: String
)

data class DatasetExportResult(
    val success: Boolean,
    val filePath: String,
    val wordsExported: Int,
    val dictionaryEntriesExported: Int,
    val grammarRulesExported: Int,
    val sentencesExported: Int,
    val fileSizeFormatted: String,
    val jsonContent: String,
    val message: String
)

data class DatasetImportResult(
    val success: Boolean,
    val wordsImported: Int,
    val dictionaryEntriesImported: Int,
    val grammarRulesImported: Int,
    val sentencesImported: Int,
    val tokensExtracted: Int,
    val message: String
)

class ShinaStorageManager(
    private val database: ShinaDatabase
) {
    private val TAG = "ShinaStorageManager"
    private val PREFS_NAME = "shina_storage_meta"
    private val KEY_LAST_BACKUP = "last_backup_timestamp"

    suspend fun getStorageStats(context: Context): StorageStats = withContext(Dispatchers.IO) {
        val dbFile = context.getDatabasePath("shina_keyboard_db")
        val walFile = context.getDatabasePath("shina_keyboard_db-wal")
        val cacheDir = context.cacheDir

        val totalDbSize = if (dbFile.exists()) dbFile.length() else 0L
        val walSize = if (walFile.exists()) walFile.length() else 0L
        val cacheSize = calculateDirSize(cacheDir)

        val words = try { database.shinaWordDao().getTotalWordCount().first() } catch (e: Exception) { 0 }
        val userWords = try { database.shinaWordDao().getUserAddedWordCount().first() } catch (e: Exception) { 0 }
        val dict = try { database.dictionaryDao().getTotalDictionaryCount().first() } catch (e: Exception) { 0 }
        val userDict = try { database.dictionaryDao().getUserContributedCount().first() } catch (e: Exception) { 0 }
        val sentences = try { database.trainingDataDao().getTotalSentenceCount().first() } catch (e: Exception) { 0 }
        val verifiedSentences = try { database.trainingDataDao().getVerifiedSentenceCount().first() } catch (e: Exception) { 0 }
        val ngrams = try { database.ngramDao().getTotalNgrams().first() } catch (e: Exception) { 0 }
        val phrases = try { database.learnedPhraseDao().getTotalLearnedCount().first() } catch (e: Exception) { 0 }
        val community = try { database.communityWordDao().getTotalCommunityWordCount().first() } catch (e: Exception) { 0 }
        val voice = try { database.voiceContributionDao().getTotalVoiceCount().first() } catch (e: Exception) { 0 }
        val clips = try { database.clipboardDao().getAllClips().first().size } catch (e: Exception) { 0 }
        val trainingLogs = try { database.trainingDataDao().getTotalAutoTrainingEventsCount().first() } catch (e: Exception) { 0 }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastBackup = if (prefs.contains(KEY_LAST_BACKUP)) prefs.getLong(KEY_LAST_BACKUP, 0L) else null

        StorageStats(
            totalDbSizeBytes = totalDbSize,
            walSizeBytes = walSize,
            totalCacheSizeBytes = cacheSize,
            wordsCount = words,
            userAddedWordsCount = userWords,
            dictionaryCount = dict,
            userDictionaryCount = userDict,
            sentencesCount = sentences,
            verifiedSentencesCount = verifiedSentences,
            ngramsCount = ngrams,
            phrasesCount = phrases,
            communityCount = community,
            voiceCount = voice,
            clipsCount = clips,
            trainingLogsCount = trainingLogs,
            databasePath = dbFile.absolutePath,
            isWalEnabled = walFile.exists(),
            schemaVersion = 5,
            lastBackupTimestamp = lastBackup
        )
    }

    suspend fun verifyDatabaseIntegrity(): String = withContext(Dispatchers.IO) {
        try {
            val cursor = database.openHelper.writableDatabase.query("PRAGMA integrity_check")
            var result = "ok"
            if (cursor.moveToFirst()) {
                result = cursor.getString(0)
            }
            cursor.close()
            result
        } catch (e: Exception) {
            Log.e(TAG, "Integrity check failed", e)
            "Error: ${e.localizedMessage ?: "Unknown SQLite check error"}"
        }
    }

    suspend fun compactAndVacuumDatabase(): Boolean = withContext(Dispatchers.IO) {
        try {
            database.openHelper.writableDatabase.execSQL("PRAGMA wal_checkpoint(FULL)")
            database.openHelper.writableDatabase.execSQL("VACUUM")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Database compaction failed", e)
            false
        }
    }

    suspend fun createBackup(context: Context, note: String = ""): BackupResult = withContext(Dispatchers.IO) {
        try {
            val backupRoot = JSONObject()
            val now = System.currentTimeMillis()
            val dateStr = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US).format(Date(now))

            backupRoot.put("app", "Shina AI Keyboard")
            backupRoot.put("version", "1.2.0")
            backupRoot.put("schemaVersion", 5)
            backupRoot.put("timestamp", now)
            backupRoot.put("dateFormatted", dateStr)
            backupRoot.put("note", note)

            // 1. User Added Words
            val userWordsList = database.shinaWordDao().getUserAddedWords().first()
            val wordsArray = JSONArray()
            for (w in userWordsList) {
                val obj = JSONObject()
                obj.put("word", w.word)
                obj.put("normalizedWord", w.normalizedWord)
                obj.put("frequency", w.frequency)
                obj.put("dialect", w.dialect)
                obj.put("lastUsedTimestamp", w.lastUsedTimestamp)
                wordsArray.put(obj)
            }
            backupRoot.put("user_words", wordsArray)

            // 2. User Contributed & All Dictionary Entries
            val dictEntries = database.dictionaryDao().getAllEntries().first()
            val dictArray = JSONArray()
            for (d in dictEntries) {
                val obj = JSONObject()
                obj.put("wordShina", d.wordShina)
                obj.put("romanized", d.romanized)
                obj.put("partOfSpeech", d.partOfSpeech)
                obj.put("definitionUrdu", d.definitionUrdu)
                obj.put("definitionEnglish", d.definitionEnglish)
                obj.put("synonyms", d.synonyms)
                obj.put("exampleSentenceShina", d.exampleSentenceShina)
                obj.put("exampleTranslation", d.exampleTranslation)
                obj.put("dialect", d.dialect)
                obj.put("contributorName", d.contributorName)
                obj.put("isUserContributed", d.isUserContributed)
                obj.put("isVerified", d.isVerified)
                dictArray.put(obj)
            }
            backupRoot.put("dictionary_entries", dictArray)

            // 3. Shina Sentences
            val sentencesList = database.trainingDataDao().getAllSentences().first()
            val sentencesArray = JSONArray()
            for (s in sentencesList) {
                val obj = JSONObject()
                obj.put("sentenceShina", s.sentenceShina)
                obj.put("translationUrdu", s.translationUrdu)
                obj.put("translationEnglish", s.translationEnglish)
                obj.put("dialect", s.dialect)
                obj.put("category", s.category)
                obj.put("isVerified", s.isVerified)
                sentencesArray.put(obj)
            }
            backupRoot.put("sentences", sentencesArray)

            // 4. Learned Phrases
            val phrasesList = database.learnedPhraseDao().getTopLearnedPhrases().first()
            val phrasesArray = JSONArray()
            for (p in phrasesList) {
                val obj = JSONObject()
                obj.put("phrase", p.phrase)
                obj.put("frequency", p.frequency)
                obj.put("dialect", p.dialect)
                phrasesArray.put(obj)
            }
            backupRoot.put("learned_phrases", phrasesArray)

            // 5. Community Word Contributions
            val communityList = database.communityWordDao().getAllCommunityWords().first()
            val communityArray = JSONArray()
            for (c in communityList) {
                val obj = JSONObject()
                obj.put("word", c.word)
                obj.put("dialect", c.dialect)
                obj.put("contributorName", c.contributorName)
                obj.put("status", c.status)
                communityArray.put(obj)
            }
            backupRoot.put("community_contributions", communityArray)

            val totalItems = wordsArray.length() + dictArray.length() + sentencesArray.length() + phrasesArray.length() + communityArray.length()

            // Save to persistent backup directory
            val backupDir = File(context.filesDir, "backups")
            if (!backupDir.exists()) backupDir.mkdirs()

            val backupFile = File(backupDir, "shina_backup_$dateStr.json")
            backupFile.writeText(backupRoot.toString(2))

            // Also maintain rolling latest auto-backup
            val latestFile = File(backupDir, "shina_autobackup_latest.json")
            latestFile.writeText(backupRoot.toString(2))

            // Update metadata
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putLong(KEY_LAST_BACKUP, now)
                .apply()

            val sizeKb = String.format(Locale.US, "%.1f KB", backupFile.length() / 1024.0)

            BackupResult(
                success = true,
                filePath = backupFile.absolutePath,
                fileSizeFormatted = sizeKb,
                itemsCount = totalItems,
                message = "Backup saved successfully with $totalItems linguistic entries."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Backup creation failed", e)
            BackupResult(
                success = false,
                filePath = "",
                fileSizeFormatted = "0 KB",
                itemsCount = 0,
                message = "Backup failed: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    suspend fun restoreFromBackupJson(jsonString: String): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            var wordsRestored = 0
            var dictRestored = 0
            var sentencesRestored = 0
            var phrasesRestored = 0

            // 1. Restore User Words
            if (root.has("user_words")) {
                val array = root.getJSONArray("user_words")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val word = obj.getString("word")
                    val dialect = obj.optString("dialect", "Gilgit")
                    val freq = obj.optInt("frequency", 5)
                    database.shinaWordDao().insertOrUpdateWord(
                        ShinaWordEntity(
                            word = word,
                            normalizedWord = word.lowercase(),
                            frequency = freq,
                            dialect = dialect,
                            isApproved = true,
                            isUserAdded = true
                        )
                    )
                    wordsRestored++
                }
            }

            // 2. Restore Dictionary Entries
            if (root.has("dictionary_entries")) {
                val array = root.getJSONArray("dictionary_entries")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val word = obj.getString("wordShina")
                    val roman = obj.optString("romanized", "")
                    val pos = obj.optString("partOfSpeech", "Noun")
                    val urdu = obj.getString("definitionUrdu")
                    val eng = obj.optString("definitionEnglish", "")
                    val syns = obj.optString("synonyms", "")
                    val sent = obj.optString("exampleSentenceShina", "")
                    val trans = obj.optString("exampleTranslation", "")
                    val dialect = obj.optString("dialect", "Gilgit")
                    val contributor = obj.optString("contributorName", "Restored Archive")
                    val isUser = obj.optBoolean("isUserContributed", true)

                    database.dictionaryDao().insertEntry(
                        ShinaDictionaryEntryEntity(
                            wordShina = word,
                            romanized = roman,
                            partOfSpeech = pos,
                            definitionUrdu = urdu,
                            definitionEnglish = eng,
                            synonyms = syns,
                            exampleSentenceShina = sent,
                            exampleTranslation = trans,
                            dialect = dialect,
                            contributorName = contributor,
                            isUserContributed = isUser,
                            isVerified = true
                        )
                    )
                    dictRestored++
                }
            }

            // 3. Restore Sentences
            if (root.has("sentences")) {
                val array = root.getJSONArray("sentences")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val shina = obj.getString("sentenceShina")
                    val urdu = obj.optString("translationUrdu", "")
                    val eng = obj.optString("translationEnglish", "")
                    val dialect = obj.optString("dialect", "Gilgit")
                    val cat = obj.optString("category", "General")
                    val verified = obj.optBoolean("isVerified", true)

                    database.trainingDataDao().insertSentence(
                        ShinaSentenceEntity(
                            sentenceShina = shina,
                            translationUrdu = urdu,
                            translationEnglish = eng,
                            dialect = dialect,
                            category = cat,
                            isVerified = verified
                        )
                    )
                    sentencesRestored++
                }
            }

            // 4. Restore Learned Phrases
            if (root.has("learned_phrases")) {
                val array = root.getJSONArray("learned_phrases")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val phrase = obj.getString("phrase")
                    val freq = obj.optInt("frequency", 1)
                    val dialect = obj.optString("dialect", "Gilgit")

                    database.learnedPhraseDao().insertOrUpdatePhrase(
                        LearnedPhraseEntity(
                            phrase = phrase,
                            frequency = freq,
                            dialect = dialect
                        )
                    )
                    phrasesRestored++
                }
            }

            RestoreResult(
                success = true,
                wordsRestored = wordsRestored,
                dictionaryRestored = dictRestored,
                sentencesRestored = sentencesRestored,
                phrasesRestored = phrasesRestored,
                message = "Successfully restored $wordsRestored words, $dictRestored dictionary definitions, and $sentencesRestored sentences!"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Restore from JSON failed", e)
            RestoreResult(
                success = false,
                wordsRestored = 0,
                dictionaryRestored = 0,
                sentencesRestored = 0,
                phrasesRestored = 0,
                message = "Restore failed: ${e.localizedMessage ?: "Invalid backup file structure"}"
            )
        }
    }

    suspend fun clearTemporaryCache(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            // Clear unpinned clips
            database.clipboardDao().clearUnpinned()
            // Clear cache directory
            val cacheFiles = context.cacheDir.listFiles()
            if (cacheFiles != null) {
                for (f in cacheFiles) {
                    if (f.isFile) f.delete()
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Clearing cache failed", e)
            false
        }
    }

    fun getAvailableBackups(context: Context): List<File> {
        val backupDir = File(context.filesDir, "backups")
        if (!backupDir.exists()) return emptyList()
        return backupDir.listFiles { file ->
            file.extension.equals("json", ignoreCase = true)
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    private fun calculateDirSize(dir: File): Long {
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (f in files) {
            size += if (f.isDirectory) calculateDirSize(f) else f.length()
        }
        return size
    }

    // =========================================================================
    // CONTRIBUTED VOCABULARY & GRAMMAR DATASET EXPORT & IMPORT
    // =========================================================================

    suspend fun exportContributedLinguisticDataset(context: Context): DatasetExportResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject()
            val now = System.currentTimeMillis()
            val dateStr = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now))

            // 1. Metadata
            val meta = JSONObject()
            meta.put("title", "'SAIPBG' Shina Linguistic Contributed Dataset")
            meta.put("exportedAt", now)
            meta.put("exportedDateFormatted", SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date(now)))
            meta.put("appVersion", "1.0")
            meta.put("format", "shina_ai_json_dataset")
            root.put("metadata", meta)

            // 2. Contributed Vocabulary
            val userWords = database.shinaWordDao().getUserAddedWords().first()
            val vocabArray = JSONArray()
            for (w in userWords) {
                val obj = JSONObject()
                obj.put("word", w.word)
                obj.put("dialect", w.dialect)
                obj.put("frequency", w.frequency)
                vocabArray.put(obj)
            }
            root.put("vocabulary", vocabArray)

            // 3. User Contributed Dictionary Entries
            val dictEntries = database.dictionaryDao().getUserContributedEntries().first()
            val dictArray = JSONArray()
            for (d in dictEntries) {
                val obj = JSONObject()
                obj.put("wordShina", d.wordShina)
                obj.put("romanized", d.romanized)
                obj.put("partOfSpeech", d.partOfSpeech)
                obj.put("definitionUrdu", d.definitionUrdu)
                obj.put("definitionEnglish", d.definitionEnglish)
                obj.put("synonyms", d.synonyms)
                obj.put("exampleSentenceShina", d.exampleSentenceShina)
                obj.put("exampleTranslation", d.exampleTranslation)
                obj.put("dialect", d.dialect)
                obj.put("contributorName", d.contributorName)
                dictArray.put(obj)
            }
            root.put("dictionary_entries", dictArray)

            // 4. Grammar Rules
            val grammarList = database.trainingDataDao().getAllGrammarRules().first()
            val grammarArray = JSONArray()
            for (g in grammarList) {
                val obj = JSONObject()
                obj.put("ruleName", g.ruleName)
                obj.put("ruleCategory", g.ruleCategory)
                obj.put("patternExample", g.patternExample)
                obj.put("explanation", g.explanation)
                obj.put("dialect", g.dialect)
                grammarArray.put(obj)
            }
            root.put("grammar_rules", grammarArray)

            // 5. Sentences Dataset
            val sentenceList = database.trainingDataDao().getAllSentences().first()
            val sentenceArray = JSONArray()
            for (s in sentenceList) {
                val obj = JSONObject()
                obj.put("sentenceShina", s.sentenceShina)
                obj.put("translationUrdu", s.translationUrdu)
                obj.put("translationEnglish", s.translationEnglish)
                obj.put("dialect", s.dialect)
                obj.put("category", s.category)
                obj.put("isVerified", s.isVerified)
                sentenceArray.put(obj)
            }
            root.put("sentences", sentenceArray)

            val jsonString = root.toString(2)

            // Save to datasets directory
            val datasetsDir = File(context.filesDir, "datasets")
            if (!datasetsDir.exists()) datasetsDir.mkdirs()

            val exportFile = File(datasetsDir, "shina_dataset_$dateStr.json")
            exportFile.writeText(jsonString)

            val sizeKb = String.format(Locale.US, "%.1f KB", exportFile.length() / 1024.0)

            DatasetExportResult(
                success = true,
                filePath = exportFile.absolutePath,
                wordsExported = vocabArray.length(),
                dictionaryEntriesExported = dictArray.length(),
                grammarRulesExported = grammarArray.length(),
                sentencesExported = sentenceArray.length(),
                fileSizeFormatted = sizeKb,
                jsonContent = jsonString,
                message = "Dataset exported successfully with ${vocabArray.length()} vocabulary, ${dictArray.length()} dictionary entries, and ${grammarArray.length()} grammar rules."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Dataset export failed", e)
            DatasetExportResult(
                success = false,
                filePath = "",
                wordsExported = 0,
                dictionaryEntriesExported = 0,
                grammarRulesExported = 0,
                sentencesExported = 0,
                fileSizeFormatted = "0 KB",
                jsonContent = "",
                message = "Dataset export failed: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }

    suspend fun importLinguisticDataset(context: Context, jsonString: String): DatasetImportResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            var wordsCount = 0
            var dictCount = 0
            var grammarCount = 0
            var sentenceCount = 0
            var tokensCount = 0

            // 1. Import Vocabulary
            val vocabArray = when {
                root.has("vocabulary") -> root.getJSONArray("vocabulary")
                root.has("user_words") -> root.getJSONArray("user_words")
                else -> null
            }
            if (vocabArray != null) {
                for (i in 0 until vocabArray.length()) {
                    val obj = vocabArray.getJSONObject(i)
                    val word = obj.getString("word")
                    val dialect = obj.optString("dialect", "Gilgit")
                    val freq = obj.optInt("frequency", 10)
                    database.shinaWordDao().insertOrUpdateWord(
                        ShinaWordEntity(
                            word = word,
                            normalizedWord = word.lowercase(),
                            frequency = freq,
                            dialect = dialect,
                            isApproved = true,
                            isUserAdded = true
                        )
                    )
                    wordsCount++
                    tokensCount++
                }
            }

            // 2. Import Dictionary Entries
            val dictArray = when {
                root.has("dictionary_entries") -> root.getJSONArray("dictionary_entries")
                root.has("dictionary") -> root.getJSONArray("dictionary")
                else -> null
            }
            if (dictArray != null) {
                for (i in 0 until dictArray.length()) {
                    val obj = dictArray.getJSONObject(i)
                    val word = obj.getString("wordShina")
                    val roman = obj.optString("romanized", "")
                    val pos = obj.optString("partOfSpeech", "Noun")
                    val urdu = obj.getString("definitionUrdu")
                    val eng = obj.optString("definitionEnglish", "")
                    val syns = obj.optString("synonyms", "")
                    val sent = obj.optString("exampleSentenceShina", "")
                    val trans = obj.optString("exampleTranslation", "")
                    val dialect = obj.optString("dialect", "Gilgit")
                    val contributor = obj.optString("contributorName", "Imported Dataset")

                    database.dictionaryDao().insertEntry(
                        ShinaDictionaryEntryEntity(
                            wordShina = word,
                            romanized = roman,
                            partOfSpeech = pos,
                            definitionUrdu = urdu,
                            definitionEnglish = eng,
                            synonyms = syns,
                            exampleSentenceShina = sent,
                            exampleTranslation = trans,
                            dialect = dialect,
                            contributorName = contributor,
                            isUserContributed = true,
                            isVerified = true
                        )
                    )
                    dictCount++
                    tokensCount += 2
                }
            }

            // 3. Import Grammar Rules
            val grammarArray = when {
                root.has("grammar_rules") -> root.getJSONArray("grammar_rules")
                root.has("grammar") -> root.getJSONArray("grammar")
                else -> null
            }
            if (grammarArray != null) {
                for (i in 0 until grammarArray.length()) {
                    val obj = grammarArray.getJSONObject(i)
                    val ruleName = obj.getString("ruleName")
                    val category = obj.optString("ruleCategory", "Grammar")
                    val pattern = obj.getString("patternExample")
                    val explanation = obj.getString("explanation")
                    val dialect = obj.optString("dialect", "Gilgit")

                    database.trainingDataDao().insertGrammarRule(
                        ShinaGrammarRuleEntity(
                            ruleName = ruleName,
                            ruleCategory = category,
                            patternExample = pattern,
                            explanation = explanation,
                            dialect = dialect,
                            isTrained = false
                        )
                    )
                    grammarCount++
                    tokensCount += 5
                }
            }

            // 4. Import Sentences
            val sentenceArray = when {
                root.has("sentences") -> root.getJSONArray("sentences")
                root.has("sentence_corpus") -> root.getJSONArray("sentence_corpus")
                else -> null
            }
            if (sentenceArray != null) {
                for (i in 0 until sentenceArray.length()) {
                    val obj = sentenceArray.getJSONObject(i)
                    val shina = obj.getString("sentenceShina")
                    val urdu = obj.optString("translationUrdu", "")
                    val eng = obj.optString("translationEnglish", "")
                    val dialect = obj.optString("dialect", "Gilgit")
                    val cat = obj.optString("category", "General")
                    val verified = obj.optBoolean("isVerified", true)

                    database.trainingDataDao().insertSentence(
                        ShinaSentenceEntity(
                            sentenceShina = shina,
                            translationUrdu = urdu,
                            translationEnglish = eng,
                            dialect = dialect,
                            category = cat,
                            isTrained = false,
                            isVerified = verified
                        )
                    )
                    sentenceCount++
                    tokensCount += shina.split("\\s+".toRegex()).size
                }
            }

            // Auto-log training event so AI model incorporates new dataset immediately
            database.historicalLearningDao().insertLog(
                ShinaAutoTrainingLogEntity(
                    source = "Dataset Import",
                    inputSnippet = "Imported $wordsCount words, $dictCount dict entries, $grammarCount grammar rules",
                    tokensExtracted = tokensCount,
                    ngramsLearned = (wordsCount + sentenceCount).coerceAtLeast(1),
                    dialect = "Multi-Dialect",
                    timestamp = System.currentTimeMillis()
                )
            )

            DatasetImportResult(
                success = true,
                wordsImported = wordsCount,
                dictionaryEntriesImported = dictCount,
                grammarRulesImported = grammarCount,
                sentencesImported = sentenceCount,
                tokensExtracted = tokensCount,
                message = "Dataset imported! Added $wordsCount words, $dictCount dict entries, $grammarCount grammar rules, and $sentenceCount sentences."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Dataset import failed", e)
            DatasetImportResult(
                success = false,
                wordsImported = 0,
                dictionaryEntriesImported = 0,
                grammarRulesImported = 0,
                sentencesImported = 0,
                tokensExtracted = 0,
                message = "Dataset import failed: ${e.localizedMessage ?: "Invalid JSON format"}"
            )
        }
    }
}

