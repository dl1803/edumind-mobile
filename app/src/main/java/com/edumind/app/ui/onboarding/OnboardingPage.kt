package com.edumind.app.ui.onboarding

/**
 * Model chứa dữ liệu nội dung của từng Slide Onboarding.
 */
data class OnboardingPage(
    val title: String,
    val description: String
)

/**
 * Danh sách các trang Onboarding Walkthrough.
 */
val onboardingPages = listOf(
    OnboardingPage(
        title = "Xem bài giảng mọi lúc",
        description = "Học trực tuyến linh hoạt với video chất lượng cao, chia chương bài giảng và phụ đề đồng bộ."
    ),
    OnboardingPage(
        title = "Hỏi AI Trợ giảng",
        description = "Hỏi đáp theo đúng ngữ cảnh video bài giảng, AI trả lời tức thì kèm đoạn trích dẫn transcript liên quan."
    ),
    OnboardingPage(
        title = "Ghi chú thông minh",
        description = "Gắn ghi chú trực tiếp vào từng mốc thời gian video và củng cố kiến thức với bài tập trắc nghiệm."
    ),
    OnboardingPage(
        title = "Sẵn sàng học tập",
        description = "Bắt đầu hành trình chinh phục kiến thức công nghệ ngay hôm nay cùng EduMind."
    )
)
