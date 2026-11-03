package com.sourdough.companion.data

import androidx.room.withTransaction
import com.sourdough.companion.data.db.AnchorType
import com.sourdough.companion.data.db.SourdoughDatabase
import com.sourdough.companion.data.db.TemplateEntity
import com.sourdough.companion.data.db.TemplateStepEntity
import com.sourdough.engine.Phase

/**
 * Editing schedule templates. Steps are kept ordered "before mixing" first, then "after mixing",
 * with positions renumbered 0..n after every change so a bake can copy them as-is.
 * Running bakes have their own copy of the steps, so nothing here affects them.
 */
class TemplateRepository(private val db: SourdoughDatabase) {
    private val dao = db.templateDao()

    fun template(id: Long) = dao.observe(id)

    fun steps(templateId: Long) = dao.observeSteps(templateId)

    suspend fun getStep(id: Long) = dao.getStep(id)

    suspend fun create(): Long = dao.insert(TemplateEntity(name = "New schedule", anchorType = AnchorType.FEED))

    suspend fun clone(id: Long): Long = db.withTransaction {
        val original = dao.get(id)
        val copyId = dao.insert(original.copy(id = 0, name = "${original.name} (copy)"))
        dao.insertSteps(dao.stepsFor(id).map { it.copy(id = 0, templateId = copyId) })
        copyId
    }

    suspend fun rename(id: Long, name: String) = dao.update(dao.get(id).copy(name = name))

    suspend fun setNotes(id: Long, notes: String) = dao.update(dao.get(id).copy(notes = notes.ifBlank { null }))

    suspend fun delete(id: Long) = dao.delete(id)

    /**
     * Inserts or updates a step. A new step goes at the end of its section; a step moved to the
     * other section lands next to the "Done mixing" boundary.
     */
    suspend fun saveStep(step: TemplateStepEntity) = db.withTransaction {
        val existing = if (step.id != 0L) dao.getStep(step.id) else null
        val position = when {
            existing == null -> Int.MAX_VALUE
            existing.phase == step.phase -> existing.position
            step.phase == Phase.PRE_MIX -> Int.MAX_VALUE
            else -> -1
        }
        val positioned = step.copy(position = position)
        if (existing == null) dao.insertStep(positioned) else dao.updateSteps(listOf(positioned))
        renumber(step.templateId)
    }

    suspend fun deleteStep(step: TemplateStepEntity) = db.withTransaction {
        dao.deleteStep(step.id)
        renumber(step.templateId)
    }

    /** Swaps a step with its neighbour, staying within its own section. */
    suspend fun move(templateId: Long, stepId: Long, up: Boolean) = db.withTransaction {
        val steps = ordered(templateId).toMutableList()
        val i = steps.indexOfFirst { it.id == stepId }
        val j = if (up) i - 1 else i + 1
        if (i < 0 || j !in steps.indices || steps[j].phase != steps[i].phase) return@withTransaction
        steps[i] = steps[j].also { steps[j] = steps[i] }
        dao.updateSteps(steps.mapIndexed { n, s -> s.copy(position = n) })
    }

    private suspend fun ordered(templateId: Long) =
        dao.stepsFor(templateId).sortedWith(compareBy({ it.phase.ordinal }, { it.position }))

    private suspend fun renumber(templateId: Long) =
        dao.updateSteps(ordered(templateId).mapIndexed { n, s -> s.copy(position = n) })
}
