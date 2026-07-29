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

    @Query("SELECT * FROM shina_words ORDER BY frequency DESC LIMIT 50")
    fun getTopWords(): Flow<List<ShinaWordEntity>>

    @Query("SELECT * FROM shina_words ORDER BY frequency DESC")
    fun getAllWords(): Flow<List<ShinaWordEntity>>

    @Query("DELETE FROM shina_words WHERE word = :word")
    suspend fun deleteWord(word: String)
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

    @Query("DELETE FROM community_words WHERE id = :id")
    suspend fun deleteCommunityWord(id: Long)
}

@Dao
interface LearnedPhraseDao {
    @Query("SELECT phrase FROM learned_user_phrases WHERE phrase LIKE :prefix || '%' ORDER BY frequency DESC, lastUsedTimestamp DESC LIMIT :limit")
    fun getLearnedPhraseSuggestions(prefix: String, limit: Int = 5): Flow<List<String>>

    @Query("SELECT * FROM learned_user_phrases WHERE phrase = :phrase LIMIT 1")
    suspend fun getPhrase(phrase: String): LearnedPhraseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePhrase(phrase: LearnedPhraseEntity)

    @Query("UPDATE learned_user_phrases SET frequency = frequency + 1, lastUsedTimestamp = :timestamp WHERE phrase = :phrase")
    suspend fun incrementPhraseFrequency(phrase: String, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT COUNT(*) FROM learned_user_phrases")
    fun getTotalLearnedCount(): Flow<Int>

    @Query("SELECT * FROM learned_user_phrases ORDER BY frequency DESC LIMIT 50")
    fun getTopLearnedPhrases(): Flow<List<LearnedPhraseEntity>>

    @Query("DELETE FROM learned_user_phrases WHERE id = :id")
    suspend fun deleteLearnedPhrase(id: Long)

    @Query("DELETE FROM learned_user_phrases")
    suspend fun clearAllLearnedPhrases()
}
