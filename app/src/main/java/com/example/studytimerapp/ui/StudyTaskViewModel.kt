package com.example.studytimerapp.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.studytimerapp.data.StudyTask
import com.example.studytimerapp.data.StudyTaskDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TimerUiState(
    val taskId: Int = 0,
    val taskTitle: String = "",
    val selectedMinutes: Int = 25,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false
)

class StudyTaskViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = StudyTaskDatabase.getDatabase(application).studyTaskDao()

    val allTasks: LiveData<List<StudyTask>> = dao.getAllTasks().asLiveData()

    // Timer State
    private val _timerUiState = MutableStateFlow(TimerUiState())
    val timerUiState: StateFlow<TimerUiState> = _timerUiState.asStateFlow()

    private var timerJob: Job? = null

    // Task Management
    fun addTask(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            dao.insert(StudyTask(title = title))
        }
    }

    fun toggleTaskCompletion(task: StudyTask) {
        viewModelScope.launch {
            dao.update(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: StudyTask) {
        viewModelScope.launch {
            dao.delete(task)
        }
    }

    fun deleteCompletedTasks() {
        viewModelScope.launch {
            dao.deleteCompletedTasks()
        }
    }

    // Timer Actions
    fun setupTimer(taskId: Int) {
        viewModelScope.launch {
            val task = dao.getTaskById(taskId)
            task?.let {
                _timerUiState.update { state ->
                    state.copy(
                        taskId = it.id,
                        taskTitle = it.title,
                        selectedMinutes = 25,
                        remainingSeconds = 25 * 60,
                        isRunning = false,
                        isFinished = false
                    )
                }
            }
        }
    }

    fun selectMinutes(minutes: Int) {
        _timerUiState.update { it.copy(selectedMinutes = minutes, remainingSeconds = minutes * 60, isRunning = false, isFinished = false) }
        stopTimer()
    }

    fun startTimer() {
        if (_timerUiState.value.isRunning || _timerUiState.value.isFinished) return

        _timerUiState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (_timerUiState.value.remainingSeconds > 0) {
                delay(1000)
                _timerUiState.update { it.copy(remainingSeconds = it.remainingSeconds - 1) }
            }
            _timerUiState.update { it.copy(isRunning = false, isFinished = true) }
        }
    }

    fun pauseTimer() {
        _timerUiState.update { it.copy(isRunning = false) }
        stopTimer()
    }

    fun resetTimer() {
        stopTimer()
        val minutes = _timerUiState.value.selectedMinutes
        _timerUiState.update { it.copy(remainingSeconds = minutes * 60, isRunning = false, isFinished = false) }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun completeTask(onComplete: () -> Unit) {
        viewModelScope.launch {
            val taskId = _timerUiState.value.taskId
            val task = dao.getTaskById(taskId)
            task?.let {
                dao.update(it.copy(isCompleted = true))
                // Clear timer state after completion
                _timerUiState.update { state -> state.copy(isFinished = false, isRunning = false) }
            }
            onComplete()
        }
    }
}
