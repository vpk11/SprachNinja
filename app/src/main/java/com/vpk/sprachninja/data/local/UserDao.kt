package com.vpk.sprachninja.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Upsert
    suspend fun upsertUser(user: User)

    @Query("SELECT * FROM user LIMIT 1")
    fun getUser(): Flow<User?>

    /**
     * Updates the germanLevel for the single user in the database.
     * As this is a single-user app, we don't need a WHERE clause.
     *
     * @param newLevel The new German proficiency level to set.
     */
    @Query("UPDATE user SET germanLevel = :newLevel")
    suspend fun updateUserLevel(newLevel: String)
}