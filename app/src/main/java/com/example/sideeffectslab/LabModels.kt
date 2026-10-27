package com.example.sideeffectslab

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay

/** A tiny external object that is not managed by Compose. */
object Analytics {
    @Volatile
    var userType: String = "free"
}

sealed interface ImageLoadResult {
    data object Loading : ImageLoadResult
    data class Success(val url: String) : ImageLoadResult
    data class Error(val url: String) : ImageLoadResult
}

class FakeImageRepository {
    suspend fun load(url: String): ImageLoadResult {
        Log.d("Task6ProduceState", "Repository starts loading: $url")
        try {
            delay(500)
            return if (url.endsWith(".png", ignoreCase = true)) {
                ImageLoadResult.Success(url)
            } else {
                ImageLoadResult.Error(url)
            }
        } catch (cancelled: CancellationException) {
            Log.d("Task6ProduceState", "Repository load cancelled: $url")
            throw cancelled
        }
    }
}
