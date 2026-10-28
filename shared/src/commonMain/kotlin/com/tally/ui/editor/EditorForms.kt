package com.tally.ui.editor

import com.tally.domain.Exercise

/** The outcome of validating a form: either the parsed value or a message to show the user. */
sealed interface FormResult<out T> {
    data class Valid<T>(val value: T) : FormResult<T>
    data class Invalid(val message: String) : FormResult<Nothing>
}

/** The raw text of the exercise dialog. Numbers stay strings until [validate] parses them. */
data class ExerciseForm(
    val name: String = "",
    val sets: String = "3",
    val reps: String = "10",
    val rest: String = "60",
    val notes: String = "",
    val imagePath: String? = null,
) {
    /** Parses the form into an [Exercise] with the given [id]. */
    fun validate(id: String): FormResult<Exercise> {
        if (name.isBlank()) return FormResult.Invalid("Enter an exercise name.")
        val counts = Counts.parse(sets, reps, rest)
            ?: return FormResult.Invalid("Use positive sets and reps, and a non-negative rest.")
        return FormResult.Valid(
            Exercise(
                id = id,
                name = name.trim(),
                defaultSets = counts.repeats,
                defaultReps = counts.reps,
                defaultRestSeconds = counts.restSeconds,
                notes = notes.trim().ifEmpty { null },
                imagePath = imagePath,
            ),
        )
    }

    companion object {
        fun of(exercise: Exercise) = ExerciseForm(
            name = exercise.name,
            sets = exercise.defaultSets.toString(),
            reps = exercise.defaultReps.toString(),
            rest = exercise.defaultRestSeconds.toString(),
            notes = exercise.notes.orEmpty(),
            imagePath = exercise.imagePath,
        )
    }
}

/** The raw text of the superset dialog. */
data class SupersetForm(
    val members: List<Exercise> = emptyList(),
    val rounds: String = "3",
    val reps: String = "10",
    val rest: String = "60",
) {
    /** Parses the form into a superset block with the given [key]. */
    fun validate(key: String): FormResult<DraftBlock.Superset> {
        if (members.size < MIN_SUPERSET_MEMBERS) {
            return FormResult.Invalid("A superset needs at least two exercises.")
        }
        val counts = Counts.parse(rounds, reps, rest)
            ?: return FormResult.Invalid("Enter positive rounds and reps, and a non-negative rest.")
        return FormResult.Valid(
            DraftBlock.Superset(key, members, counts.repeats, counts.reps, counts.restSeconds),
        )
    }

    companion object {
        private const val MIN_SUPERSET_MEMBERS = 2

        fun of(superset: DraftBlock.Superset) = SupersetForm(
            members = superset.members,
            rounds = superset.rounds.toString(),
            reps = superset.reps.toString(),
            rest = superset.restSeconds.toString(),
        )
    }
}

/** Sets or rounds and reps of at least one, and a rest of zero or more seconds. */
private data class Counts(val repeats: Int, val reps: Int, val restSeconds: Int) {
    companion object {
        fun parse(repeats: String, reps: String, rest: String): Counts? = Counts(
            repeats = repeats.toIntOrNull()?.takeIf { it >= 1 } ?: return null,
            reps = reps.toIntOrNull()?.takeIf { it >= 1 } ?: return null,
            restSeconds = rest.toIntOrNull()?.takeIf { it >= 0 } ?: return null,
        )
    }
}
