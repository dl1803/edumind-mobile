package com.edumind.app.data.local.db.dao

import androidx.room.*
import com.edumind.app.data.local.db.entity.ChatHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<ChatHistoryEntity>)

    @Query("SELECT * FROM chat_history WHERE lesson_id = :lessonId ORDER BY created_at ASC")
    fun getHistoryByLesson(lessonId: String): Flow<List<ChatHistoryEntity>>

    @Query("DELETE FROM chat_history WHERE lesson_id = :lessonId")
    suspend fun clearHistoryByLesson(lessonId: String)

    @Query("DELETE FROM chat_history")
    suspend fun clearAll()
}
