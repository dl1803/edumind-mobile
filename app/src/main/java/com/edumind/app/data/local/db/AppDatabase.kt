package com.edumind.app.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.edumind.app.data.local.db.dao.ChatHistoryDao
import com.edumind.app.data.local.db.dao.CourseCacheDao
import com.edumind.app.data.local.db.dao.NoteDao
import com.edumind.app.data.local.db.dao.PlaybackPositionDao
import com.edumind.app.data.local.db.entity.ChatHistoryEntity
import com.edumind.app.data.local.db.entity.CourseCache
import com.edumind.app.data.local.db.entity.NoteEntity
import com.edumind.app.data.local.db.entity.PlaybackPositionEntity

@Database(
    entities = [
        NoteEntity::class,
        CourseCache::class,
        ChatHistoryEntity::class,
        PlaybackPositionEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun courseCacheDao(): CourseCacheDao
    abstract fun chatHistoryDao(): ChatHistoryDao
    abstract fun playbackPositionDao(): PlaybackPositionDao

    companion object {
        const val DATABASE_NAME = "edumind.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `playback_positions` (
                        `lessonId` TEXT NOT NULL,
                        `positionMs` INTEGER NOT NULL,
                        `durationMs` INTEGER NOT NULL,
                        `completed` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        `synced` INTEGER NOT NULL,
                        PRIMARY KEY(`lessonId`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `server_id` TEXT")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `is_deleted` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE `notes` SET `server_id` = `id` WHERE `is_synced` = 1")
            }
        }
    }
}
