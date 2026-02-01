package com.delacruz.ectrivia.ui.screens.joinroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
class NicknameViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NicknameUiState())
    val uiState: StateFlow<NicknameUiState> = _uiState.asStateFlow()

    fun setRoomCode(roomCode: String) {
        _uiState.update { it.copy(roomCode = roomCode) }
    }

    fun updateNickname(nickname: String) {
        _uiState.update { it.copy(nickname = nickname, error = null) }
    }

    fun joinRoom() {
        if (_uiState.value.nickname.isBlank()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = roomRepository.joinRoom(
                _uiState.value.roomCode,
                _uiState.value.nickname
            )) {
                is NetworkResult.Success -> {
                    val session = result.data
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            joinedPlayerId = session.playerId,
                            isHost = session.isHost
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

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class NicknameUiState(
    val roomCode: String = "",
    val nickname: String = "",
    val isLoading: Boolean = false,
    val joinedPlayerId: Long? = null,
    val isHost: Boolean = false,
    val error: String? = null
)
