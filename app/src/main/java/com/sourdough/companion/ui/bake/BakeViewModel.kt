package com.sourdough.companion.ui.bake

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sourdough.companion.SourdoughApp
import com.sourdough.companion.data.BakeRepository
import com.sourdough.companion.data.TemplateRepository
import com.sourdough.companion.data.UndoableChange
import com.sourdough.companion.data.db.BakeEntity
import com.sourdough.companion.data.db.BakeStepEntity
import com.sourdough.companion.data.db.FlavourEntity
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
    val flavours: List<FlavourEntity> = emptyList(),
    val bake: BakeEntity? = null,
    val steps: List<BakeStepEntity> = emptyList(),
    val defaultTemplateId: Long? = null,
    /** The last Done, Skip, +15 or Stop timers on this bake, if it can still be undone. */
    val undo: UndoableChange? = null,
) {
    /** Schedules with the default one first. */
    val sortedTemplates: List<TemplateEntity> get() = templates.sortedByDescending { it.id == defaultTemplateId }

    val flavour: FlavourEntity? get() = flavours.firstOrNull { it.id == bake?.flavourId }
    val templateName: String? get() = templates.firstOrNull { it.id == bake?.templateId }?.name
}

/** The running bake. Scoped to the activity, so every tab (and the timer bar) shares one instance. */
class BakeViewModel(
    private val repository: BakeRepository,
    private val templateRepository: TemplateRepository,
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    private val activeBakeWithSteps = repository.activeBake.flatMapLatest { bake ->
        if (bake == null) {
            flowOf<Pair<BakeEntity?, List<BakeStepEntity>>>(null to emptyList())
        } else {
            repository.steps(bake.id).map { bake to it }
        }
    }

    val state: StateFlow<BakeUiState> = combine(
        repository.templates,
        repository.flavours,
        activeBakeWithSteps,
        templateRepository.defaultTemplateId,
        repository.lastChange,
    ) { templates, flavours, (bake, steps), defaultId, lastChange ->
        BakeUiState(
            loaded = true,
            templates = templates,
            flavours = flavours,
            bake = bake,
            steps = steps,
            defaultTemplateId = defaultId,
            undo = lastChange?.takeIf { it.bakeId == bake?.id },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BakeUiState())

    init {
        viewModelScope.launch {
            repository.seedIfEmpty()
            // Cheap and idempotent: makes sure alarms and the timer exist even if Android dropped them.
            repository.replanActive()
        }
    }

    fun canScheduleExactAlarms() = repository.canScheduleExactAlarms()

    fun startBake(templateId: Long) = launch { repository.startBake(templateId) }

    fun newTemplate(onCreated: (Long) -> Unit) = launch { onCreated(templateRepository.create()) }

    fun doneMixing(bakeId: Long) = launch { repository.doneMixing(bakeId) }

    fun markDone(bakeId: Long, stepId: Long) = launch { repository.markDone(bakeId, stepId) }

    fun addMinutes(bakeId: Long, stepId: Long) = launch { repository.addMinutes(bakeId, stepId) }

    fun stopTimers(bakeId: Long) = launch { repository.stopTimers(bakeId) }

    fun skip(bakeId: Long, stepId: Long) = launch { repository.skip(bakeId, stepId) }

    fun undo() = launch { repository.undoLastChange() }

    fun setFlavour(bakeId: Long, flavourId: Long?) = launch { repository.setFlavour(bakeId, flavourId) }

    fun addFlavour(bakeId: Long, name: String) = launch { repository.setFlavour(bakeId, repository.addFlavour(name)) }

    /** Ends the bake and hands its id to [onEnded], which opens it in the log for rating. */
    fun endBake(bakeId: Long, onEnded: (Long) -> Unit) = launch {
        repository.endBake(bakeId)
        onEnded(bakeId)
    }

    fun cancelBake(bakeId: Long) = launch { repository.cancelBake(bakeId) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as SourdoughApp).container
                BakeViewModel(container.bakeRepository, container.templateRepository)
            }
        }
    }
}
