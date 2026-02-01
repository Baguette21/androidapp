package com.delacruz.ectrivia.data.remote.api

import com.delacruz.ectrivia.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface TriviaApiService {

    // Room endpoints
    @POST("api/rooms")
    suspend fun createRoom(@Body request: CreateRoomRequest): Response<CreateRoomResponse>

    @GET("api/rooms/{roomCode}")
    suspend fun getRoom(@Path("roomCode") roomCode: String): Response<RoomDto>

    @POST("api/rooms/{roomCode}/join")
    suspend fun joinRoom(
        @Path("roomCode") roomCode: String,
        @Body request: JoinRoomRequest
    ): Response<JoinRoomResponse>

    @DELETE("api/rooms/{roomCode}/leave")
    suspend fun leaveRoom(
        @Path("roomCode") roomCode: String,
        @Query("playerId") playerId: Long
    ): Response<Unit>

    @POST("api/rooms/{roomCode}/start")
    suspend fun startGame(
        @Path("roomCode") roomCode: String,
        @Query("playerId") playerId: Long
    ): Response<StartGameResponse>

    // Category endpoints
    @GET("api/categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @GET("api/categories/{categoryId}/questions")
    suspend fun getCategoryQuestions(
        @Path("categoryId") categoryId: Long,
        @Query("limit") limit: Int = 10
    ): Response<List<QuestionDto>>

    // Question endpoints
    @POST("api/rooms/{roomCode}/questions")
    suspend fun addQuestions(
        @Path("roomCode") roomCode: String,
        @Body request: AddQuestionsRequest
    ): Response<AddQuestionsResponse>

    @GET("api/rooms/{roomCode}/questions")
    suspend fun getRoomQuestions(@Path("roomCode") roomCode: String): Response<List<QuestionDto>>

    @PUT("api/rooms/{roomCode}/questions/{questionId}")
    suspend fun updateQuestion(
        @Path("roomCode") roomCode: String,
        @Path("questionId") questionId: Long,
        @Body request: QuestionInputDto
    ): Response<Unit>

    @DELETE("api/rooms/{roomCode}/questions/{questionId}")
    suspend fun deleteQuestion(
        @Path("roomCode") roomCode: String,
        @Path("questionId") questionId: Long
    ): Response<Unit>

    // Game play endpoints
    @POST("api/rooms/{roomCode}/answer")
    suspend fun submitAnswer(
        @Path("roomCode") roomCode: String,
        @Body request: SubmitAnswerRequest
    ): Response<SubmitAnswerResponse>

    @GET("api/rooms/{roomCode}/leaderboard")
    suspend fun getLeaderboard(@Path("roomCode") roomCode: String): Response<LeaderboardDto>

    @GET("api/rooms/{roomCode}/results")
    suspend fun getGameResults(@Path("roomCode") roomCode: String): Response<GameResultsDto>
}
