package com.dush1729.cfseeker.data.remote.api

import com.dush1729.cfseeker.data.local.entity.RatedUserEntity
import com.dush1729.cfseeker.data.remote.model.User
import com.dush1729.cfseeker.data.repository.toRatedUserEntity
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeToSequence

private val lenientJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

@OptIn(ExperimentalSerializationApi::class)
actual suspend fun streamParseRatedUsers(
    statement: HttpStatement,
    onBatch: suspend (List<RatedUserEntity>) -> Unit
) {
    statement.execute { response ->
        val channel = response.bodyAsChannel()
        val inputStream = channel.toInputStream()

        // The response is {"status":"OK","result":[...]}
        // We need to skip past the "result":[ part to get to the array
        // Read until we find the opening bracket of the result array
        val buffer = StringBuilder()
        var foundArrayStart = false
        while (!foundArrayStart) {
            val byte = inputStream.read()
            if (byte == -1) return@execute
            val char = byte.toChar()
            buffer.append(char)
            if (buffer.endsWith("\"result\":[") || buffer.endsWith("\"result\": [")) {
                foundArrayStart = true
            }
        }

        // Now we're positioned right after the "[" of the result array
        // Read User objects one by one using decodeToSequence
        // We need to wrap the remaining stream to look like a JSON array
        val wrappedStream = java.io.SequenceInputStream(
            "[".byteInputStream(),
            inputStream
        )

        val batch = mutableListOf<RatedUserEntity>()
        val batchSize = 5000

        lenientJson.decodeToSequence<User>(wrappedStream).forEach { user ->
            batch.add(user.toRatedUserEntity())
            if (batch.size >= batchSize) {
                onBatch(batch.toList())
                batch.clear()
            }
        }

        if (batch.isNotEmpty()) {
            onBatch(batch.toList())
        }
    }
}
