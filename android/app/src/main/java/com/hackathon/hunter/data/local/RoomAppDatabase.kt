package com.hackathon.hunter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hackathon.hunter.data.local.dao.HackathonDao
import com.hackathon.hunter.data.local.entity.HackathonEntity
import com.hackathon.hunter.data.local.entity.ReportedHackathonEntity

@Database(
    entities = [HackathonEntity::class, ReportedHackathonEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RoomAppDatabase : RoomDatabase() {
    abstract val hackathonDao: HackathonDao
}
