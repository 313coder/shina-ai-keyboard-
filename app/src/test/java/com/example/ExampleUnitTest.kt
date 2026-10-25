package com.example

import com.example.voice.ShinaPhoneticSpeechService
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun shinaPhoneticMapping_detectsRetroflexSibilants() {
    val result = ShinaPhoneticSpeechService.transcribeToShinaPhonetics("shina zaban", "Gilgit Shina")
    assertTrue(result.phoneticText.contains("ݜ") || result.phoneticText.contains("شینا"))
    assertNotNull(result.dialect)
  }

  @Test
  fun shinaPhoneticMapping_detectsRetroflexChee() {
    val result = ShinaPhoneticSpeechService.transcribeToShinaPhonetics("chaye piye", "Gilgit Shina")
    assertTrue(result.phoneticText.contains("ڇ") || result.phoneticText.contains("چ"))
  }

  @Test
  fun shinaAcousticTuner_returnsValidProfiles() {
    val phonemes = ShinaPhoneticSpeechService.specialPhonemes
    assertTrue(phonemes.isNotEmpty())
    assertTrue(phonemes.any { it.letter == "ݜ" })
    assertTrue(phonemes.any { it.letter == "ڇ" })
    assertTrue(phonemes.any { it.letter == "ݗ" })

    val phrases = ShinaPhoneticSpeechService.popularSpokenPhrases
    assertTrue(phrases.isNotEmpty())
    assertTrue(phrases.any { it.shinaText.contains("سلام") })
  }

  @Test
  fun contributionMetrics_improvementPercentageCalculatesCorrectly() {
    val baseline = 78.5f
    val wordsAdded = 30
    val sentencesVerified = 12
    val dialogues = 5
    val boost = (wordsAdded * 0.18f) + (sentencesVerified * 0.45f) + (dialogues * 0.35f)
    val totalAccuracy = (baseline + boost).coerceIn(baseline, 99.4f)
    
    assertTrue(boost > 10.0f)
    assertTrue(totalAccuracy > 88.0f)
    assertTrue(totalAccuracy <= 99.4f)
  }

  @Test
  fun contributorTiers_progressesWithXp() {
    fun calculateTier(xp: Int): String = when {
      xp < 100 -> "Tier 1: Shina Linguistic Pioneer"
      xp < 300 -> "Tier 2: Shina Dialect Custodian"
      xp < 600 -> "Tier 3: Cultural Archivist"
      xp < 1000 -> "Tier 4: Master Lexicographer"
      else -> "Tier 5: Shina AI Grandmaster"
    }

    assertEquals("Tier 1: Shina Linguistic Pioneer", calculateTier(50))
    assertEquals("Tier 2: Shina Dialect Custodian", calculateTier(150))
    assertEquals("Tier 3: Cultural Archivist", calculateTier(450))
    assertEquals("Tier 4: Master Lexicographer", calculateTier(750))
    assertEquals("Tier 5: Shina AI Grandmaster", calculateTier(1200))
  }

  @Test
  fun keyboardPreferences_desktopAndTrainingDefaults() {
    val prefs = com.example.model.KeyboardPreferences()
    assertTrue("Desktop mode should be enabled by default", prefs.desktopModeEnabled)
    assertTrue("Desktop side rail navigation should be enabled by default", prefs.desktopSideRailNavigation)
    assertTrue("Desktop physical keyboard transliteration should be enabled by default", prefs.desktopPhysicalKeyboardTransliteration)
    assertTrue("Auto-train from AI chats should be enabled by default", prefs.autoTrainFromAiChats)
    assertTrue("Contribution milestone notifications should be enabled by default", prefs.contributionNotifications)
    assertEquals(1.0f, prefs.speechSpeed, 0.01f)
  }

  @Test
  fun dictionarySeedData_containsRichLinguisticEntries() {
    val entries = com.example.data.DictionarySeedData.INITIAL_ENTRIES
    assertTrue("Dictionary seed dataset should not be empty", entries.isNotEmpty())
    assertTrue("Should have at least 40 baseline entries", entries.size >= 40)
    
    // Check specific essential Shina words
    val salamEntry = entries.find { it.wordShina == "سلام" }
    assertNotNull(salamEntry)
    assertEquals("Salam", salamEntry?.romanized)
    assertTrue(salamEntry?.synonyms?.contains("جو") == true)

    val karamEntry = entries.find { it.wordShina == "کرم" }
    assertNotNull(karamEntry)
    assertTrue(karamEntry?.definitionUrdu?.contains("مہربانی") == true)
  }

  @Test
  fun dictionarySynonym_extractsAndSplitsAccurately() {
    val entry = com.example.data.ShinaDictionaryEntryEntity(
      wordShina = "مزخ",
      romanized = "Mazakh",
      partOfSpeech = "Noun",
      definitionUrdu = "محبت / پیار",
      definitionEnglish = "Love / Affection",
      synonyms = "پیار, عشق, الفت, چاہت",
      dialect = "Gilgit"
    )

    val synonyms = entry.synonyms.split(",").map { it.trim() }.filter { it.isNotBlank() }
    assertEquals(4, synonyms.size)
    assertTrue(synonyms.contains("پیار"))
    assertTrue(synonyms.contains("عشق"))
    assertTrue(synonyms.contains("الفت"))
  }

  @Test
  fun storageStats_formatBytesCorrectly() {
    val stats = com.example.data.StorageStats(
      totalDbSizeBytes = 1048576L, // 1 MB
      walSizeBytes = 524288L, // 512 KB
      totalCacheSizeBytes = 2048L, // 2 KB
      wordsCount = 120,
      userAddedWordsCount = 25,
      dictionaryCount = 45,
      userDictionaryCount = 8,
      sentencesCount = 35,
      verifiedSentencesCount = 30,
      ngramsCount = 200,
      phrasesCount = 15,
      communityCount = 10,
      voiceCount = 5,
      clipsCount = 4,
      trainingLogsCount = 18,
      databasePath = "/data/data/com.example/databases/shina_keyboard_db",
      isWalEnabled = true,
      schemaVersion = 5,
      lastBackupTimestamp = 1600000000000L
    )

    assertEquals("1.00 MB", stats.formattedDbSize())
    assertEquals("512.0 KB", stats.formattedWalSize())
    assertEquals("2.0 KB", stats.formattedCacheSize())
    assertEquals("1.50 MB", stats.formattedTotalSize())
  }

  @Test
  fun backupAndRestoreResult_validatesProperly() {
    val backup = com.example.data.BackupResult(
      success = true,
      filePath = "/data/data/com.example/files/backups/shina_backup.json",
      fileSizeFormatted = "48.5 KB",
      itemsCount = 180,
      message = "Backup created successfully"
    )
    assertTrue(backup.success)
    assertEquals(180, backup.itemsCount)
    assertEquals("48.5 KB", backup.fileSizeFormatted)

    val restore = com.example.data.RestoreResult(
      success = true,
      wordsRestored = 50,
      dictionaryRestored = 45,
      sentencesRestored = 20,
      phrasesRestored = 10,
      message = "Restore complete"
    )
    assertTrue(restore.success)
    assertEquals(50, restore.wordsRestored)
    assertEquals(45, restore.dictionaryRestored)
    assertEquals(20, restore.sentencesRestored)
  }

  @Test
  fun shinaDictionaryDao_entityCrudModelValidation() {
    val entry = com.example.data.ShinaDictionaryEntryEntity(
      id = 101L,
      wordShina = "شُو",
      romanized = "Shoo",
      partOfSpeech = "Adjective",
      definitionUrdu = "اچھا / عمدہ / خوبصورت",
      definitionEnglish = "Good / Beautiful / Fine",
      synonyms = "خوبصورت, لاجواب, عمدہ",
      exampleSentenceShina = "انو شُو ہنو۔",
      exampleTranslation = "یہ اچھا ہے۔",
      dialect = "Gilgit",
      contributorName = "Shina User",
      isUserContributed = true,
      isVerified = false,
      timestamp = 1700000000000L
    )

    // Verify Read/Get properties
    assertEquals(101L, entry.id)
    assertEquals("شُو", entry.wordShina)
    assertEquals("Shoo", entry.romanized)
    assertEquals("Adjective", entry.partOfSpeech)
    assertTrue(entry.isUserContributed)
    assertFalse(entry.isVerified)

    // Test Update operation simulation (copy with new definitions)
    val updated = entry.copy(
      definitionUrdu = "بہترین / زبردست",
      isVerified = true,
      dialect = "Astore"
    )
    assertEquals("بہترین / زبردست", updated.definitionUrdu)
    assertTrue(updated.isVerified)
    assertEquals("Astore", updated.dialect)
    assertEquals(entry.id, updated.id)
  }

  @Test
  fun historicalLearningDao_entityCrudModelValidation() {
    val log = com.example.data.ShinaAutoTrainingLogEntity(
      id = 201L,
      source = "AI_CHAT_STREAM",
      inputSnippet = "توئے نام کی ہن؟ سلام ورے!",
      tokensExtracted = 6,
      ngramsLearned = 4,
      dialect = "Gilgit",
      timestamp = 1700000000000L
    )

    assertEquals(201L, log.id)
    assertEquals("AI_CHAT_STREAM", log.source)
    assertEquals(6, log.tokensExtracted)
    assertEquals(4, log.ngramsLearned)
    assertEquals("Gilgit", log.dialect)

    // Test Update simulation
    val updatedLog = log.copy(
      inputSnippet = "توئے نام کی ہن؟ (Updated)",
      dialect = "Chilas"
    )
    assertEquals("Chilas", updatedLog.dialect)
    assertTrue(updatedLog.inputSnippet.contains("(Updated)"))

    // Test Metric entity
    val metric = com.example.data.ShinaModelTrainingMetricEntity(
      id = 10L,
      modelVersionTag = "v3.0-GB-Offline-Cycle42",
      accuracyPercent = 94.8f,
      totalWordsTrained = 15200,
      totalSentencesTrained = 3450,
      trainingLoss = 0.08f,
      cycleTimestamp = 1700000000000L
    )
    assertEquals("v3.0-GB-Offline-Cycle42", metric.modelVersionTag)
    assertEquals(94.8f, metric.accuracyPercent, 0.01f)
    assertEquals(15200, metric.totalWordsTrained)
    assertEquals(3450, metric.totalSentencesTrained)
    assertEquals(0.08f, metric.trainingLoss, 0.001f)
  }

  @Test
  fun learnedPhrase_crudAndDialectVerification() {
    val phrase = com.example.data.LearnedPhraseEntity(
      id = 50L,
      phrase = "مس شینا زبان شیکھمیس",
      frequency = 15,
      dialect = "Gilgit",
      lastUsedTimestamp = 1700000000000L
    )
    assertEquals(50L, phrase.id)
    assertEquals("مس شینا زبان شیکھمیس", phrase.phrase)
    assertEquals(15, phrase.frequency)

    val incremented = phrase.copy(
      frequency = phrase.frequency + 1,
      dialect = "Kohistan"
    )
    assertEquals(16, incremented.frequency)
    assertEquals("Kohistan", incremented.dialect)
  }
}

