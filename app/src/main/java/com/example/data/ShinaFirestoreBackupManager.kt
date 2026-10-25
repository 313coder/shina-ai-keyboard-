package com.example.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class CloudSyncResult(
    val success: Boolean,
    val message: String,
    val wordsSynced: Int = 0,
    val sentencesSynced: Int = 0,
    val grammarRulesSynced: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class CloudRestoreResult(
    val success: Boolean,
    val message: String,
    val wordsRestored: Int = 0,
    val sentencesRestored: Int = 0,
    val grammarRulesRestored: Int = 0
)

/**
 * Manages two-way backup and synchronization between local Room database
 * and Firebase Firestore for user-contributed Shina vocabulary, sentences, and grammar rules.
 */
class ShinaFirestoreBackupManager(
    private val context: Context,
    private val repository: ShinaKeyboardRepository
) {
    companion object {
        private const val TAG = "ShinaFirestoreBackup"
    }

    // Custom Database ID as mandated by Firebase Firestore guidelines
    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
    }

    val auth: FirebaseAuth by lazy {
        Firebase.auth
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser = _currentUser.asStateFlow()

    private val _syncState = MutableStateFlow<CloudSyncResult?>(null)
    val syncState = _syncState.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing = _isSyncing.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUser.value = firebaseAuth.currentUser
        }
    }

    /**
     * Signs in using Google via Jetpack Credential Manager.
     */
    suspend fun signInWithGoogle(activity: Activity): Result<FirebaseUser> = withContext(Dispatchers.Main) {
        try {
            val credentialManager = CredentialManager.create(activity)
            val serverClientId = activity.getString(R.string.default_web_client_id)

            val signInOption = GetSignInWithGoogleOption.Builder(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInOption)
                .build()

            val result = credentialManager.getCredential(activity, request)
            val credential = result.credential

            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val authCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

            val authResult = auth.signInWithCredential(authCredential).await()
            val user = authResult.user ?: throw IllegalStateException("Firebase user was null after Google Sign-In")
            _currentUser.value = user
            Result.success(user)
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "Google Sign-In was cancelled: ${e.message}")
            Result.failure(Exception("Sign-in cancelled. Please choose a Google account to enable automatic cloud backup."))
        } catch (e: Exception) {
            Log.e(TAG, "Failed Google Sign-In: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
    }

    /**
     * Backs up user-contributed Shina data from Room database to Firebase Firestore.
     */
    suspend fun syncLocalDataToFirestore(): CloudSyncResult = withContext(Dispatchers.IO) {
        val user = auth.currentUser
        if (user == null) {
            val result = CloudSyncResult(
                success = false,
                message = "Google Sign-In required to backup data to cloud."
            )
            _syncState.value = result
            return@withContext result
        }

        _isSyncing.value = true
        try {
            val uid = user.uid

            // 1. Get user contributed dictionary entries
            val allEntries = repository.dictionaryDao.getAllEntries().first()
            val userEntries = allEntries.filter { it.isUserContributed }

            // 2. Get sentences
            val sentences = repository.trainingDao.getAllSentences().first()

            // 3. Get grammar rules
            val grammarRules = repository.trainingDao.getAllGrammarRules().first()

            var batch = firestore.batch()
            var operationCount = 0

            // Upload vocabulary
            for (entry in userEntries) {
                val docRef = firestore.collection("users")
                    .document(uid)
                    .collection("vocabulary")
                    .document("word_${entry.id}")

                val data = mapOf(
                    "id" to "word_${entry.id}",
                    "userId" to uid,
                    "word" to entry.wordShina,
                    "dialect" to entry.dialect,
                    "urduMeaning" to entry.definitionUrdu,
                    "englishMeaning" to entry.definitionEnglish,
                    "partOfSpeech" to entry.partOfSpeech,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                batch.set(docRef, data, SetOptions.merge())
                operationCount++
                if (operationCount >= 400) {
                    batch.commit().await()
                    batch = firestore.batch()
                    operationCount = 0
                }
            }

            // Upload sentences
            for (sentence in sentences) {
                val docRef = firestore.collection("users")
                    .document(uid)
                    .collection("sentences")
                    .document("sentence_${sentence.id}")

                val data = mapOf(
                    "id" to "sentence_${sentence.id}",
                    "userId" to uid,
                    "sentenceShina" to sentence.sentenceShina,
                    "translationUrdu" to sentence.translationUrdu,
                    "dialect" to sentence.dialect,
                    "category" to sentence.category,
                    "isVerified" to sentence.isVerified,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                batch.set(docRef, data, SetOptions.merge())
                operationCount++
                if (operationCount >= 400) {
                    batch.commit().await()
                    batch = firestore.batch()
                    operationCount = 0
                }
            }

            // Upload grammar rules
            for (rule in grammarRules) {
                val docRef = firestore.collection("users")
                    .document(uid)
                    .collection("grammar_rules")
                    .document("rule_${rule.id}")

                val data = mapOf(
                    "id" to "rule_${rule.id}",
                    "userId" to uid,
                    "ruleTitle" to rule.ruleName,
                    "ruleDescription" to rule.explanation,
                    "shinaExample" to rule.patternExample,
                    "urduTranslation" to "",
                    "category" to rule.ruleCategory,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                batch.set(docRef, data, SetOptions.merge())
                operationCount++
                if (operationCount >= 400) {
                    batch.commit().await()
                    batch = firestore.batch()
                    operationCount = 0
                }
            }

            // Update backup sync metadata
            val syncMetaDoc = firestore.collection("users")
                .document(uid)
                .collection("backup_metadata")
                .document("sync_state")

            val metaData = mapOf(
                "userId" to uid,
                "lastBackupTimestamp" to FieldValue.serverTimestamp(),
                "totalWordsBackedUp" to userEntries.size,
                "totalSentencesBackedUp" to sentences.size,
                "totalGrammarRulesBackedUp" to grammarRules.size
            )
            batch.set(syncMetaDoc, metaData, SetOptions.merge())

            batch.commit().await()

            val successResult = CloudSyncResult(
                success = true,
                message = "Successfully synced ${userEntries.size} words, ${sentences.size} sentences, and ${grammarRules.size} grammar rules to Firebase Firestore.",
                wordsSynced = userEntries.size,
                sentencesSynced = sentences.size,
                grammarRulesSynced = grammarRules.size
            )
            _syncState.value = successResult
            successResult
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing to Firestore: ${e.message}", e)
            val failureResult = CloudSyncResult(
                success = false,
                message = "Backup failed: ${e.localizedMessage ?: "Unknown network error"}"
            )
            _syncState.value = failureResult
            failureResult
        } finally {
            _isSyncing.value = false
        }
    }

    /**
     * Restores user-contributed Shina data from Firebase Firestore into Room database.
     */
    suspend fun restoreDataFromFirestore(): CloudRestoreResult = withContext(Dispatchers.IO) {
        val user = auth.currentUser
        if (user == null) {
            return@withContext CloudRestoreResult(false, "Please sign in to restore cloud backups.")
        }

        try {
            val uid = user.uid
            var wordsCount = 0
            var sentencesCount = 0
            var rulesCount = 0

            // 1. Fetch vocabulary
            val vocabDocs = firestore.collection("users")
                .document(uid)
                .collection("vocabulary")
                .get()
                .await()

            for (doc in vocabDocs.documents) {
                val word = doc.getString("word") ?: continue
                val existing = repository.dictionaryDao.getEntryByWord(word)
                if (existing == null) {
                    repository.dictionaryDao.insertEntry(
                        ShinaDictionaryEntryEntity(
                            wordShina = word,
                            dialect = doc.getString("dialect") ?: "Gilgit",
                            definitionUrdu = doc.getString("urduMeaning") ?: "",
                            definitionEnglish = doc.getString("englishMeaning") ?: "",
                            partOfSpeech = doc.getString("partOfSpeech") ?: "Noun",
                            isUserContributed = true,
                            isVerified = true
                        )
                    )
                    wordsCount++
                }
            }

            // 2. Fetch sentences
            val sentenceDocs = firestore.collection("users")
                .document(uid)
                .collection("sentences")
                .get()
                .await()

            for (doc in sentenceDocs.documents) {
                val shinaText = doc.getString("sentenceShina") ?: continue
                repository.trainingDao.insertSentence(
                    ShinaSentenceEntity(
                        sentenceShina = shinaText,
                        translationUrdu = doc.getString("translationUrdu") ?: "",
                        dialect = doc.getString("dialect") ?: "Gilgit",
                        category = doc.getString("category") ?: "General",
                        isVerified = doc.getBoolean("isVerified") ?: true,
                        isTrained = false
                    )
                )
                sentencesCount++
            }

            // 3. Fetch grammar rules
            val ruleDocs = firestore.collection("users")
                .document(uid)
                .collection("grammar_rules")
                .get()
                .await()

            for (doc in ruleDocs.documents) {
                val title = doc.getString("ruleTitle") ?: continue
                repository.trainingDao.insertGrammarRule(
                    ShinaGrammarRuleEntity(
                        ruleName = title,
                        ruleCategory = doc.getString("category") ?: "General",
                        patternExample = doc.getString("shinaExample") ?: "",
                        explanation = doc.getString("ruleDescription") ?: "",
                        dialect = "Gilgit",
                        isTrained = false
                    )
                )
                rulesCount++
            }

            CloudRestoreResult(
                success = true,
                message = "Restored $wordsCount words, $sentencesCount sentences, and $rulesCount grammar rules from Firestore.",
                wordsRestored = wordsCount,
                sentencesRestored = sentencesCount,
                grammarRulesRestored = rulesCount
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring from Firestore: ${e.message}", e)
            CloudRestoreResult(
                success = false,
                message = "Restore failed: ${e.localizedMessage ?: "Unknown network error"}"
            )
        }
    }
}
