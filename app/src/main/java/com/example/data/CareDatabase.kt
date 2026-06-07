package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Medication::class, MedicalDocument::class, SymptomLog::class, UserProfile::class], version = 2, exportSchema = false)
abstract class CareDatabase : RoomDatabase() {

    abstract fun careDao(): CareDao

    companion object {
        @Volatile
        private var INSTANCE: CareDatabase? = null

        fun getDatabase(context: Context): CareDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CareDatabase::class.java,
                    "care_giver_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
