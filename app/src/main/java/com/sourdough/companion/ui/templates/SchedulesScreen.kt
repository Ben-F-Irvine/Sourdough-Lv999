package com.sourdough.companion.ui.templates

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sourdough.companion.ui.bake.BakeViewModel
import com.sourdough.companion.ui.components.DefaultBadge

/** The Schedules tab: every template, editable even while a bake is running. */
@Composable
fun SchedulesScreen(viewModel: BakeViewModel, onEditTemplate: (Long) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Text("Schedules", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 16.dp)) }
        if (state.bake != null) {
            item {
                Text(
                    "Changes apply to your next bake; the one running now keeps its own copy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(state.sortedTemplates, key = { it.id }) { template ->
            Card(Modifier.fillMaxWidth().clickable { onEditTemplate(template.id) }) {
                Column(Modifier.padding(16.dp)) {
                    if (template.id == state.defaultTemplateId) DefaultBadge()
                    Text(template.name, style = MaterialTheme.typography.titleMedium)
                    template.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }
        }
        item {
            OutlinedButton(onClick = { viewModel.newTemplate(onEditTemplate) }, modifier = Modifier.fillMaxWidth()) {
                Text("New schedule")
            }
        }
    }
}
