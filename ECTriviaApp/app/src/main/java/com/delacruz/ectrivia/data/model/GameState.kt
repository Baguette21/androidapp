package com.delacruz.ectrivia.data.model

data class GameState(
    val eventType: GameEventType,
    val roomCode: String,
    val question: Question?,
    val questionIndex: Int,
    val totalQuestions: Int,
    val timerSeconds: Int,
    val questionStartTime: Long
)

enum class GameEventType {
    PLAYER_JOINED,
    PLAYER_LEFT,
    GAME_STARTING,
    QUESTION_START,
    QUESTION_END,
    GAME_FINISHED,
    HOST_CHANGED
}

data class AnswerResult(
    val isCorrect: Boolean,
    val correctAnswerIndex: Int,
    val pointsEarned: Int,
    val newTotalScore: Int,
    val newStreak: Int,
    val currentRank: Int
)

data class PlayerSession(
    val playerId: Long,
    val nickname: String,
    val roomCode: String,
    val isHost: Boolean
)
