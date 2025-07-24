package com.vpk.sprachninja.data.repository

import com.vpk.sprachninja.data.local.LevelStats
import com.vpk.sprachninja.data.local.LevelStatsDao
import com.vpk.sprachninja.domain.repository.LevelStatsRepository
import kotlinx.coroutines.flow.Flow

class LevelStatsRepositoryImpl(
    private val levelStatsDao: LevelStatsDao
) : LevelStatsRepository {

    /**
     * This function now correctly calls the DAO method that returns a Flow.
     */
    override fun getStatsForLevel(level: String): Flow<LevelStats?> {
        return levelStatsDao.getStatsForLevelFlow(level)
    }

    override suspend fun incrementCorrectCount(level: String) {
        ensureStatsExist(level)
        levelStatsDao.incrementCorrectCount(level)
    }

    override suspend fun incrementWrongCount(level: String) {
        ensureStatsExist(level)
        levelStatsDao.incrementWrongCount(level)
    }

    /**
     * This suspend function now correctly calls the suspend DAO method.
     */
    private suspend fun ensureStatsExist(level: String) {
        if (levelStatsDao.getStatsForLevel(level) == null) {
            levelStatsDao.upsertStats(
                LevelStats(
                    germanLevel = level,
                    correctCount = 0,
                    wrongCount = 0
                )
            )
        }
    }
}