package com.vpk.sprachninja.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Curriculum(
    val levels: List<Level>
)

@Serializable
data class Level(
    val level: String,
    @SerialName("sub_levels")
    val subLevels: List<SubLevel>
)

@Serializable
data class SubLevel(
    @SerialName("sub_level")
    val subLevelName: String,
    val topics: List<String>
)