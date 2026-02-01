package com.delacruz.ectrivia.ui.screens.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.delacruz.ectrivia.data.model.Player
import com.delacruz.ectrivia.ui.components.ECTriviaButton
import com.delacruz.ectrivia.ui.components.ErrorDialog
import com.delacruz.ectrivia.ui.components.LoadingIndicator
import com.delacruz.ectrivia.ui.components.PlayerAvatar
import com.delacruz.ectrivia.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen(
    roomCode: String,
    playerId: Long,
    isHost: Boolean,
    onNavigateBack: () -> Unit,
    onGameStart: (Boolean, Long) -> Unit,
    onLeaveRoom: () -> Unit,
    viewModel: LobbyViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(roomCode, playerId, isHost) {
        viewModel.initialize(roomCode, playerId, isHost)
    }

    LaunchedEffect(uiState.gameStarted) {
        if (uiState.gameStarted) {
            val resolvedPlayerId = when {
                playerId != 0L -> playerId
                isHost -> uiState.players.firstOrNull { it.isHost }?.id ?: playerId
                else -> playerId
            }
            onGameStart(uiState.isThemeBased, resolvedPlayerId)
        }
    }

    LaunchedEffect(uiState.leftRoom) {
        if (uiState.leftRoom) {
            onLeaveRoom()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lobby", color = TextPrimary) },
                actions = {
                    IconButton(onClick = { viewModel.leaveRoom() }) {
                        Icon(
                            Icons.Filled.ExitToApp,
                            contentDescription = "Leave",
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
                    // Room Code Display
                    Text(
                        text = "Room Code",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = roomCode,
                        style = MaterialTheme.typography.displaySmall,
                        color = ECTriviaPrimary
                    )
                    Text(
                        text = "Share this code with friends!",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Players count
                    Text(
                        text = "${uiState.players.size} Players",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Player List
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.players) { player ->
                            PlayerListItem(player = player)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isHost) {
                        ECTriviaButton(
                            text = "Start Game",
                            onClick = { viewModel.startGame() },
                            enabled = uiState.players.size >= 1 // At least host
                        )
                    } else {
                        Text(
                            text = "Waiting for host to start...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
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

@Composable
fun PlayerListItem(player: Player) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ECTriviaSurfaceVariant)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PlayerAvatar(
            nickname = player.nickname,
            size = 40.dp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = player.nickname,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            if (player.isHost) {
                Text(
                    text = "Host",
                    style = MaterialTheme.typography.bodySmall,
                    color = ECTriviaSecondary
                )
            }
        }
        
        if (!player.isConnected) {
            Text(
                text = "Disconnected",
                style = MaterialTheme.typography.bodySmall,
                color = IncorrectRed
            )
        }
    }
}
