package com.example.sideeffectslab

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger

@Composable
fun Task6ProduceStateScreen(onBack: () -> Unit) {
    var url by remember { mutableStateOf("https://example.com/picture.png") }
    val repository = remember { FakeImageRepository() }
    val compositionCount = remember { AtomicInteger(0) }
    val loadState by loadNetworkImage(url, repository, compositionCount)

    SideEffect {
        val count = compositionCount.incrementAndGet()
        Log.d("Task6ProduceState", "UI successful composition #$count")
    }

    TaskScreenScaffold(title = "Task 6: produceState (bonus)", onBack = onBack) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("The repository waits 500 ms. URLs ending in .png return Success; other URLs return Error.")
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Image URL") },
                singleLine = true
            )
            Button(onClick = { url = "https://example.com/photo.png" }) {
                Text("Try .png (Success)")
            }
            Button(onClick = { url = "https://example.com/photo.jpg" }) {
                Text("Try .jpg (Error)")
            }
            Text("Result:")
            when (val result = loadState) {
                ImageLoadResult.Loading -> Text("Loading…")
                is ImageLoadResult.Success -> Text("Success: ${result.url}")
                is ImageLoadResult.Error -> Text("Error: ${result.url}")
            }
            Text("Change the URL while loading to cancel the old producer. Navigate away to cancel the active producer; inspect Task6ProduceState in Logcat.")
            Text("The producer deliberately stays active after loading so the lab can demonstrate cancellation in finally when the URL changes or this screen leaves.")
        }
    }
}

@Composable
private fun loadNetworkImage(
    url: String,
    repository: FakeImageRepository,
    uiCompositionCount: AtomicInteger
): State<ImageLoadResult> {
    return produceState<ImageLoadResult>(
        initialValue = ImageLoadResult.Loading,
        key1 = url,
        key2 = repository
    ) {
        // produceState retains its State object across key changes, so explicitly restore Loading.
        value = ImageLoadResult.Loading
        Log.d("Task6ProduceState", "Producer started for $url")
        try {
            val result = repository.load(url)
            value = result

            // Verify that assigning the same value does not cause another recomposition.
            delay(250)
            val before = uiCompositionCount.get()
            Log.d("Task6ProduceState", "Assigning identical result; composition count before=$before")
            value = result // structural equality: no state change, so no recomposition from this assignment
            delay(250)
            val after = uiCompositionCount.get()
            Log.d("Task6ProduceState", "Composition count after identical assignment=$after (should equal before)")

            // Kept active for this lab so navigating away visibly exercises producer cancellation.
            awaitCancellation()
        } catch (cancelled: CancellationException) {
            Log.d("Task6ProduceState", "Producer cancelled for $url")
            throw cancelled
        } finally {
            Log.d("Task6ProduceState", "Producer finally: $url")
        }
    }
}
