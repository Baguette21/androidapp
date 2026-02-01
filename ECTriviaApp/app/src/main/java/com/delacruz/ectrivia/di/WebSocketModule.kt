package com.delacruz.ectrivia.di

import com.delacruz.ectrivia.data.remote.websocket.StompService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WebSocketModule {

    @Provides
    @Singleton
    fun provideStompService(okHttpClient: OkHttpClient): StompService {
        return StompService(okHttpClient)
    }
}
