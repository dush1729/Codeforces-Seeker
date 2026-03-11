package com.dush1729.cfseeker.data.remote.api

import com.dush1729.cfseeker.data.remote.model.CodeforcesApiResponse
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import kotlinx.serialization.json.Json

private val lenientJson = Json { ignoreUnknownKeys = true }

suspend fun <T> safeApiCall(apiCall: suspend () -> T): T {
    return try {
        apiCall()
    } catch (e: ClientRequestException) {
        val errorBody = e.response.body<String>()

        val errorMessage = try {
            val errorResponse = lenientJson.decodeFromString<CodeforcesApiResponse<Unit>>(errorBody)
            errorResponse.comment ?: "API error: ${e.response.status}"
        } catch (_: Exception) {
            "API error: ${e.response.status}"
        }

        throw Exception(errorMessage)
    } catch (e: ServerResponseException) {
        throw Exception("Server error: ${e.response.status}")
    }
}
