package com.sourdough.companion.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.Instant

class Converters {
    @TypeConverter
    fun instantToEpochMillis(value: Instant?): Long? = value?.toEpochMilli()

    @TypeConverter
    fun epochMillisToInstant(value: Long?): Instant? = value?.let(Instant::ofEpochMilli)
}

@Database(
    entities = [
        TemplateEntity::class,
        TemplateStepEntity::class,
        FlavourEntity::class,
        FlavourIngredientEntity::class,
        TempCurvePointEntity::class,
        BakeEntity::class,
        BakeStepEntity::class,
        TempReadingEntity::class,
        SettingEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class SourdoughDatabase : RoomDatabase() {
    abstract fun templateDao(): TemplateDao

    abstract fun bakeDao(): BakeDao

    companion object {
        fun create(context: Context): SourdoughDatabase =
            Room.databaseBuilder(context, SourdoughDatabase::class.java, "sourdough.db").build()
    }
}
