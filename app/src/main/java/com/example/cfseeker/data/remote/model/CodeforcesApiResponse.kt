package com.example.cfseeker.data.remote.model


import com.google.gson.annotations.SerializedName

data class CodeforcesApiResponse<T>(
    @SerializedName("status")
    val status: String,
    @SerializedName("result")
    val result: List<T>?,
    @SerializedName("comment")
    val comment: String?,
)