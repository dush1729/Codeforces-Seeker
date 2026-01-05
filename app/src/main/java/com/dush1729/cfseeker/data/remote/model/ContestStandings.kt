package com.dush1729.cfseeker.data.remote.model

import com.google.gson.annotations.SerializedName

data class ContestStandings(
    @SerializedName("contest")
    val contest: Contest,

    @SerializedName("problems")
    val problems: List<Problem>,

    @SerializedName("rows")
    val rows: List<RanklistRow>
)
