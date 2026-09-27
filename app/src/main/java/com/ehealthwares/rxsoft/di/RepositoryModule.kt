package com.rxsoft.mobile.di

import com.rxsoft.mobile.data.repository.*
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule
