package com.delacruz.ectrivia.di

import com.delacruz.ectrivia.data.remote.api.TriviaApiService
import com.delacruz.ectrivia.data.remote.websocket.StompService
import com.delacruz.ectrivia.data.repository.GameRepository
import com.delacruz.ectrivia.data.repository.QuestionRepository
import com.delacruz.ectrivia.data.repository.RoomRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideRoomRepository(
        apiService: TriviaApiService,
        stompService: StompService
    ): RoomRepository {
        return RoomRepository(apiService, stompService)
    }

    @Provides
    @Singleton
    fun provideGameRepository(
        apiService: TriviaApiService,
        stompService: StompService
    ): GameRepository {
        return GameRepository(apiService, stompService)
    }

    @Provides
    @Singleton
    fun provideQuestionRepository(
        apiService: TriviaApiService
    ): QuestionRepository {
        return QuestionRepository(apiService)
    }
}
