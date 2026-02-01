package com.delacruz.ectrivia.ui.screens.createroom

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delacruz.ectrivia.data.repository.QuestionInput
import com.delacruz.ectrivia.data.repository.QuestionRepository
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
class QuestionEditorViewModel @Inject constructor(
    private val questionRepository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionEditorUiState())
    val uiState: StateFlow<QuestionEditorUiState> = _uiState.asStateFlow()

    fun setRoomCode(roomCode: String) {
        _uiState.update { it.copy(roomCode = roomCode) }
    }

    fun updateQuestionText(text: String) {
        _uiState.update { 
            it.copy(questionText = text).validate()
        }
    }

    fun updateAnswer(index: Int, text: String) {
        val newAnswers = _uiState.value.answers.toMutableList()
        newAnswers[index] = text
        _uiState.update { 
            it.copy(answers = newAnswers).validate()
        }
    }

    fun setCorrectAnswer(index: Int) {
        _uiState.update { 
            it.copy(correctAnswerIndex = index).validate()
        }
    }

    fun addQuestion() {
        if (!_uiState.value.isValid) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val question = QuestionInput(
                questionText = _uiState.value.questionText,
                answers = _uiState.value.answers,
                correctAnswerIndex = _uiState.value.correctAnswerIndex,
                timerSeconds = Constants.DEFAULT_TIMER_SECONDS
            )
            
            when (val result = questionRepository.addQuestions(
                _uiState.value.roomCode,
                listOf(question)
            )) {
                is NetworkResult.Success -> {
                    _uiState.update { 
                        it.copy(isLoading = false, questionAdded = true)
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

    private fun QuestionEditorUiState.validate(): QuestionEditorUiState {
        val isValid = questionText.isNotBlank() &&
                answers.all { it.isNotBlank() } &&
                correctAnswerIndex in 0..3
        return copy(isValid = isValid)
    }
}

data class QuestionEditorUiState(
    val roomCode: String = "",
    val questionText: String = "",
    val answers: List<String> = listOf("", "", "", ""),
    val correctAnswerIndex: Int = 0,
    val isValid: Boolean = false,
    val isLoading: Boolean = false,
    val questionAdded: Boolean = false,
    val error: String? = null
)
