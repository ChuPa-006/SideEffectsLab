package com.example.sideeffectslab

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun Task1SplashScreen(onBack: () -> Unit) {
    var version by remember { mutableIntStateOf(0) }
    var runId by remember { mutableIntStateOf(0) }

    // Snapshot the Int into an immutable value. The callback closes over this version,
    // so an effect that captured the old lambda really will log the old version.
    val callbackVersion = version
    val onTimeout: () -> Unit = {
        Log.d("Task1Callback", "callback fired: v$callbackVersion")
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(500)
            version++
        }
    }

    TaskScreenScaffold(title = "Task 1: Splash screen", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Current callback version: v$version", style = MaterialTheme.typography.titleMedium)
            Text("Each timer waits 2 seconds. The parent changes the callback every 500 ms.")
            Button(onClick = { runId++ }) { Text("Restart all comparisons") }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(modifier = Modifier.weight(1f)) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Direct capture", style = MaterialTheme.typography.titleSmall)
                        Text("Expected: old version")
                        DirectCaptureVariant(onTimeout = onTimeout, runId = runId)
                    }
                }
                Card(modifier = Modifier.weight(1f)) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Updated state", style = MaterialTheme.typography.titleSmall)
                        Text("Expected: latest version")
                        UpdatedStateVariant(onTimeout = onTimeout, runId = runId)
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Extra experiment: callback as key", style = MaterialTheme.typography.titleMedium)
                    Text("This timer restarts whenever the callback changes. Because it changes every 500 ms, the 2-second wait normally never finishes.")
                    RestartOnCallbackVariant(onTimeout = onTimeout)
                }
            }
            Text("Check Logcat tags: Task1Direct, Task1Updated, Task1Restart. The callback version is printed in each message.")
        }
    }
}

@Composable
private fun DirectCaptureVariant(onTimeout: () -> Unit, runId: Int) {
    var status by remember(runId) { mutableStateOf("Waiting for 2 seconds...") }
    LaunchedEffect(runId) {
        status = "Timer running"
        delay(2_000)
        // Deliberately captures the original lambda. This is the stale-callback example.
        onTimeout()
        status = "Finished; inspect Logcat"
        Log.d("Task1Direct", "Direct-capture timer finished")
    }
    Text(status, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun UpdatedStateVariant(onTimeout: () -> Unit, runId: Int) {
    val currentOnTimeout by rememberUpdatedState(onTimeout)
    var status by remember(runId) { mutableStateOf("Waiting for 2 seconds...") }
    LaunchedEffect(runId) {
        status = "Timer running"
        delay(2_000)
        currentOnTimeout()
        status = "Finished; inspect Logcat"
        Log.d("Task1Updated", "Updated-state timer finished")
    }
    Text(status, style = MaterialTheme.typography.bodySmall)
}

@Composable
private fun RestartOnCallbackVariant(onTimeout: () -> Unit) {
    // The callback changes every 500 ms, so using it as a key cancels and restarts the wait.
    LaunchedEffect(onTimeout) {
        Log.d("Task1Restart", "Timer started for a new callback")
        try {
            delay(2_000)
            onTimeout()
        } finally {
            Log.d("Task1Restart", "Timer cancelled or restarted because the callback changed")
        }
    }
    Text("Watch Task1Restart in Logcat", style = MaterialTheme.typography.bodySmall)
}
