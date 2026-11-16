package com.sourdough.companion.ui.bake

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sourdough.companion.data.BakeStatus
import com.sourdough.companion.data.db.BakeEntity
import com.sourdough.companion.data.db.TemplateEntity
import com.sourdough.companion.data.formatCountdown
import com.sourdough.companion.ui.components.ConfirmDialog
import com.sourdough.companion.ui.components.DefaultBadge
import com.sourdough.companion.ui.components.FlavourChip
import com.sourdough.companion.ui.components.FlavourPickerDialog
import com.sourdough.companion.ui.components.StepIcon
import com.sourdough.companion.ui.components.formatTime
import com.sourdough.companion.ui.components.rememberNow
import com.sourdough.engine.Phase
import com.sourdough.engine.PlannedStep
import com.sourdough.engine.Planner
import com.sourdough.engine.StepStatus
import java.time.Instant

/** The Bake tab follows the bake: one Start bake button, then the mix checklist, then the live timer. */
@Composable
fun BakeHome(
    viewModel: BakeViewModel,
    onBakeEnded: (bakeId: Long) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val bake = state.bake
    when {
        !state.loaded -> Unit
        bake == null -> StartBake(state.sortedTemplates, state.defaultTemplateId, viewModel)
        bake.mixedAt == null -> MixChecklist(state, bake, viewModel)
        else -> LiveBake(state, bake, bake.mixedAt, viewModel, onBakeEnded)
    }
}

/**
 * One big Start bake button for the default schedule. Another schedule can be picked for this bake
 * only; editing schedules lives in the Schedules tab.
 */
@Composable
private fun StartBake(templates: List<TemplateEntity>, defaultId: Long?, viewModel: BakeViewModel) {
    var chosenId by rememberSaveable { mutableStateOf<Long?>(null) }
    var picking by remember { mutableStateOf(false) }
    val chosen = templates.firstOrNull { it.id == chosenId }
        ?: templates.firstOrNull { it.id == defaultId }
        ?: templates.firstOrNull()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AlarmSetup(viewModel)
        Text("🍞", fontSize = 88.sp, modifier = Modifier.padding(top = 32.dp))
        Text("Ready to bake?", style = MaterialTheme.typography.headlineMedium)

        if (chosen == null) {
            Text("Add a schedule in the Schedules tab first.", style = MaterialTheme.typography.bodyLarge)
            return@Column
        }

        Card(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (chosen.id == defaultId) DefaultBadge()
                Text(chosen.name, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                chosen.notes?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
        Button(onClick = { viewModel.startBake(chosen.id) }, modifier = Modifier.fillMaxWidth().height(64.dp)) {
            Text("Start bake", style = MaterialTheme.typography.titleMedium)
        }
        if (templates.size > 1) {
            TextButton(onClick = { picking = true }) { Text("Use a different schedule") }
        }
    }

    if (picking) {
        AlertDialog(
            onDismissRequest = { picking = false },
            title = { Text("Which schedule?") },
            text = {
                Column {
                    templates.forEach { template ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { chosenId = template.id; picking = false },
                        ) {
                            RadioButton(selected = template.id == chosen?.id, onClick = { chosenId = template.id; picking = false })
                            Text(template.name + if (template.id == defaultId) "  ★" else "")
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { picking = false }) { Text("Close") } },
        )
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
    // Out of the way once everything is allowed.
    if (notificationsGranted && exactAlarms) return

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
private fun MixChecklist(state: BakeUiState, bake: BakeEntity, viewModel: BakeViewModel) {
    val preMix = state.steps.filter { it.phase == Phase.PRE_MIX }
    var confirmCancel by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Mix checklist", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 16.dp)) }
        item { FlavourRow(state, bake, viewModel) }
        items(preMix, key = { it.id }) { step ->
            var checked by rememberSaveable(step.id) { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.Top) {
                Checkbox(checked = checked, onCheckedChange = { checked = it })
                StepIcon(step.name, size = 28.dp, modifier = Modifier.padding(top = 10.dp, end = 8.dp))
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
            TextButton(onClick = { confirmCancel = true }) { Text("Cancel bake") }
        }
    }
    if (confirmCancel) {
        ConfirmDialog(
            title = "Cancel this bake?",
            text = "It hasn't started yet, so it won't be saved to the log.",
            confirm = "Cancel bake",
            onConfirm = { viewModel.cancelBake(bake.id) },
            onDismiss = { confirmCancel = false },
        )
    }
}

/** The flavour chip plus schedule name; the flavour can be changed at any point in the bake. */
@Composable
private fun FlavourRow(state: BakeUiState, bake: BakeEntity, viewModel: BakeViewModel) {
    var picking by remember { mutableStateOf(false) }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FlavourChip(state.flavour, onClick = { picking = true })
        state.templateName?.let {
            Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (picking) {
        FlavourPickerDialog(
            flavours = state.flavours,
            selectedId = bake.flavourId,
            onPick = { viewModel.setFlavour(bake.id, it); picking = false },
            onAdd = { viewModel.addFlavour(bake.id, it); picking = false },
            onDismiss = { picking = false },
        )
    }
}

@Composable
private fun LiveBake(
    state: BakeUiState,
    bake: BakeEntity,
    mixedAt: Instant,
    viewModel: BakeViewModel,
    onBakeEnded: (Long) -> Unit,
) {
    val now = rememberNow()
    val status = BakeStatus.of(bake, state.steps, now)
    val plan = status?.plan ?: Planner.plan(mixedAt, state.steps.filter { it.phase == Phase.POST_MIX }.map { it.toLiveStep() }, now)
    var confirmEnd by remember { mutableStateOf(false) }
    var confirmStop by remember { mutableStateOf(false) }
    // Steps that never started but were skipped mean the timers were stopped early, not finished.
    val timersStopped = state.steps.lastOrNull { it.phase == Phase.POST_MIX }
        ?.let { it.status == StepStatus.SKIPPED && it.doneAt == null } == true

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item { Box(Modifier.fillMaxWidth().padding(top = 12.dp)) { FlavourRow(state, bake, viewModel) } }

        if (status == null) {
            item { BakeComplete(timersStopped, onFinish = { viewModel.endBake(bake.id, onBakeEnded) }) }
        } else {
            item { TimerHero(status, now) }
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(status.step.name, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                    if (status.step.instructions.isNotBlank()) {
                        Text(
                            status.step.instructions,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { viewModel.markDone(bake.id, status.step.id) }, modifier = Modifier.weight(1.4f)) {
                        Text("Done")
                    }
                    OutlinedButton(onClick = { viewModel.addMinutes(bake.id, status.step.id) }, modifier = Modifier.weight(1f)) {
                        Text("+15 min")
                    }
                    OutlinedButton(onClick = { viewModel.skip(bake.id, status.step.id) }, modifier = Modifier.weight(1f)) {
                        Text("Skip")
                    }
                }
            }
            item {
                TextButton(onClick = { confirmStop = true }) { Text("⏹  Stop timers") }
            }
        }

        item {
            Card(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Timeline", style = MaterialTheme.typography.titleSmall)
                    plan.forEachIndexed { i, planned ->
                        val current = status?.currentIndex
                        TimelineRow(planned, isCurrent = i == current, isPast = current == null || i < current)
                    }
                }
            }
        }
        // Once nothing is left to time, "Rate it" above does the same job.
        if (status != null) {
            item {
                TextButton(onClick = { confirmEnd = true }, modifier = Modifier.padding(bottom = 16.dp)) { Text("End bake now") }
            }
        }
    }

    if (confirmStop) {
        ConfirmDialog(
            title = "Stop all timers?",
            text = "No more alarms or countdown for this bake. It stays here so you can rate it once it's baked.",
            confirm = "Stop timers",
            onConfirm = { confirmStop = false; viewModel.stopTimers(bake.id) },
            onDismiss = { confirmStop = false },
        )
    }
    if (confirmEnd) {
        ConfirmDialog(
            title = "End this bake now?",
            text = "Its alarms stop and it moves to the log, where you can rate it.",
            confirm = "End bake",
            onConfirm = { confirmEnd = false; viewModel.endBake(bake.id, onBakeEnded) },
            onDismiss = { confirmEnd = false },
        )
    }
}

/** The big countdown: a progress ring with the next thing and the time left inside it. */
@Composable
private fun TimerHero(status: BakeStatus, now: Instant) {
    val remaining = status.remaining(now)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
        CircularProgressIndicator(
            progress = { status.progress(now) },
            modifier = Modifier.size(280.dp),
            strokeWidth = 14.dp,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(220.dp)) {
            StepIcon(status.step.name, size = 52.dp)
            Text(
                if (remaining == null) "Check now" else formatCountdown(remaining),
                style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
            )
            Text(
                if (remaining == null) "Tap Done when it's ready" else "until ${status.nextLabel}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            status.target?.let {
                Text(formatTime(it), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun BakeComplete(timersStopped: Boolean, onFinish: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 32.dp),
    ) {
        Text(if (timersStopped) "❄️" else "🎉", style = MaterialTheme.typography.displayLarge)
        Text(if (timersStopped) "Timers off" else "Bake complete", style = MaterialTheme.typography.headlineMedium)
        Text(
            if (timersStopped) "No more alarms for this one. Come back and rate it once it's baked." else "How did it turn out?",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onFinish) { Text(if (timersStopped) "Rate it" else "Rate and finish") }
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().alpha(if (step.isFinished || isPast) 0.5f else 1f),
    ) {
        Text(marker, modifier = Modifier.width(18.dp), color = MaterialTheme.colorScheme.primary)
        StepIcon(step.spec.name, size = 24.dp, modifier = Modifier.padding(end = 8.dp))
        Text(formatTime(planned.start), modifier = Modifier.width(80.dp), style = MaterialTheme.typography.bodySmall)
        Text(
            step.spec.name + repeats,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
