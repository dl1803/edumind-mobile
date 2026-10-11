package com.edumind.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.edumind.app.data.local.db.dao.ChatHistoryDao
import com.edumind.app.data.local.db.dao.CourseCacheDao
import com.edumind.app.data.local.db.dao.NoteDao
import com.edumind.app.data.local.db.entity.ChatHistoryEntity
import com.edumind.app.data.local.db.entity.CourseCache
import com.edumind.app.data.local.db.entity.NoteEntity

@Database(
    entities = [
        NoteEntity::class,
        CourseCache::class,
        ChatHistoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun courseCacheDao(): CourseCacheDao
    abstract fun chatHistoryDao(): ChatHistoryDao

    companion object {
        const val DATABASE_NAME = "edumind.db"
    }
}
