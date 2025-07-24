package com.vpk.sprachninja.domain.repository

import com.vpk.sprachninja.data.local.User
import kotlinx.coroutines.flow.Flow

/**
 * An interface that defines the contract for user data operations.
 * This abstraction decouples the domain layer from the data layer, allowing for
 * different data source implementations without changing the business logic.
 */
interface UserRepository {

    /**
     * Inserts a new user or updates an existing one.
     *
     * @param user The user profile to save.
     */
    suspend fun upsertUser(user: User)

    /**
     * Retrieves the current user profile as a reactive stream.
     *
     * @return A Flow that emits the current User, or null if no user is saved.
     */
    fun getUser(): Flow<User?>
}