package com.example.sideeffectslab

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun Task3LifecycleScreen(onBack: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current
    var showObserver by remember { mutableStateOf(true) }

    TaskScreenScaffold(title = "Task 3: Lifecycle logger", onBack = onBack) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Toggle the switch several times. Every addObserver line should have a matching removeObserver line.")
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Observer visible")
                Switch(checked = showObserver, onCheckedChange = { showObserver = it })
            }
            if (showObserver) {
                LifecycleLoggerPanel(lifecycleOwner)
                Text("The listener is currently registered. Leave this screen or turn the switch off to run onDispose.")
            } else {
                Text("The listener composable is hidden; its observer should have been removed.")
            }
            Text("Logcat tag: Task3Lifecycle. Also press Home and return to the app to observe ON_STOP / ON_START when available.")
        }
    }
}

@Composable
private fun LifecycleLoggerPanel(lifecycleOwner: LifecycleOwner) {
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> Log.d("Task3Lifecycle", "ON_START")
                Lifecycle.Event.ON_STOP -> Log.d("Task3Lifecycle", "ON_STOP")
                else -> Log.d("Task3Lifecycle", event.name)
            }
        }

        Log.d("Task3Lifecycle", "addObserver")
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            Log.d("Task3Lifecycle", "removeObserver (onDispose)")
        }
    }
}
