package com.delacruz.ectrivia.data.repository

import com.delacruz.ectrivia.data.model.*
import com.delacruz.ectrivia.data.remote.api.TriviaApiService
import com.delacruz.ectrivia.data.remote.dto.*
import com.delacruz.ectrivia.util.NetworkResult
import com.delacruz.ectrivia.util.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionRepository @Inject constructor(
    private val apiService: TriviaApiService
) {

    suspend fun getCategories(): NetworkResult<List<Category>> {
        return when (val result = safeApiCall { apiService.getCategories() }) {
            is NetworkResult.Success -> {
                NetworkResult.Success(result.data.map { it.toDomain() })
            }
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    suspend fun getCategoryQuestions(categoryId: Long, limit: Int = 10): NetworkResult<List<Question>> {
        return when (val result = safeApiCall { apiService.getCategoryQuestions(categoryId, limit) }) {
            is NetworkResult.Success -> {
                NetworkResult.Success(result.data.map { it.toDomain() })
            }
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    suspend fun addQuestions(
        roomCode: String,
        questions: List<QuestionInput>
    ): NetworkResult<AddQuestionsResponse> {
        return safeApiCall {
            apiService.addQuestions(
                roomCode,
                AddQuestionsRequest(questions.map { it.toDto() })
            )
        }
    }

    suspend fun getRoomQuestions(roomCode: String): NetworkResult<List<Question>> {
        return when (val result = safeApiCall { apiService.getRoomQuestions(roomCode) }) {
            is NetworkResult.Success -> NetworkResult.Success(result.data.map { it.toDomain() })
            is NetworkResult.Error -> result
            is NetworkResult.Loading -> result
        }
    }

    suspend fun updateQuestion(
        roomCode: String,
        questionId: Long,
        question: QuestionInput
    ): NetworkResult<Unit> {
        return safeApiCall {
            apiService.updateQuestion(roomCode, questionId, question.toDto())
        }
    }

    suspend fun deleteQuestion(roomCode: String, questionId: Long): NetworkResult<Unit> {
        return safeApiCall {
            apiService.deleteQuestion(roomCode, questionId)
        }
    }

    private fun CategoryDto.toDomain(): Category = Category(
        id = id,
        name = name,
        description = description,
        questionCount = questionCount ?: 0
    )

    private fun QuestionDto.toDomain(): Question = Question(
        id = id,
        questionText = questionText,
        answers = answers.mapIndexedNotNull { index, answer ->
            val safeAnswer = answer ?: return@mapIndexedNotNull null
            val text = safeAnswer.text?.ifBlank { "Option ${index + 1}" } ?: "Option ${index + 1}"
            Answer(
                index = safeAnswer.index ?: index,
                text = text
            )
        },
        questionOrder = questionOrder,
        timerSeconds = timerSeconds ?: 15
    )

    private fun QuestionInput.toDto(): QuestionInputDto = QuestionInputDto(
        questionText = questionText,
        answers = answers,
        correctAnswerIndex = correctAnswerIndex,
        timerSeconds = timerSeconds
    )
}

data class QuestionInput(
    val questionText: String,
    val answers: List<String>,
    val correctAnswerIndex: Int,
    val timerSeconds: Int
)
