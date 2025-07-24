package com.vpk.sprachninja.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The main database class for the application.
 * This class is annotated with @Database and lists all entities and the database version.
 */
@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Abstract method to get the Data Access Object for the User entity.
     * Room will generate the implementation for this method.
     */
    abstract fun userDao(): UserDao

    /**
     * Companion object to provide access to the database instance using a singleton pattern.
     * This ensures that only one instance of the database is created at a time, which is
     * a resource-intensive operation.
     */
    companion object {
        // The @Volatile annotation ensures that writes to this field are immediately
        // made visible to other threads.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton database instance. If the instance doesn't exist, it creates it
         * in a thread-safe way.
         *
         * @param context The application context.
         * @return The singleton AppDatabase instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            // Return the existing instance if it's not null.
            // If it is null, create the database inside a synchronized block.
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sprachninja_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}