package com.vpk.sprachninja.domain.repository

import com.vpk.sprachninja.data.local.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun upsertUser(user: User)

    fun getUser(): Flow<User?>

    /**
     * Updates the user's current German proficiency level.
     *
     * @param newLevel The new level to be saved.
     */
    suspend fun updateUserLevel(newLevel: String)
}