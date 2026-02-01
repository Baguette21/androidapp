package com.delacruz.ectrivia.ui.screens.createroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delacruz.ectrivia.data.repository.RoomRepository
import com.delacruz.ectrivia.util.Constants
import com.delacruz.ectrivia.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateRoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRoomUiState())
    val uiState: StateFlow<CreateRoomUiState> = _uiState.asStateFlow()

    fun updateHostNickname(nickname: String) {
        if (nickname.length <= Constants.MAX_NICKNAME_LENGTH) {
            _uiState.update { it.copy(hostNickname = nickname) }
        }
    }

    fun toggleThemeBased() {
        _uiState.update { it.copy(isThemeBased = !it.isThemeBased) }
    }

    fun updateTimer(seconds: Int) {
        _uiState.update { it.copy(timerSeconds = seconds) }
    }

    fun createRoom() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = roomRepository.createRoom(
                categoryId = null,
                isThemeBased = _uiState.value.isThemeBased,
                timerSeconds = _uiState.value.timerSeconds,
                maxPlayers = Constants.MAX_PLAYERS
            )) {
                is NetworkResult.Success -> {
                    val roomCode = result.data.roomCode
                    // Join the room as host
                    when (val joinResult = roomRepository.joinRoom(
                        roomCode,
                        _uiState.value.hostNickname
                    )) {
                        is NetworkResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    roomCreated = true,
                                    createdRoomCode = roomCode,
                                    playerId = joinResult.data.playerId
                                )
                            }
                        }
                        is NetworkResult.Error -> {
                            _uiState.update {
                                it.copy(isLoading = false, error = joinResult.message)
                            }
                        }
                        is NetworkResult.Loading -> {}
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CreateRoomUiState(
    val hostNickname: String = "",
    val isThemeBased: Boolean = false,
    val timerSeconds: Int = Constants.DEFAULT_TIMER_SECONDS,
    val isLoading: Boolean = false,
    val roomCreated: Boolean = false,
    val createdRoomCode: String? = null,
    val playerId: Long? = null,
    val error: String? = null
)
