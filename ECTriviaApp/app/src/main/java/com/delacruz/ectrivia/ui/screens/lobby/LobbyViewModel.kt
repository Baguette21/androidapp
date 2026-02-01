package com.delacruz.ectrivia.ui.screens.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delacruz.ectrivia.data.model.Player
import com.delacruz.ectrivia.data.repository.GameRepository
import com.delacruz.ectrivia.data.repository.RoomRepository
import com.delacruz.ectrivia.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LobbyViewModel @Inject constructor(
    private val roomRepository: RoomRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LobbyUiState())
    val uiState: StateFlow<LobbyUiState> = _uiState.asStateFlow()

    fun initialize(roomCode: String, playerId: Long, isHost: Boolean) {
        _uiState.update { 
            it.copy(
                roomCode = roomCode,
                playerId = playerId,
                isHost = isHost
            )
        }
        loadRoom()
        observePlayerUpdates()
        observeGameUpdates()
    }

    private fun loadRoom() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = roomRepository.getRoom(_uiState.value.roomCode)) {
                is NetworkResult.Success -> {
                    val room = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            players = room.players,
                            isThemeBased = room.isThemeBased
                        )
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    private fun observePlayerUpdates() {
        viewModelScope.launch {
            try {
                roomRepository.playerEvents.collect { event ->
                    // Guard against null event or null player in event (though DTO should enforce it)
                    if (event == null || event.player == null) return@collect
                    
                    when (event.eventType) {
                        "PLAYER_JOINED" -> {
                            val newPlayer = Player(
                                id = event.player.id,
                                nickname = event.player.nickname,
                                isHost = event.player.isHost,
                                isProxyHost = event.player.isProxyHost,
                                totalScore = 0,
                                currentStreak = 0,
                                isConnected = true
                            )
                            _uiState.update {
                                if (it.players.any { p -> p.id == newPlayer.id }) {
                                    it
                                } else {
                                    it.copy(players = it.players + newPlayer)
                                }
                            }
                        }
                        "PLAYER_LEFT" -> {
                            // FIX: Ensure player exists before trying to filter
                            _uiState.update {
                                 val updatedList = it.players.filter { p -> p.id != event.player.id }
                                 it.copy(players = updatedList)
                            }
                        }
                        "GAME_STARTING" -> {
                            _uiState.update { it.copy(gameStarted = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                // Log the error or handle it quietly to prevent crash
                _uiState.update { it.copy(error = "Connection update error: ${e.localizedMessage}") }
            }
        }
    }

    private fun observeGameUpdates() {
        viewModelScope.launch {
            try {
                gameRepository.gameEvents.collect { event ->
                    if (event.eventType == "GAME_STARTING") {
                        _uiState.update { it.copy(gameStarted = true) }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Game update error: ${e.localizedMessage}") }
            }
        }
    }

    fun startGame() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val resolvedPlayerId = if (_uiState.value.playerId != 0L) {
                _uiState.value.playerId
            } else {
                _uiState.value.players.firstOrNull { it.isHost }?.id ?: 0L
            }

            if (resolvedPlayerId == 0L) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unable to start game: host not resolved"
                    )
                }
                return@launch
            }
            
            when (val result = roomRepository.startGame(
                _uiState.value.roomCode,
                resolvedPlayerId
            )) {
                is NetworkResult.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, gameStarted = true)
                    }
                }
                is NetworkResult.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun leaveRoom() {
        viewModelScope.launch {
            roomRepository.leaveRoom(_uiState.value.roomCode, _uiState.value.playerId)
            _uiState.update { it.copy(leftRoom = true) }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class LobbyUiState(
    val roomCode: String = "",
    val playerId: Long = 0L,
    val isHost: Boolean = false,
    val isThemeBased: Boolean = false,
    val players: List<Player> = emptyList(),
    val isLoading: Boolean = false,
    val gameStarted: Boolean = false,
    val leftRoom: Boolean = false,
    val error: String? = null
)
