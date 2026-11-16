package com.sourdough.companion.data

import androidx.room.withTransaction
import com.sourdough.companion.alarms.AlarmScheduler
import com.sourdough.companion.alarms.BakeTimerNotifier
import com.sourdough.companion.data.db.BakeEntity
import com.sourdough.companion.data.db.BakeStepEntity
import com.sourdough.companion.data.db.BakeSummary
import com.sourdough.companion.data.db.FlavourEntity
import com.sourdough.companion.data.db.SettingEntity
import com.sourdough.companion.data.db.SourdoughDatabase
import com.sourdough.companion.data.db.TemplateEntity
import com.sourdough.companion.data.db.TemplateStepEntity
import com.sourdough.engine.AlarmKind
import com.sourdough.engine.AlarmSpec
import com.sourdough.engine.LiveStep
import com.sourdough.engine.Phase
import com.sourdough.engine.Planner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Duration
import java.time.Instant

/** A change to the running bake that Undo can take back; [before] is the steps as they were. */
data class UndoableChange(
    val bakeId: Long,
    /** What happened, e.g. "Shape done", for the Undo button. */
    val label: String,
    val at: Instant,
    val before: List<BakeStepEntity>,
) {
    /** Undo is only offered for a few minutes; after that the tap was clearly meant. */
    fun offeredAt(now: Instant): Boolean = Duration.between(at, now) < WINDOW

    private companion object {
        val WINDOW: Duration = Duration.ofMinutes(5)
    }
}

/**
 * Every change to a bake goes through here: update bake_steps, ask the engine for the new plan,
 * then cancel and re-set the alarms and refresh the ongoing timer notification. Alarms are only
 * ever set from this one place.
 */
class BakeRepository(
    private val db: SourdoughDatabase,
    private val alarms: AlarmScheduler,
    private val timer: BakeTimerNotifier,
    private val clock: () -> Instant = Instant::now,
) {
    private val templateDao = db.templateDao()
    private val bakeDao = db.bakeDao()
    private val flavourDao = db.flavourDao()
    private val settingsDao = db.settingsDao()
    private val mutex = Mutex()
    private val _lastChange = MutableStateFlow<UndoableChange?>(null)

    /** The last Done, Skip, +15 or Stop timers on the running bake, while it can still be undone. */
    val lastChange: StateFlow<UndoableChange?> = _lastChange.asStateFlow()

    val templates = templateDao.observeAll()
    val flavours: Flow<List<FlavourEntity>> = flavourDao.observeAll()
    val activeBake: Flow<BakeEntity?> = bakeDao.observeActive()
    val log: Flow<List<BakeSummary>> = bakeDao.observeLog()

    fun steps(bakeId: Long): Flow<List<BakeStepEntity>> = bakeDao.observeSteps(bakeId)

    fun summary(bakeId: Long): Flow<BakeSummary?> = bakeDao.observeSummary(bakeId)

    suspend fun seedIfEmpty() = mutex.withLock {
        db.withTransaction {
            if (templateDao.count() == 0) {
                val ids = SeedTemplates.all.map { (template, steps) -> insertTemplate(template, steps) }
                settingsDao.put(SettingEntity(SettingKeys.DEFAULT_TEMPLATE, ids.first().toString()))
                settingsDao.put(SettingEntity(SettingKeys.SEEDED_COLD_BULK, "1"))
            } else if (settingsDao.get(SettingKeys.SEEDED_COLD_BULK) == null) {
                // Installs from before the cold-bulk schedule existed get it once, as the new default.
                // The flag means it never comes back if it's deleted later.
                val (template, steps) = SeedTemplates.coldBulk
                val id = insertTemplate(template, steps)
                settingsDao.put(SettingEntity(SettingKeys.DEFAULT_TEMPLATE, id.toString()))
                settingsDao.put(SettingEntity(SettingKeys.SEEDED_COLD_BULK, "1"))
            }
            if (flavourDao.count() == 0) SeedFlavours.all.forEach { flavourDao.insert(it) }
        }
    }

    private suspend fun insertTemplate(template: TemplateEntity, steps: List<TemplateStepEntity>): Long {
        val id = templateDao.insert(template)
        templateDao.insertSteps(steps.mapIndexed { i, step -> step.copy(templateId = id, position = i) })
        return id
    }

    /** Copies the template's steps into a new bake, so later template edits never touch it. */
    suspend fun startBake(templateId: Long): Long = mutex.withLock {
        db.withTransaction {
            val bakeId = bakeDao.insert(BakeEntity(templateId = templateId, flavourId = null))
            bakeDao.insertSteps(
                templateDao.stepsFor(templateId).map { t ->
                    BakeStepEntity(
                        bakeId = bakeId,
                        position = t.position,
                        name = t.name,
                        instructions = t.instructions,
                        durationMin = t.durationMin,
                        repeatCount = t.repeatCount,
                        repeatIntervalMin = t.repeatIntervalMin,
                        waitForUser = t.waitForUser,
                        tempAdjusted = t.tempAdjusted,
                        phase = t.phase,
                    )
                },
            )
            bakeId
        }
    }

    /** Starts the real clock: every post-mix alarm is scheduled from this moment. */
    suspend fun doneMixing(bakeId: Long) = mutex.withLock {
        bakeDao.update(bakeDao.get(bakeId).copy(mixedAt = clock()))
        replan(bakeId)
    }

    /** [onlyIfCurrent] is for reminder buttons: nothing happens once the bake has moved past that step. */
    suspend fun markDone(bakeId: Long, stepId: Long, onlyIfCurrent: Boolean = false) =
        changeSteps(bakeId, stepId, onlyIfCurrent, { "$it done" }) { _, steps, now ->
            Planner.done(steps, stepId, now)
        }

    suspend fun skip(bakeId: Long, stepId: Long) = changeSteps(bakeId, stepId, label = { "$it skipped" }) { anchor, steps, now ->
        Planner.skip(anchor, steps, stepId, now)
    }

    /**
     * Skips everything left, so no alarms or countdown remain. The bake stays active (and on the
     * Bake tab) until it's rated, e.g. after an overnight cold bulk you don't need reminders for.
     */
    suspend fun stopTimers(bakeId: Long) = changeSteps(bakeId, label = { "Timers stopped" }) { anchor, steps, now ->
        Planner.stopAll(anchor, steps, now)
    }

    suspend fun addMinutes(bakeId: Long, stepId: Long, minutes: Int = 15, onlyIfCurrent: Boolean = false) =
        changeSteps(bakeId, stepId, onlyIfCurrent, { "+$minutes min on $it" }) { anchor, steps, now ->
            Planner.extend(anchor, steps, stepId, now, minutes)
        }

    /**
     * Puts the steps back as they were before the last Done, Skip, +15 or Stop timers, and re-plans.
     * Kept in memory only: it is for a slip of the finger, not for changing your mind tomorrow.
     */
    suspend fun undoLastChange() = mutex.withLock {
        val change = _lastChange.value ?: return@withLock
        _lastChange.value = null
        if (bakeDao.getActive()?.id != change.bakeId) return@withLock
        val before = change.before.associateBy { it.id }
        bakeDao.updateSteps(
            postMixSteps(change.bakeId).map { entity ->
                val old = before[entity.id] ?: return@map entity
                entity.copy(status = old.status, doneAt = old.doneAt, extraMin = old.extraMin)
            },
        )
        replan(change.bakeId)
    }

    /** Ends the bake, finished or abandoned: it moves to the log and its alarms stop. */
    suspend fun endBake(bakeId: Long) = mutex.withLock {
        cancelAlarmsAndTimer(bakeId)
        bakeDao.update(bakeDao.get(bakeId).copy(finishedAt = clock()))
    }

    /** Throws away a bake that never got past the mix checklist, so it doesn't clutter the log. */
    suspend fun cancelBake(bakeId: Long) = mutex.withLock {
        cancelAlarmsAndTimer(bakeId)
        bakeDao.delete(bakeId)
    }

    // --- Flavour ("what kind of sourdough"), changeable at any point in the bake or afterwards ---

    suspend fun setFlavour(bakeId: Long, flavourId: Long?) = mutex.withLock {
        bakeDao.update(bakeDao.get(bakeId).copy(flavourId = flavourId))
        if (bakeDao.getActive()?.id == bakeId) refreshTimer(bakeId)
    }

    suspend fun addFlavour(name: String): Long = flavourDao.insert(FlavourEntity(name = name.trim()))

    // --- Bake log ---

    /** A bake made without the app, logged by hand. */
    suspend fun logPastBake(): Long = bakeDao.insert(BakeEntity(templateId = null, flavourId = null, finishedAt = clock()))

    suspend fun setRating(bakeId: Long, rating: Int?) = mutex.withLock {
        bakeDao.update(bakeDao.get(bakeId).copy(rating = rating))
    }

    suspend fun setNotes(bakeId: Long, notes: String) = mutex.withLock {
        bakeDao.update(bakeDao.get(bakeId).copy(notes = notes.ifBlank { null }))
    }

    suspend fun setFinishedAt(bakeId: Long, at: Instant) = mutex.withLock {
        bakeDao.update(bakeDao.get(bakeId).copy(finishedAt = at))
    }

    suspend fun deleteBake(bakeId: Long) = mutex.withLock {
        cancelAlarmsAndTimer(bakeId)
        bakeDao.delete(bakeId)
    }

    // --- Alarms and the ongoing timer ---

    /** Re-sets the active bake's alarms and timer; safe to call any time (app start, reboot, reinstall). */
    suspend fun replanActive() = mutex.withLock {
        bakeDao.getActive()?.let { replan(it.id) } ?: timer.cancel()
    }

    /** Moves the ongoing notification on to the next step; called whenever an alarm fires. */
    suspend fun refreshActiveTimer() = mutex.withLock {
        bakeDao.getActive()?.let { refreshTimer(it.id) } ?: timer.cancel()
    }

    fun canScheduleExactAlarms() = alarms.canScheduleExact()

    private suspend fun changeSteps(
        bakeId: Long,
        stepId: Long? = null,
        onlyIfCurrent: Boolean = false,
        label: (stepName: String) -> String,
        change: (anchor: Instant, steps: List<LiveStep>, now: Instant) -> List<LiveStep>,
    ) = mutex.withLock {
        // A reminder can outlive its bake; the live screen only ever acts on the active one.
        if (onlyIfCurrent && bakeDao.getActive()?.id != bakeId) return@withLock
        val anchor = bakeDao.get(bakeId).mixedAt ?: return@withLock
        val entities = postMixSteps(bakeId)
        val now = clock()
        val live = entities.map { it.toLiveStep() }
        if (onlyIfCurrent && Planner.currentStepId(anchor, live, now) != stepId) return@withLock
        val changed = change(anchor, live, now)
        _lastChange.value = UndoableChange(
            bakeId = bakeId,
            label = label(entities.firstOrNull { it.id == stepId }?.name.orEmpty()),
            at = now,
            before = entities,
        )
        bakeDao.updateSteps(
            entities.zip(changed).map { (entity, live) ->
                entity.copy(
                    status = live.status,
                    doneAt = live.doneAt,
                    extraMin = live.extraMin,
                )
            },
        )
        replan(bakeId)
    }

    private suspend fun replan(bakeId: Long) {
        val anchor = bakeDao.get(bakeId).mixedAt ?: return
        val entities = postMixSteps(bakeId)
        val live = entities.map { it.toLiveStep() }
        val now = clock()
        val plan = Planner.plan(anchor, live, now)

        bakeDao.updateSteps(entities.zip(plan).map { (entity, planned) -> entity.copy(plannedAt = planned.start) })

        cancelAlarms(entities)
        val byId = entities.associateBy { it.id }
        Planner.alarms(plan, now).forEach { alarm ->
            val step = byId.getValue(alarm.key.stepId)
            val text = if (alarm.key.kind == AlarmKind.FINISH) "Tap to rate your loaf." else step.instructions
            val actions = Planner.hasQuickActions(alarm.key, step.toLiveStep().spec)
            alarms.schedule(alarm, titleFor(alarm, step), text, actionsForBake = bakeId.takeIf { actions })
        }
        refreshTimer(bakeId)
    }

    private suspend fun refreshTimer(bakeId: Long) {
        val summary = bakeDao.getSummary(bakeId) ?: return timer.cancel()
        val status = BakeStatus.of(summary.bake, bakeDao.stepsFor(bakeId), clock())
        if (status == null) timer.cancel() else timer.show(status, summary.flavourName)
    }

    private suspend fun cancelAlarmsAndTimer(bakeId: Long) {
        _lastChange.value = null
        cancelAlarms(postMixSteps(bakeId))
        timer.cancel()
    }

    private suspend fun postMixSteps(bakeId: Long) = bakeDao.stepsFor(bakeId).filter { it.phase == Phase.POST_MIX }

    private fun cancelAlarms(steps: List<BakeStepEntity>) =
        Planner.allAlarmKeys(steps.map { it.toLiveStep() }).forEach(alarms::cancel)

    private fun titleFor(alarm: AlarmSpec, step: BakeStepEntity): String = when {
        alarm.key.kind == AlarmKind.FINISH -> "🎉 Bake complete"
        alarm.key.kind == AlarmKind.CHECK -> "${StepIcons.emoji(step.name)} Check the dough: ${step.name}"
        step.repeatCount > 1 -> "${StepIcons.emoji(step.name)} ${step.name} ${alarm.key.occurrence + 1} of ${step.repeatCount}"
        else -> "${StepIcons.emoji(step.name)} ${step.name}"
    }
}
