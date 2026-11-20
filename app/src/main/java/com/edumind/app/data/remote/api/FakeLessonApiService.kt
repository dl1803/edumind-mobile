package com.edumind.app.data.remote.api

import com.edumind.app.data.model.response.LessonDetailResponseDto
import com.edumind.app.data.model.response.TranscriptSegmentDto
import com.edumind.app.util.Constants
import retrofit2.Response

class FakeLessonApiService : LessonApiService {

    override suspend fun getLessonDetail(lessonId: String): Response<LessonDetailResponseDto> {
        return Response.success(
            LessonDetailResponseDto(
                id = lessonId,
                title = "Bài giảng mẫu",
                courseId = "fake_course",
                hlsStreamUrl = Constants.SAMPLE_HLS_URL,
                duration = 765,
                transcript = listOf(
                    TranscriptSegmentDto(start = 0f, end = 6f, text = "Chào mừng bạn đến với bài giảng mẫu của EduMind."),
                    TranscriptSegmentDto(start = 6f, end = 15f, text = "Hệ thống hỗ trợ phát video định dạng HLS mượt mà và tối ưu."),
                    TranscriptSegmentDto(start = 15f, end = 28f, text = "Bạn có thể theo dõi phụ đề và ghi chú trực tiếp trong lúc học."),
                    TranscriptSegmentDto(start = 28f, end = 45f, text = "Chạm vào bất kỳ dòng nào trong danh sách phụ đề để tua video."),
                    TranscriptSegmentDto(start = 45f, end = 65f, text = "Chức năng tự động cuộn sẽ giữ cho dòng lời thoại luôn ở giữa màn hình."),
                    TranscriptSegmentDto(start = 65f, end = 90f, text = "Phụ đề cũng được lưu vào bộ nhớ đệm để xem khi không có mạng."),
                    TranscriptSegmentDto(start = 90f, end = 120f, text = "Chúc bạn có những trải nghiệm học tập hiệu quả cùng EduMind!")
                )
            )
        )
    }
}

