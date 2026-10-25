package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Shina Sentences Dataset: Stores authentic Shina sentences, context, and translations.
 */
@Entity(tableName = "shina_sentences")
data class ShinaSentenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sentenceShina: String,
    val translationUrdu: String = "",
    val translationEnglish: String = "",
    val dialect: String = "Gilgit", // Gilgit, Astore, Diamer/Chilas, Kohistani, Gurez/Dras
    val category: String = "General", // Greetings, Daily Life, Market, Folklore, Idiom
    val isTrained: Boolean = false,
    val isVerified: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Shina Conversions & Translations: Text conversion pairs across languages and scripts.
 */
@Entity(tableName = "shina_conversions")
data class ShinaConversionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sourceText: String,
    val sourceLanguage: String = "Urdu", // Urdu, English, Roman Shina, Balti, Burushaski
    val targetShinaText: String,
    val conversionType: String = "Translation", // Translation, Romanization, Dialect Adaptation
    val dialect: String = "Gilgit",
    val isTrained: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Shina Grammar Rules: Linguistic rules covering case markings, verb aspects, and phonology.
 */
@Entity(tableName = "shina_grammar_rules")
data class ShinaGrammarRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ruleName: String,
    val ruleCategory: String = "Ergative Case", // Ergative, Retroflex Phonemes, Verb Tense, Gender/Number, Postpositions
    val patternExample: String,
    val explanation: String,
    val dialect: String = "Gilgit",
    val isTrained: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Shina Poetry: Traditional and modern bait, ghazals, maye, and folklore verses.
 */
@Entity(tableName = "shina_poetry")
data class ShinaPoetryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val poetName: String = "Anonymous / Traditional",
    val poetryType: String = "Bait (بیت)", // Bait, Ghazal, Maye, Seelo
    val versesShina: String,
    val translationUrduEnglish: String = "",
    val dialect: String = "Gilgit",
    val isTrained: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Shina Dialogues & Conversations: Multi-turn conversational exchanges.
 */
@Entity(tableName = "shina_dialogues")
data class ShinaDialogueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dialogueTitle: String,
    val scenario: String = "Daily Conversation", // Mountain Bazaar, Greeting, Jirga, Travel, Family
    val speakerAText: String,
    val speakerBReply: String,
    val dialect: String = "Gilgit",
    val isTrained: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Shina Media Corpus: Audio voice recordings, video transcripts, and spoken audio metadata.
 */
@Entity(tableName = "shina_media_corpus")
data class ShinaMediaCorpusEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val mediaType: String = "Voice Audio", // Voice Audio, Video Transcript, Folk Song Audio, Broadcast Clip
    val mediaUriOrUrl: String = "",
    val transcriptShina: String,
    val translation: String = "",
    val dialect: String = "Gilgit",
    val durationSeconds: Int = 0,
    val phoneticNotes: String = "",
    val isTrained: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Shina Model Training Metrics: Records on-device offline training iterations and loss.
 */
@Entity(tableName = "shina_training_metrics")
data class ShinaModelTrainingMetricEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cycleTimestamp: Long = System.currentTimeMillis(),
    val totalWordsTrained: Int = 0,
    val totalSentencesTrained: Int = 0,
    val totalGrammarRulesTrained: Int = 0,
    val totalPoetryTrained: Int = 0,
    val totalDialoguesTrained: Int = 0,
    val totalMediaTrained: Int = 0,
    val trainingLoss: Float = 0.12f,
    val perplexity: Float = 4.2f,
    val accuracyPercent: Float = 95.4f,
    val modelVersionTag: String = "v3.0-GB-Offline"
)

/**
 * Shina Auto-Training Log: Logs live automatic learning instances from user chats & voice.
 */
@Entity(tableName = "shina_auto_training_logs")
data class ShinaAutoTrainingLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val source: String = "AI Chat", // AI Chat, Voice Talk, Keyboard Typing
    val inputSnippet: String,
    val tokensExtracted: Int,
    val ngramsLearned: Int,
    val dialect: String = "Gilgit",
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface TrainingDataDao {
    // --- Sentences ---
    @Query("SELECT * FROM shina_sentences ORDER BY timestamp DESC")
    fun getAllSentences(): Flow<List<ShinaSentenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSentence(sentence: ShinaSentenceEntity): Long

    @Query("SELECT COUNT(*) FROM shina_sentences")
    fun getTotalSentenceCount(): Flow<Int>

    @Query("SELECT * FROM shina_sentences WHERE isTrained = 0")
    suspend fun getUntrainedSentences(): List<ShinaSentenceEntity>

    @Query("UPDATE shina_sentences SET isTrained = 1")
    suspend fun markAllSentencesTrained()

    @Query("SELECT COUNT(*) FROM shina_sentences WHERE isVerified = 1")
    fun getVerifiedSentenceCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM shina_sentences WHERE isVerified = 0")
    fun getPendingSentenceCount(): Flow<Int>

    @Query("UPDATE shina_sentences SET isVerified = :isVerified WHERE id = :id")
    suspend fun setSentenceVerified(id: Long, isVerified: Boolean)

    @Query("SELECT * FROM shina_sentences WHERE isVerified = 0 ORDER BY timestamp DESC")
    fun getPendingSentences(): Flow<List<ShinaSentenceEntity>>

    @Query("SELECT * FROM shina_sentences WHERE isVerified = 1 ORDER BY timestamp DESC")
    fun getVerifiedSentences(): Flow<List<ShinaSentenceEntity>>

    @Query("DELETE FROM shina_sentences WHERE id = :id")
    suspend fun deleteSentence(id: Long)

    // --- Conversions ---
    @Query("SELECT * FROM shina_conversions ORDER BY timestamp DESC")
    fun getAllConversions(): Flow<List<ShinaConversionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversion(conversion: ShinaConversionEntity): Long

    @Query("SELECT COUNT(*) FROM shina_conversions")
    fun getTotalConversionCount(): Flow<Int>

    @Query("SELECT * FROM shina_conversions WHERE isTrained = 0")
    suspend fun getUntrainedConversions(): List<ShinaConversionEntity>

    @Query("UPDATE shina_conversions SET isTrained = 1")
    suspend fun markAllConversionsTrained()

    @Query("DELETE FROM shina_conversions WHERE id = :id")
    suspend fun deleteConversion(id: Long)

    // --- Grammar Rules ---
    @Query("SELECT * FROM shina_grammar_rules ORDER BY timestamp DESC")
    fun getAllGrammarRules(): Flow<List<ShinaGrammarRuleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrammarRule(rule: ShinaGrammarRuleEntity): Long

    @Query("SELECT COUNT(*) FROM shina_grammar_rules")
    fun getTotalGrammarRuleCount(): Flow<Int>

    @Query("SELECT * FROM shina_grammar_rules WHERE isTrained = 0")
    suspend fun getUntrainedGrammarRules(): List<ShinaGrammarRuleEntity>

    @Query("UPDATE shina_grammar_rules SET isTrained = 1")
    suspend fun markAllGrammarRulesTrained()

    @Query("DELETE FROM shina_grammar_rules WHERE id = :id")
    suspend fun deleteGrammarRule(id: Long)

    // --- Poetry ---
    @Query("SELECT * FROM shina_poetry ORDER BY timestamp DESC")
    fun getAllPoetry(): Flow<List<ShinaPoetryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoetry(poetry: ShinaPoetryEntity): Long

    @Query("SELECT COUNT(*) FROM shina_poetry")
    fun getTotalPoetryCount(): Flow<Int>

    @Query("SELECT * FROM shina_poetry WHERE isTrained = 0")
    suspend fun getUntrainedPoetry(): List<ShinaPoetryEntity>

    @Query("UPDATE shina_poetry SET isTrained = 1")
    suspend fun markAllPoetryTrained()

    @Query("DELETE FROM shina_poetry WHERE id = :id")
    suspend fun deletePoetry(id: Long)

    // --- Dialogues ---
    @Query("SELECT * FROM shina_dialogues ORDER BY timestamp DESC")
    fun getAllDialogues(): Flow<List<ShinaDialogueEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDialogue(dialogue: ShinaDialogueEntity): Long

    @Query("SELECT COUNT(*) FROM shina_dialogues")
    fun getTotalDialogueCount(): Flow<Int>

    @Query("SELECT * FROM shina_dialogues WHERE isTrained = 0")
    suspend fun getUntrainedDialogues(): List<ShinaDialogueEntity>

    @Query("UPDATE shina_dialogues SET isTrained = 1")
    suspend fun markAllDialoguesTrained()

    @Query("DELETE FROM shina_dialogues WHERE id = :id")
    suspend fun deleteDialogue(id: Long)

    // --- Media Corpus ---
    @Query("SELECT * FROM shina_media_corpus ORDER BY timestamp DESC")
    fun getAllMediaCorpus(): Flow<List<ShinaMediaCorpusEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaCorpus(media: ShinaMediaCorpusEntity): Long

    @Query("SELECT COUNT(*) FROM shina_media_corpus")
    fun getTotalMediaCount(): Flow<Int>

    @Query("SELECT * FROM shina_media_corpus WHERE isTrained = 0")
    suspend fun getUntrainedMedia(): List<ShinaMediaCorpusEntity>

    @Query("UPDATE shina_media_corpus SET isTrained = 1")
    suspend fun markAllMediaTrained()

    @Query("DELETE FROM shina_media_corpus WHERE id = :id")
    suspend fun deleteMediaCorpus(id: Long)

    // --- Training Metrics ---
    @Query("SELECT * FROM shina_training_metrics ORDER BY cycleTimestamp DESC LIMIT 1")
    fun getLatestTrainingMetric(): Flow<ShinaModelTrainingMetricEntity?>

    @Query("SELECT * FROM shina_training_metrics ORDER BY cycleTimestamp DESC LIMIT 20")
    fun getRecentTrainingMetrics(): Flow<List<ShinaModelTrainingMetricEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainingMetric(metric: ShinaModelTrainingMetricEntity): Long

    // --- Auto-Training Logs ---
    @Query("SELECT * FROM shina_auto_training_logs ORDER BY timestamp DESC LIMIT 30")
    fun getRecentAutoTrainingLogs(): Flow<List<ShinaAutoTrainingLogEntity>>

    @Query("SELECT COUNT(*) FROM shina_auto_training_logs")
    fun getTotalAutoTrainingEventsCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAutoTrainingLog(log: ShinaAutoTrainingLogEntity): Long
}
