package com.delacruz.ectrivia.ui.screens.results

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delacruz.ectrivia.data.model.LeaderboardEntry
import com.delacruz.ectrivia.data.repository.GameRepository
import com.delacruz.ectrivia.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun initialize(roomCode: String) {
        _uiState.update { it.copy(roomCode = roomCode) }
        loadLeaderboard()
        startCountdown()
        observeGameEvents()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            when (val result = gameRepository.getLeaderboard(_uiState.value.roomCode)) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            topPlayers = result.data
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            for (i in 5 downTo 1) {
                _uiState.update { it.copy(countdownSeconds = i) }
                delay(1000)
            }
            _uiState.update { it.copy(nextQuestionReady = true) }
        }
    }

    private fun stopCountdown() {
        _uiState.update { it.copy(countdownSeconds = 0, nextQuestionReady = false) }
    }

    private fun observeGameEvents() {
        viewModelScope.launch {
            gameRepository.gameEvents.collect { event ->
                when (event.eventType) {
                    "QUESTION_END" -> {
                        val payloadMap = event.payload as? Map<*, *>
                        val correctIndex = payloadMap?.get("correctAnswerIndex") as? Number
                        val correctText = when (val value = payloadMap?.get("correctAnswerText")) {
                            is String -> value
                            else -> null
                        }
                        val questionMap = payloadMap?.get("question") as? Map<*, *>
                        val answers = questionMap?.get("answers") as? List<*>
                        val resolvedText = when {
                            !correctText.isNullOrBlank() -> correctText
                            correctIndex != null && !answers.isNullOrEmpty() -> {
                                val answerMap = answers.getOrNull(correctIndex.toInt()) as? Map<*, *>
                                answerMap?.get("answerText") as? String
                                    ?: answerMap?.get("text") as? String
                            }
                            else -> null
                        }
                        _uiState.update { it.copy(correctAnswerText = resolvedText ?: it.correctAnswerText) }
                    }
                    "QUESTION_START" -> {
                        _uiState.update { it.copy(nextQuestionReady = true) }
                    }
                    "GAME_FINISHED" -> {
                        stopCountdown()
                        _uiState.update { it.copy(gameEnded = true) }
                    }
                }
            }
        }
    }
}

data class ResultUiState(
    val roomCode: String = "",
    val correctAnswerText: String = "The correct answer",
    val yourResult: PlayerResult? = null,
    val topPlayers: List<LeaderboardEntry> = emptyList(),
    val countdownSeconds: Int = 5,
    val isLoading: Boolean = true,
    val nextQuestionReady: Boolean = false,
    val gameEnded: Boolean = false
)

data class PlayerResult(
    val isCorrect: Boolean,
    val pointsEarned: Int,
    val newRank: Int
)
