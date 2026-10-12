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
        delay(600)
        return Result.success(
            listOf(
                FeaturedCourse(
                    id = "c_python_01",
                    title = "Nhập môn Lập trình Python",
                    subtitle = "Từ zero đến hero · Dự án thực chiến",
                    bannerUrl = "file:///android_asset/courses/course_python.jpg",
                    priceVnd = 499_000L,
                    originalPriceVnd = 899_000L,
                    rating = 4.8f,
                    reviewCount = 256,
                    badgeText = "Nổi bật nhất tuần",
                    hasAiAssistant = true
                ),
                FeaturedCourse(
                    id = "c_android_02",
                    title = "Lập trình Android với Kotlin",
                    subtitle = "Xây dựng ứng dụng hiện đại chuẩn Clean Architecture",
                    bannerUrl = "file:///android_asset/courses/course_android.jpg",
                    priceVnd = 899_000L,
                    originalPriceVnd = 1_499_000L,
                    rating = 4.9f,
                    reviewCount = 210,
                    badgeText = "Xu hướng 2026",
                    hasAiAssistant = true
                ),
                FeaturedCourse(
                    id = "c_ml_03",
                    title = "Machine Learning cơ bản",
                    subtitle = "Mô hình toán học và ứng dụng thực tế",
                    bannerUrl = "file:///android_asset/courses/course_ml.jpg",
                    priceVnd = 699_000L,
                    originalPriceVnd = 999_000L,
                    rating = 4.7f,
                    reviewCount = 86,
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
                    thumbnailUrl = "file:///android_asset/courses/course_python.jpg",
                    progressPercent = 35,
                    remainingTimeText = "12:45 còn lại"
                ),
                ContinueLearningItem(
                    courseId = "c_sql_02",
                    lessonId = "l_sql_12",
                    courseTitle = "Cơ sở dữ liệu với SQL",
                    currentLessonTitle = "Bài 12: JOIN tables nâng cao",
                    thumbnailUrl = "file:///android_asset/courses/course_sql.jpg",
                    progressPercent = 68,
                    remainingTimeText = "05:20 còn lại"
                ),
                ContinueLearningItem(
                    courseId = "c_web_03",
                    lessonId = "l_web_03",
                    courseTitle = "Lập trình Web cơ bản",
                    currentLessonTitle = "Bài 3: CSS Flexbox & Grid",
                    thumbnailUrl = "file:///android_asset/courses/course_web.jpg",
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
                    title = "Machine Learning cơ bản",
                    thumbnailUrl = "file:///android_asset/courses/course_ml.jpg",
                    instructorName = "ThS. Lê Hoàng Long",
                    rating = 4.7f,
                    reviewCount = 86,
                    priceVnd = 699_000L
                ),
                CourseSummary(
                    id = "c_uiux_02",
                    title = "UI/UX Design cho người mới",
                    thumbnailUrl = "file:///android_asset/courses/course_uiux.jpg",
                    instructorName = "ThS. Trần Minh Châu",
                    rating = 4.5f,
                    reviewCount = 128,
                    priceVnd = 0L // Miễn phí
                ),
                CourseSummary(
                    id = "c_android_03",
                    title = "Lập trình Android với Kotlin",
                    thumbnailUrl = "file:///android_asset/courses/course_android.jpg",
                    instructorName = "ThS. Phạm Đức Minh",
                    rating = 4.9f,
                    reviewCount = 210,
                    priceVnd = 899_000L
                ),
                CourseSummary(
                    id = "c_ds_04",
                    title = "Data Science với Python",
                    thumbnailUrl = "file:///android_asset/courses/course_datascience.svg",
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
                    title = "An ninh mạng căn bản",
                    thumbnailUrl = "file:///android_asset/courses/course_security.svg",
                    instructorName = "ThS. Đỗ Tuấn Anh",
                    rating = 4.3f,
                    reviewCount = 42,
                    priceVnd = 399_000L
                ),
                CourseSummary(
                    id = "c_devops_06",
                    title = "DevOps & CI/CD Pipeline",
                    thumbnailUrl = "file:///android_asset/courses/course_devops.svg",
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
