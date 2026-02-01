package com.delacruz.ectrivia.data.repository

import com.delacruz.ectrivia.data.model.*
import com.delacruz.ectrivia.data.remote.api.TriviaApiService
import com.delacruz.ectrivia.data.remote.dto.*
import com.delacruz.ectrivia.data.remote.websocket.StompService
import com.delacruz.ectrivia.util.NetworkResult
import com.delacruz.ectrivia.util.safeApiCall
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val apiService: TriviaApiService,
    private val stompService: StompService
) {
    val gameEvents: SharedFlow<GameStateDto> = stompService.gameEvents
    val leaderboardUpdates: SharedFlow<List<LeaderboardEntryDto>> = stompService.leaderboardUpdates
    val scoreUpdates: SharedFlow<ScoreUpdateDto> = stompService.scoreUpdates
    val connectionState: StateFlow<com.delacruz.ectrivia.data.remote.websocket.ConnectionState> = stompService.connectionState

    suspend fun submitAnswer(
        roomCode: String,
        playerId: Long,
        questionId: Long,
        selectedAnswerIndex: Int,
        answerTimeMs: Long
    ): NetworkResult<AnswerResult> {
        return when (val result = safeApiCall {
            apiService.submitAnswer(
                roomCode,
                SubmitAnswerRequest(
                    playerId = playerId,
                    questionId = questionId,
                    selectedAnswerIndex = selectedAnswerIndex,
                    answerTimeMs = answerTimeMs
                )
            )
        }) {
            is NetworkResult.Success -> {
                val response = result.data
                NetworkResult.Success(
                    AnswerResult(
                        isCorrect = response.isCorrect,
                        correctAnswerIndex = response.correctAnswerIndex,
                        pointsEarned = response.pointsEarned,
                        newTotalScore = response.newTotalScore,
                        newStreak = response.newStreak,
                        currentRank = response.currentRank
                    )
                )
            }
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    suspend fun getLeaderboard(roomCode: String): NetworkResult<List<LeaderboardEntry>> {
        return when (val result = safeApiCall { apiService.getLeaderboard(roomCode) }) {
            is NetworkResult.Success -> {
                NetworkResult.Success(result.data.leaderboard.map { it.toDomain() })
            }
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    suspend fun getGameResults(roomCode: String): NetworkResult<GameResults> {
        return when (val result = safeApiCall { apiService.getGameResults(roomCode) }) {
            is NetworkResult.Success -> {
                val dto = result.data
                NetworkResult.Success(
                    GameResults(
                        roomCode = dto.roomCode,
                        totalQuestions = dto.totalQuestions,
                        duration = dto.duration,
                        podium = dto.podium.map { PodiumEntry(it.rank, it.nickname, it.totalScore) },
                        allPlayers = dto.allPlayers.map { it.toDomain() },
                        finishedAt = dto.finishedAt ?: ""
                    )
                )
            }
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    fun sendPing(roomCode: String) {
        stompService.sendPing(roomCode)
    }

    private fun LeaderboardEntryDto.toDomain(): LeaderboardEntry = LeaderboardEntry(
        rank = rank,
        playerId = playerId,
        nickname = nickname,
        totalScore = totalScore,
        currentStreak = currentStreak
    )
}
