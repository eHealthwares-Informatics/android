package com.ehealthwares.rxsoft.di

import com.ehealthwares.rxsoft.data.repository.*
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule
