package com.sourdough.companion.ui.templates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StepEditorViewModel(savedState: SavedStateHandle, private val repository: TemplateRepository) : ViewModel() {
    private val templateId: Long = checkNotNull(savedState["templateId"])
    private val stepId: Long = checkNotNull(savedState["stepId"])
    private val phase = Phase.valueOf(checkNotNull(savedState["phase"]))

    val isNew = stepId == 0L

    private val _step = MutableStateFlow<TemplateStepEntity?>(null)
    val step = _step.asStateFlow()

    init {
        viewModelScope.launch {
            _step.value = if (isNew) {
                TemplateStepEntity(templateId = templateId, position = 0, name = "", durationMin = 30, phase = phase)
            } else {
                repository.getStep(stepId)
            }
        }
    }

    fun save(step: TemplateStepEntity, onSaved: () -> Unit) = viewModelScope.launch {
        repository.saveStep(step)
        onSaved()
    }

    fun delete(onDeleted: () -> Unit) = viewModelScope.launch {
        _step.value?.let { repository.deleteStep(it) }
        onDeleted()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as SourdoughApp
                StepEditorViewModel(createSavedStateHandle(), app.container.templateRepository)
            }
        }
    }
}

@Composable
fun StepEditorScreen(onBack: () -> Unit, viewModel: StepEditorViewModel = viewModel(factory = StepEditorViewModel.Factory)) {
    val loaded by viewModel.step.collectAsStateWithLifecycle()
    val original = loaded ?: return

    var name by remember(original) { mutableStateOf(original.name) }
    var instructions by remember(original) { mutableStateOf(original.instructions) }
    var phase by remember(original) { mutableStateOf(original.phase) }
    var hours by remember(original) { mutableStateOf((original.durationMin / 60).toString()) }
    var minutes by remember(original) { mutableStateOf((original.durationMin % 60).toString()) }
    var repeats by remember(original) { mutableStateOf(original.repeatCount > 1) }
    var count by remember(original) { mutableStateOf(original.repeatCount.coerceAtLeast(2).toString()) }
    var interval by remember(original) { mutableStateOf(original.repeatIntervalMin.takeIf { it > 0 }?.toString() ?: "30") }
    var waitForUser by remember(original) { mutableStateOf(original.waitForUser) }

    val countN = count.toIntOrNull()?.takeIf { it >= 2 }
    val intervalN = interval.toIntOrNull()?.takeIf { it >= 1 }
    // A repeating step lasts one interval past its last repeat, so the next step comes one gap later.
    val durationMin = if (repeats) {
        if (countN != null && intervalN != null) countN * intervalN else null
    } else {
        val h = hours.ifBlank { "0" }.toIntOrNull()
        val m = minutes.ifBlank { "0" }.toIntOrNull()
        if (h != null && m != null && h * 60 + m > 0) h * 60 + m else null
    }
    val valid = name.isNotBlank() && durationMin != null

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ScreenHeader(if (viewModel.isNew) "New step" else "Edit step", onBack)

        Row(verticalAlignment = Alignment.CenterVertically) {
            StepIcon(name, size = 40.dp, modifier = Modifier.padding(end = 12.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = phase == Phase.PRE_MIX, onClick = { phase = Phase.PRE_MIX }, label = { Text("Before mixing") })
            FilterChip(selected = phase == Phase.POST_MIX, onClick = { phase = Phase.POST_MIX }, label = { Text("After mixing") })
        }

        SwitchRow("Repeat this step", "e.g. stretch and folds", repeats) { repeats = it }
        if (repeats) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField("Times", count, Modifier.weight(1f)) { count = it }
                NumberField("Minutes apart", interval, Modifier.weight(1f)) { interval = it }
            }
            if (countN != null && intervalN != null) {
                Hint("${countN} times, ${formatDuration(intervalN)} apart. The next step starts ${formatDuration(intervalN)} after the last one.")
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField("Hours", hours, Modifier.weight(1f)) { hours = it }
                NumberField("Minutes", minutes, Modifier.weight(1f)) { minutes = it }
            }
        }

        SwitchRow(
            "Wait for me to tap Done",
            "The time becomes a reminder to check; the next step waits for you",
            waitForUser,
        ) { waitForUser = it }

        OutlinedTextField(
            value = instructions,
            onValueChange = { instructions = it },
            label = { Text("Instructions") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            enabled = valid,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                viewModel.save(
                    original.copy(
                        name = name.trim(),
                        instructions = instructions.trim(),
                        phase = phase,
                        durationMin = durationMin ?: return@Button,
                        repeatCount = if (repeats) countN ?: 2 else 1,
                        repeatIntervalMin = if (repeats) intervalN ?: 0 else 0,
                        waitForUser = waitForUser,
                    ),
                    onBack,
                )
            },
        ) { Text("Save") }

        if (!viewModel.isNew) {
            TextButton(onClick = { viewModel.delete(onBack) }) { Text("Delete step") }
        }
    }
}

@Composable
private fun SwitchRow(title: String, hint: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Hint(hint)
        }
        Switch(checked = checked, onCheckedChange = onChange)
    }
}

@Composable
private fun NumberField(label: String, value: String, modifier: Modifier, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { text -> onChange(text.filter(Char::isDigit).take(4)) },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier,
    )
}

@Composable
private fun Hint(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
