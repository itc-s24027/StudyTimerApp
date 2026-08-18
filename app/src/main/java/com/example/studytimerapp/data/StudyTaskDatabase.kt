package com.example.studytimerapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StudyTask::class], version = 1, exportSchema = false)
abstract class StudyTaskDatabase : RoomDatabase() {
    abstract fun studyTaskDao(): StudyTaskDao

    companion object {
        @Volatile
        private var INSTANCE: StudyTaskDatabase? = null

        fun getDatabase(context: Context): StudyTaskDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    StudyTaskDatabase::class.java,
                    "study_task_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
