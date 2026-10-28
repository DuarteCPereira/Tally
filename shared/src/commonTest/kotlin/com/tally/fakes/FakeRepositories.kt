package com.tally.fakes

import com.tally.data.ExerciseRepository
import com.tally.data.HistoryRepository
import com.tally.data.SettingsRepository
import com.tally.data.WorkoutRepository
import com.tally.domain.Exercise
import com.tally.domain.Session
import com.tally.domain.SessionEntry
import com.tally.domain.Workout
import com.tally.domain.WorkoutContent
import com.tally.domain.WorkoutSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update

/**
 * In-memory repositories. Each flow is backed by a nullable state that stays `null` (nothing
 * emitted) until the test loads it, which is how tests observe the "still loading" state.
 */
class FakeWorkoutRepository(
    workouts: List<Workout>? = emptyList(),
    contents: Map<String, WorkoutContent> = emptyMap(),
) : WorkoutRepository {
    val stored = MutableStateFlow(workouts)
    val contents = MutableStateFlow(contents)
    val deleted = mutableListOf<String>()
    val duplicated = mutableListOf<String>()

    override val workouts: Flow<List<Workout>> = stored.filterNotNull()

    override suspend fun workout(id: String) = stored.filterNotNull().first().find { it.id == id }

    override suspend fun content(workoutId: String) = contents.value[workoutId] ?: WorkoutContent.Empty

    override suspend fun save(workout: Workout, content: WorkoutContent) {
        stored.update { current ->
            val list = current.orEmpty()
            if (list.any { it.id == workout.id }) {
                list.map { if (it.id == workout.id) workout.copy(position = it.position) else it }
            } else {
                list + workout.copy(position = list.size)
            }
        }
        contents.update { it + (workout.id to content) }
    }

    override suspend fun duplicate(id: String): Workout? {
        duplicated += id
        return null
    }

    override suspend fun delete(id: String) {
        deleted += id
        stored.update { list -> list?.filterNot { it.id == id } }
    }

    override suspend fun reorder(ids: List<String>) = Unit
}

class FakeExerciseRepository(exercises: List<Exercise>? = emptyList()) : ExerciseRepository {
    val stored = MutableStateFlow(exercises)

    override val exercises: Flow<List<Exercise>> = stored.filterNotNull()

    override suspend fun save(exercise: Exercise) {
        stored.update { list -> list.orEmpty().filterNot { it.id == exercise.id } + exercise }
    }

    override suspend fun delete(id: String) {
        stored.update { list -> list?.filterNot { it.id == id } }
    }
}

class FakeSettingsRepository(settings: WorkoutSettings? = WorkoutSettings()) : SettingsRepository {
    val stored = MutableStateFlow(settings)

    override val settings: Flow<WorkoutSettings> = stored.filterNotNull()

    override suspend fun update(settings: WorkoutSettings) {
        stored.value = settings
    }
}

class FakeHistoryRepository(
    sessions: List<Session>? = emptyList(),
    entries: List<SessionEntry> = emptyList(),
) : HistoryRepository {
    val storedSessions = MutableStateFlow(sessions)
    val storedEntries = MutableStateFlow(entries)
    var failNextSave = false

    override val sessions: Flow<List<Session>> = storedSessions.filterNotNull()
    override val entries: Flow<List<SessionEntry>> = storedEntries

    override suspend fun addSession(session: Session, entries: List<SessionEntry>) {
        if (failNextSave) {
            failNextSave = false
            error("Disk full")
        }
        storedSessions.update { it.orEmpty() + session }
        storedEntries.update { it + entries }
    }
}
