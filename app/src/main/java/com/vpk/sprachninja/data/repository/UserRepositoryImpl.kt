package com.vpk.sprachninja.data.repository

import com.vpk.sprachninja.data.local.User
import com.vpk.sprachninja.data.local.UserDao
import com.vpk.sprachninja.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {

    override suspend fun upsertUser(user: User) {
        userDao.upsertUser(user)
    }

    override fun getUser(): Flow<User?> {
        return userDao.getUser()
    }

    /**
     * Delegates the level update operation directly to the UserDao.
     */
    override suspend fun updateUserLevel(newLevel: String) {
        userDao.updateUserLevel(newLevel)
    }
}