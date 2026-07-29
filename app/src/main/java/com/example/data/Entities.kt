package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shina_words")
data class ShinaWordEntity(
    @PrimaryKey val word: String,
    val normalizedWord: String,
    val frequency: Int = 1,
    val dialect: String = "Gilgit",
    val isApproved: Boolean = true,
    val isUserAdded: Boolean = false,
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "shina_ngrams")
data class NgramEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word1: String,
    val word2: String,
    val word3: String = "",
    val count: Int = 1,
    val dialect: String = "Gilgit"
)

@Entity(tableName = "clipboard_items")
data class ClipboardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "voice_contributions")
data class VoiceContributionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val audioPath: String,
    val transcribedText: String,
    val dialect: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isUploaded: Boolean = false
)

@Entity(tableName = "community_words")
data class CommunityWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val word: String,
    val dialect: String,
    val contributorName: String = "Anonymous",
    val status: String = "Pending", // Pending, Approved, Rejected
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "learned_user_phrases")
data class LearnedPhraseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val phrase: String,
    val frequency: Int = 1,
    val dialect: String = "Gilgit",
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)
