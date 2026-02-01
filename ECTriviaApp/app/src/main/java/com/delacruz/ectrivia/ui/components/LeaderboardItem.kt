package com.delacruz.ectrivia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.delacruz.ectrivia.data.model.LeaderboardEntry
import com.delacruz.ectrivia.ui.theme.*

@Composable
fun LeaderboardItem(
    entry: LeaderboardEntry,
    modifier: Modifier = Modifier,
    isCurrentPlayer: Boolean = false
) {
    val backgroundColor = when (entry.rank) {
        1 -> StreakGold.copy(alpha = 0.2f)
        2 -> Color.LightGray.copy(alpha = 0.2f)
        3 -> StreakOrange.copy(alpha = 0.2f)
        else -> ECTriviaSurfaceVariant
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isCurrentPlayer) ECTriviaPrimary.copy(alpha = 0.3f) else backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rank
        Text(
            text = "#${entry.rank}",
            style = MaterialTheme.typography.titleMedium,
            color = when (entry.rank) {
                1 -> StreakGold
                2 -> Color.LightGray
                3 -> StreakOrange
                else -> TextSecondary
            },
            modifier = Modifier.width(40.dp)
        )
        
        // Avatar
        PlayerAvatar(
            nickname = entry.nickname,
            size = 40.dp
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Name and streak
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.nickname,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            if (entry.currentStreak > 0) {
                Text(
                    text = "${entry.currentStreak} streak",
                    style = MaterialTheme.typography.bodySmall,
                    color = StreakOrange
                )
            }
        }
        
        // Score
        Text(
            text = "${entry.totalScore}",
            style = MaterialTheme.typography.titleLarge,
            color = ECTriviaSecondary
        )
    }
}
