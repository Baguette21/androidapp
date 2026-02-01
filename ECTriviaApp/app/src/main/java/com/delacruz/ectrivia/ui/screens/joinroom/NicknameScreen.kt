package com.delacruz.ectrivia.ui.screens.joinroom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.delacruz.ectrivia.ui.components.ECTriviaButton
import com.delacruz.ectrivia.ui.components.ECTriviaTextField
import com.delacruz.ectrivia.ui.components.ErrorDialog
import com.delacruz.ectrivia.ui.components.LoadingIndicator
import com.delacruz.ectrivia.ui.theme.ECTriviaBackground
import com.delacruz.ectrivia.ui.theme.TextPrimary
import com.delacruz.ectrivia.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NicknameScreen(
    roomCode: String,
    onNavigateBack: () -> Unit,
    onJoined: (Long, Boolean) -> Unit,
    viewModel: NicknameViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(roomCode) {
        viewModel.setRoomCode(roomCode)
    }

    LaunchedEffect(uiState.joinedPlayerId) {
        uiState.joinedPlayerId?.let { playerId ->
            onJoined(playerId, uiState.isHost)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Choose Nickname", color = TextPrimary) },
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
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    Text(
                        text = "Room: $roomCode",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    ECTriviaTextField(
                        value = uiState.nickname,
                        onValueChange = { 
                            if (it.length <= Constants.MAX_NICKNAME_LENGTH) {
                                viewModel.updateNickname(it)
                            }
                        },
                        label = "Your Nickname",
                        placeholder = "Enter your nickname",
                        imeAction = ImeAction.Done,
                        onImeAction = { viewModel.joinRoom() },
                        isError = uiState.error != null,
                        errorMessage = uiState.error
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    ECTriviaButton(
                        text = "Join Game",
                        onClick = { viewModel.joinRoom() },
                        enabled = uiState.nickname.isNotBlank()
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
