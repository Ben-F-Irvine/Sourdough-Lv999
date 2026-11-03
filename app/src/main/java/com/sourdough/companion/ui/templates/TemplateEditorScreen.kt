package com.sourdough.companion.ui.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.sourdough.companion.data.TemplateRepository
import com.sourdough.companion.data.db.TemplateStepEntity
import com.sourdough.companion.ui.components.ScreenHeader
import com.sourdough.companion.ui.components.StepIcon
import com.sourdough.companion.ui.components.formatDuration
import com.sourdough.engine.Phase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplateEditorViewModel(savedState: SavedStateHandle, private val repository: TemplateRepository) : ViewModel() {
    val templateId: Long = checkNotNull(savedState["templateId"])

    val template = repository.template(templateId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val steps = repository.steps(templateId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun rename(name: String) = viewModelScope.launch { repository.rename(templateId, name) }

    fun setNotes(notes: String) = viewModelScope.launch { repository.setNotes(templateId, notes) }

    fun move(stepId: Long, up: Boolean) = viewModelScope.launch { repository.move(templateId, stepId, up) }

    fun clone(onCloned: (Long) -> Unit) = viewModelScope.launch { onCloned(repository.clone(templateId)) }

    fun delete(onDeleted: () -> Unit) = viewModelScope.launch {
        repository.delete(templateId)
        onDeleted()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SourdoughApp
                TemplateEditorViewModel(createSavedStateHandle(), app.container.templateRepository)
            }
        }
    }
}

@Composable
fun TemplateEditorScreen(
    onBack: () -> Unit,
    onEditStep: (templateId: Long, stepId: Long, phase: Phase) -> Unit,
    onOpenTemplate: (Long) -> Unit,
    viewModel: TemplateEditorViewModel = viewModel(factory = TemplateEditorViewModel.Factory),
) {
    val template by viewModel.template.collectAsStateWithLifecycle()
    val steps by viewModel.steps.collectAsStateWithLifecycle()
    val loaded = template ?: return
    // Typed text lives here and is written through to the database; reading it back would move the cursor.
    var name by remember(loaded.id) { mutableStateOf(loaded.name) }
    var notes by remember(loaded.id) { mutableStateOf(loaded.notes.orEmpty()) }
    var confirmDelete by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item { ScreenHeader("Edit schedule", onBack) }
        item {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it; viewModel.rename(it) },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it; viewModel.setNotes(it) },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
            )
        }

        for (phase in Phase.entries) {
            val section = steps.filter { it.phase == phase }
            item(key = "header-$phase") { SectionHeader(phase) }
            items(section, key = { it.id }) { step ->
                StepRow(
                    step = step,
                    canMoveUp = step != section.first(),
                    canMoveDown = step != section.last(),
                    onMove = { up -> viewModel.move(step.id, up) },
                    onClick = { onEditStep(loaded.id, step.id, step.phase) },
                )
            }
            item(key = "add-$phase") {
                TextButton(onClick = { onEditStep(loaded.id, 0, phase) }) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("Add step")
                }
            }
        }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            ) {
                OutlinedButton(onClick = { viewModel.clone(onOpenTemplate) }) { Text("Make a copy") }
                TextButton(onClick = { confirmDelete = true }) { Text("Delete schedule") }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete ${loaded.name}?") },
            text = { Text("Past bakes keep their steps. This can't be undone.") },
            confirmButton = { TextButton(onClick = { viewModel.delete(onBack) }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SectionHeader(phase: Phase) {
    Column(Modifier.padding(top = 16.dp)) {
        Text(
            if (phase == Phase.PRE_MIX) "Before mixing" else "After mixing",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            if (phase == Phase.PRE_MIX) "Shown as the mix checklist" else "Alarms start when you tap Done mixing",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun StepRow(
    step: TemplateStepEntity,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMove: (up: Boolean) -> Unit,
    onClick: () -> Unit,
) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            StepIcon(step.name, size = 32.dp, modifier = Modifier.padding(end = 12.dp))
            Column(Modifier.weight(1f)) {
                Text(step.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    stepSummary(step),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { onMove(true) }, enabled = canMoveUp) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Move up")
            }
            IconButton(onClick = { onMove(false) }, enabled = canMoveDown) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Move down")
            }
        }
    }
}

private fun stepSummary(step: TemplateStepEntity): String {
    val time = if (step.repeatCount > 1) {
        "${step.repeatCount}× every ${formatDuration(step.repeatIntervalMin)}"
    } else {
        formatDuration(step.durationMin)
    }
    return if (step.waitForUser) "$time · waits for you" else time
}
