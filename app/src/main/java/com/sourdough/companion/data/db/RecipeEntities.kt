package com.sourdough.companion.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sourdough.engine.Phase

// Reusable "recipes": templates, flavours and the temperature curve. A bake copies what it
// needs from these at start, so editing a template never changes past bakes.

enum class AnchorType { FEED, MIX }

enum class CurveType { BULK, STARTER }

@Entity(tableName = "templates")
data class TemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val anchorType: AnchorType,
    val notes: String? = null,
)

@Entity(
    tableName = "template_steps",
    foreignKeys = [
        ForeignKey(
            entity = TemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("templateId")],
)
data class TemplateStepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val templateId: Long,
    /** Step order within the template ("order" is an SQL keyword). */
    val position: Int,
    val name: String,
    val instructions: String = "",
    val durationMin: Int,
    val repeatCount: Int = 1,
    val repeatIntervalMin: Int = 0,
    val waitForUser: Boolean = false,
    val tempAdjusted: Boolean = false,
    val phase: Phase,
)

@Entity(tableName = "flavours")
data class FlavourEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val notes: String? = null,
    /** Step at which the add-ins go in, e.g. "Stretch and fold 2". Null means at mix. */
    val addStepName: String? = null,
)

@Entity(
    tableName = "flavour_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = FlavourEntity::class,
            parentColumns = ["id"],
            childColumns = ["flavourId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("flavourId")],
)
data class FlavourIngredientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val flavourId: Long,
    val name: String,
    val amount: Double,
    val unit: String,
    val isPercentOfFlour: Boolean = false,
)

@Entity(tableName = "temp_curve_points")
data class TempCurvePointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val curve: CurveType,
    val tempC: Double,
    val durationMin: Int,
)
