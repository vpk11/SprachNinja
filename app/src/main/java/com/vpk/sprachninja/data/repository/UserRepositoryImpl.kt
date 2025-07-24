package com.vpk.sprachninja.data.repository

import com.vpk.sprachninja.data.local.User
import com.vpk.sprachninja.data.local.UserDao
import com.vpk.sprachninja.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

/**
 * The concrete implementation of the UserRepository interface.
 * This class is responsible for coordinating data operations from the local data source (Room DAO).
 *
 * @param userDao The Data Access Object for the User entity.
 */
class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    /**
     * Delegates the upsert operation directly to the UserDao.
     */
    override suspend fun upsertUser(user: User) {
        userDao.upsertUser(user)
    }

    /**
     * Delegates the get operation directly to the UserDao.
     */
    override fun getUser(): Flow<User?> {
        return userDao.getUser()
    }
}