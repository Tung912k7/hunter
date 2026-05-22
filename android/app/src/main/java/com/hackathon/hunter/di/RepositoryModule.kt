package com.hackathon.hunter.di

import com.hackathon.hunter.data.repository.HackathonRepository
import com.hackathon.hunter.data.repository.HackathonRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindHackathonRepository(
        hackathonRepositoryImpl: HackathonRepositoryImpl
    ): HackathonRepository
}
