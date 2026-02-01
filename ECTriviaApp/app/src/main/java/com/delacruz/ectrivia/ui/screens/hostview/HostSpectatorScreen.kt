package com.delacruz.ectrivia.ui.screens.hostview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.delacruz.ectrivia.ui.components.CountdownTimer
import com.delacruz.ectrivia.ui.components.LeaderboardItem
import com.delacruz.ectrivia.ui.components.LoadingIndicator
import com.delacruz.ectrivia.ui.theme.*

@Composable
fun HostSpectatorScreen(
    roomCode: String,
    onQuestionEnd: () -> Unit,
    onGameEnd: () -> Unit,
    viewModel: HostSpectatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(roomCode) {
        viewModel.initialize(roomCode)
    }

    LaunchedEffect(uiState.questionEnded) {
        if (uiState.questionEnded) {
            onQuestionEnd()
        }
    }

    LaunchedEffect(uiState.gameEnded) {
        if (uiState.gameEnded) {
            onGameEnd()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ECTriviaBackground)
    ) {
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Question Progress
                Text(
                    text = "Question ${uiState.currentQuestionIndex + 1}/${uiState.totalQuestions}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Timer
                CountdownTimer(
                    remainingSeconds = uiState.remainingSeconds,
                    totalSeconds = uiState.timerSeconds
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Current Question
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ECTriviaSurfaceVariant)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.currentQuestionText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Answer Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatBox(
                        label = "Answered",
                        value = "${uiState.answeredCount}/${uiState.totalPlayers}"
                    )
                    StatBox(
                        label = "Correct",
                        value = "${uiState.correctCount}"
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Live Leaderboard
                Text(
                    text = "Live Leaderboard",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.leaderboard) { entry ->
                        LeaderboardItem(entry = entry)
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ECTriviaSurfaceVariant)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = ECTriviaSecondary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}
