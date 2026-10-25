package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ShinaWordEntity::class,
        NgramEntity::class,
        ClipboardEntity::class,
        VoiceContributionEntity::class,
        CommunityWordEntity::class,
        LearnedPhraseEntity::class,
        ShinaSentenceEntity::class,
        ShinaConversionEntity::class,
        ShinaGrammarRuleEntity::class,
        ShinaPoetryEntity::class,
        ShinaDialogueEntity::class,
        ShinaMediaCorpusEntity::class,
        ShinaModelTrainingMetricEntity::class,
        ShinaAutoTrainingLogEntity::class,
        ShinaDictionaryEntryEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class ShinaDatabase : RoomDatabase() {
    abstract fun shinaWordDao(): ShinaWordDao
    abstract fun ngramDao(): NgramDao
    abstract fun clipboardDao(): ClipboardDao
    abstract fun voiceContributionDao(): VoiceContributionDao
    abstract fun communityWordDao(): CommunityWordDao
    abstract fun learnedPhraseDao(): LearnedPhraseDao
    abstract fun trainingDataDao(): TrainingDataDao
    abstract fun dictionaryDao(): ShinaDictionaryDao
    abstract fun historicalLearningDao(): HistoricalLearningDao

    companion object {
        @Volatile
        private var INSTANCE: ShinaDatabase? = null

        fun getDatabase(context: Context): ShinaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShinaDatabase::class.java,
                    "shina_keyboard_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
