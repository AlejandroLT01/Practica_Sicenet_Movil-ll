package com.example.sicenetmultiplatform.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

lateinit var appContext: Context

actual fun getDatabaseBuilder(): RoomDatabase.Builder<SicenetDatabase> {
    val dbFile = appContext.getDatabasePath("sicenet_database.db")
    return Room.databaseBuilder<SicenetDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}
