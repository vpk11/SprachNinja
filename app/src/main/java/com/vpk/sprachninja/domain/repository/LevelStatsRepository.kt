package com.vpk.sprachninja.domain.repository

import com.vpk.sprachninja.data.local.LevelStats
import kotlinx.coroutines.flow.Flow

interface LevelStatsRepository {

    /**
     * Retrieves the statistics for a specific level as a reactive stream.
     */
    fun getStatsForLevel(level: String): Flow<LevelStats?>

    /**
     * Increments the correct answer count for a given level.
     */
    suspend fun incrementCorrectCount(level: String)

    /**
     * Increments the wrong answer count for a given level.
     */
    suspend fun incrementWrongCount(level: String)
}