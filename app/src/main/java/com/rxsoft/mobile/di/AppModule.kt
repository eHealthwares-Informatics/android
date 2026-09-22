package com.rxsoft.mobile.di

import android.content.Context
import com.rxsoft.mobile.data.local.ChatStateStore
import com.rxsoft.mobile.util.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager {
        return TokenManager(context)
    }

    @Provides
    @Singleton
    fun provideChatStateStore(@ApplicationContext context: Context): ChatStateStore {
        return ChatStateStore(context)
    }
}
