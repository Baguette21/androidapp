package com.delacruz.ectrivia.ui.screens.game

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.delacruz.ectrivia.data.model.Answer
import com.delacruz.ectrivia.ui.components.CountdownTimer
import com.delacruz.ectrivia.ui.components.LoadingIndicator
import com.delacruz.ectrivia.ui.theme.*

@Composable
fun GamePlayScreen(
    roomCode: String,
    playerId: Long,
    onQuestionEnd: () -> Unit,
    onGameEnd: () -> Unit,
    viewModel: GameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(roomCode, playerId) {
        viewModel.initialize(roomCode, playerId)
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
        } else if (uiState.currentQuestion != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Question Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Question ${uiState.currentQuestionIndex + 1}/${uiState.totalQuestions}",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "${uiState.totalScore} pts",
                        style = MaterialTheme.typography.titleMedium,
                        color = ECTriviaSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timer
                CountdownTimer(
                    remainingSeconds = uiState.remainingSeconds,
                    totalSeconds = uiState.timerSeconds
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Question Card
                QuestionCard(
                    questionText = uiState.currentQuestion!!.questionText,
                    modifier = Modifier.weight(0.3f)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Answer Buttons
                val answerColors = listOf(AnswerRed, AnswerBlue, AnswerYellow, AnswerGreen)
                val answers = uiState.currentQuestion!!.answers.sortedBy { it.index }
                if (answers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(0.7f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Waiting for answers...",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                Column(
                    modifier = Modifier.weight(0.7f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    answers.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            row.forEach { answer ->
                                AnswerButton(
                                    answer = answer,
                                    color = answerColors[answer.index % answerColors.size],
                                    isSelected = uiState.selectedAnswerIndex == answer.index,
                                    isCorrect = uiState.showResult && answer.index == uiState.correctAnswerIndex,
                                    isWrong = uiState.showResult && uiState.selectedAnswerIndex == answer.index && answer.index != uiState.correctAnswerIndex,
                                    enabled = !uiState.hasAnswered,
                                    onClick = { viewModel.submitAnswer(answer.index) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
                }

                // Answer Feedback
                AnimatedVisibility(
                    visible = uiState.showResult,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    AnswerFeedback(
                        isCorrect = uiState.lastAnswerCorrect,
                        pointsEarned = uiState.lastPointsEarned,
                        newStreak = uiState.currentStreak
                    )
                }
            }
        }
    }
}

@Composable
fun QuestionCard(
    questionText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ECTriviaSurfaceVariant)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = questionText,
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AnswerButton(
    answer: Answer,
    color: Color,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isCorrect -> CorrectGreen
        isWrong -> IncorrectRed
        isSelected -> color.copy(alpha = 0.7f)
        else -> color
    }

    Button(
        onClick = onClick,
        modifier = modifier.fillMaxHeight(),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.6f)
        )
    ) {
        Text(
            text = answer.text,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun AnswerFeedback(
    isCorrect: Boolean,
    pointsEarned: Int,
    newStreak: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isCorrect) "Correct!" else "Wrong!",
            style = MaterialTheme.typography.headlineMedium,
            color = if (isCorrect) CorrectGreen else IncorrectRed
        )
        
        if (isCorrect && pointsEarned > 0) {
            Text(
                text = "+$pointsEarned points",
                style = MaterialTheme.typography.titleLarge,
                color = ECTriviaSecondary
            )
            if (newStreak > 1) {
                Text(
                    text = "$newStreak streak!",
                    style = MaterialTheme.typography.titleMedium,
                    color = StreakOrange
                )
            }
        }
    }
}
