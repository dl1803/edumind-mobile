package com.edumind.app.data.local.db.dao

import androidx.room.*
import com.edumind.app.data.local.db.entity.CourseCache
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseCacheDao {
    @Upsert
    suspend fun upsertCourse(course: CourseCache)

    @Upsert
    suspend fun upsertCourses(courses: List<CourseCache>)

    @Query("SELECT * FROM courses_cache ORDER BY updated_at DESC")
    fun getAllCourses(): Flow<List<CourseCache>>

    @Query("SELECT * FROM courses_cache WHERE course_id = :courseId LIMIT 1")
    suspend fun getCourseById(courseId: String): CourseCache?

    @Query("DELETE FROM courses_cache WHERE course_id = :courseId")
    suspend fun deleteCourseById(courseId: String)

    @Query("DELETE FROM courses_cache")
    suspend fun clearAll()
}
