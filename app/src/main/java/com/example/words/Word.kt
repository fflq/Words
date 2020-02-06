package com.example.words

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Words")
data class Word(
    @ColumnInfo(name="english")
    var english: String,
    @ColumnInfo(name="chinese")
    var chinese: String,
    @ColumnInfo(name="isChineseInvisible")
    var isChineseInvisible: Boolean = false ){
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    //@Ignore var _ignore: String = ""

    constructor(id: Int, english: String, chinese: String, isChineseInvisible: Boolean=false):
            this(english, chinese, isChineseInvisible) {
        this.id = id
    }
}