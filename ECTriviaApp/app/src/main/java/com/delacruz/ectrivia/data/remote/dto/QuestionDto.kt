package com.delacruz.ectrivia.data.remote.dto

import com.google.gson.annotations.SerializedName

data class QuestionDto(
    @SerializedName("id") val id: Long,
    @SerializedName("questionText") val questionText: String,
    @SerializedName("answers") val answers: List<AnswerDto?>,
    @SerializedName("questionOrder") val questionOrder: Int,
    @SerializedName("timerSeconds") val timerSeconds: Int?
)

data class AnswerDto(
    @SerializedName(value = "index", alternate = ["answerIndex"]) val index: Int?,
    @SerializedName(value = "text", alternate = ["answerText"]) val text: String?
)

data class AddQuestionsRequest(
    @SerializedName("questions") val questions: List<QuestionInputDto>
)

data class QuestionInputDto(
    @SerializedName("questionText") val questionText: String,
    @SerializedName("answers") val answers: List<String>,
    @SerializedName("correctAnswerIndex") val correctAnswerIndex: Int,
    @SerializedName("timerSeconds") val timerSeconds: Int
)

data class AddQuestionsResponse(
    @SerializedName("addedCount") val addedCount: Int,
    @SerializedName("totalQuestions") val totalQuestions: Int
)

data class SubmitAnswerRequest(
    @SerializedName("playerId") val playerId: Long,
    @SerializedName("questionId") val questionId: Long,
    @SerializedName("selectedAnswerIndex") val selectedAnswerIndex: Int,
    @SerializedName("answerTimeMs") val answerTimeMs: Long
)

data class SubmitAnswerResponse(
    @SerializedName("isCorrect") val isCorrect: Boolean,
    @SerializedName("correctAnswerIndex") val correctAnswerIndex: Int,
    @SerializedName("pointsEarned") val pointsEarned: Int,
    @SerializedName("newTotalScore") val newTotalScore: Int,
    @SerializedName("newStreak") val newStreak: Int,
    @SerializedName("currentRank") val currentRank: Int
)
