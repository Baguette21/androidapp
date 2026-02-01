package com.delacruz.ectrivia.util

import retrofit2.Response

suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): NetworkResult<T> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            response.body()?.let {
                NetworkResult.Success(it)
            } ?: NetworkResult.Error("Empty response body")
        } else {
            NetworkResult.Error(response.message(), response.code())
        }
    } catch (e: Exception) {
        NetworkResult.Error(e.message ?: "Unknown error occurred")
    }
}

fun String.isValidRoomCode(): Boolean {
    return this.length == Constants.ROOM_CODE_LENGTH && this.all { it.isLetterOrDigit() }
}

fun String.isValidNickname(): Boolean {
    return this.isNotBlank() && this.length <= Constants.MAX_NICKNAME_LENGTH
}
