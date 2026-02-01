package com.delacruz.ectrivia.ui.screens.createroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delacruz.ectrivia.data.model.Question
import com.delacruz.ectrivia.data.repository.QuestionRepository
import com.delacruz.ectrivia.util.NetworkResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestionListViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionListUiState())
    val uiState: StateFlow<QuestionListUiState> = _uiState.asStateFlow()

    fun setRoomCode(roomCode: String) {
        _uiState.update { it.copy(roomCode = roomCode) }
    }

    fun loadQuestions() {
        val roomCode = _uiState.value.roomCode
        if (roomCode.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = questionRepository.getRoomQuestions(roomCode)) {
                is NetworkResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, questions = result.data) }
                }
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun deleteQuestion(questionId: Long) {
        val roomCode = _uiState.value.roomCode
        if (roomCode.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = questionRepository.deleteQuestion(roomCode, questionId)) {
                is NetworkResult.Success -> loadQuestions()
                is NetworkResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class QuestionListUiState(
    val roomCode: String = "",
    val questions: List<Question> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
