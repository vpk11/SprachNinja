package com.vpk.sprachninja.data.repository

import com.vpk.sprachninja.data.local.RecentQuestion
import com.vpk.sprachninja.data.local.RecentQuestionDao
import com.vpk.sprachninja.domain.repository.RecentQuestionRepository

/**
 * The concrete implementation of the RecentQuestionRepository interface.
 *
 * @param recentQuestionDao The Data Access Object for the RecentQuestion entity.
 */
class RecentQuestionRepositoryImpl(
    private val recentQuestionDao: RecentQuestionDao
) : RecentQuestionRepository {

    override suspend fun insert(question: RecentQuestion) {
        recentQuestionDao.insert(question)
    }

    override suspend fun getRecentQuestionsForLevel(level: String): List<RecentQuestion> {
        return recentQuestionDao.getRecentQuestionsForLevel(level)
    }
}