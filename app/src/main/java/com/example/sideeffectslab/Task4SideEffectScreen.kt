package com.example.sideeffectslab

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.concurrent.atomic.AtomicInteger

@Composable
fun Task4SideEffectScreen(onBack: () -> Unit) {
    var userType by remember { mutableStateOf("free") }
    var unrelatedCounter by remember { mutableIntStateOf(0) }
    var externalValueReadback by remember { mutableStateOf(Analytics.userType) }
    val sideEffectRuns = remember { AtomicInteger(0) }

    // This is the required SideEffect: it runs after every successful recomposition.
    // The counter is an AtomicInteger, not Compose State, so it does not create an infinite loop.
    SideEffect {
        Analytics.userType = userType
        val count = sideEffectRuns.incrementAndGet()
        Log.d("Task4SideEffect", "SideEffect run #$count; Analytics.userType=${Analytics.userType}")
    }

    TaskScreenScaffold(title = "Task 4: SideEffect", onBack = onBack) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Choose a user type. SideEffect publishes it to the external Analytics object after composition.")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("free", "premium", "admin").forEach { option ->
                    FilterChip(
                        selected = userType == option,
                        onClick = { userType = option },
                        label = { Text(option) }
                    )
                }
            }
            Text("Selected Compose state: $userType")
            Text("External Analytics value (last read): $externalValueReadback")
            Button(onClick = { externalValueReadback = Analytics.userType }) {
                Text("Read Analytics.userType")
            }
            Button(onClick = { unrelatedCounter++ }) {
                Text("Force recomposition without changing userType")
            }
            Text("Unrelated counter: $unrelatedCounter")
            Text("Observe Task4SideEffect in Logcat. It should run for userType changes and for unrelated recompositions too.")
        }
    }
}
