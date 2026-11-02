package com.sourdough.companion.ui.bake

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sourdough.companion.data.db.BakeEntity
import com.sourdough.companion.data.db.BakeStepEntity
import com.sourdough.companion.data.db.TemplateEntity
import com.sourdough.companion.ui.Screen
import com.sourdough.engine.Phase
import com.sourdough.engine.PlannedStep
import com.sourdough.engine.Planner
import com.sourdough.engine.StepStatus
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** The home screen follows the bake: pick a template, then the mix checklist, then the live bake. */
@Composable
fun BakeHome(onOpen: (route: String) -> Unit, viewModel: BakeViewModel = viewModel(factory = BakeViewModel.Factory)) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bake = state.bake
    when {
        !state.loaded -> Unit
        bake == null -> TemplatePicker(state.templates, viewModel, onOpen)
        bake.mixedAt == null -> MixChecklist(bake, state.steps, viewModel)
        else -> LiveBake(bake, bake.mixedAt, state.steps, viewModel)
    }
}

@Composable
private fun TemplatePicker(templates: List<TemplateEntity>, viewModel: BakeViewModel, onOpen: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Start a bake", style = MaterialTheme.typography.headlineMedium) }
        item { AlarmSetup(viewModel) }
        items(templates, key = { it.id }) { template ->
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(template.name, style = MaterialTheme.typography.titleMedium)
                    template.notes?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                    Button(onClick = { viewModel.startBake(template.id) }) { Text("Start bake") }
                }
            }
        }
        item {
            Column(Modifier.padding(top = 16.dp)) {
                Text("Coming later", style = MaterialTheme.typography.labelLarge)
                Screen.entries
                    .filter { it != Screen.Home }
                    .forEach { screen -> TextButton(onClick = { onOpen(screen.route) }) { Text(screen.title) } }
            }
        }
    }
}

/** Notification permission, exact-alarm access and the Samsung battery setting, in one card. */
@Composable
private fun AlarmSetup(viewModel: BakeViewModel) {
    val context = LocalContext.current
    var notificationsGranted by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val requestNotifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        notificationsGranted = it
    }
    val exactAlarms = viewModel.canScheduleExactAlarms()

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Alarm setup", style = MaterialTheme.typography.titleSmall)
            if (!notificationsGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                FilledTonalButton(onClick = { requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                    Text("Allow notifications")
                }
            }
            if (!exactAlarms) {
                FilledTonalButton(onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}")),
                    )
                }) { Text("Allow alarms and reminders") }
            }
            Text(
                "Samsung: Settings → Battery → Background usage limits → Never sleeping apps → add Sourdough, " +
                    "or One UI may hold alarms back.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun MixChecklist(bake: BakeEntity, steps: List<BakeStepEntity>, viewModel: BakeViewModel) {
    val preMix = steps.filter { it.phase == Phase.PRE_MIX }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Mix checklist", style = MaterialTheme.typography.headlineMedium) }
        items(preMix, key = { it.id }) { step ->
            var checked by rememberSaveable(step.id) { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.Top) {
                Checkbox(checked = checked, onCheckedChange = { checked = it })
                Column(Modifier.padding(top = 12.dp)) {
                    Text(step.name, style = MaterialTheme.typography.titleMedium)
                    if (step.instructions.isNotBlank()) Text(step.instructions, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            Button(onClick = { viewModel.doneMixing(bake.id) }, modifier = Modifier.fillMaxWidth()) {
                Text("Done mixing")
            }
        }
        item {
            TextButton(onClick = { viewModel.endBake(bake.id) }) { Text("Cancel bake") }
        }
    }
}

@Composable
private fun LiveBake(bake: BakeEntity, mixedAt: Instant, steps: List<BakeStepEntity>, viewModel: BakeViewModel) {
    val now by produceState(Instant.now()) {
        while (true) {
            delay(1_000)
            value = Instant.now()
        }
    }
    val postMix = steps.filter { it.phase == Phase.POST_MIX }
    val plan = Planner.plan(mixedAt, postMix.map { it.toLiveStep() }, now)
    val currentIndex = Planner.currentIndex(plan, now)
    val nextAlarm = Planner.alarms(plan, now).minByOrNull { it.at }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (currentIndex == null) {
            item { Text("Bake complete", style = MaterialTheme.typography.headlineMedium) }
            item {
                Button(onClick = { viewModel.endBake(bake.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Finish bake")
                }
            }
        } else {
            val current = postMix[currentIndex]
            item { CurrentStep(current, plan[currentIndex], now) }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = { viewModel.markDone(bake.id, current.id) }) { Text("Done") }
                    OutlinedButton(onClick = { viewModel.addMinutes(bake.id, current.id) }) { Text("+15 min") }
                    OutlinedButton(onClick = { viewModel.skip(bake.id, current.id) }) { Text("Skip") }
                }
            }
            item {
                val nextText = nextAlarm?.let { "Next alarm ${formatTime(it.at)}, in ${formatCountdown(now, it.at)}" }
                    ?: "No alarm until you tap Done"
                Text(nextText, style = MaterialTheme.typography.bodyLarge)
            }
        }

        item { Text("Timeline", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp)) }
        itemsIndexed(plan, key = { _, p -> p.step.id }) { i, planned ->
            TimelineRow(planned, isCurrent = i == currentIndex, isPast = currentIndex == null || i < currentIndex)
        }
        item {
            TextButton(onClick = { viewModel.endBake(bake.id) }) { Text("End bake now") }
        }
    }
}

@Composable
private fun CurrentStep(step: BakeStepEntity, planned: PlannedStep, now: Instant) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Now", style = MaterialTheme.typography.labelLarge)
            Text(step.name, style = MaterialTheme.typography.headlineSmall)
            if (step.instructions.isNotBlank()) Text(step.instructions, style = MaterialTheme.typography.bodyMedium)
            val ends = if (step.waitForUser) "Check at" else "Ends at"
            Text(
                "$ends ${formatTime(planned.end)} (${formatCountdown(now, planned.end)})",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun TimelineRow(planned: PlannedStep, isCurrent: Boolean, isPast: Boolean) {
    val step = planned.step
    val marker = when {
        step.status == StepStatus.DONE -> "✓"
        step.status == StepStatus.SKIPPED -> "–"
        isCurrent -> "▶"
        isPast -> "✓"
        else -> ""
    }
    val repeats = if (step.spec.repeatCount > 1) " ×${step.spec.repeatCount}" else ""
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(marker, modifier = Modifier.width(24.dp))
        Text(formatTime(planned.start), modifier = Modifier.width(88.dp), style = MaterialTheme.typography.bodyMedium)
        Text(
            step.spec.name + repeats,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
        )
    }
}

private val timeFormat = DateTimeFormatter.ofPattern("EEE HH:mm")

private fun formatTime(at: Instant): String = timeFormat.format(at.atZone(ZoneId.systemDefault()))

private fun formatCountdown(now: Instant, at: Instant): String {
    val seconds = Duration.between(now, at).seconds
    if (seconds <= 0) return "now"
    return "%d:%02d:%02d".format(seconds / 3600, seconds % 3600 / 60, seconds % 60)
}
