package com.example.sideeffectslab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    SideEffectsLabApp()
                }
            }
        }
    }
}

private enum class LabTask(val screenTitle: String, val subtitle: String) {
    SPLASH("Task 1: Splash screen", "LaunchedEffect + rememberUpdatedState"),
    SNACKBAR("Task 2: Snackbar button", "rememberCoroutineScope"),
    LIFECYCLE("Task 3: Lifecycle logger", "DisposableEffect + onDispose"),
    ANALYTICS("Task 4: SideEffect and analytics", "Publish Compose state to external code"),
    SCROLL("Task 5: Scroll and snapshotFlow", "derivedStateOf + snapshotFlow"),
    PRODUCE_STATE("Task 6: produceState (bonus)", "Loading / Success / Error")
}

@Composable
private fun SideEffectsLabApp() {
    // The nullable first entry represents the home menu. A stack allows Task 2
    // to navigate to another screen and then return to the same task.
    val backStack = remember { mutableStateListOf<LabTask?>(null) }
    val currentTask = backStack.lastOrNull()
    val goBack: () -> Unit = {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }
    val navigateTo: (LabTask) -> Unit = { task -> backStack.add(task) }

    BackHandler(enabled = backStack.size > 1) { goBack() }

    when (currentTask) {
        null -> HomeScreen(onSelect = navigateTo)
        LabTask.SPLASH -> Task1SplashScreen(onBack = goBack)
        LabTask.SNACKBAR -> Task2SnackbarScreen(
            onBack = goBack,
            onNavigateAway = { navigateTo(LabTask.LIFECYCLE) }
        )
        LabTask.LIFECYCLE -> Task3LifecycleScreen(onBack = goBack)
        LabTask.ANALYTICS -> Task4SideEffectScreen(onBack = goBack)
        LabTask.SCROLL -> Task5ScrollScreen(onBack = goBack)
        LabTask.PRODUCE_STATE -> Task6ProduceStateScreen(onBack = goBack)
    }
}

@Composable
private fun HomeScreen(onSelect: (LabTask) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("SideEffectsLab") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Practice 9 — Side-effects in Jetpack Compose",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text("Open each task, try the required interaction, and check Logcat.")
            Spacer(Modifier.height(4.dp))
            LabTask.entries.forEach { task ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(task) }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(task.screenTitle, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(task.subtitle, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Logcat tags: Task1Direct, Task1Updated, Task1Restart, Task2Snackbar, Task3Lifecycle, Task4SideEffect, Task5Analytics, Task6ProduceState")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreenScaffold(
    title: String,
    onBack: () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 24.sp)
                    }
                }
            )
        },
        content = content
    )
}
