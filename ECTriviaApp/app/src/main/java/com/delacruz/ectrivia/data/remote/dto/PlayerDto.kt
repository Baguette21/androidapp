package com.delacruz.ectrivia.data.remote.dto

import com.google.gson.annotations.SerializedName

data class PlayerDto(
    @SerializedName("id") val id: Long,
    @SerializedName("nickname") val nickname: String,
    @SerializedName("isHost") val isHost: Boolean,
    @SerializedName("isProxyHost") val isProxyHost: Boolean,
    @SerializedName("totalScore") val totalScore: Int,
    @SerializedName("currentStreak") val currentStreak: Int,
    @SerializedName("isConnected") val isConnected: Boolean
)
