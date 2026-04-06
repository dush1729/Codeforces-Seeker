package com.dush1729.cfseeker.data.remote.api

import com.dush1729.cfseeker.data.local.entity.RatedUserEntity
import com.dush1729.cfseeker.data.remote.model.CodeforcesApiResponse
import com.dush1729.cfseeker.data.remote.model.User
import com.dush1729.cfseeker.data.repository.toRatedUserEntity
import io.ktor.client.call.body
import io.ktor.client.statement.HttpStatement

actual suspend fun streamParseRatedUsers(
    statement: HttpStatement,
    onBatch: suspend (List<RatedUserEntity>) -> Unit
) {
    statement.execute { response ->
        val apiResponse: CodeforcesApiResponse<User> = response.body()
        val users = apiResponse.result ?: return@execute

        // Process in batches to avoid large single DB transactions
        users.asSequence()
            .map { it.toRatedUserEntity() }
            .chunked(5000)
            .forEach { batch ->
                onBatch(batch)
            }
    }
}
