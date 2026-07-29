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

    init {
        // Seed initial Shina vocabulary asynchronously
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

    suspend fun deleteWordByString(word: String) = wordDao.deleteWord(word)
    suspend fun deleteVoiceContribution(id: Long) = voiceDao.deleteVoiceContribution(id)
    suspend fun deleteCommunityWord(id: Long) = communityDao.deleteCommunityWord(id)
    suspend fun deleteLearnedPhrase(id: Long) = learnedPhraseDao.deleteLearnedPhrase(id)
    suspend fun clearAllLearnedPhrases() = learnedPhraseDao.clearAllLearnedPhrases()

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
    }
}
