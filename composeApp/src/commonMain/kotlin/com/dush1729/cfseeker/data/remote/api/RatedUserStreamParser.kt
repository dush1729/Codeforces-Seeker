package com.dush1729.cfseeker.data.remote.api

import com.dush1729.cfseeker.data.local.entity.RatedUserEntity
import io.ktor.client.statement.HttpStatement

expect suspend fun streamParseRatedUsers(
    statement: HttpStatement,
    onBatch: suspend (List<RatedUserEntity>) -> Unit
)
