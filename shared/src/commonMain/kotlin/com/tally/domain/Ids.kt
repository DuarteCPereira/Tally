package com.tally.domain

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Returns a new random identifier for a persisted entity. */
@OptIn(ExperimentalUuidApi::class)
fun newId(): String = Uuid.random().toString()
