package com.vpk.sprachninja.domain.repository

import com.vpk.sprachninja.data.local.LevelStats
import kotlinx.coroutines.flow.Flow

/**
 * An interface that defines the contract for level statistics data operations.
 */
interface LevelStatsRepository {

    /**
     * Retrieves the statistics for a specific level as a reactive stream.
     * Note: A Flow is used here to allow the Profile screen to update automatically.
     *
     * @param level The German level to fetch stats for.
     * @return A Flow that emits the [LevelStats] object, or null if none exists.
     */
    fun getStatsForLevel(level: String): Flow<LevelStats?>

    /**
     * Increments the correct answer count for a given level.
     *
     * @param level The German level to update.
     */
    suspend fun incrementCorrectCount(level: String)

    /**
     * Increments the wrong answer count for a given level.
     *
     * @param level The German level to update.
     */
    suspend fun incrementWrongCount(level: String)
}