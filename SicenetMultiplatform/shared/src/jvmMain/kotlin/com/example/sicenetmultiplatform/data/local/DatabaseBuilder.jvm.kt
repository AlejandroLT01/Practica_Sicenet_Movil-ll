package com.example.sicenetmultiplatform.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun getDatabaseBuilder(): RoomDatabase.Builder<SicenetDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "sicenet_database.db")
    return Room.databaseBuilder<SicenetDatabase>(
        name = dbFile.absolutePath,
    )
}
