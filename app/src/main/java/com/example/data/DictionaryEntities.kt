package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Entity representing an offline Shina dictionary entry with real-time definitions,
 * Roman Shina transliteration, synonyms, and user contribution metadata.
 */
@Entity(tableName = "shina_dictionary")
data class ShinaDictionaryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val wordShina: String,
    val romanized: String = "",
    val partOfSpeech: String = "Noun", // Noun, Verb, Adjective, Pronoun, Adverb, Idiom, Conjunction, Greeting
    val definitionUrdu: String,
    val definitionEnglish: String = "",
    val synonyms: String = "", // Comma-separated synonyms in Shina
    val exampleSentenceShina: String = "",
    val exampleTranslation: String = "",
    val dialect: String = "Gilgit", // Gilgit, Astore, Diamer, Chilas, Kohistan, Gupis, Punial
    val contributorName: String = "Zaigham Abbas / Shina Linguistic Archive",
    val isUserContributed: Boolean = false,
    val isVerified: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface ShinaDictionaryDao {

    @Query("SELECT * FROM shina_dictionary ORDER BY wordShina ASC")
    fun getAllEntries(): Flow<List<ShinaDictionaryEntryEntity>>

    @Query("""
        SELECT * FROM shina_dictionary 
        WHERE wordShina LIKE '%' || :query || '%' 
           OR romanized LIKE '%' || :query || '%' 
           OR definitionUrdu LIKE '%' || :query || '%' 
           OR definitionEnglish LIKE '%' || :query || '%' 
           OR synonyms LIKE '%' || :query || '%'
        ORDER BY 
            CASE 
                WHEN wordShina = :query THEN 1
                WHEN wordShina LIKE :query || '%' THEN 2
                WHEN romanized LIKE :query || '%' THEN 3
                ELSE 4
            END,
            wordShina ASC
    """)
    fun searchEntries(query: String): Flow<List<ShinaDictionaryEntryEntity>>

    @Query("""
        SELECT * FROM shina_dictionary 
        WHERE (wordShina LIKE '%' || :query || '%' 
           OR romanized LIKE '%' || :query || '%' 
           OR definitionUrdu LIKE '%' || :query || '%' 
           OR definitionEnglish LIKE '%' || :query || '%' 
           OR synonyms LIKE '%' || :query || '%')
          AND (:dialect = '' OR dialect = :dialect)
          AND (:partOfSpeech = '' OR partOfSpeech LIKE '%' || :partOfSpeech || '%')
        ORDER BY wordShina ASC
    """)
    fun searchFilteredEntries(query: String, dialect: String, partOfSpeech: String): Flow<List<ShinaDictionaryEntryEntity>>

    @Query("SELECT * FROM shina_dictionary WHERE wordShina = :word LIMIT 1")
    suspend fun getEntryByWord(word: String): ShinaDictionaryEntryEntity?

    @Query("SELECT * FROM shina_dictionary WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): ShinaDictionaryEntryEntity?

    @Query("SELECT * FROM shina_dictionary WHERE dialect = :dialect ORDER BY wordShina ASC")
    fun getEntriesByDialect(dialect: String): Flow<List<ShinaDictionaryEntryEntity>>

    @Query("SELECT * FROM shina_dictionary WHERE partOfSpeech LIKE '%' || :partOfSpeech || '%' ORDER BY wordShina ASC")
    fun getEntriesByPartOfSpeech(partOfSpeech: String): Flow<List<ShinaDictionaryEntryEntity>>

    @Query("SELECT * FROM shina_dictionary ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEntries(limit: Int = 20): Flow<List<ShinaDictionaryEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: ShinaDictionaryEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntries(entries: List<ShinaDictionaryEntryEntity>)

    @Update
    suspend fun updateEntry(entry: ShinaDictionaryEntryEntity)

    @Query("""
        UPDATE shina_dictionary 
        SET definitionUrdu = :urdu,
            definitionEnglish = :eng,
            synonyms = :syns,
            exampleSentenceShina = :sentence,
            exampleTranslation = :transl
        WHERE id = :id
    """)
    suspend fun updateDefinitionsAndSynonyms(
        id: Long,
        urdu: String,
        eng: String,
        syns: String,
        sentence: String,
        transl: String
    ): Int

    @Query("UPDATE shina_dictionary SET dialect = :dialect, partOfSpeech = :pos WHERE id = :id")
    suspend fun updateDialectAndPartOfSpeech(id: Long, dialect: String, pos: String): Int

    @Query("UPDATE shina_dictionary SET isVerified = :isVerified WHERE id = :id")
    suspend fun setVerificationStatus(id: Long, isVerified: Boolean): Int

    @Query("DELETE FROM shina_dictionary WHERE id = :id")
    suspend fun deleteEntry(id: Long)

    @Query("DELETE FROM shina_dictionary WHERE wordShina = :word")
    suspend fun deleteEntryByWord(word: String): Int

    @Query("DELETE FROM shina_dictionary WHERE isUserContributed = 1")
    suspend fun deleteUserContributedEntries(): Int

    @Query("DELETE FROM shina_dictionary")
    suspend fun clearAllDictionary()

    @Query("SELECT COUNT(*) FROM shina_dictionary")
    fun getTotalDictionaryCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM shina_dictionary WHERE isUserContributed = 1")
    fun getUserContributedCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM shina_dictionary WHERE isVerified = 1")
    fun getVerifiedCount(): Flow<Int>

    @Query("SELECT * FROM shina_dictionary WHERE isUserContributed = 1 ORDER BY timestamp DESC")
    fun getUserContributedEntries(): Flow<List<ShinaDictionaryEntryEntity>>
}
