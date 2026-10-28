package com.tally.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.tally.database.WorkoutDatabase
import com.tally.domain.Session
import com.tally.domain.SessionEntry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface HistoryRepository {
    /** Every finished session, most recent first. */
    val sessions: Flow<List<Session>>

    /** The entries of every session, ordered by position within their session. */
    val entries: Flow<List<SessionEntry>>

    suspend fun addSession(session: Session, entries: List<SessionEntry> = emptyList())
}

class SqlDelightHistoryRepository(
    database: WorkoutDatabase,
    private val io: CoroutineDispatcher = Dispatchers.IO,
) : HistoryRepository {
    private val queries = database.exerciseQueries

    override val sessions: Flow<List<Session>> = queries.selectAllSessions()
        .asFlow()
        .mapToList(io)
        .map { rows -> rows.map { it.toSession() } }

    override val entries: Flow<List<SessionEntry>> = queries.selectAllSessionEntries()
        .asFlow()
        .mapToList(io)
        .map { rows -> rows.map { it.toSessionEntry() } }

    override suspend fun addSession(session: Session, entries: List<SessionEntry>) = withContext(io) {
        queries.transaction {
            queries.insertSession(
                id = session.id,
                workoutId = session.workoutId,
                workoutName = session.workoutName,
                startedAt = session.startedAt.toString(),
                durationSeconds = session.durationSeconds.toLong(),
            )
            entries.forEachIndexed { index, entry ->
                queries.insertSessionEntry(
                    id = entry.id,
                    sessionId = session.id,
                    position = index.toLong(),
                    exerciseName = entry.exerciseName,
                    itemType = entry.itemType.name,
                    setNumber = entry.setNumber?.toLong(),
                    roundNumber = entry.roundNumber?.toLong(),
                    targetReps = entry.targetReps.toLong(),
                    actualReps = entry.actualReps?.toLong(),
                    completed = if (entry.completed) 1L else 0L,
                )
            }
        }
    }
}
