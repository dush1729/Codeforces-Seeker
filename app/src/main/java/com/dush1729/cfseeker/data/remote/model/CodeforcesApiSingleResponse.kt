package com.dush1729.cfseeker.data.remote.model

import com.google.gson.annotations.SerializedName

data class CodeforcesApiSingleResponse<T>(
    @SerializedName("status")
    val status: String,
    @SerializedName("result")
    val result: T?,
    @SerializedName("comment")
    val comment: String?,
)
