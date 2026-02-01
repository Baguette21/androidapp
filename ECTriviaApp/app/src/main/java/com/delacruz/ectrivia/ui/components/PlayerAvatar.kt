package com.delacruz.ectrivia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.delacruz.ectrivia.ui.theme.AnswerBlue
import com.delacruz.ectrivia.ui.theme.AnswerGreen
import com.delacruz.ectrivia.ui.theme.AnswerRed
import com.delacruz.ectrivia.ui.theme.AnswerYellow

@Composable
fun PlayerAvatar(
    nickname: String,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    isHost: Boolean = false
) {
    val colors = listOf(AnswerRed, AnswerBlue, AnswerYellow, AnswerGreen)
    val colorIndex = nickname.hashCode().let { if (it < 0) -it else it } % colors.size
    val backgroundColor = colors[colorIndex]
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = nickname.take(2).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
    }
}
