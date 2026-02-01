package com.delacruz.ectrivia.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LeaderboardDto(
    @SerializedName("roomCode") val roomCode: String,
    @SerializedName("questionIndex") val questionIndex: Int,
    @SerializedName("totalQuestions") val totalQuestions: Int,
    @SerializedName("leaderboard") val leaderboard: List<LeaderboardEntryDto>
)

data class LeaderboardEntryDto(
    @SerializedName("rank") val rank: Int,
    @SerializedName("playerId") val playerId: Long,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("totalScore") val totalScore: Int,
    @SerializedName("currentStreak") val currentStreak: Int
)

data class GameResultsDto(
    @SerializedName("roomCode") val roomCode: String,
    @SerializedName("status") val status: String,
    @SerializedName("totalQuestions") val totalQuestions: Int,
    @SerializedName("duration") val duration: Int,
    @SerializedName("podium") val podium: List<PodiumEntryDto>,
    @SerializedName("allPlayers") val allPlayers: List<LeaderboardEntryDto>,
    @SerializedName("finishedAt") val finishedAt: String?
)

data class PodiumEntryDto(
    @SerializedName("rank") val rank: Int,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("totalScore") val totalScore: Int
)
