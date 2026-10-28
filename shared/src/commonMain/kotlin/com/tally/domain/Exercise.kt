package com.tally.domain

/** An exercise in the user's library, with the defaults used when it is added to a workout. */
data class Exercise(
    val id: String,
    val name: String,
    val defaultSets: Int = 3,
    val defaultReps: Int = 10,
    val defaultRestSeconds: Int = 60,
    val notes: String? = null,
    val imagePath: String? = null,
)
