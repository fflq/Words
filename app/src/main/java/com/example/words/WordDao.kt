package com.example.words

import androidx.paging.DataSource
import androidx.room.*

@Dao
interface WordDao {
    @Insert
    fun insert(vararg word: Word)

    @Query("select * from words order by id desc")
    //fun selectAll(): LiveData<List<Word>>
    fun selectAll(): DataSource.Factory<Int, Word>

    @Query("select * from words where english like :pattern order by id desc")
    //fun select(pattern: String): LiveData<List<Word>>
    fun select(pattern: String): DataSource.Factory<Int, Word>

    @Update
    fun update(vararg word: Word)

    @Query("delete from words")
    fun deleteAll()

    @Delete
    fun delete(vararg word: Word)
}