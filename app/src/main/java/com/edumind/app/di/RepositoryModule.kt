package com.edumind.app.di

import com.edumind.app.repository.AuthRepository
import com.edumind.app.repository.AuthRepositoryImpl
import com.edumind.app.repository.CourseRepository
import com.edumind.app.repository.FakeCourseRepository
import com.edumind.app.repository.FakeHomeRepository
import com.edumind.app.repository.HomeRepository
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
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindHomeRepository(
        fakeHomeRepository: FakeHomeRepository
    ): HomeRepository

    @Binds
    @Singleton
    abstract fun bindCourseRepository(
        fakeCourseRepository: FakeCourseRepository
    ): CourseRepository

}
