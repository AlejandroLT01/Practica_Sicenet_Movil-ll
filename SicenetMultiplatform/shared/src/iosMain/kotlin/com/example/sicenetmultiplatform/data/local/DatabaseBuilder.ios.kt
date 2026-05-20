package com.example.sicenetmultiplatform.data.local

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSHomeDirectory

actual fun getDatabaseBuilder(): RoomDatabase.Builder<SicenetDatabase> {
    val dbFilePath = NSHomeDirectory() + "/sicenet_database.db"
    return Room.databaseBuilder<SicenetDatabase>(
        name = dbFilePath,
        factory =  { SicenetDatabase::class.instantiateImpl() }
    )
}
