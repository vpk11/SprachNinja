package com.vpk.sprachninja.domain.usecase

import com.vpk.sprachninja.data.local.User
import com.vpk.sprachninja.domain.repository.UserRepository

/**
 * A use case that encapsulates the business logic for saving a user's profile.
 * This class follows the Single Responsibility Principle, with its only purpose
 * being to save a user.
 *
 * @property userRepository The repository that provides user data operations.
 */
class SaveUserUseCase(private val userRepository: UserRepository) {

    /**
     * Executes the use case to save the user profile.
     * The `operator` keyword allows this class to be invoked as a function.
     *
     * @param user The user object to be saved.
     */
    suspend operator fun invoke(user: User) {
        userRepository.upsertUser(user)
    }
}