package com.delacruz.ectrivia.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GameStateDto(
    @SerializedName("eventId") val eventId: String,
    @SerializedName("roomCode") val roomCode: String,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("questionId") val questionId: Long?,
    @SerializedName("questionIndex") val questionIndex: Int?,
    @SerializedName("totalQuestions") val totalQuestions: Int?,
    @SerializedName("questionStartTime") val questionStartTime: Long?,
    @SerializedName("timerSeconds") val timerSeconds: Int?,
    @SerializedName("payload") val payload: Any?,
    @SerializedName("question") val question: Any?,
    @SerializedName("serverTimestamp") val serverTimestamp: Long
)

data class ScoreUpdateDto(
    @SerializedName("eventId") val eventId: String,
    @SerializedName("roomCode") val roomCode: String,
    @SerializedName("playerId") val playerId: Long,
    @SerializedName("playerNickname") val playerNickname: String,
    @SerializedName("questionId") val questionId: Long,
    @SerializedName("isCorrect") val isCorrect: Boolean,
    @SerializedName("correctAnswerIndex") val correctAnswerIndex: Int,
    @SerializedName("pointsEarned") val pointsEarned: Int,
    @SerializedName("newTotalScore") val newTotalScore: Int,
    @SerializedName("previousStreak") val previousStreak: Int,
    @SerializedName("newStreak") val newStreak: Int,
    @SerializedName("currentRank") val currentRank: Int,
    @SerializedName("serverTimestamp") val serverTimestamp: Long
)

data class PlayerEventDto(
    @SerializedName("eventId") val eventId: String,
    @SerializedName("roomCode") val roomCode: String,
    @SerializedName("eventType") val eventType: String,
    @SerializedName("player") val player: PlayerDto,
    @SerializedName("serverTimestamp") val serverTimestamp: Long
)
