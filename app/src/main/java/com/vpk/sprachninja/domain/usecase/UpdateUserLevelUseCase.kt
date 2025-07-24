package com.vpk.sprachninja.domain.usecase

import com.vpk.sprachninja.domain.repository.UserRepository

/**
 * A use case that encapsulates the business logic for updating the user's German level.
 *
 * @property userRepository The repository that provides user data operations.
 */
class UpdateUserLevelUseCase(private val userRepository: UserRepository) {

    /**
     * Executes the use case to update the user's level.
     * The `operator` keyword allows this class to be invoked as a function.
     *
     * @param newLevel The new German proficiency level to set.
     */
    suspend operator fun invoke(newLevel: String) {
        // Here, you could add validation logic if needed, e.g.,
        // ensure the newLevel string matches a valid format.
        userRepository.updateUserLevel(newLevel)
    }
}