package com.sourdough.companion.data

import androidx.room.withTransaction
import com.sourdough.companion.alarms.AlarmScheduler
import com.sourdough.companion.data.db.BakeEntity
import com.sourdough.companion.data.db.BakeStepEntity
import com.sourdough.companion.data.db.SourdoughDatabase
import com.sourdough.engine.AlarmKind
import com.sourdough.engine.AlarmSpec
import com.sourdough.engine.LiveStep
import com.sourdough.engine.Phase
import com.sourdough.engine.Planner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

/**
 * Every change to a running bake goes through here: update bake_steps, ask the engine for the new
 * plan, then cancel and re-set the alarms. Alarms are only ever set from this one place.
 */
class BakeRepository(
    private val db: SourdoughDatabase,
    private val alarms: AlarmScheduler,
    private val clock: () -> Instant = Instant::now,
) {
    private val templateDao = db.templateDao()
    private val bakeDao = db.bakeDao()
    private val mutex = Mutex()

    val templates = templateDao.observeAll()
    val activeBake: Flow<BakeEntity?> = bakeDao.observeActive()

    fun steps(bakeId: Long): Flow<List<BakeStepEntity>> = bakeDao.observeSteps(bakeId)

    suspend fun seedIfEmpty() = mutex.withLock {
        if (templateDao.count() > 0) return@withLock
        db.withTransaction {
            SeedTemplates.all.forEach { (template, steps) ->
                val id = templateDao.insert(template)
                templateDao.insertSteps(steps.mapIndexed { i, step -> step.copy(templateId = id, position = i) })
            }
        }
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

    suspend fun markDone(bakeId: Long, stepId: Long) = changeSteps(bakeId) { _, steps, now ->
        Planner.done(steps, stepId, now)
    }

    suspend fun skip(bakeId: Long, stepId: Long) = changeSteps(bakeId) { anchor, steps, now ->
        Planner.skip(anchor, steps, stepId, now)
    }

    suspend fun addMinutes(bakeId: Long, stepId: Long, minutes: Int = 15) = changeSteps(bakeId) { anchor, steps, now ->
        Planner.extend(anchor, steps, stepId, now, minutes)
    }

    /** Ends the bake, finished or abandoned, and cancels whatever alarms are left. */
    suspend fun endBake(bakeId: Long) = mutex.withLock {
        cancelAlarms(postMixSteps(bakeId))
        bakeDao.update(bakeDao.get(bakeId).copy(finishedAt = clock()))
    }

    /** Re-sets the active bake's alarms; safe to call any time (app start, reboot, reinstall). */
    suspend fun replanActive() = mutex.withLock {
        bakeDao.getActive()?.let { replan(it.id) }
    }

    fun canScheduleExactAlarms() = alarms.canScheduleExact()

    private suspend fun changeSteps(
        bakeId: Long,
        change: (anchor: Instant, steps: List<LiveStep>, now: Instant) -> List<LiveStep>,
    ) = mutex.withLock {
        val anchor = bakeDao.get(bakeId).mixedAt ?: return@withLock
        val entities = postMixSteps(bakeId)
        val now = clock()
        val changed = change(anchor, entities.map { it.toLiveStep() }, now)
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
            alarms.schedule(alarm, titleFor(alarm, step), step.instructions)
        }
    }

    private suspend fun postMixSteps(bakeId: Long) = bakeDao.stepsFor(bakeId).filter { it.phase == Phase.POST_MIX }

    private fun cancelAlarms(steps: List<BakeStepEntity>) =
        Planner.allAlarmKeys(steps.map { it.toLiveStep() }).forEach(alarms::cancel)

    private fun titleFor(alarm: AlarmSpec, step: BakeStepEntity): String = when {
        alarm.key.kind == AlarmKind.CHECK -> "Check the dough: ${step.name}"
        step.repeatCount > 1 -> "${step.name} ${alarm.key.occurrence + 1} of ${step.repeatCount}"
        else -> step.name
    }
}
