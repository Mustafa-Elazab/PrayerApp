package com.mostafa.alaymiatask.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mostafa.alaymiatask.data.local.converter.Converters
import com.mostafa.alaymiatask.data.local.dao.PrayTimeDao
import com.mostafa.alaymiatask.data.remote.dto.AladhanResponseDTO


@Database(
    entities = [AladhanResponseDTO::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun prayDao(): PrayTimeDao


}