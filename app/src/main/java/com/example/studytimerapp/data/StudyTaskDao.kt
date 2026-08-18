package com.example.studytimerapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyTaskDao {
    @Insert
    suspend fun insert(task: StudyTask)

    @Update
    suspend fun update(task: StudyTask)

    @Delete
    suspend fun delete(task: StudyTask)

    @Query("SELECT * FROM study_tasks ORDER BY id DESC")
    fun getAllTasks(): Flow<List<StudyTask>>

    @Query("SELECT * FROM study_tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): StudyTask?

    @Query("DELETE FROM study_tasks WHERE isCompleted = 1")
    suspend fun deleteCompletedTasks()
}
