package com.hackathon.hunter.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reported_hackathons")
data class ReportedHackathonEntity(
    @PrimaryKey val hackathonId: Int,
    val reportedAt: Long = System.currentTimeMillis()
)
