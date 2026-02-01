package com.delacruz.ectrivia.ui.screens.createroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delacruz.ectrivia.data.model.Category
import com.delacruz.ectrivia.data.repository.QuestionRepository
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
class ThemeSelectorViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val roomRepository: RoomRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemeSelectorUiState())
    val uiState: StateFlow<ThemeSelectorUiState> = _uiState.asStateFlow()

    private var hostNickname: String = ""
    private var timerSeconds: Int = 15

    fun initialize(nickname: String, timer: Int) {
        this.hostNickname = nickname
        this.timerSeconds = timer
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = questionRepository.getCategories()) {
                is NetworkResult.Success -> {
                    _uiState.update { 
                        it.copy(isLoading = false, categories = result.data)
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

    fun selectCategory(category: Category) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedCategory = category) }

            // Create the room with the selected category
            when (val result = roomRepository.createRoom(
                categoryId = category.id,
                isThemeBased = true,
                timerSeconds = timerSeconds,
                maxPlayers = Constants.MAX_PLAYERS
            )) {
                is NetworkResult.Success -> {
                    val roomCode = result.data.roomCode
                    // Join the room as host
                    when (val joinResult = roomRepository.joinRoom(
                        roomCode,
                        hostNickname
                    )) {
                        is NetworkResult.Success -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    roomCode = roomCode,
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

data class ThemeSelectorUiState(
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val isLoading: Boolean = false,
    val roomCode: String? = null,
    val playerId: Long? = null,
    val error: String? = null
)
