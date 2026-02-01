package com.delacruz.ectrivia.ui.screens.createroom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.delacruz.ectrivia.ui.components.ECTriviaButton
import com.delacruz.ectrivia.ui.components.ECTriviaTextField
import com.delacruz.ectrivia.ui.components.ErrorDialog
import com.delacruz.ectrivia.ui.components.LoadingIndicator
import com.delacruz.ectrivia.ui.theme.*
import com.delacruz.ectrivia.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRoomScreen(
    onNavigateBack: () -> Unit,
    onThemeSelected: (String, Int) -> Unit,
    onCustomQuestions: (String, Long, Boolean) -> Unit,
    onRoomCreated: (String, Long, Boolean) -> Unit,
    viewModel: CreateRoomViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.roomCreated) {
        uiState.createdRoomCode?.let { roomCode ->
            uiState.playerId?.let { playerId ->
                if (uiState.isThemeBased) {
                    onRoomCreated(roomCode, playerId, true)
                } else {
                    onCustomQuestions(roomCode, playerId, true)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Room", color = TextPrimary) },
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
                    Text(
                        text = "Room Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Host nickname
                    ECTriviaTextField(
                        value = uiState.hostNickname,
                        onValueChange = { viewModel.updateHostNickname(it) },
                        label = "Your Nickname"
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Question Mode Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Use Theme Questions",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = uiState.isThemeBased,
                            onCheckedChange = { viewModel.toggleThemeBased() },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = ECTriviaPrimary,
                                checkedTrackColor = ECTriviaPrimary.copy(alpha = 0.5f)
                            )
                        )
                    }

                    if (uiState.isThemeBased) {
                        Text(
                            text = "You'll play as a regular player with premade questions",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        Text(
                            text = "You'll create custom questions and watch players compete",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Timer Slider
                    Text(
                        text = "Question Timer: ${uiState.timerSeconds}s",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimary
                    )
                    Slider(
                        value = uiState.timerSeconds.toFloat(),
                        onValueChange = { viewModel.updateTimer(it.toInt()) },
                        valueRange = Constants.MIN_TIMER_SECONDS.toFloat()..Constants.MAX_TIMER_SECONDS.toFloat(),
                        steps = Constants.MAX_TIMER_SECONDS - Constants.MIN_TIMER_SECONDS - 1,
                        colors = SliderDefaults.colors(
                            thumbColor = ECTriviaPrimary,
                            activeTrackColor = ECTriviaPrimary
                        )
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Corrected Logic: Only show "Create Room" directly if it's NOT theme-based (Custom Mode).
                    // If it IS theme-based, we must go to "Select Theme" first.
                    // The ViewModel's createRoom() should ONLY be called for Custom mode here, 
                    // or after theme selection.
                    
                    if (uiState.isThemeBased) {
                         ECTriviaButton(
                            text = "Select Theme",
                            onClick = { 
                                onThemeSelected(uiState.hostNickname, uiState.timerSeconds)
                            },
                            enabled = uiState.hostNickname.isNotBlank()
                        )
                    } else {
                        ECTriviaButton(
                            text = "Create Room",
                            onClick = { viewModel.createRoom() },
                            enabled = uiState.hostNickname.isNotBlank()
                        )
                    }
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
