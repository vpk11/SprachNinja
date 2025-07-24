package com.vpk.sprachninja.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the User entity.
 * Defines the database interactions for the user table.
 */
@Dao
interface UserDao {

    /**
     * Inserts a new user or updates an existing one if it already exists.
     * The @Upsert annotation handles this logic automatically.
     *
     * @param user The user object to be inserted or updated.
     */
    @Upsert
    suspend fun upsertUser(user: User)

    /**
     * Retrieves the single user profile from the database.
     * Since this is a single-user app, we limit the result to one.
     * Returns a Flow to enable reactive updates in the UI.
     *
     * @return A Flow that emits the User object, or null if no user exists.
     */
    @Query("SELECT * FROM user LIMIT 1")
    fun getUser(): Flow<User?>
}