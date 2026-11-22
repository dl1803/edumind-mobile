package com.edumind.app.di

import com.edumind.app.BuildConfig
import com.edumind.app.repository.FakeQuizRepository
import com.edumind.app.repository.QuizRepository
import com.edumind.app.repository.RemoteQuizRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QuizRepositoryModule {

    @Provides
    @Singleton
    fun provideQuizRepository(
        fakeQuizRepository: Provider<FakeQuizRepository>,
        remoteQuizRepository: Provider<RemoteQuizRepository>
    ): QuizRepository {
        return if (BuildConfig.USE_FAKE_QUIZ_API) {
            fakeQuizRepository.get()
        } else {
            remoteQuizRepository.get()
        }
    }
}
