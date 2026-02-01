package com.delacruz.ectrivia.data.repository

import com.delacruz.ectrivia.data.model.*
import com.delacruz.ectrivia.data.remote.api.TriviaApiService
import com.delacruz.ectrivia.data.remote.dto.*
import com.delacruz.ectrivia.data.remote.websocket.StompService
import com.delacruz.ectrivia.util.NetworkResult
import com.delacruz.ectrivia.util.safeApiCall
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomRepository @Inject constructor(
    private val apiService: TriviaApiService,
    private val stompService: StompService
) {
    val playerEvents: SharedFlow<PlayerEventDto> = stompService.playerEvents

    suspend fun createRoom(
        categoryId: Long? = null,
        isThemeBased: Boolean = false,
        timerSeconds: Int = 15,
        maxPlayers: Int = 100
    ): NetworkResult<CreateRoomResponse> {
        return safeApiCall {
            apiService.createRoom(
                CreateRoomRequest(
                    categoryId = categoryId,
                    isThemeBased = isThemeBased,
                    questionTimerSeconds = timerSeconds,
                    maxPlayers = maxPlayers
                )
            )
        }
    }

    suspend fun getRoom(roomCode: String): NetworkResult<Room> {
        return when (val result = safeApiCall { apiService.getRoom(roomCode) }) {
            is NetworkResult.Success -> NetworkResult.Success(result.data.toDomain())
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    suspend fun joinRoom(roomCode: String, nickname: String): NetworkResult<PlayerSession> {
        return when (val result = safeApiCall { 
            apiService.joinRoom(roomCode, JoinRoomRequest(nickname)) 
        }) {
            is NetworkResult.Success -> {
                val response = result.data
                stompService.connect(roomCode)
                NetworkResult.Success(
                    PlayerSession(
                        playerId = response.playerId,
                        nickname = response.nickname,
                        roomCode = response.roomCode,
                        isHost = response.isHost
                    )
                )
            }
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    suspend fun leaveRoom(roomCode: String, playerId: Long): NetworkResult<Unit> {
        stompService.disconnect()
        return safeApiCall { apiService.leaveRoom(roomCode, playerId) }
    }

    suspend fun startGame(roomCode: String, playerId: Long): NetworkResult<StartGameResponse> {
        return safeApiCall { apiService.startGame(roomCode, playerId) }
    }

    fun connectWebSocket(roomCode: String) {
        stompService.connect(roomCode)
    }

    fun disconnectWebSocket() {
        stompService.disconnect()
    }

    private fun RoomDto.toDomain(): Room = Room(
        id = id,
        roomCode = roomCode,
        status = RoomStatus.valueOf(status),
        category = category?.toDomain(),
        isThemeBased = isThemeBased,
        questionTimerSeconds = questionTimerSeconds,
        maxPlayers = maxPlayers,
        currentQuestionIndex = currentQuestionIndex,
        totalQuestions = totalQuestions,
        players = players?.map { it.toDomain() } ?: emptyList(),
        createdAt = createdAt
    )

    private fun CategoryDto.toDomain(): Category = Category(
        id = id,
        name = name,
        description = description,
        questionCount = questionCount ?: 0
    )

    private fun PlayerDto.toDomain(): Player = Player(
        id = id,
        nickname = nickname,
        isHost = isHost,
        isProxyHost = isProxyHost,
        totalScore = totalScore,
        currentStreak = currentStreak,
        isConnected = isConnected
    )
}
