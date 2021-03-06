package com.adeuga.develob.ade_uga.fc.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(DbDayData::class, DbTask::class), version = 6)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): DbDayDataDao
}