package com.vpk.sprachninja.domain.repository

import com.vpk.sprachninja.data.local.RecentQuestion

/**
 * An interface that defines the contract for recent question data operations.
 */
interface RecentQuestionRepository {

    /**
     * Inserts a question into the history.
     * @param question The question to save.
     */
    suspend fun insert(question: RecentQuestion)

    /**
     * Retrieves recent questions for a specific level.
     * @param level The user's current German level.
     * @return A list of recent questions.
     */
    suspend fun getRecentQuestionsForLevel(level: String): List<RecentQuestion>
}