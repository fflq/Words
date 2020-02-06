package com.example.words

import android.app.Application
import android.os.AsyncTask
import androidx.paging.DataSource

class WordRepository(application: Application) {
    var wordDao: WordDao
    init {
        var wordDatabase = WordDatabase.getWordDatabase(application.applicationContext)!!
        wordDao = wordDatabase.getWordDao()!!
    }

    //fun selectAll(): LiveData<List<Word>> = wordDao.selectAll()
    fun selectAll(): DataSource.Factory<Int, Word> = wordDao.selectAll()

    //fun select(pattern: String): LiveData<List<Word>> = wordDao.select("%$pattern%")
    fun select(pattern: String): DataSource.Factory<Int, Word> = wordDao.select("%$pattern%")

    fun deleteAll() = DeleteAllAsyncTask(wordDao).execute()
    fun delete(vararg word: Word) = DeleteAsyncTask(wordDao).execute(*word)

    fun insert(vararg word: Word) = InsertAsyncTask(wordDao).execute(*word)

    fun update(vararg word: Word) = UpdateAsyncTask(wordDao).execute(*word)

    class DeleteAllAsyncTask(var wordDao: WordDao): AsyncTask<Unit, Unit, Unit>() {
        override fun doInBackground(vararg params: Unit) = wordDao.deleteAll()
    }

    class DeleteAsyncTask(var wordDao: WordDao): AsyncTask<Word, Unit, Unit>() {
        override fun doInBackground(vararg params: Word) = wordDao.delete(*params)
    }

    class InsertAsyncTask(var wordDao: WordDao): AsyncTask<Word, Unit, Unit>() {
        override fun doInBackground(vararg params: Word) = wordDao.insert(*params)
    }

    class UpdateAsyncTask(var wordDao: WordDao): AsyncTask<Word, Unit, Unit>() {
        override fun doInBackground(vararg params: Word) = wordDao.update(*params)
    }
}