package com.delacruz.ectrivia.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delacruz.ectrivia.data.model.LeaderboardEntry
import com.delacruz.ectrivia.data.model.PodiumEntry
import com.delacruz.ectrivia.data.repository.GameRepository
import com.delacruz.ectrivia.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    fun loadResults(roomCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = gameRepository.getGameResults(roomCode)) {
                is NetworkResult.Success -> {
                    val gameResults = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            podium = gameResults.podium,
                            allPlayers = gameResults.allPlayers,
                            totalQuestions = gameResults.totalQuestions,
                            gameDuration = gameResults.duration
                        )
                    }
                }
                is NetworkResult.Error -> {
                    // Fallback to leaderboard
                    when (val leaderboardResult = gameRepository.getLeaderboard(roomCode)) {
                        is NetworkResult.Success -> {
                            val players = leaderboardResult.data
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    podium = players.take(3).map { p ->
                                        PodiumEntry(p.rank, p.nickname, p.totalScore)
                                    },
                                    allPlayers = players
                                )
                            }
                        }
                        is NetworkResult.Error -> {
                            _uiState.update { it.copy(isLoading = false) }
                        }
                        is NetworkResult.Loading -> {}
                    }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }
}

data class LeaderboardUiState(
    val isLoading: Boolean = true,
    val podium: List<PodiumEntry> = emptyList(),
    val allPlayers: List<LeaderboardEntry> = emptyList(),
    val currentPlayerId: Long = 0L,
    val totalQuestions: Int = 0,
    val gameDuration: Int = 0
)
