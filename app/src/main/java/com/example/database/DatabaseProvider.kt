package com.example.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var instance: GameDatabase? = null

    fun getDatabase(context: Context): GameDatabase {
        return instance ?: synchronized(this) {
            val newDb = Room.databaseBuilder(
                context.applicationContext,
                GameDatabase::class.java,
                "chappal_fight_arena_db"
            )
            .fallbackToDestructiveMigration()
            .build()
            instance = newDb
            newDb
        }
    }
}
