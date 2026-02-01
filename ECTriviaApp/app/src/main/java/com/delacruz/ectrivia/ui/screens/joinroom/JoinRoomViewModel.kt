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
class JoinRoomViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinRoomUiState())
    val uiState: StateFlow<JoinRoomUiState> = _uiState.asStateFlow()

    fun updateRoomCode(code: String) {
        _uiState.update { it.copy(roomCode = code, error = null) }
    }

    fun checkRoom() {
        if (_uiState.value.roomCode.length != 6) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = roomRepository.getRoom(_uiState.value.roomCode)) {
                is NetworkResult.Success -> {
                    val room = result.data
                    if (room.status.name == "LOBBY") {
                        _uiState.update { it.copy(isLoading = false, roomFound = true) }
                    } else {
                        _uiState.update { 
                            it.copy(isLoading = false, error = "Game has already started") 
                        }
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

data class JoinRoomUiState(
    val roomCode: String = "",
    val isLoading: Boolean = false,
    val roomFound: Boolean = false,
    val error: String? = null
)
