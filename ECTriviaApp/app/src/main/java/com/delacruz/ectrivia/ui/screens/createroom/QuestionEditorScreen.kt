package com.delacruz.ectrivia.ui.screens.createroom

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.delacruz.ectrivia.ui.components.ECTriviaButton
import com.delacruz.ectrivia.ui.components.ECTriviaTextField
import com.delacruz.ectrivia.ui.components.ErrorDialog
import com.delacruz.ectrivia.ui.components.LoadingIndicator
import com.delacruz.ectrivia.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionEditorScreen(
    roomCode: String,
    onNavigateBack: () -> Unit,
    onQuestionAdded: () -> Unit,
    viewModel: QuestionEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(roomCode) {
        viewModel.setRoomCode(roomCode)
    }

    LaunchedEffect(uiState.questionAdded) {
        if (uiState.questionAdded) {
            onQuestionAdded()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Question", color = TextPrimary) },
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
                        .padding(24.dp)
                ) {
                    // Question Text
                    ECTriviaTextField(
                        value = uiState.questionText,
                        onValueChange = { viewModel.updateQuestionText(it) },
                        label = "Question",
                        placeholder = "Enter your question",
                        maxLines = 3,
                        singleLine = false
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Answer Options (tap to mark correct)",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val answerColors = listOf(AnswerRed, AnswerBlue, AnswerYellow, AnswerGreen)
                    
                    uiState.answers.forEachIndexed { index, answer ->
                        AnswerInputRow(
                            answer = answer,
                            index = index,
                            color = answerColors[index],
                            isCorrect = uiState.correctAnswerIndex == index,
                            onAnswerChange = { viewModel.updateAnswer(index, it) },
                            onSelectCorrect = { viewModel.setCorrectAnswer(index) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    ECTriviaButton(
                        text = "Add Question",
                        onClick = { viewModel.addQuestion() },
                        enabled = uiState.isValid
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
fun AnswerInputRow(
    answer: String,
    index: Int,
    color: androidx.compose.ui.graphics.Color,
    isCorrect: Boolean,
    onAnswerChange: (String) -> Unit,
    onSelectCorrect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.2f))
            .border(
                width = if (isCorrect) 3.dp else 0.dp,
                color = if (isCorrect) CorrectGreen else color.copy(alpha = 0f),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelectCorrect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            if (isCorrect) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Correct",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = ('A' + index).toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        OutlinedTextField(
            value = answer,
            onValueChange = onAnswerChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Answer ${index + 1}") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = color,
                unfocusedBorderColor = color.copy(alpha = 0.5f),
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )
    }
}
