package com.dush1729.cfseeker.data.remote.api

import com.dush1729.cfseeker.data.remote.model.CodeforcesApiResponse
import com.dush1729.cfseeker.data.remote.model.CodeforcesApiSingleResponse
import com.dush1729.cfseeker.data.remote.model.Contest
import com.dush1729.cfseeker.data.remote.model.ContestStandings
import com.dush1729.cfseeker.data.remote.model.RatingChange
import com.dush1729.cfseeker.data.remote.model.User
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

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
}
