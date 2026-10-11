package com.edumind.app.di

import android.content.Context
import androidx.room.Room
import com.edumind.app.data.local.db.AppDatabase
import com.edumind.app.data.local.db.dao.ChatHistoryDao
import com.edumind.app.data.local.db.dao.CourseCacheDao
import com.edumind.app.data.local.db.dao.NoteDao
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
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    fun provideNoteDao(database: AppDatabase): NoteDao {
        return database.noteDao()
    }

    @Provides
    fun provideCourseCacheDao(database: AppDatabase): CourseCacheDao {
        return database.courseCacheDao()
    }

    @Provides
    fun provideChatHistoryDao(database: AppDatabase): ChatHistoryDao {
        return database.chatHistoryDao()
    }
}
