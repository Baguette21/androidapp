package com.delacruz.ectrivia.ui.screens.leaderboard

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
import com.delacruz.ectrivia.data.model.PodiumEntry
import com.delacruz.ectrivia.ui.components.ECTriviaButton
import com.delacruz.ectrivia.ui.components.LeaderboardItem
import com.delacruz.ectrivia.ui.components.LoadingIndicator
import com.delacruz.ectrivia.ui.theme.*

@Composable
fun FinalLeaderboardScreen(
    roomCode: String,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit,
    viewModel: LeaderboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(roomCode) {
        viewModel.loadResults(roomCode)
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
                    text = "Game Over!",
                    style = MaterialTheme.typography.displaySmall,
                    color = ECTriviaPrimary
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Podium Display
                if (uiState.podium.isNotEmpty()) {
                    PodiumDisplay(podium = uiState.podium)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Full Leaderboard
                Text(
                    text = "Final Standings",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.allPlayers) { entry ->
                        LeaderboardItem(
                            entry = entry,
                            isCurrentPlayer = entry.playerId == uiState.currentPlayerId
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ECTriviaButton(
                    text = "Play Again",
                    onClick = onPlayAgain
                )

                Spacer(modifier = Modifier.height(8.dp))

                ECTriviaButton(
                    text = "Exit",
                    onClick = onExit,
                    isPrimary = false
                )
            }
        }
    }
}

@Composable
fun PodiumDisplay(podium: List<PodiumEntry>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd Place
        if (podium.size > 1) {
            PodiumPlace(
                entry = podium[1],
                height = 120.dp,
                color = androidx.compose.ui.graphics.Color.LightGray
            )
        }
        
        // 1st Place
        if (podium.isNotEmpty()) {
            PodiumPlace(
                entry = podium[0],
                height = 160.dp,
                color = StreakGold
            )
        }
        
        // 3rd Place
        if (podium.size > 2) {
            PodiumPlace(
                entry = podium[2],
                height = 100.dp,
                color = StreakOrange
            )
        }
    }
}

@Composable
fun PodiumPlace(
    entry: PodiumEntry,
    height: androidx.compose.ui.unit.Dp,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = entry.nickname,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        
        Text(
            text = "${entry.totalScore}",
            style = MaterialTheme.typography.titleMedium,
            color = ECTriviaSecondary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#${entry.rank}",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary
            )
        }
    }
}
