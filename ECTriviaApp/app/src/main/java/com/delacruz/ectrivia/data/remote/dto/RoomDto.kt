package com.delacruz.ectrivia.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RoomDto(
    @SerializedName("id") val id: Long,
    @SerializedName("roomCode") val roomCode: String,
    @SerializedName("status") val status: String,
    @SerializedName("category") val category: CategoryDto?,
    @SerializedName("isThemeBased") val isThemeBased: Boolean,
    @SerializedName("questionTimerSeconds") val questionTimerSeconds: Int,
    @SerializedName("maxPlayers") val maxPlayers: Int,
    @SerializedName("currentQuestionIndex") val currentQuestionIndex: Int,
    @SerializedName("totalQuestions") val totalQuestions: Int,
    @SerializedName("players") val players: List<PlayerDto>?,
    @SerializedName("createdAt") val createdAt: String
)

data class CreateRoomRequest(
    @SerializedName("categoryId") val categoryId: Long?,
    @SerializedName("isThemeBased") val isThemeBased: Boolean,
    @SerializedName("questionTimerSeconds") val questionTimerSeconds: Int,
    @SerializedName("maxPlayers") val maxPlayers: Int
)

data class CreateRoomResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("roomCode") val roomCode: String,
    @SerializedName("status") val status: String,
    @SerializedName("isThemeBased") val isThemeBased: Boolean,
    @SerializedName("questionTimerSeconds") val questionTimerSeconds: Int,
    @SerializedName("maxPlayers") val maxPlayers: Int,
    @SerializedName("playerCount") val playerCount: Int,
    @SerializedName("createdAt") val createdAt: String
)

data class JoinRoomRequest(
    @SerializedName("nickname") val nickname: String
)

data class JoinRoomResponse(
    @SerializedName(value = "playerId", alternate = ["id"]) val playerId: Long,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("roomCode") val roomCode: String,
    @SerializedName("isHost") val isHost: Boolean,
    @SerializedName("joinOrder") val joinOrder: Int
)

data class StartGameResponse(
    @SerializedName("roomCode") val roomCode: String,
    @SerializedName("status") val status: String,
    @SerializedName("totalQuestions") val totalQuestions: Int,
    @SerializedName("startedAt") val startedAt: String
)
