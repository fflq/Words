package com.example.words

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList

class WordViewModel(application: Application) : AndroidViewModel(application) {
    var mWordRepository: WordRepository = WordRepository(application)
    //var mLiveWords: LiveData<List<Word>>
    var mLiveWords: LiveData<PagedList<Word>>
    val mPageSize = 2

    init {
        //mLiveWords = mWordRepository.selectAll()!!
        mLiveWords = LivePagedListBuilder<Int, Word>(mWordRepository.selectAll(), mPageSize).build()
    }

    /*
    fun selectAll(isnew :Boolean=false): LiveData<List<Word>> = when(isnew) {
        true -> mWordRepository.selectAll()
        else -> mLiveWords
    }
    */
    fun selectAll(isnew: Boolean = false): LiveData<PagedList<Word>> = when (isnew) {
        true -> LivePagedListBuilder<Int, Word>(mWordRepository.selectAll(), mPageSize).build()
        else -> mLiveWords
    }

    /*
    fun select(pattern: String): LiveData<List<Word>> {
        return wordRepository.select(pattern)
    }
     */

    fun select(pattern: String): LiveData<PagedList<Word>> {
        return LivePagedListBuilder<Int, Word>(mWordRepository.select(pattern), mPageSize).build()
    }

    fun deleteAll() = mWordRepository.deleteAll()

    fun delete(vararg word: Word) = mWordRepository.delete(*word)

    fun insert(vararg word: Word) = mWordRepository.insert(*word)

    // 为什么有时update会失灵
    // 发现update后停留在当前页面一小会就能update成功
    fun update(vararg word: Word) = mWordRepository.update(*word)
}