package com.delacruz.ectrivia.ui.screens.results

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.delacruz.ectrivia.ui.components.ECTriviaButton
import com.delacruz.ectrivia.ui.components.LeaderboardItem
import com.delacruz.ectrivia.ui.components.LoadingIndicator
import com.delacruz.ectrivia.ui.theme.*

@Composable
fun QuestionResultScreen(
    roomCode: String,
    onNextQuestion: () -> Unit,
    onGameEnd: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(roomCode) {
        viewModel.initialize(roomCode)
    }

    LaunchedEffect(uiState.nextQuestionReady) {
        if (uiState.nextQuestionReady) {
            onNextQuestion()
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Question Results",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Correct Answer Display
                Text(
                    text = "Correct Answer",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextSecondary
                )
                Text(
                    text = uiState.correctAnswerText,
                    style = MaterialTheme.typography.titleLarge,
                    color = CorrectGreen,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Your Result
                if (uiState.yourResult != null) {
                    YourResultCard(
                        isCorrect = uiState.yourResult!!.isCorrect,
                        pointsEarned = uiState.yourResult!!.pointsEarned,
                        newRank = uiState.yourResult!!.newRank
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Top 5 Leaderboard
                Text(
                    text = "Top Players",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.topPlayers.take(5)) { entry ->
                        LeaderboardItem(entry = entry)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Next question in ${uiState.countdownSeconds}s",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
fun YourResultCard(
    isCorrect: Boolean,
    pointsEarned: Int,
    newRank: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isCorrect) CorrectGreen.copy(alpha = 0.2f) 
                else IncorrectRed.copy(alpha = 0.2f)
            )
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isCorrect) "You got it right!" else "Better luck next time",
            style = MaterialTheme.typography.titleLarge,
            color = if (isCorrect) CorrectGreen else IncorrectRed
        )
        if (isCorrect) {
            Text(
                text = "+$pointsEarned points",
                style = MaterialTheme.typography.headlineMedium,
                color = ECTriviaSecondary
            )
        }
        Text(
            text = "Current Rank: #$newRank",
            style = MaterialTheme.typography.bodyLarge,
            color = TextPrimary
        )
    }
}
