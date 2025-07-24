package com.vpk.sprachninja.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

/**
 * Data Access Object for the LevelStats entity.
 * Defines the database interactions for the level statistics table.
 */
@Dao
interface LevelStatsDao {

    /**
     * Retrieves the statistics for a single, specific level.
     *
     * @param level The German level (e.g., "A1.1") to fetch stats for.
     * @return The [LevelStats] object for the level, or null if no stats exist yet.
     */
    @Query("SELECT * FROM levelstats WHERE germanLevel = :level")
    suspend fun getStatsForLevel(level: String): LevelStats?

    /**
     * Inserts a new stats record or updates an existing one.
     *
     * @param stats The [LevelStats] object to save.
     */
    @Upsert
    suspend fun upsertStats(stats: LevelStats)

    /**
     * Increments the correct answer count for a specific level by one.
     *
     * @param level The German level for which to increment the count.
     */
    @Query("UPDATE levelstats SET correctCount = correctCount + 1 WHERE germanLevel = :level")
    suspend fun incrementCorrectCount(level: String)

    /**
     * Increments the wrong answer count for a specific level by one.
     *
     * @param level The German level for which to increment the count.
     */
    @Query("UPDATE levelstats SET wrongCount = wrongCount + 1 WHERE germanLevel = :level")
    suspend fun incrementWrongCount(level: String)
}