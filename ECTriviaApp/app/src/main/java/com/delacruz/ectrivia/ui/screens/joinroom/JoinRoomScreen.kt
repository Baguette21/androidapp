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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
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
fun JoinRoomScreen(
    onNavigateBack: () -> Unit,
    onRoomFound: (String) -> Unit,
    viewModel: JoinRoomViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.roomFound) {
        if (uiState.roomFound) {
            onRoomFound(uiState.roomCode)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Join Room", color = TextPrimary) },
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
                        text = "Enter Room Code",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    ECTriviaTextField(
                        value = uiState.roomCode,
                        onValueChange = { 
                            if (it.length <= Constants.ROOM_CODE_LENGTH) {
                                viewModel.updateRoomCode(it.uppercase())
                            }
                        },
                        label = "Room Code",
                        placeholder = "ABC123",
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done,
                        capitalization = KeyboardCapitalization.Characters,
                        onImeAction = { viewModel.checkRoom() },
                        isError = uiState.error != null,
                        errorMessage = uiState.error
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    ECTriviaButton(
                        text = "Continue",
                        onClick = { viewModel.checkRoom() },
                        enabled = uiState.roomCode.length == Constants.ROOM_CODE_LENGTH
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
