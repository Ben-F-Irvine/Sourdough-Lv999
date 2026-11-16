package com.sourdough.companion.ui.log

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sourdough.companion.SourdoughApp
import com.sourdough.companion.data.BakeRepository
import com.sourdough.companion.data.FlavourIcons
import com.sourdough.companion.data.db.BakeSummary
import com.sourdough.companion.ui.components.ConfirmDialog
import com.sourdough.companion.ui.components.FlavourChip
import com.sourdough.companion.ui.components.FlavourPickerDialog
import com.sourdough.companion.ui.components.ScreenHeader
import com.sourdough.companion.ui.components.StepIcon
import com.sourdough.companion.ui.components.formatDate
import com.sourdough.companion.ui.components.formatTime
import com.sourdough.engine.StepStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class BakeLogViewModel(private val repository: BakeRepository) : ViewModel() {
    val log = repository.log.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun logPastBake(onCreated: (Long) -> Unit) = viewModelScope.launch { onCreated(repository.logPastBake()) }

    companion object {
        val Factory = viewModelFactory {
            initializer { BakeLogViewModel((this[APPLICATION_KEY] as SourdoughApp).container.bakeRepository) }
        }
    }
}

class BakeDetailViewModel(savedState: SavedStateHandle, private val repository: BakeRepository) : ViewModel() {
    private val bakeId: Long = checkNotNull(savedState["bakeId"])

    val summary = repository.summary(bakeId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val steps = repository.steps(bakeId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val flavours = repository.flavours.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setRating(rating: Int?) = viewModelScope.launch { repository.setRating(bakeId, rating) }

    fun setNotes(notes: String) = viewModelScope.launch { repository.setNotes(bakeId, notes) }

    fun setDate(at: Instant) = viewModelScope.launch { repository.setFinishedAt(bakeId, at) }

    fun setFlavour(id: Long) = viewModelScope.launch { repository.setFlavour(bakeId, id) }

    fun addFlavour(name: String) = viewModelScope.launch { repository.setFlavour(bakeId, repository.addFlavour(name)) }

    fun delete(onDeleted: () -> Unit) = viewModelScope.launch {
        repository.deleteBake(bakeId)
        onDeleted()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SourdoughApp
                BakeDetailViewModel(createSavedStateHandle(), app.container.bakeRepository)
            }
        }
    }
}

@Composable
fun BakeLogScreen(onOpenBake: (Long) -> Unit, viewModel: BakeLogViewModel = viewModel(factory = BakeLogViewModel.Factory)) {
    val log by viewModel.log.collectAsStateWithLifecycle()
    val bakes = log ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                Text("Bake log", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { viewModel.logPastBake(onOpenBake) }) { Text("+ Add a bake") }
            }
        }
        if (bakes.isEmpty()) {
            item {
                Text(
                    "Finished bakes show up here, with your rating and notes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(bakes, key = { it.bake.id }) { summary -> LogCard(summary, onClick = { onOpenBake(summary.bake.id) }) }
    }
}

@Composable
private fun LogCard(summary: BakeSummary, onClick: () -> Unit) {
    val bake = summary.bake
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(FlavourIcons.emoji(summary.flavourName), style = MaterialTheme.typography.headlineMedium)
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text(summary.flavourName ?: "Sourdough", style = MaterialTheme.typography.titleMedium)
                Text(
                    listOfNotNull(bake.finishedAt?.let(::formatDate), summary.templateName).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                bake.notes?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Text(stars(bake.rating), color = MaterialTheme.colorScheme.primary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BakeDetailScreen(onBack: () -> Unit, viewModel: BakeDetailViewModel = viewModel(factory = BakeDetailViewModel.Factory)) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val flavours by viewModel.flavours.collectAsStateWithLifecycle()
    val loaded = summary ?: return
    val bake = loaded.bake

    // Typed notes live here and are written through; reading them back would move the cursor.
    var notes by remember(bake.id) { mutableStateOf(bake.notes.orEmpty()) }
    var pickingFlavour by remember { mutableStateOf(false) }
    var pickingDate by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ScreenHeader("Your bake", onBack) }
        item {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FlavourChip(flavours.firstOrNull { it.id == bake.flavourId }, onClick = { pickingFlavour = true })
                TextButton(onClick = { pickingDate = true }) { Text("📅 ${bake.finishedAt?.let(::formatDate) ?: "Set date"}") }
            }
        }
        loaded.templateName?.let { name ->
            item { Text("Schedule: $name", style = MaterialTheme.typography.bodyMedium) }
        }
        item {
            Column {
                Text("Rating", style = MaterialTheme.typography.titleSmall)
                StarRating(bake.rating, onRate = { viewModel.setRating(if (it == bake.rating) null else it) })
            }
        }
        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it; viewModel.setNotes(it) },
                label = { Text("Comments") },
                placeholder = { Text("Crumb, crust, oven spring, what you'd change…") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        val timed = steps.filter { it.plannedAt != null }
        if (timed.isNotEmpty()) {
            item { Text("Timeline", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp)) }
            items(timed, key = { it.id }) { step ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StepIcon(step.name, size = 24.dp, modifier = Modifier.padding(end = 8.dp))
                    Text(formatTime(step.plannedAt!!), style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(80.dp))
                    Text(step.name + if (step.status == StepStatus.SKIPPED) " (skipped)" else "", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        item {
            TextButton(onClick = { confirmDelete = true }, modifier = Modifier.padding(vertical = 16.dp)) { Text("Delete this bake") }
        }
    }

    if (pickingFlavour) {
        FlavourPickerDialog(
            flavours = flavours,
            selectedId = bake.flavourId,
            onPick = { viewModel.setFlavour(it); pickingFlavour = false },
            onAdd = { viewModel.addFlavour(it); pickingFlavour = false },
            onDismiss = { pickingFlavour = false },
        )
    }
    if (pickingDate) {
        val zone = ZoneId.systemDefault()
        val current = bake.finishedAt ?: Instant.now()
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = current.atZone(zone).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { pickingDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        // The picker works in UTC days; keep the bake's original time of day.
                        val day = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        viewModel.setDate(day.atTime(current.atZone(zone).toLocalTime()).atZone(zone).toInstant())
                    }
                    pickingDate = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { pickingDate = false }) { Text("Cancel") } },
        ) { DatePicker(state = pickerState) }
    }
    if (confirmDelete) {
        ConfirmDialog(
            title = "Delete this bake?",
            text = "Its rating, comments and timeline are removed. This can't be undone.",
            confirm = "Delete",
            onConfirm = { confirmDelete = false; viewModel.delete(onBack) },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun StarRating(rating: Int?, onRate: (Int) -> Unit) {
    Row {
        (1..5).forEach { n ->
            Text(
                if (rating != null && n <= rating) "★" else "☆",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onRate(n) }.padding(end = 6.dp),
            )
        }
    }
}

private fun stars(rating: Int?): String = if (rating == null) "" else "★".repeat(rating) + "☆".repeat(5 - rating)

