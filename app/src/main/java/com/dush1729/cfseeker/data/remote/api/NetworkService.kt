package com.dush1729.cfseeker.data.remote.api

import com.dush1729.cfseeker.data.remote.model.CodeforcesApiResponse
import com.dush1729.cfseeker.data.remote.model.RatingChange
import com.dush1729.cfseeker.data.remote.model.User
import retrofit2.http.GET
import retrofit2.http.Query

interface NetworkService {

    @GET("user.rating")
    suspend fun getRatingChanges(@Query("handle") handle: String): CodeforcesApiResponse<RatingChange>

    @GET("user.info")
    suspend fun getUser(@Query("handles") handle: String): CodeforcesApiResponse<User>
}