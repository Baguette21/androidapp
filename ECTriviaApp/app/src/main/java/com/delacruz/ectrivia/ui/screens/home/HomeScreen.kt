package com.delacruz.ectrivia.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.delacruz.ectrivia.ui.components.ECTriviaButton
import com.delacruz.ectrivia.ui.theme.ECTriviaBackground
import com.delacruz.ectrivia.ui.theme.ECTriviaPrimary
import com.delacruz.ectrivia.ui.theme.TextPrimary
import com.delacruz.ectrivia.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ECTriviaBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))
        
        // Logo / Title
        Text(
            text = "EC",
            style = MaterialTheme.typography.displayLarge,
            color = ECTriviaPrimary
        )
        Text(
            text = "TRIVIA",
            style = MaterialTheme.typography.displayMedium,
            color = TextPrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Real-time multiplayer trivia",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Buttons
        ECTriviaButton(
            text = "Create Room",
            onClick = onCreateRoom,
            isPrimary = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        ECTriviaButton(
            text = "Join Room",
            onClick = onJoinRoom,
            isPrimary = false
        )
        
        Spacer(modifier = Modifier.height(48.dp))
    }
}
