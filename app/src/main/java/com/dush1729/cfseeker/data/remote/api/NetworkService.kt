package com.dush1729.cfseeker.data.remote.api

import com.dush1729.cfseeker.data.remote.model.CodeforcesApiResponse
import com.dush1729.cfseeker.data.remote.model.CodeforcesApiSingleResponse
import com.dush1729.cfseeker.data.remote.model.Contest
import com.dush1729.cfseeker.data.remote.model.ContestStandings
import com.dush1729.cfseeker.data.remote.model.RatingChange
import com.dush1729.cfseeker.data.remote.model.User
import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkService {

    @GET("user.rating")
    suspend fun getRatingChanges(@Query("handle") handle: String): CodeforcesApiResponse<RatingChange>

    @GET("user.info")
    suspend fun getUser(@Query("handles") handle: String): CodeforcesApiResponse<User>

    @GET("contest.list")
    suspend fun getContests(@Query("gym") gym: Boolean = false): CodeforcesApiResponse<Contest>

    @GET("contest.standings")
    suspend fun getContestStandings(
        @Query("contestId") contestId: Int,
        @Query("from") from: Int? = null,
        @Query("count") count: Int? = null,
        @Query("showUnofficial") showUnofficial: Boolean? = null,
        @Query("room") room: Int? = null
    ): CodeforcesApiSingleResponse<ContestStandings>

    @GET("contest.ratingChanges")
    suspend fun getContestRatingChanges(
        @Query("contestId") contestId: Int
    ): CodeforcesApiResponse<RatingChange>
}