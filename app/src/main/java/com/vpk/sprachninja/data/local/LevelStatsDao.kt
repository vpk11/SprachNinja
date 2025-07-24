package com.vpk.sprachninja.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelStatsDao {

    /**
     * Retrieves the statistics for a single level as a reactive Flow.
     * Use this for observing changes in the UI.
     */
    @Query("SELECT * FROM levelstats WHERE germanLevel = :level")
    fun getStatsForLevelFlow(level: String): Flow<LevelStats?>

    /**
     * Retrieves the statistics for a single level as a one-shot suspend function.
     * Use this for imperative checks inside other suspend functions.
     */
    @Query("SELECT * FROM levelstats WHERE germanLevel = :level")
    suspend fun getStatsForLevel(level: String): LevelStats?

    @Upsert
    suspend fun upsertStats(stats: LevelStats)

    @Query("UPDATE levelstats SET correctCount = correctCount + 1 WHERE germanLevel = :level")
    suspend fun incrementCorrectCount(level: String)

    @Query("UPDATE levelstats SET wrongCount = wrongCount + 1 WHERE germanLevel = :level")
    suspend fun incrementWrongCount(level: String)
}