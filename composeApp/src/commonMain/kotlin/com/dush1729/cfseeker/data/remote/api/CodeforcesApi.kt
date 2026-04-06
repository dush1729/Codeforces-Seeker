package com.dush1729.cfseeker.data.remote.api

import com.dush1729.cfseeker.data.remote.model.CodeforcesApiResponse
import com.dush1729.cfseeker.data.remote.model.CodeforcesApiSingleResponse
import com.dush1729.cfseeker.data.remote.model.Contest
import com.dush1729.cfseeker.data.remote.model.ContestStandings
import com.dush1729.cfseeker.data.remote.model.ProblemsetProblems
import com.dush1729.cfseeker.data.remote.model.RatingChange
import com.dush1729.cfseeker.data.remote.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.HttpStatement

class CodeforcesApi(private val client: HttpClient) {

    companion object {
        const val BASE_URL = "https://codeforces.com/api/"
    }

    suspend fun getRatingChanges(handle: String): CodeforcesApiResponse<RatingChange> {
        return client.get("${BASE_URL}user.rating") {
            parameter("handle", handle)
        }.body()
    }

    suspend fun getUser(handles: String): CodeforcesApiResponse<User> {
        return client.get("${BASE_URL}user.info") {
            parameter("handles", handles)
        }.body()
    }

    suspend fun getContests(gym: Boolean = false): CodeforcesApiResponse<Contest> {
        return client.get("${BASE_URL}contest.list") {
            parameter("gym", gym)
        }.body()
    }

    suspend fun getContestStandings(
        contestId: Int,
        from: Int? = null,
        count: Int? = null,
        showUnofficial: Boolean? = null,
        room: Int? = null
    ): CodeforcesApiSingleResponse<ContestStandings> {
        return client.get("${BASE_URL}contest.standings") {
            parameter("contestId", contestId)
            from?.let { parameter("from", it) }
            count?.let { parameter("count", it) }
            showUnofficial?.let { parameter("showUnofficial", it) }
            room?.let { parameter("room", it) }
        }.body()
    }

    suspend fun getContestRatingChanges(contestId: Int): CodeforcesApiResponse<RatingChange> {
        return client.get("${BASE_URL}contest.ratingChanges") {
            parameter("contestId", contestId)
        }.body()
    }

    suspend fun getRatedList(activeOnly: Boolean? = null): CodeforcesApiResponse<User> {
        return client.get("${BASE_URL}user.ratedList") {
            activeOnly?.let { parameter("activeOnly", it) }
        }.body()
    }

    suspend fun getRatedListStreaming(activeOnly: Boolean? = null): HttpStatement {
        return client.prepareGet("${BASE_URL}user.ratedList") {
            activeOnly?.let { parameter("activeOnly", it) }
        }
    }

    suspend fun getProblemsetProblems(tags: String? = null): CodeforcesApiSingleResponse<ProblemsetProblems> {
        return client.get("${BASE_URL}problemset.problems") {
            tags?.let { parameter("tags", it) }
        }.body()
    }
}
