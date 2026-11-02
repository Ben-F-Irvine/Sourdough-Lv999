package com.sourdough.companion.ui.bake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sourdough.companion.SourdoughApp
import com.sourdough.companion.data.BakeRepository
import com.sourdough.companion.data.db.BakeEntity
import com.sourdough.companion.data.db.BakeStepEntity
import com.sourdough.companion.data.db.TemplateEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BakeUiState(
    val loaded: Boolean = false,
    val templates: List<TemplateEntity> = emptyList(),
    val bake: BakeEntity? = null,
    val steps: List<BakeStepEntity> = emptyList(),
)

class BakeViewModel(private val repository: BakeRepository) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val activeBakeWithSteps = repository.activeBake.flatMapLatest { bake ->
        if (bake == null) {
            flowOf<Pair<BakeEntity?, List<BakeStepEntity>>>(null to emptyList())
        } else {
            repository.steps(bake.id).map { bake to it }
        }
    }

    val state: StateFlow<BakeUiState> = combine(repository.templates, activeBakeWithSteps) { templates, (bake, steps) ->
        BakeUiState(loaded = true, templates = templates, bake = bake, steps = steps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BakeUiState())

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
            // Cheap and idempotent: makes sure alarms exist even if Android dropped them.
            repository.replanActive()
        }
    }

    fun canScheduleExactAlarms() = repository.canScheduleExactAlarms()

    fun startBake(templateId: Long) = launch { repository.startBake(templateId) }

    fun doneMixing(bakeId: Long) = launch { repository.doneMixing(bakeId) }

    fun markDone(bakeId: Long, stepId: Long) = launch { repository.markDone(bakeId, stepId) }

    fun addMinutes(bakeId: Long, stepId: Long) = launch { repository.addMinutes(bakeId, stepId) }

    fun skip(bakeId: Long, stepId: Long) = launch { repository.skip(bakeId, stepId) }

    fun endBake(bakeId: Long) = launch { repository.endBake(bakeId) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { BakeViewModel((this[APPLICATION_KEY] as SourdoughApp).container.bakeRepository) }
        }
    }
}
