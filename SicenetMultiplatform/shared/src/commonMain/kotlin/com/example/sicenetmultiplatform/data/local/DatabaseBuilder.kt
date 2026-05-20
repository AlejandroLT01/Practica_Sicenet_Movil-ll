package com.example.sicenetmultiplatform.data.local

import androidx.room.RoomDatabase

expect fun getDatabaseBuilder(): RoomDatabase.Builder<SicenetDatabase>
