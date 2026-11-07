package com.edumind.app.repository

import com.edumind.app.data.model.response.SubtitleSegment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSubtitleRepository @Inject constructor() : SubtitleRepository {

    override suspend fun getSegments(lessonId: String): List<SubtitleSegment> {
        if (lessonId == NO_SUBTITLE_LESSON_ID) return emptyList()
        return MOCK_SEGMENTS
    }

    companion object {
        const val NO_SUBTITLE_LESSON_ID = "no_subtitle_lesson"

        private fun segment(startSec: Long, endSec: Long, text: String) =
            SubtitleSegment(startSec * 1000L, endSec * 1000L, text)

        private val MOCK_SEGMENTS = listOf(
            segment(0, 6, "Chào mừng các bạn đến với bài học về biến trong Python."),
            segment(6, 12, "Biến là tên gọi dùng để lưu trữ một giá trị trong bộ nhớ."),
            segment(12, 19, "Trong Python, bạn không cần khai báo kiểu dữ liệu trước khi dùng biến."),
            segment(19, 26, "Chỉ cần viết tên biến, dấu bằng, rồi đến giá trị muốn gán."),
            segment(26, 33, "Ví dụ: tuoi bằng 20 sẽ tạo ra một biến kiểu số nguyên."),
            segment(33, 40, "Tên biến nên viết thường và nối các từ bằng dấu gạch dưới."),
            segment(40, 47, "Quy tắc này nằm trong chuẩn PEP 8 mà cộng đồng Python tuân theo."),
            segment(47, 54, "Tên biến không được bắt đầu bằng chữ số."),
            segment(54, 61, "Tên biến cũng không được chứa khoảng trắng hay ký tự đặc biệt."),
            segment(61, 68, "Python phân biệt chữ hoa và chữ thường trong tên biến."),
            segment(73, 80, "Bây giờ chúng ta sang phần kiểu dữ liệu cơ bản."),
            segment(80, 87, "Kiểu số nguyên int dùng cho các số không có phần thập phân."),
            segment(87, 94, "Kiểu số thực float dùng cho các số có phần thập phân."),
            segment(94, 101, "Kiểu chuỗi str dùng để lưu trữ văn bản đặt trong dấu nháy."),
            segment(101, 108, "Kiểu logic bool chỉ có hai giá trị là True và False."),
            segment(108, 115, "Bạn có thể dùng hàm type để kiểm tra kiểu của một biến."),
            segment(115, 122, "Python sẽ tự nhận diện kiểu dựa trên giá trị được gán."),
            segment(122, 129, "Khi nhập dữ liệu từ bàn phím, giá trị luôn là kiểu chuỗi."),
            segment(129, 136, "Hãy dùng int hoặc float để ép kiểu trước khi tính toán."),
            segment(136, 144, "Đó là toàn bộ nội dung của bài, hẹn gặp lại ở bài tiếp theo.")
        )
    }
}

