package com.hasanbilgin.artbookkotlin.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hasanbilgin.artbookkotlin.model.ArtModel
import com.hasanbilgin.artbookkotlin.roomdb.ArtDao

@Database(entities = [ArtModel::class], version = 1)
 abstract class ArtDatabase : RoomDatabase() {
    abstract fun artDao(): ArtDao
}