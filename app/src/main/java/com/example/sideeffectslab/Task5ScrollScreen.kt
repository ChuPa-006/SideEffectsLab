package com.example.sideeffectslab

import android.graphics.Color as AndroidColor
import android.util.Log
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.runtime.snapshotFlow
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@Composable
fun Task5ScrollScreen(onBack: () -> Unit) {
    val directListState = rememberLazyListState()
    val derivedListState = rememberLazyListState()
    val eventCount = remember { AtomicInteger(0) }
    val eventCounterView = remember { AtomicReference<TextView?>(null) }

    LaunchedEffect(derivedListState) {
        // snapshotFlow is cold: this chain starts when collect starts.
        snapshotFlow { derivedListState.firstVisibleItemIndex }
            .map { index -> index > 0 }
            .distinctUntilChanged()
            .filter { isPastFirstItem -> isPastFirstItem }
            .collect {
                val count = eventCount.incrementAndGet()
                Log.d("Task5Analytics", "sendScrolledPastFirstItemEvent #$count")
                eventCounterView.get()?.text = "Analytics events: $count"
            }
    }

    TaskScreenScaffold(title = "Task 5: Scroll", onBack = onBack) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("Scroll both lists. Direct reading tracks every index change; derivedStateOf tracks only whether index > 0.")
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(24.dp),
                factory = { context ->
                    TextView(context).apply {
                        textSize = 13f
                        setTextColor(AndroidColor.BLACK)
                        text = "Analytics events: 0"
                        eventCounterView.set(this)
                    }
                },
                update = { view ->
                    eventCounterView.set(view)
                    view.text = "Analytics events: ${eventCount.get()}"
                }
            )
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DirectScrollVariant(
                    listState = directListState,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
                DerivedScrollVariant(
                    listState = derivedListState,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                )
            }
            Text("Logcat tags: Task5DirectCompositions, Task5DerivedCompositions, Task5Analytics")
        }
    }
}

@Composable
private fun DirectScrollVariant(
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    val showButton = listState.firstVisibleItemIndex > 0
    ScrollVariantBody(
        title = "Direct read",
        listState = listState,
        showButton = showButton,
        counterTag = "Task5DirectCompositions",
        modifier = modifier
    )
}

@Composable
private fun DerivedScrollVariant(
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    val showButton by remember(listState) {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }
    ScrollVariantBody(
        title = "derivedStateOf",
        listState = listState,
        showButton = showButton,
        counterTag = "Task5DerivedCompositions",
        modifier = modifier
    )
}

@Composable
private fun ScrollVariantBody(
    title: String,
    listState: androidx.compose.foundation.lazy.LazyListState,
    showButton: Boolean,
    counterTag: String,
    modifier: Modifier
) {
    val scope = rememberCoroutineScope()
    val compositionCount = remember { AtomicInteger(0) }
    val counterView = remember { AtomicReference<TextView?>(null) }

    Card(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title)
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(32.dp),
                factory = { context ->
                    TextView(context).apply {
                        textSize = 11f
                        setTextColor(AndroidColor.BLACK)
                        text = "Compositions: 0"
                        counterView.set(this)
                    }
                },
                update = { view ->
                    counterView.set(view)
                    view.text = "Compositions: ${compositionCount.get()}"
                }
            )
            // The counter is outside Compose State to avoid a SideEffect -> state update -> recomposition loop.
            SideEffect {
                val count = compositionCount.incrementAndGet()
                counterView.get()?.text = "Compositions: $count"
                Log.d(counterTag, "Successful composition #$count")
            }
            if (showButton) {
                Button(onClick = { scope.launch { listState.animateScrollToItem(0) } }) {
                    Text("To top")
                }
            } else {
                Text("At top (button hidden)")
            }
            Spacer(Modifier.height(2.dp))
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
                items((1..100).toList()) { number ->
                    Text(
                        text = "Message $number",
                        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp, horizontal = 3.dp)
                    )
                }
            }
        }
    }
}
