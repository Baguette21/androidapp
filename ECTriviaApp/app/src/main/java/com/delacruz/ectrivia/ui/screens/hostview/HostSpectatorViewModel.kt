package com.delacruz.ectrivia.ui.screens.hostview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delacruz.ectrivia.data.model.LeaderboardEntry
import com.delacruz.ectrivia.data.repository.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HostSpectatorViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HostSpectatorUiState())
    val uiState: StateFlow<HostSpectatorUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun initialize(roomCode: String) {
        _uiState.update { it.copy(roomCode = roomCode, isLoading = false) }
        observeGameEvents()
        observeLeaderboard()
    }

    private fun observeGameEvents() {
        viewModelScope.launch {
            gameRepository.gameEvents.collect { event ->
                when (event.eventType) {
                    "QUESTION_START" -> {
                        startQuestion(
                            questionText = "Question from server", // Would parse from payload
                            questionIndex = event.questionIndex ?: 0,
                            totalQuestions = event.totalQuestions ?: 10,
                            timerSeconds = event.timerSeconds ?: 15
                        )
                    }
                    "QUESTION_END" -> {
                        _uiState.update { it.copy(questionEnded = true) }
                    }
                    "GAME_FINISHED" -> {
                        _uiState.update { it.copy(gameEnded = true) }
                    }
                }
            }
        }

        viewModelScope.launch {
            gameRepository.scoreUpdates.collect { scoreUpdate ->
                _uiState.update {
                    it.copy(
                        answeredCount = it.answeredCount + 1,
                        correctCount = if (scoreUpdate.isCorrect) it.correctCount + 1 else it.correctCount
                    )
                }
            }
        }
    }

    private fun observeLeaderboard() {
        viewModelScope.launch {
            gameRepository.leaderboardUpdates.collect { entries ->
                val leaderboard = entries.map { dto ->
                    LeaderboardEntry(
                        rank = dto.rank,
                        playerId = dto.playerId,
                        nickname = dto.nickname,
                        totalScore = dto.totalScore,
                        currentStreak = dto.currentStreak
                    )
                }
                _uiState.update { it.copy(leaderboard = leaderboard) }
            }
        }
    }

    private fun startQuestion(
        questionText: String,
        questionIndex: Int,
        totalQuestions: Int,
        timerSeconds: Int
    ) {
        timerJob?.cancel()
        
        _uiState.update {
            it.copy(
                currentQuestionText = questionText,
                currentQuestionIndex = questionIndex,
                totalQuestions = totalQuestions,
                timerSeconds = timerSeconds,
                remainingSeconds = timerSeconds,
                answeredCount = 0,
                correctCount = 0,
                questionEnded = false
            )
        }
        
        startTimer()
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000)
                _uiState.update { 
                    it.copy(remainingSeconds = it.remainingSeconds - 1)
                }
            }
        }
    }
}

data class HostSpectatorUiState(
    val roomCode: String = "",
    val currentQuestionText: String = "",
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val timerSeconds: Int = 15,
    val remainingSeconds: Int = 15,
    val totalPlayers: Int = 0,
    val answeredCount: Int = 0,
    val correctCount: Int = 0,
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = true,
    val questionEnded: Boolean = false,
    val gameEnded: Boolean = false
)
