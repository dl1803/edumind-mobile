package com.edumind.app.repository

import com.edumind.app.data.model.response.LessonDetail

class LessonAccessDeniedException(
    message: String = "Vui lòng mua hoặc đăng ký khóa học để truy cập bài học này."
) : Exception(message)

class VideoProcessingException(
    message: String = "Video bài giảng đang được hệ thống xử lý. Vui lòng quay lại sau."
) : Exception(message)

class LessonNotFoundException(
    message: String = "Bài giảng không tồn tại hoặc đã bị xóa."
) : Exception(message)

interface LessonRepository {
    suspend fun getLessonDetail(lessonId: String): Result<LessonDetail>
}

