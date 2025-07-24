package com.vpk.sprachninja.data.repository

import com.vpk.sprachninja.data.local.LevelStats
import com.vpk.sprachninja.data.local.LevelStatsDao
import com.vpk.sprachninja.domain.repository.LevelStatsRepository
import kotlinx.coroutines.flow.Flow

/**
 * The concrete implementation of the LevelStatsRepository interface.
 *
 * @param levelStatsDao The Data Access Object for the LevelStats entity.
 */
class LevelStatsRepositoryImpl(
    private val levelStatsDao: LevelStatsDao
) : LevelStatsRepository {

    override fun getStatsForLevel(level: String): Flow<LevelStats?> {
        return levelStatsDao.getStatsForLevel(level)
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
     * A private helper function to check if a stats entry for a given level exists.
     * If it doesn't, it creates a new entry with default zero counts.
     * This prevents errors when trying to update a non-existent row.
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