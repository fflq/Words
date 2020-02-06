package com.example.words

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Word::class), version = 3, exportSchema = false)
abstract class WordDatabase: RoomDatabase() {

    companion object {
        var INSTANCE: WordDatabase? = null
        val DATABASE_NAME = "Word_Database"

        fun getWordDatabase(context: Context): WordDatabase {
            synchronized(WordDatabase::class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context, WordDatabase::class.java, DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE!!
        }
    }

    abstract fun getWordDao(): WordDao
}