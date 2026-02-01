package com.delacruz.ectrivia.ui.screens.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delacruz.ectrivia.data.model.Answer
import com.delacruz.ectrivia.data.model.Question
import com.delacruz.ectrivia.data.remote.dto.GameStateDto
import com.delacruz.ectrivia.data.repository.GameRepository
import com.delacruz.ectrivia.util.NetworkResult
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
class GameViewModel @Inject constructor(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var questionStartTime: Long = 0L

    fun initialize(roomCode: String, playerId: Long) {
        _uiState.update { 
            it.copy(roomCode = roomCode, playerId = playerId)
        }
        observeGameEvents()
    }

    private fun observeGameEvents() {
        viewModelScope.launch {
            gameRepository.gameEvents.collect { event ->
                when (event.eventType) {
                    "QUESTION_START" -> {
                        val question = parseQuestion(event) ?: return@collect
                        startQuestion(
                            question = question,
                            questionIndex = event.questionIndex ?: question.questionOrder,
                            totalQuestions = event.totalQuestions ?: _uiState.value.totalQuestions.takeIf { it > 0 } ?: 10
                        )
                    }
                    "QUESTION_END" -> {
                        _uiState.update { it.copy(questionEnded = true) }
                    }
                    "GAME_FINISHED" -> {
                        _uiState.update { it.copy(gameEnded = true) }
                    }
                    "GAME_STARTING" -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                }
            }
        }

        viewModelScope.launch {
            gameRepository.scoreUpdates.collect { scoreUpdate ->
                if (scoreUpdate.playerId == _uiState.value.playerId) {
                    _uiState.update {
                        it.copy(
                            showResult = true,
                            lastAnswerCorrect = scoreUpdate.isCorrect,
                            correctAnswerIndex = scoreUpdate.correctAnswerIndex,
                            lastPointsEarned = scoreUpdate.pointsEarned,
                            totalScore = scoreUpdate.newTotalScore,
                            currentStreak = scoreUpdate.newStreak
                        )
                    }
                }
            }
        }
    }

    private fun startQuestion(question: Question, questionIndex: Int, totalQuestions: Int) {
        timerJob?.cancel()
        questionStartTime = System.currentTimeMillis()
        
        _uiState.update {
            it.copy(
                isLoading = false,
                currentQuestion = question,
                currentQuestionIndex = questionIndex,
                totalQuestions = totalQuestions,
                timerSeconds = question.timerSeconds,
                remainingSeconds = question.timerSeconds,
                selectedAnswerIndex = null,
                hasAnswered = false,
                showResult = false,
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

    private fun parseQuestion(event: GameStateDto): Question? {
        val eventQuestionMap = event.question.asMap()
        val payloadMap = event.payload.asMap()
        val payloadQuestionMap = payloadMap?.get("question").asMap()
        val questionMap = when {
            eventQuestionMap != null -> eventQuestionMap
            payloadQuestionMap != null -> payloadQuestionMap
            payloadMap != null -> payloadMap
            else -> null
        } ?: return null

        val questionId = (questionMap["id"] as? Number)?.toLong()
            ?: event.questionId
            ?: return null
        val text = questionMap["questionText"] as? String
            ?: questionMap["text"] as? String
            ?: questionMap["question"] as? String
            ?: "Question"
        val answers = parseAnswers(
            questionMap["answers"]
                ?: questionMap["answerOptions"]
                ?: questionMap["options"]
        )
        val questionOrder = (questionMap["questionOrder"] as? Number)?.toInt()
            ?: (questionMap["questionIndex"] as? Number)?.toInt()
            ?: event.questionIndex
            ?: 0
        val timerSeconds = (questionMap["timerSeconds"] as? Number)?.toInt()
            ?: event.timerSeconds
            ?: 15

        return Question(
            id = questionId,
            questionText = text,
            answers = answers,
            questionOrder = questionOrder,
            timerSeconds = timerSeconds
        )
    }

    private fun parseAnswers(raw: Any?): List<Answer> {
        val list = raw as? List<*> ?: return emptyList()
        return list.mapIndexedNotNull { index, item ->
            when (item) {
                is Map<*, *> -> {
                    val answerIndex = (item["answerIndex"] as? Number)?.toInt()
                        ?: (item["index"] as? Number)?.toInt()
                        ?: (item["id"] as? Number)?.toInt()
                        ?: index
                    val text = item["answerText"] as? String
                        ?: item["text"] as? String
                        ?: item["answer"] as? String
                        ?: ""
                    val safeText = if (text.isBlank()) "Option ${index + 1}" else text
                    Answer(index = answerIndex, text = safeText)
                }
                is String -> {
                    val safeText = if (item.isBlank()) "Option ${index + 1}" else item
                    Answer(index = index, text = safeText)
                }
                else -> null
            }
        }
    }

    private fun Any?.asMap(): Map<*, *>? = this as? Map<*, *>

    fun submitAnswer(answerIndex: Int) {
        if (_uiState.value.hasAnswered) return
        
        val answerTimeMs = System.currentTimeMillis() - questionStartTime
        _uiState.update { 
            it.copy(
                selectedAnswerIndex = answerIndex,
                hasAnswered = true
            )
        }
        
        viewModelScope.launch {
            val currentQuestion = _uiState.value.currentQuestion ?: return@launch
            
            when (val result = gameRepository.submitAnswer(
                roomCode = _uiState.value.roomCode,
                playerId = _uiState.value.playerId,
                questionId = currentQuestion.id,
                selectedAnswerIndex = answerIndex,
                answerTimeMs = answerTimeMs
            )) {
                is NetworkResult.Success -> {
                    val answerResult = result.data
                    _uiState.update {
                        it.copy(
                            showResult = true,
                            lastAnswerCorrect = answerResult.isCorrect,
                            correctAnswerIndex = answerResult.correctAnswerIndex,
                            lastPointsEarned = answerResult.pointsEarned,
                            totalScore = answerResult.newTotalScore,
                            currentStreak = answerResult.newStreak
                        )
                    }
                }
                is NetworkResult.Error -> {
                    // Handle error - maybe allow retry
                }
                is NetworkResult.Loading -> {}
            }
        }
    }
}

data class GameUiState(
    val roomCode: String = "",
    val playerId: Long = 0L,
    val currentQuestion: Question? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val timerSeconds: Int = 15,
    val remainingSeconds: Int = 15,
    val selectedAnswerIndex: Int? = null,
    val hasAnswered: Boolean = false,
    val showResult: Boolean = false,
    val lastAnswerCorrect: Boolean = false,
    val correctAnswerIndex: Int = -1,
    val lastPointsEarned: Int = 0,
    val totalScore: Int = 0,
    val currentStreak: Int = 0,
    val isLoading: Boolean = true,
    val questionEnded: Boolean = false,
    val gameEnded: Boolean = false,
    val error: String? = null
)
