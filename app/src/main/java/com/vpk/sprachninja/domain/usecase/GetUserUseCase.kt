package com.vpk.sprachninja.domain.usecase

import com.vpk.sprachninja.data.local.User
import com.vpk.sprachninja.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow

/**
 * A use case that encapsulates the business logic for retrieving the current user.
 *
 * @property userRepository The repository that provides user data operations.
 */
class GetUserUseCase(private val userRepository: UserRepository) {

    /**
     * Executes the use case to get the user profile as a Flow.
     * The `operator` keyword allows this class to be invoked as a function.
     */
    operator fun invoke(): Flow<User?> {
        return userRepository.getUser()
    }
}