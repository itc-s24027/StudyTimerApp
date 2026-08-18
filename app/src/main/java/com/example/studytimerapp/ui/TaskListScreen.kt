package com.example.studytimerapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.studytimerapp.data.StudyTask

enum class TaskFilter(val label: String) {
    ALL("すべて"),
    UNCOMPLETED("未完了"),
    COMPLETED("完了済み")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    viewModel: StudyTaskViewModel,
    onStartTimer: (Int) -> Unit
) {
    val tasks: List<StudyTask> by viewModel.allTasks.observeAsState(initial = emptyList())
    var newTaskTitle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var taskToDelete by remember { mutableStateOf<StudyTask?>(null) }
    var currentFilter by remember { mutableStateOf(TaskFilter.ALL) }
    var showDeleteCompletedDialog by remember { mutableStateOf(false) }

    val filteredTasks = when (currentFilter) {
        TaskFilter.ALL -> tasks
        TaskFilter.UNCOMPLETED -> tasks.filter { !it.isCompleted }
        TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "学習タイマー",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                TextField(
                    value = newTaskTitle,
                    onValueChange = {
                        newTaskTitle = it
                        errorMessage = null
                    },
                    placeholder = { Text("タスクを追加 (例: 数学の宿題)") },
                    modifier = Modifier.weight(1f),
                    isError = errorMessage != null,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newTaskTitle.isBlank()) {
                            errorMessage = "タスクを入力してね！"
                        } else {
                            viewModel.addTask(newTaskTitle)
                            newTaskTitle = ""
                            errorMessage = null
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("追加")
                }
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SecondaryTabRow(
            selectedTabIndex = currentFilter.ordinal,
            containerColor = Color.Transparent,
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            TaskFilter.entries.forEach { filter ->
                Tab(
                    selected = currentFilter == filter,
                    onClick = { currentFilter = filter },
                    text = { 
                        Text(
                            text = filter.label,
                            style = MaterialTheme.typography.titleSmall
                        ) 
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (currentFilter == TaskFilter.COMPLETED && filteredTasks.isNotEmpty()) {
            TextButton(
                onClick = { showDeleteCompletedDialog = true },
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("完了済みをすべて削除", style = MaterialTheme.typography.labelMedium)
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = filteredTasks,
                key = { it.id }
            ) { taskItem ->
                TaskItem(
                    task = taskItem,
                    onToggle = { viewModel.toggleTaskCompletion(taskItem) },
                    onStart = { onStartTimer(taskItem.id) },
                    onDelete = { taskToDelete = taskItem }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val completedCount = tasks.filter { it.isCompleted }.size
        val totalCount = tasks.size
        
        if (totalCount > 0) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = " 完了：${completedCount}件 / 全体：${totalCount}件",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                    )
                    if (completedCount == totalCount) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "タスクは全部完了しました！",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("削除しますか？") },
            text = { Text("「${task.title}」をリストから削除します") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTask(task)
                    taskToDelete = null
                }) {
                    Text("削除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("キャンセル")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Delete All Completed Confirmation Dialog
    if (showDeleteCompletedDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteCompletedDialog = false },
            title = { Text("一括削除の確認") },
            text = { Text("完了したタスクをすべて削除しますか？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCompletedTasks()
                    showDeleteCompletedDialog = false
                }) {
                    Text("削除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCompletedDialog = false }) {
                    Text("キャンセル")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun TaskItem(
    task: StudyTask,
    onToggle: () -> Unit,
    onStart: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = task.isCompleted
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) 
                MaterialTheme.colorScheme.surfaceVariant
            else 
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Checkbox(
                checked = isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            
            Text(
                text = task.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge.copy(
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isCompleted) Color.Gray else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.Medium
                )
            )

            if (!isCompleted) {
                IconButton(
                    onClick = onStart,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        Icons.Default.PlayArrow, 
                        contentDescription = "開始", 
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = "削除", 
                    tint = if (isCompleted) Color.Gray.copy(alpha = 0.3f) else Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
