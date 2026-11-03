package com.sourdough.companion.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TemplateDao {
    @Query("SELECT * FROM templates ORDER BY id")
    fun observeAll(): Flow<List<TemplateEntity>>

    @Query("SELECT COUNT(*) FROM templates")
    suspend fun count(): Int

    @Query("SELECT * FROM templates WHERE id = :id")
    fun observe(id: Long): Flow<TemplateEntity?>

    @Query("SELECT * FROM templates WHERE id = :id")
    suspend fun get(id: Long): TemplateEntity

    @Insert
    suspend fun insert(template: TemplateEntity): Long

    @Update
    suspend fun update(template: TemplateEntity)

    @Query("DELETE FROM templates WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM template_steps WHERE templateId = :templateId ORDER BY position")
    fun observeSteps(templateId: Long): Flow<List<TemplateStepEntity>>

    @Query("SELECT * FROM template_steps WHERE templateId = :templateId ORDER BY position")
    suspend fun stepsFor(templateId: Long): List<TemplateStepEntity>

    @Query("SELECT * FROM template_steps WHERE id = :id")
    suspend fun getStep(id: Long): TemplateStepEntity?

    @Insert
    suspend fun insertStep(step: TemplateStepEntity): Long

    @Insert
    suspend fun insertSteps(steps: List<TemplateStepEntity>)

    @Update
    suspend fun updateSteps(steps: List<TemplateStepEntity>)

    @Query("DELETE FROM template_steps WHERE id = :id")
    suspend fun deleteStep(id: Long)
}

@Dao
interface BakeDao {
    /** The bake in progress, if any. Only one bake runs at a time. */
    @Query("SELECT * FROM bakes WHERE finishedAt IS NULL ORDER BY id DESC LIMIT 1")
    fun observeActive(): Flow<BakeEntity?>

    @Query("SELECT * FROM bakes WHERE finishedAt IS NULL ORDER BY id DESC LIMIT 1")
    suspend fun getActive(): BakeEntity?

    @Query("SELECT * FROM bakes WHERE id = :id")
    suspend fun get(id: Long): BakeEntity

    @Insert
    suspend fun insert(bake: BakeEntity): Long

    @Update
    suspend fun update(bake: BakeEntity)

    @Query("SELECT * FROM bake_steps WHERE bakeId = :bakeId ORDER BY position")
    fun observeSteps(bakeId: Long): Flow<List<BakeStepEntity>>

    @Query("SELECT * FROM bake_steps WHERE bakeId = :bakeId ORDER BY position")
    suspend fun stepsFor(bakeId: Long): List<BakeStepEntity>

    @Insert
    suspend fun insertSteps(steps: List<BakeStepEntity>)

    @Update
    suspend fun updateSteps(steps: List<BakeStepEntity>)
}
