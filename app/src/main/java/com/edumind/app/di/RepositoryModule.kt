package com.edumind.app.di

import com.edumind.app.repository.AIRepository
import com.edumind.app.repository.AuthRepository
import com.edumind.app.repository.AuthRepositoryImpl
import com.edumind.app.repository.CourseRepository
import com.edumind.app.repository.CourseRepositoryImpl
import com.edumind.app.repository.FakeAIRepository
import com.edumind.app.repository.FakeHomeRepository
import com.edumind.app.repository.FakeProfileRepository
import com.edumind.app.repository.FakeSubtitleRepository
import com.edumind.app.repository.HomeRepository
import com.edumind.app.repository.LessonRepository
import com.edumind.app.repository.LessonRepositoryImpl
import com.edumind.app.repository.NoteRepository
import com.edumind.app.repository.NoteRepositoryImpl
import com.edumind.app.repository.ProfileRepository
import com.edumind.app.repository.ProgressRepository
import com.edumind.app.repository.ProgressRepositoryImpl
import com.edumind.app.repository.SubtitleRepository
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
        courseRepositoryImpl: CourseRepositoryImpl
    ): CourseRepository

    @Binds
    @Singleton
    abstract fun bindProgressRepository(
        progressRepositoryImpl: ProgressRepositoryImpl
    ): ProgressRepository

    @Binds
    @Singleton
    abstract fun bindLessonRepository(
        lessonRepositoryImpl: LessonRepositoryImpl
    ): LessonRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        fakeProfileRepository: FakeProfileRepository
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(
        noteRepositoryImpl: NoteRepositoryImpl
    ): NoteRepository

    @Binds
    @Singleton
    abstract fun bindSubtitleRepository(
        fakeSubtitleRepository: FakeSubtitleRepository
    ): SubtitleRepository

    @Binds
    @Singleton
    abstract fun bindAIRepository(
        fakeAIRepository: FakeAIRepository
    ): AIRepository
}
