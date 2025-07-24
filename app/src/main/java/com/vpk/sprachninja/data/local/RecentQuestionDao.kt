package com.vpk.sprachninja.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

/**
 * Data Access Object for the RecentQuestion entity.
 * Defines the database interactions for the recent questions table.
 */
@Dao
interface RecentQuestionDao {

    /**
     * Inserts a new recent question into the database.
     * If a question with the same text already exists, it will be ignored.
     *
     * @param question The RecentQuestion object to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(question: RecentQuestion)

    /**
     * Retrieves the last 20 questions asked for a specific user level.
     *
     * @param level The user level (e.g., "A1.1") to fetch questions for.
     * @return A list of the most recent [RecentQuestion] objects.
     */
    @Query("SELECT * FROM recentquestion WHERE userLevel = :level ORDER BY id DESC LIMIT 20")
    suspend fun getRecentQuestionsForLevel(level: String): List<RecentQuestion>
}