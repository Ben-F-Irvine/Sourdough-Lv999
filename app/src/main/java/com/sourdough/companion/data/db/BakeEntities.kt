package com.sourdough.companion.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sourdough.engine.LiveStep
import com.sourdough.engine.Phase
import com.sourdough.engine.StepSpec
import com.sourdough.engine.StepStatus
import com.sourdough.engine.temperature.TemperatureSourceType
import java.time.Instant

@Entity(
    tableName = "bakes",
    foreignKeys = [
        ForeignKey(
            entity = TemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = FlavourEntity::class,
            parentColumns = ["id"],
            childColumns = ["flavourId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("templateId"), Index("flavourId")],
)
data class BakeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long?,
    val flavourId: Long?,
    val flourG: Int? = null,
    val hydrationPct: Double? = null,
    val fedAt: Instant? = null,
    val mixedAt: Instant? = null,
    val finishedAt: Instant? = null,
    val rating: Int? = null,
    val notes: String? = null,
    val photoPath: String? = null,
)

/**
 * A step copied from the template when the bake starts. The step definition is copied too,
 * so the alarm engine never reads the (editable) template mid-bake.
 */
@Entity(
    tableName = "bake_steps",
    foreignKeys = [
        ForeignKey(
            entity = BakeEntity::class,
            parentColumns = ["id"],
            childColumns = ["bakeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("bakeId")],
)
data class BakeStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bakeId: Long,
    val position: Int,
    val name: String,
    val instructions: String = "",
    val durationMin: Int,
    val repeatCount: Int = 1,
    val repeatIntervalMin: Int = 0,
    val waitForUser: Boolean = false,
    val tempAdjusted: Boolean = false,
    val phase: Phase,
    /** Cache of the latest plan for display; the engine recomputes it from durations on every change. */
    val plannedAt: Instant? = null,
    val startedAt: Instant? = null,
    val doneAt: Instant? = null,
    /** Minutes added with "+15". */
    val extraMin: Int = 0,
    /** Temperature-adjusted steps only: 0.0..1.0 of the curve's duration completed so far. */
    val progressPct: Double = 0.0,
    val status: StepStatus = StepStatus.PENDING,
) {
    fun toLiveStep() = LiveStep(
        id = id,
        spec = StepSpec(name, durationMin, phase, repeatCount, repeatIntervalMin, waitForUser, tempAdjusted),
        status = status,
        doneAt = doneAt,
        extraMin = extraMin,
    )
}

@Entity(
    tableName = "temp_readings",
    foreignKeys = [
        ForeignKey(
            entity = BakeEntity::class,
            parentColumns = ["id"],
            childColumns = ["bakeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("bakeId")],
)
data class TempReadingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bakeId: Long,
    val readAt: Instant,
    val tempC: Double,
    val source: TemperatureSourceType,
)

/** Key/value settings: temperature source, Home Assistant URL and token, sensor ID. */
@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String,
)
