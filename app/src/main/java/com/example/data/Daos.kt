package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ShinaWordDao {
    @Query("SELECT * FROM shina_words WHERE normalizedWord LIKE :prefix || '%' ORDER BY frequency DESC, lastUsedTimestamp DESC LIMIT :limit")
    fun getSuggestions(prefix: String, limit: Int = 5): Flow<List<ShinaWordEntity>>

    @Query("SELECT * FROM shina_words WHERE word = :word LIMIT 1")
    suspend fun getWord(word: String): ShinaWordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWord(word: ShinaWordEntity)

    @Query("UPDATE shina_words SET frequency = frequency + 1, lastUsedTimestamp = :timestamp WHERE word = :word")
    suspend fun incrementFrequency(word: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM shina_words")
    fun getTotalWordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM shina_words WHERE isUserAdded = 1")
    fun getUserAddedWordCount(): Flow<Int>

    @Query("SELECT * FROM shina_words WHERE isUserAdded = 1 ORDER BY lastUsedTimestamp DESC")
    fun getUserAddedWords(): Flow<List<ShinaWordEntity>>

    @Query("SELECT * FROM shina_words WHERE dialect = :dialect ORDER BY frequency DESC")
    fun getWordsByDialect(dialect: String): Flow<List<ShinaWordEntity>>

    @Query("SELECT * FROM shina_words ORDER BY frequency DESC LIMIT 50")
    fun getTopWords(): Flow<List<ShinaWordEntity>>

    @Query("SELECT * FROM shina_words ORDER BY frequency DESC")
    fun getAllWords(): Flow<List<ShinaWordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<ShinaWordEntity>)

    @Update
    suspend fun updateWord(word: ShinaWordEntity)

    @Query("UPDATE shina_words SET dialect = :dialect WHERE word = :word")
    suspend fun updateWordDialect(word: String, dialect: String): Int

    @Query("UPDATE shina_words SET isApproved = :isApproved WHERE word = :word")
    suspend fun setWordApproval(word: String, isApproved: Boolean): Int

    @Query("DELETE FROM shina_words WHERE word = :word")
    suspend fun deleteWord(word: String)

    @Query("DELETE FROM shina_words WHERE isUserAdded = 1")
    suspend fun deleteUserAddedWords(): Int

    @Query("DELETE FROM shina_words WHERE lastUsedTimestamp < :cutoffTimestamp AND isUserAdded = 1")
    suspend fun deleteOldUserWords(cutoffTimestamp: Long): Int
}

@Dao
interface NgramDao {
    @Query("SELECT word2 FROM shina_ngrams WHERE word1 = :prevWord ORDER BY count DESC LIMIT :limit")
    fun getNextWordPredictions(prevWord: String, limit: Int = 3): Flow<List<String>>

    @Query("SELECT * FROM shina_ngrams WHERE word1 = :word1 AND word2 = :word2 LIMIT 1")
    suspend fun getNgram(word1: String, word2: String): NgramEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateNgram(ngram: NgramEntity)

    @Query("UPDATE shina_ngrams SET count = count + 1 WHERE id = :id")
    suspend fun incrementNgram(id: Long)

    @Query("SELECT COUNT(*) FROM shina_ngrams")
    fun getTotalNgrams(): Flow<Int>
}

@Dao
interface ClipboardDao {
    @Query("SELECT * FROM clipboard_items ORDER BY isPinned DESC, timestamp DESC LIMIT 30")
    fun getAllClips(): Flow<List<ClipboardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClip(clip: ClipboardEntity)

    @Query("DELETE FROM clipboard_items WHERE id = :id")
    suspend fun deleteClip(id: Long)

    @Query("UPDATE clipboard_items SET isPinned = :isPinned WHERE id = :id")
    suspend fun setPinned(id: Long, isPinned: Boolean)

    @Query("DELETE FROM clipboard_items WHERE isPinned = 0")
    suspend fun clearUnpinned()
}

@Dao
interface VoiceContributionDao {
    @Query("SELECT * FROM voice_contributions ORDER BY timestamp DESC")
    fun getAllVoiceContributions(): Flow<List<VoiceContributionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceContribution(contribution: VoiceContributionEntity)

    @Query("SELECT COUNT(*) FROM voice_contributions")
    fun getTotalVoiceCount(): Flow<Int>

    @Query("DELETE FROM voice_contributions WHERE id = :id")
    suspend fun deleteVoiceContribution(id: Long)
}

@Dao
interface CommunityWordDao {
    @Query("SELECT * FROM community_words ORDER BY timestamp DESC")
    fun getAllCommunityWords(): Flow<List<CommunityWordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommunityWord(word: CommunityWordEntity)

    @Query("UPDATE community_words SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM community_words WHERE status = 'Pending'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM community_words WHERE status = 'Approved'")
    fun getApprovedCommunityWordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM community_words")
    fun getTotalCommunityWordCount(): Flow<Int>

    @Query("DELETE FROM community_words WHERE id = :id")
    suspend fun deleteCommunityWord(id: Long)
}

@Dao
interface LearnedPhraseDao {
    @Query("SELECT phrase FROM learned_user_phrases WHERE phrase LIKE :prefix || '%' ORDER BY frequency DESC, lastUsedTimestamp DESC LIMIT :limit")
    fun getLearnedPhraseSuggestions(prefix: String, limit: Int = 5): Flow<List<String>>

    @Query("SELECT * FROM learned_user_phrases WHERE phrase = :phrase LIMIT 1")
    suspend fun getPhrase(phrase: String): LearnedPhraseEntity?

    @Query("SELECT * FROM learned_user_phrases WHERE id = :id LIMIT 1")
    suspend fun getPhraseById(id: Long): LearnedPhraseEntity?

    @Query("SELECT * FROM learned_user_phrases ORDER BY lastUsedTimestamp DESC")
    fun getAllLearnedPhrases(): Flow<List<LearnedPhraseEntity>>

    @Query("SELECT * FROM learned_user_phrases WHERE dialect = :dialect ORDER BY frequency DESC")
    fun getPhrasesByDialect(dialect: String): Flow<List<LearnedPhraseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePhrase(phrase: LearnedPhraseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhrases(phrases: List<LearnedPhraseEntity>)

    @Update
    suspend fun updatePhrase(phrase: LearnedPhraseEntity)

    @Query("UPDATE learned_user_phrases SET frequency = frequency + 1, lastUsedTimestamp = :timestamp WHERE phrase = :phrase")
    suspend fun incrementPhraseFrequency(phrase: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE learned_user_phrases SET dialect = :dialect WHERE id = :id")
    suspend fun updatePhraseDialect(id: Long, dialect: String): Int

    @Query("SELECT COUNT(*) FROM learned_user_phrases")
    fun getTotalLearnedCount(): Flow<Int>

    @Query("SELECT * FROM learned_user_phrases ORDER BY frequency DESC LIMIT 50")
    fun getTopLearnedPhrases(): Flow<List<LearnedPhraseEntity>>

    @Query("DELETE FROM learned_user_phrases WHERE id = :id")
    suspend fun deleteLearnedPhrase(id: Long)

    @Query("DELETE FROM learned_user_phrases WHERE phrase = :phrase")
    suspend fun deletePhraseByText(phrase: String): Int

    @Query("DELETE FROM learned_user_phrases WHERE lastUsedTimestamp < :cutoffTimestamp")
    suspend fun deleteOldLearnedPhrases(cutoffTimestamp: Long): Int

    @Query("DELETE FROM learned_user_phrases")
    suspend fun clearAllLearnedPhrases()
}

/**
 * Dedicated Data Access Object (DAO) for historical on-device learning data,
 * automatic linguistic adaptation logs, and learning metric snapshots.
 */
@Dao
interface HistoricalLearningDao {
    // --- Create ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ShinaAutoTrainingLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<ShinaAutoTrainingLogEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(metric: ShinaModelTrainingMetricEntity): Long

    // --- Read ---
    @Query("SELECT * FROM shina_auto_training_logs ORDER BY timestamp DESC")
    fun getAllHistoricalLogs(): Flow<List<ShinaAutoTrainingLogEntity>>

    @Query("SELECT * FROM shina_auto_training_logs WHERE id = :id LIMIT 1")
    suspend fun getLogById(id: Long): ShinaAutoTrainingLogEntity?

    @Query("SELECT * FROM shina_auto_training_logs WHERE source = :source ORDER BY timestamp DESC")
    fun getLogsBySource(source: String): Flow<List<ShinaAutoTrainingLogEntity>>

    @Query("SELECT * FROM shina_auto_training_logs WHERE dialect = :dialect ORDER BY timestamp DESC")
    fun getLogsByDialect(dialect: String): Flow<List<ShinaAutoTrainingLogEntity>>

    @Query("SELECT * FROM shina_auto_training_logs WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getLogsSince(sinceTimestamp: Long): Flow<List<ShinaAutoTrainingLogEntity>>

    @Query("SELECT COUNT(*) FROM shina_auto_training_logs")
    fun getTotalLogCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(tokensExtracted), 0) FROM shina_auto_training_logs")
    fun getTotalTokensExtracted(): Flow<Int>

    @Query("SELECT COALESCE(SUM(ngramsLearned), 0) FROM shina_auto_training_logs")
    fun getTotalNgramsLearned(): Flow<Int>

    @Query("SELECT * FROM shina_training_metrics ORDER BY cycleTimestamp DESC")
    fun getAllHistoricalMetrics(): Flow<List<ShinaModelTrainingMetricEntity>>

    @Query("SELECT * FROM shina_training_metrics ORDER BY cycleTimestamp DESC LIMIT 1")
    fun getLatestMetric(): Flow<ShinaModelTrainingMetricEntity?>

    // --- Update ---
    @Update
    suspend fun updateLog(log: ShinaAutoTrainingLogEntity)

    @Query("UPDATE shina_auto_training_logs SET inputSnippet = :snippet, dialect = :dialect WHERE id = :id")
    suspend fun updateLogDetails(id: Long, snippet: String, dialect: String): Int

    // --- Delete ---
    @Query("DELETE FROM shina_auto_training_logs WHERE id = :id")
    suspend fun deleteLog(id: Long)

    @Query("DELETE FROM shina_auto_training_logs WHERE timestamp < :cutoffTimestamp")
    suspend fun deleteLogsOlderThan(cutoffTimestamp: Long): Int

    @Query("DELETE FROM shina_auto_training_logs WHERE source = :source")
    suspend fun deleteLogsBySource(source: String): Int

    @Query("DELETE FROM shina_auto_training_logs")
    suspend fun clearAllHistoricalLogs()

    @Query("DELETE FROM shina_training_metrics WHERE cycleTimestamp < :cutoffTimestamp")
    suspend fun deleteMetricsOlderThan(cutoffTimestamp: Long): Int
}
