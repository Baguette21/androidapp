package com.delacruz.ectrivia.data.model

data class LeaderboardEntry(
    val rank: Int,
    val playerId: Long,
    val nickname: String,
    val totalScore: Int,
    val currentStreak: Int
)

data class GameResults(
    val roomCode: String,
    val totalQuestions: Int,
    val duration: Int,
    val podium: List<PodiumEntry>,
    val allPlayers: List<LeaderboardEntry>,
    val finishedAt: String
)

data class PodiumEntry(
    val rank: Int,
    val nickname: String,
    val totalScore: Int
)
