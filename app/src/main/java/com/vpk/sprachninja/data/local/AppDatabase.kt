package com.vpk.sprachninja.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The main database class for the application.
 * Updated to include the RecentQuestion entity.
 */
// 1. Add RecentQuestion to entities array and increment version number to 2.
@Database(entities = [User::class, RecentQuestion::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    // 3. Add the new abstract function for the RecentQuestionDao.
    abstract fun recentQuestionDao(): RecentQuestionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sprachninja_database"
                )
                    // 2. Add fallbackToDestructiveMigration to handle the version increment.
                    //    This is simple for development; a real app would need a proper migration.
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}