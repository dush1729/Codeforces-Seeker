package com.dush1729.cfseeker.data.remote.api

import com.dush1729.cfseeker.data.remote.model.CodeforcesApiResponse
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import retrofit2.HttpException

suspend fun <T> safeApiCall(apiCall: suspend () -> T): T {
    return try {
        apiCall()
    } catch (e: HttpException) {
        val errorBody = e.response()?.errorBody()?.string()

        val errorMessage = if (errorBody != null) {
            try {
                val errorResponse = Gson().fromJson(errorBody, CodeforcesApiResponse::class.java)
                errorResponse.comment ?: "API error: ${e.message}"
            } catch (parseException: JsonSyntaxException) {
                "API error: ${e.code()} ${e.message()}"
            }
        } else {
            "Network error: ${e.message()}"
        }

        throw Exception(errorMessage)
    }
}
