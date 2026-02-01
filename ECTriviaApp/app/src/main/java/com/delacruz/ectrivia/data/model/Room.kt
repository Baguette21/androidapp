package com.delacruz.ectrivia.data.model

data class Room(
    val id: Long,
    val roomCode: String,
    val status: RoomStatus,
    val category: Category?,
    val isThemeBased: Boolean,
    val questionTimerSeconds: Int,
    val maxPlayers: Int,
    val currentQuestionIndex: Int,
    val totalQuestions: Int,
    val players: List<Player>,
    val createdAt: String
)

enum class RoomStatus {
    LOBBY,
    IN_PROGRESS,
    FINISHED,
    CANCELLED
}
