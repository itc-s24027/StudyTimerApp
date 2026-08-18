package com.example.studytimerapp.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    taskId: Int,
    viewModel: StudyTaskViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.timerUiState.collectAsState()

    LaunchedEffect(taskId) {
        viewModel.setupTimer(taskId)
    }

    // Animations
    val circleColor by animateColorAsState(
        targetValue = if (uiState.isFinished) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
        animationSpec = tween(durationMillis = 1000),
        label = "CircleColor"
    )

    val circleScale by animateFloatAsState(
        targetValue = if (uiState.isFinished) 1.1f else 1.0f,
        animationSpec = tween(durationMillis = 500),
        label = "CircleScale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("タイマー", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = "${uiState.taskTitle}",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
            
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .scale(circleScale)
                    .clip(CircleShape)
                    .background(circleColor.copy(alpha = 0.2f))
                    .border(4.dp, circleColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (uiState.isFinished) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(48.dp)
                        )
                    } else {
                        Text(
                            text = "残り時間",
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
                        )
                    }
                    
                    val minutes = uiState.remainingSeconds / 60
                    val seconds = uiState.remainingSeconds % 60
                    Text(
                        text = "%d:%02d".format(minutes, seconds),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 72.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            if (!uiState.isRunning && !uiState.isFinished) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(8.dp)
                ) {
                    listOf(1, 5, 25).forEach { min ->
                        FilterChip(
                            selected = uiState.selectedMinutes == min,
                            onClick = { viewModel.selectMinutes(min) },
                            label = { Text("${min}分") },
                            modifier = Modifier.padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            AnimatedVisibility(
                visible = uiState.isFinished,
                enter = fadeIn() + expandVertically() + scaleIn(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "タイマーが終了しました！",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            viewModel.completeTask(onNavigateBack)
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .fillMaxWidth(0.6f)
                    ) {
                        Text("完了にする", fontSize = 18.sp)
                    }
                }
            }

            if (!uiState.isFinished) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (!uiState.isRunning) {
                        Button(
                            onClick = { viewModel.startTimer() },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(56.dp).weight(1f)
                        ) {
                            Text("スタート", fontSize = 18.sp)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.pauseTimer() },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                            modifier = Modifier.height(56.dp).weight(1f)
                        ) {
                            Text("一時停止", fontSize = 18.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    OutlinedButton(
                        onClick = { viewModel.resetTimer() },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(56.dp).weight(1f)
                    ) {
                        Text("リセット", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
