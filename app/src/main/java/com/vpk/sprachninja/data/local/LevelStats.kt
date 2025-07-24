package com.vpk.sprachninja.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents the user's statistics for a specific German proficiency level.
 * Each row in this table corresponds to one level (e.g., "A1.1").
 *
 * @property germanLevel The proficiency level these stats are for. This is the primary key.
 * @property correctCount The total number of correctly answered questions for this level.
 * @property wrongCount The total number of incorrectly answered questions for this level.
 */
@Entity(tableName = "levelstats")
data class LevelStats(
    @PrimaryKey
    val germanLevel: String,
    val correctCount: Int,
    val wrongCount: Int
)