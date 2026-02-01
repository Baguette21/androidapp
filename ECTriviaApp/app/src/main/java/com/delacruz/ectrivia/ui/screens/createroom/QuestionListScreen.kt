package com.delacruz.ectrivia.ui.screens.createroom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.delacruz.ectrivia.ui.components.ErrorDialog
import com.delacruz.ectrivia.ui.components.LoadingIndicator
import com.delacruz.ectrivia.ui.components.ECTriviaButton
import com.delacruz.ectrivia.ui.theme.*

data class QuestionDisplay(
    val id: Long,
    val text: String,
    val answersCount: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionListScreen(
    roomCode: String,
    playerId: Long,
    isHost: Boolean,
    onNavigateBack: () -> Unit,
    onAddQuestion: () -> Unit,
    onStartGame: (Long, Boolean) -> Unit,
    viewModel: QuestionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(roomCode) {
        viewModel.setRoomCode(roomCode)
        viewModel.loadQuestions()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Questions", color = TextPrimary)
                        Text(
                            text = "Room: $roomCode",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ECTriviaBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddQuestion,
                containerColor = ECTriviaPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Question", tint = TextPrimary)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ECTriviaBackground)
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                LoadingIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    if (uiState.questions.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No questions yet",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap + to add your first question",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(uiState.questions) { index, question ->
                                QuestionListItem(
                                    index = index + 1,
                                    question = QuestionDisplay(
                                        id = question.id,
                                        text = question.questionText,
                                        answersCount = question.answers.size
                                    ),
                                    onDelete = { viewModel.deleteQuestion(question.id) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    ECTriviaButton(
                        text = "Start Game (${uiState.questions.size} questions)",
                        onClick = { onStartGame(playerId, isHost) },
                        enabled = uiState.questions.isNotEmpty()
                    )
                }
            }

            uiState.error?.let { error ->
                ErrorDialog(
                    title = "Error",
                    message = error,
                    onDismiss = { viewModel.clearError() }
                )
            }
        }
    }
}

@Composable
fun QuestionListItem(
    index: Int,
    question: QuestionDisplay,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ECTriviaSurfaceVariant)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(ECTriviaPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$index",
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = question.text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 2
        )
        
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = IncorrectRed
            )
        }
    }
}
