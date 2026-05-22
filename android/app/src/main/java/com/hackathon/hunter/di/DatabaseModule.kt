package com.hackathon.hunter.di

import android.content.Context
import androidx.room.Room
import com.hackathon.hunter.data.local.RoomAppDatabase
import com.hackathon.hunter.data.local.dao.HackathonDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RoomAppDatabase {
        return Room.databaseBuilder(
            context,
            RoomAppDatabase::class.java,
            "hackathon_hunter_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideHackathonDao(database: RoomAppDatabase): HackathonDao {
        return database.hackathonDao
    }
}
