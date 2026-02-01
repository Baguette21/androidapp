package com.delacruz.ectrivia.data.model

data class Player(
    val id: Long,
    val nickname: String,
    val isHost: Boolean,
    val isProxyHost: Boolean,
    val totalScore: Int,
    val currentStreak: Int,
    val isConnected: Boolean
)
