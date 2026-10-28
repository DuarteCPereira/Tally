package com.tally.ktx

import kotlin.coroutines.cancellation.CancellationException

/**
 * Like [runCatching], but rethrows [CancellationException] so a cancelled coroutine keeps
 * cancelling instead of being reported as an ordinary failure.
 */
@Suppress("TooGenericExceptionCaught")
inline fun <T> runCatchingNonCancellation(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (exception: CancellationException) {
    throw exception
} catch (exception: Exception) {
    Result.failure(exception)
}
