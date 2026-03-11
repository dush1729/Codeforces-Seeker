package com.dush1729.cfseeker.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "contest_standing_row",
    primaryKeys = ["contestId", "memberHandles"],
    indices = [Index(value = ["contestId", "rank"])]
)
data class ContestStandingRowEntity(
    val contestId: Int,
    val rank: Int,
    val points: Double,
    val penalty: Int,
    val successfulHackCount: Int,
    val unsuccessfulHackCount: Int,
    val lastSubmissionTimeSeconds: Long?,

    // Party information
    val participantType: String,
    val teamId: Int?,
    val teamName: String?,
    val ghost: Boolean,
    val room: Int?,
    val memberHandles: String, // Comma-separated handles

    // Problem results stored as JSON
    val problemResults: String
)
