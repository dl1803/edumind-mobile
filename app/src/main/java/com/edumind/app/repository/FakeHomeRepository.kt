package com.edumind.app.repository

import com.edumind.app.data.model.response.ContinueLearningItem
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.data.model.response.FeaturedCourse
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeHomeRepository @Inject constructor() : HomeRepository {

    override suspend fun getFeaturedCourses(): Result<List<FeaturedCourse>> {
        delay(600) // Giả lập độ trễ mạng
        return Result.success(
            listOf(
                FeaturedCourse(
                    id = "c_python_01",
                    title = "Nhập môn Lập trình Python",
                    subtitle = "Từ zero đến hero · Dự án thực chiến",
                    bannerUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=800&auto=format&fit=crop&q=80",
                    priceVnd = 499_000L,
                    originalPriceVnd = 899_000L,
                    rating = 4.8f,
                    reviewCount = 256,
                    badgeText = "Nổi bật nhất tuần",
                    hasAiAssistant = true
                ),
                FeaturedCourse(
                    id = "c_ai_02",
                    title = "Trí tuệ nhân tạo GenAI & Prompt Engineering",
                    subtitle = "Ứng dụng LLM vào công việc lập trình thực tế",
                    bannerUrl = "https://images.unsplash.com/photo-1677442136019-21780efad99a?w=800&auto=format&fit=crop&q=80",
                    priceVnd = 799_000L,
                    originalPriceVnd = 1_299_000L,
                    rating = 4.9f,
                    reviewCount = 312,
                    badgeText = "Xu hướng 2026",
                    hasAiAssistant = true
                ),
                FeaturedCourse(
                    id = "c_mobile_03",
                    title = "Android Jetpack Compose Toàn diện",
                    subtitle = "Xây dựng ứng dụng hiện đại chuẩn Clean Architecture",
                    bannerUrl = "https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?w=800&auto=format&fit=crop&q=80",
                    priceVnd = 899_000L,
                    originalPriceVnd = 1_499_000L,
                    rating = 4.9f,
                    reviewCount = 184,
                    badgeText = "Mới cập nhật",
                    hasAiAssistant = true
                )
            )
        )
    }

    override suspend fun getContinueLearningItems(): Result<List<ContinueLearningItem>> {
        delay(400)
        return Result.success(
            listOf(
                ContinueLearningItem(
                    courseId = "c_python_01",
                    lessonId = "l_py_05",
                    courseTitle = "Nhập môn Lập trình Python",
                    currentLessonTitle = "Bài 5: Vòng lặp for & while",
                    thumbnailUrl = "https://images.unsplash.com/photo-1526374965328-7f61d4dc18c5?w=200&auto=format&fit=crop&q=80",
                    progressPercent = 35,
                    remainingTimeText = "12:45 còn lại"
                ),
                ContinueLearningItem(
                    courseId = "c_sql_02",
                    lessonId = "l_sql_12",
                    courseTitle = "Cơ sở dữ liệu với SQL",
                    currentLessonTitle = "Bài 12: JOIN tables nâng cao",
                    thumbnailUrl = "https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=200&auto=format&fit=crop&q=80",
                    progressPercent = 68,
                    remainingTimeText = "05:20 còn lại"
                ),
                ContinueLearningItem(
                    courseId = "c_web_03",
                    lessonId = "l_web_03",
                    courseTitle = "Lập trình Web cơ bản",
                    currentLessonTitle = "Bài 3: CSS Flexbox & Grid",
                    thumbnailUrl = "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=200&auto=format&fit=crop&q=80",
                    progressPercent = 15,
                    remainingTimeText = "24:10 còn lại"
                )
            )
        )
    }

    override suspend fun getRecommendedCourses(): Result<List<CourseSummary>> {
        delay(500)
        return Result.success(
            listOf(
                CourseSummary(
                    id = "c_ml_01",
                    title = "Machine Learning cơ bản cho người mới bắt đầu",
                    thumbnailUrl = "https://images.unsplash.com/photo-1555949963-aa79dcee981c?w=400&auto=format&fit=crop&q=80",
                    instructorName = "ThS. Lê Hoàng Long",
                    rating = 4.7f,
                    reviewCount = 86,
                    priceVnd = 699_000L
                ),
                CourseSummary(
                    id = "c_uiux_02",
                    title = "UI/UX Design cho người mới",
                    thumbnailUrl = "https://images.unsplash.com/photo-1581291518655-9523c932deb8?w=400&auto=format&fit=crop&q=80",
                    instructorName = "ThS. Trần Minh Châu",
                    rating = 4.5f,
                    reviewCount = 128,
                    priceVnd = 0L // Miễn phí
                ),
                CourseSummary(
                    id = "c_android_03",
                    title = "Lập trình Android với Kotlin",
                    thumbnailUrl = "https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb?w=400&auto=format&fit=crop&q=80",
                    instructorName = "ThS. Phạm Đức Minh",
                    rating = 4.9f,
                    reviewCount = 210,
                    priceVnd = 899_000L
                ),
                CourseSummary(
                    id = "c_ds_04",
                    title = "Data Science với Python",
                    thumbnailUrl = "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=400&auto=format&fit=crop&q=80",
                    instructorName = "TS. Vũ Hải Đăng",
                    rating = 4.6f,
                    reviewCount = 95,
                    priceVnd = 599_000L
                )
            )
        )
    }

    override suspend fun getNewCourses(page: Int): Result<List<CourseSummary>> {
        delay(400)
        return Result.success(
            listOf(
                CourseSummary(
                    id = "c_sec_05",
                    title = "An ninh mạng căn bản và Phòng thủ mã độc",
                    thumbnailUrl = "https://images.unsplash.com/photo-1550751827-4bd374c3f58b?w=400&auto=format&fit=crop&q=80",
                    instructorName = "ThS. Đỗ Tuấn Anh",
                    rating = 4.3f,
                    reviewCount = 42,
                    priceVnd = 399_000L
                ),
                CourseSummary(
                    id = "c_devops_06",
                    title = "DevOps & CI/CD Pipeline với Docker, K8s",
                    thumbnailUrl = "https://images.unsplash.com/photo-1618401471353-b98afee0b2eb?w=400&auto=format&fit=crop&q=80",
                    instructorName = "Kỹ sư Lê Quốc Bảo",
                    rating = 4.8f,
                    reviewCount = 31,
                    priceVnd = 749_000L
                )
            )
        )
    }

    override suspend fun getUnreadNotificationCount(): Result<Int> {
        delay(200)
        return Result.success(3)
    }
}
