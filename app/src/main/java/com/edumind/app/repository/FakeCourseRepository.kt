package com.edumind.app.repository

import com.edumind.app.data.model.response.Category
import com.edumind.app.data.model.response.ContinueLearningItem
import com.edumind.app.data.model.response.CourseDetail
import com.edumind.app.data.model.response.CourseLesson
import com.edumind.app.data.model.response.CourseSection
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.data.model.response.InstructorSummary
import com.edumind.app.data.model.response.LessonType
import com.edumind.app.data.model.response.MyCourse
import com.edumind.app.data.model.response.PagedResponse
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeCourseRepository @Inject constructor() : CourseRepository {

    var simulateError: Boolean = false
    private val enrolledCourseIds = mutableSetOf<String>("p_1", "p_2", "p_3", "p_4")

    private val categories = listOf(
        Category(id = "cat_all", name = "Tất cả", courseCount = 45),
        Category(id = "cat_prog", name = "Lập trình", courseCount = 15),
        Category(id = "cat_design", name = "Thiết kế", courseCount = 10),
        Category(id = "cat_biz", name = "Kinh doanh", courseCount = 8),
        Category(id = "cat_data", name = "Dữ liệu", courseCount = 6),
        Category(id = "cat_ai", name = "AI/ML", courseCount = 6),
        Category(id = "cat_cloud", name = "Cloud & DevOps", courseCount = 0)
    )

    private val allCourses: List<Pair<String, CourseSummary>> = generateDummyCourses()

    override suspend fun getCourses(
        page: Int,
        pageSize: Int,
        search: String?,
        categoryId: String?,
        priceMin: Long?,
        priceMax: Long?,
        minRating: Float?,
        sortBy: String
    ): Result<PagedResponse<CourseSummary>> {
        delay(500)

        if (simulateError) {
            return Result.failure(Exception("Không thể tải danh sách khóa học. Vui lòng thử lại."))
        }

        var filtered = allCourses.asSequence()

        // 1. Lọc theo Danh mục
        if (!categoryId.isNullOrBlank() && categoryId != "cat_all") {
            filtered = filtered.filter { it.first == categoryId }
        }

        // 2. Lọc theo Từ khóa tìm kiếm
        if (!search.isNullOrBlank()) {
            val query = search.trim().lowercase()
            filtered = filtered.filter { it.second.title.lowercase().contains(query) }
        }

        // 3. Lọc theo Khoảng giá
        if (priceMin != null) {
            filtered = filtered.filter { it.second.priceVnd >= priceMin }
        }
        if (priceMax != null) {
            filtered = filtered.filter { it.second.priceVnd <= priceMax }
        }

        // 4. Lọc theo Rating tối thiểu
        if (minRating != null && minRating > 0f) {
            filtered = filtered.filter { it.second.rating >= minRating }
        }

        // 5. Sắp xếp
        val courseList = when (sortBy) {
            "price_asc" -> filtered.sortedBy { it.second.priceVnd }.map { it.second }.toList()
            "price_desc" -> filtered.sortedByDescending { it.second.priceVnd }.map { it.second }.toList()
            "popular", "recommended" -> filtered.sortedByDescending { it.second.reviewCount }.map { it.second }.toList()
            else -> filtered.map { it.second }.toList() // "newest"
        }

        // 6. Phân trang
        val total = courseList.size
        val totalPages = if (total == 0) 1 else (total + pageSize - 1) / pageSize
        val fromIndex = (page - 1) * pageSize
        val pagedItems = if (fromIndex >= total) {
            emptyList()
        } else {
            val toIndex = (fromIndex + pageSize).coerceAtMost(total)
            courseList.subList(fromIndex, toIndex)
        }

        return Result.success(
            PagedResponse(
                data = pagedItems,
                total = total,
                page = page,
                pageSize = pageSize,
                totalPages = totalPages
            )
        )
    }

    override suspend fun getCategories(): Result<List<Category>> {
        delay(200)
        return Result.success(categories)
    }

    override suspend fun getCourseDetail(courseId: String): Result<CourseDetail> {
        delay(400)

        if (simulateError || courseId == "invalid_id") {
            return Result.failure(Exception("Khóa học không tồn tại hoặc đã bị gỡ."))
        }

        val foundPair = allCourses.find { it.second.id == courseId }
        val courseSummary = foundPair?.second ?: allCourses.firstOrNull()?.second

        if (courseSummary == null) {
            return Result.failure(Exception("Khóa học không tồn tại hoặc đã bị gỡ."))
        }

        val isEnrolled = enrolledCourseIds.contains(courseId)
        val originalPrice = if (courseSummary.priceVnd > 0L) (courseSummary.priceVnd * 1.4).toLong() else null

        val detail = CourseDetail(
            id = courseId,
            title = courseSummary.title,
            categoryName = when (foundPair?.first) {
                "cat_prog" -> "Lập trình"
                "cat_design" -> "Thiết kế"
                "cat_biz" -> "Kinh doanh"
                "cat_data" -> "Dữ liệu"
                "cat_ai" -> "AI/ML"
                else -> "Lập trình"
            },
            thumbnailUrl = courseSummary.thumbnailUrl ?: "",
            priceVnd = courseSummary.priceVnd,
            originalPriceVnd = originalPrice,
            rating = courseSummary.rating,
            reviewCount = courseSummary.reviewCount,
            studentCount = courseSummary.reviewCount * 12,
            totalHours = 14,
            description = "Khóa học cung cấp lộ trình từ cơ bản đến nâng cao, kết hợp bài tập thực hành theo dự án thực tế và trợ lý AI giải đáp thắc mắc trực tiếp theo ngữ cảnh bài học.",
            keyFeatures = listOf(
                "14 giờ video chất lượng cao",
                "24 bài giảng chia làm 4 chương",
                "AI Trợ giảng hỗ trợ học tập",
                "Dự án thực hành thực tế"
            ),
            learningObjectives = listOf(
                "Nắm vững các kiến thức nền tảng và tư duy phát triển cốt lõi.",
                "Làm chủ các công cụ, thư viện và kiến trúc ứng dụng hiện đại.",
                "Thực hành tương tác trực tiếp cùng trợ lý AI ngữ cảnh.",
                "Hoàn thành đồ án thực tế có thể đưa vào hồ sơ năng lực."
            ),
            requirements = listOf(
                "Máy tính cá nhân có kết nối Internet để cài đặt môi trường.",
                "Sự kiên trì và tinh thần học hỏi, không bắt buộc kinh nghiệm trước."
            ),
            instructor = InstructorSummary(
                id = "ins_${courseSummary.id}",
                name = courseSummary.instructorName ?: "Giảng viên EduMind",
                title = "Chuyên gia đào tạo công nghệ",
                bio = "Hơn 10 năm kinh nghiệm nghiên cứu và giảng dạy chuyên sâu trong lĩnh vực Công nghệ Thông tin, từng tham gia dẫn dắt nhiều dự án chuyển đổi số quy mô lớn và cố vấn cho hàng ngàn học viên tốt nghiệp thành công.",
                rating = courseSummary.rating,
                courseCount = 8,
                studentCount = courseSummary.reviewCount * 10
            ),
            sections = listOf(
                CourseSection(
                    id = "sec_1",
                    title = "Chương 1: Tổng quan và Thiết lập môi trường",
                    durationSummary = "3 bài học • 45 phút",
                    lessons = listOf(
                        CourseLesson("l_1_1", "Bài 1: Giới thiệu khóa học & cài đặt công cụ", "12:45", LessonType.VIDEO, isFreePreview = true, isCompleted = isEnrolled),
                        CourseLesson("l_1_2", "Bài 2: Các khái niệm cơ bản đầu tiên", "18:30", LessonType.VIDEO, isFreePreview = true, isCompleted = isEnrolled),
                        CourseLesson("l_1_3", "Bài 3: Bài trắc nghiệm kiểm tra đầu khóa", "15 phút", LessonType.QUIZ, isFreePreview = false, isCompleted = isEnrolled)
                    )
                ),
                CourseSection(
                    id = "sec_2",
                    title = "Chương 2: Cú pháp cốt lõi và Kỹ thuật nền tảng",
                    durationSummary = "4 bài học • 1 giờ 20 phút",
                    lessons = listOf(
                        CourseLesson("l_2_1", "Bài 4: Cấu trúc điều khiển và xử lý luồng dữ liệu", "16:10", LessonType.VIDEO, isFreePreview = false, isCompleted = isEnrolled),
                        CourseLesson("l_2_2", "Bài 5: Tối ưu mã nguồn và kỹ thuật viết code sạch", "22:00", LessonType.VIDEO, isFreePreview = false, isCompleted = false, isCurrent = isEnrolled),
                        CourseLesson("l_2_3", "Bài 6: Xử lý ngoại lệ và bắt lỗi thực tế", "25:15", LessonType.VIDEO, isFreePreview = false, isCompleted = false),
                        CourseLesson("l_2_4", "Bài 7: Bài tập thực hành Chương 2", "15 phút", LessonType.QUIZ, isFreePreview = false, isCompleted = false)
                    )
                ),
                CourseSection(
                    id = "sec_3",
                    title = "Chương 3: Kiến trúc nâng cao và Ứng dụng thực tế",
                    durationSummary = "2 bài học • 45 phút",
                    lessons = listOf(
                        CourseLesson("l_3_1", "Bài 8: Thiết kế mô hình và phân tầng logic", "20:00", LessonType.VIDEO, isFreePreview = false),
                        CourseLesson("l_3_2", "Bài 9: Kết nối cơ sở dữ liệu và API", "24:30", LessonType.VIDEO, isFreePreview = false)
                    )
                ),
                CourseSection(
                    id = "sec_4",
                    title = "Chương 4: Hoàn thiện dự án và Đóng gói",
                    durationSummary = "1 bài học • 32 phút",
                    lessons = listOf(
                        CourseLesson("l_4_1", "Bài 10: Xây dựng dự án hoàn chỉnh cuối khóa", "32:00", LessonType.VIDEO, isFreePreview = false)
                    )
                )
            ),
            isEnrolled = isEnrolled,
            progressPercent = if (isEnrolled) 35 else 0,
            lastLessonId = if (isEnrolled) "l_2_2" else null,
            lastLessonTitle = if (isEnrolled) "Bài 5: Tối ưu mã nguồn và kỹ thuật viết code sạch" else null
        )

        return Result.success(detail)
    }

    override suspend fun enroll(courseId: String): EnrollResult {
        delay(400)
        if (simulateError) {
            return EnrollResult.Failure("Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng.")
        }
        if (enrolledCourseIds.contains(courseId)) {
            return EnrollResult.AlreadyEnrolled
        }
        val foundPair = allCourses.find { it.second.id == courseId }
        val price = foundPair?.second?.priceVnd ?: 0L
        if (price > 0L) {
            return EnrollResult.PaymentRequired
        }
        enrolledCourseIds.add(courseId)
        return EnrollResult.Success(firstLessonId = "l_1_1")
    }

    override suspend fun enrollCourse(courseId: String): Result<Boolean> {
        return when (val res = enroll(courseId)) {
            is EnrollResult.Success, is EnrollResult.AlreadyEnrolled -> Result.success(true)
            is EnrollResult.PaymentRequired -> Result.failure(Exception("Khóa học có phí, vui lòng mua để học"))
            is EnrollResult.Failure -> Result.failure(Exception(res.message))
        }
    }

    private val quickResumeData = ContinueLearningItem(
        courseId = "p_1",
        lessonId = "l_2_2",
        courseTitle = "Nhập môn Lập trình Python với AI",
        currentLessonTitle = "Bài 04: Cấu trúc điều kiện If-Else & Vòng lặp",
        thumbnailUrl = "file:///android_asset/courses/course_python.jpg",
        progressPercent = 45,
        remainingTimeText = "12:45 / 24:30"
    )

    private val myCoursesList = listOf(
        MyCourse(
            id = "p_1",
            title = "Nhập môn Lập trình Python với AI",
            thumbnailUrl = "file:///android_asset/courses/course_python.jpg",
            instructorName = "TS. Lê Hoàng Nam",
            progressPercent = 45,
            totalLessons = 20,
            completedLessons = 9,
            nextLessonTitle = "Bài 04: Cấu trúc điều kiện If-Else",
            nextLessonId = "l_2_2",
            categoryName = "Python Core",
            rating = 4.8f,
            reviewCount = 120
        ),
        MyCourse(
            id = "p_2",
            title = "Cấu trúc dữ liệu & Giải thuật Python",
            thumbnailUrl = "file:///android_asset/courses/course_sql.jpg",
            instructorName = "ThS. Đặng Minh Tuấn",
            progressPercent = 12,
            totalLessons = 25,
            completedLessons = 3,
            nextLessonTitle = "Bài 02: Phân tích độ phức tạp Big-O",
            nextLessonId = "l_1_2",
            categoryName = "Thuật toán",
            rating = 4.9f,
            reviewCount = 85
        ),
        MyCourse(
            id = "p_3",
            title = "Thiết kế Web Hiện đại với Tailwind CSS",
            thumbnailUrl = "file:///android_asset/courses/course_web.jpg",
            instructorName = "ThS. Hoàng Thùy Linh",
            progressPercent = 100,
            totalLessons = 16,
            completedLessons = 16,
            nextLessonTitle = "Bài 16: Tối ưu hiệu năng & Triển khai",
            nextLessonId = "l_4_1",
            categoryName = "Web Design",
            rating = 4.7f,
            reviewCount = 64
        ),
        MyCourse(
            id = "p_4",
            title = "Machine Learning cơ bản và ứng dụng",
            thumbnailUrl = "file:///android_asset/courses/course_ml.jpg",
            instructorName = "ThS. Lê Hoàng Long",
            progressPercent = 0,
            totalLessons = 15,
            completedLessons = 0,
            nextLessonTitle = "Bài 01: Giới thiệu Machine Learning",
            nextLessonId = "l_1_1",
            categoryName = "AI/ML",
            rating = 4.6f,
            reviewCount = 42
        )
    )

    override suspend fun getMyCourses(): Result<List<MyCourse>> {
        delay(400)
        if (simulateError) {
            return Result.failure(Exception("Không thể tải danh sách khóa học của bạn."))
        }
        return Result.success(myCoursesList)
    }

    override suspend fun getQuickResumeItem(): Result<ContinueLearningItem?> {
        delay(200)
        if (simulateError) {
            return Result.failure(Exception("Không thể tải bài học gần nhất."))
        }
        return Result.success(quickResumeData)
    }


    private fun generateDummyCourses(): List<Pair<String, CourseSummary>> {
        val list = mutableListOf<Pair<String, CourseSummary>>()

        val localThumbnails = listOf(
            "file:///android_asset/courses/course_python.jpg",
            "file:///android_asset/courses/course_uiux.jpg",
            "file:///android_asset/courses/course_android.jpg",
            "file:///android_asset/courses/course_ml.jpg",
            "file:///android_asset/courses/course_datascience.svg",
            "file:///android_asset/courses/course_web.jpg",
            "file:///android_asset/courses/course_sql.jpg",
            "file:///android_asset/courses/course_security.svg",
            "file:///android_asset/courses/course_devops.svg"
        )

        // Lập trình (15 khóa)
        val prog = listOf(
            Triple("Nhập môn Lập trình Python", "ThS. Nguyễn Hoàng Nam", 499000L),
            Triple("Lập trình Android với Kotlin & Compose", "ThS. Phạm Đức Minh", 899000L),
            Triple("Lập trình Web Fullstack Next.js", "KS. Trần Quốc Bảo", 799000L),
            Triple("Cơ bản về C++ và Cấu trúc dữ liệu", "ThS. Đỗ Tuấn Anh", 0L),
            Triple("Phát triển RESTful API Microservices", "ThS. Nguyễn Hoàng Nam", 650000L),
            Triple("Lập trình iOS hiện đại với SwiftUI", "KS. Lê Hữu Phước", 850000L),
            Triple("Lập trình Go cơ bản đến nâng cao", "KS. Bùi Văn Hải", 550000L),
            Triple("TypeScript chuyên sâu cho Frontend", "KS. Trần Quốc Bảo", 399000L),
            Triple("React 18 & Design Patterns thực chiến", "KS. Vũ Minh Khôi", 599000L),
            Triple("Git & GitHub Masterclass", "ThS. Phạm Đức Minh", 0L),
            Triple("Kiến trúc Clean Architecture Android", "ThS. Phạm Đức Minh", 990000L),
            Triple("Flutter đa nền tảng từ số 0", "KS. Đặng Thành Long", 750000L),
            Triple("Docker & Kubernetes cho Developers", "ThS. Đỗ Tuấn Anh", 890000L),
            Triple("Kiểm thử tự động với JUnit & Espresso", "ThS. Phạm Đức Minh", 450000L),
            Triple("Giải thuật LeetCode 75 bằng Java", "ThS. Nguyễn Hoàng Nam", 0L)
        )
        prog.forEachIndexed { i, it ->
            val thumb = when (i) {
                0 -> "file:///android_asset/courses/course_python.jpg"
                1 -> "file:///android_asset/courses/course_android.jpg"
                2 -> "file:///android_asset/courses/course_web.jpg"
                3 -> "file:///android_asset/courses/course_devops.svg"
                else -> localThumbnails[i % localThumbnails.size]
            }
            list.add("cat_prog" to CourseSummary("p_$i", it.first, thumb, it.second, null, (4.5f + (i % 5) * 0.1f).coerceAtMost(5.0f), 120 + i * 25, it.third))
        }

        // Thiết kế (10 khóa)
        val design = listOf(
            Triple("UI/UX Design cho người mới", "ThS. Trần Minh Châu", 0L),
            Triple("Figma Masterclass: Design System", "ThS. Trần Minh Châu", 699000L),
            Triple("Thiết kế Mobile App Material Design 3", "KS. Lê Thị Mai", 550000L),
            Triple("Typography & Hệ phối màu chuyên nghiệp", "ThS. Trần Minh Châu", 350000L),
            Triple("Adobe Illustrator từ cơ bản đến ứng dụng", "KS. Hoàng Văn Nam", 490000L),
            Triple("Motion Design với Lottie", "KS. Lê Thị Mai", 650000L),
            Triple("Thiết kế 3D căn bản với Blender", "KS. Ngô Đình Huy", 750000L),
            Triple("Xây dựng Portfolio & Case Study", "ThS. Trần Minh Châu", 250000L),
            Triple("Visual Design: Bố cục & Nhịp điệu", "KS. Lê Thị Mai", 450000L),
            Triple("Design Thinking trong sản phẩm số", "TS. Hoàng Phúc", 590000L)
        )
        design.forEachIndexed { i, it ->
            val thumb = when (i) {
                0 -> "file:///android_asset/courses/course_uiux.jpg"
                else -> localThumbnails[(i + 1) % localThumbnails.size]
            }
            val rating = when (i) {
                0 -> 4.4f // Miễn phí nhưng rating 4.4f (lọc Miễn phí + >= 4.5 sẽ ra rỗng)
                else -> (4.4f + (i % 6) * 0.1f).coerceAtMost(5.0f)
            }
            list.add("cat_design" to CourseSummary("d_$i", it.first, thumb, it.second, null, rating, 85 + i * 18, it.third))
        }

        // Kinh doanh (8 khóa)
        val biz = listOf(
            Triple("Khởi nghiệp tinh gọn cho người trẻ", "TS. Lê Trọng Nghĩa", 699000L),
            Triple("Digital Marketing thực chiến từ A-Z", "ThS. Phan Thanh Hà", 599000L),
            Triple("Product Management 101", "KS. Nguyễn Thế Vinh", 850000L),
            Triple("Kỹ năng đàm phán và thuyết phục", "TS. Lê Trọng Nghĩa", 0L),
            Triple("Quản lý tài chính cá nhân thông minh", "ThS. Phan Thanh Hà", 299000L),
            Triple("Xây dựng thương hiệu cá nhân", "ThS. Hoàng Mỹ Linh", 399000L),
            Triple("Kỹ năng thuyết trình & Pitching", "TS. Lê Trọng Nghĩa", 499000L),
            Triple("Agile & Scrum trong vận hành dự án", "KS. Nguyễn Thế Vinh", 0L)
        )
        biz.forEachIndexed { i, it ->
            val thumb = localThumbnails[(i + 2) % localThumbnails.size]
            list.add("cat_biz" to CourseSummary("b_$i", it.first, thumb, it.second, null, 4.6f, 95 + i * 14, it.third))
        }

        // Dữ liệu (6 khóa)
        val data = listOf(
            Triple("Data Science với Python và Pandas", "TS. Vũ Hải Đăng", 599000L),
            Triple("SQL cho phân tích dữ liệu kinh doanh", "ThS. Trần Đức Cương", 0L),
            Triple("Xây dựng Dashboard trực quan Power BI", "ThS. Trần Đức Cương", 699000L),
            Triple("Thống kê ứng dụng trong Data Science", "TS. Vũ Hải Đăng", 450000L),
            Triple("Data Engineering căn bản Apache Spark", "KS. Nguyễn Hải Nam", 890000L),
            Triple("Xử lý dữ liệu lớn với AWS Big Data", "KS. Nguyễn Hải Nam", 990000L)
        )
        data.forEachIndexed { i, it ->
            val thumb = when (i) {
                0 -> "file:///android_asset/courses/course_datascience.svg"
                1 -> "file:///android_asset/courses/course_sql.jpg"
                else -> localThumbnails[(i + 4) % localThumbnails.size]
            }
            list.add("cat_data" to CourseSummary("dt_$i", it.first, thumb, it.second, null, 4.7f, 110 + i * 20, it.third))
        }

        // AI/ML (6 khóa - toàn bộ có phí, lọc Miễn phí ra rỗng)
        val ai = listOf(
            Triple("Machine Learning cơ bản và ứng dụng", "ThS. Lê Hoàng Long", 699000L),
            Triple("Deep Learning PyTorch Computer Vision", "ThS. Lê Hoàng Long", 990000L),
            Triple("Xử lý ngôn ngữ tự nhiên Transformers", "TS. Nguyễn Kiều Oanh", 890000L),
            Triple("Prompt Engineering & LLMs thực chiến", "TS. Nguyễn Kiều Oanh", 590000L),
            Triple("Xây dựng AI Agent với LangChain", "KS. Trần Quốc Bảo", 790000L),
            Triple("Triển khai mô hình AI MLOps", "ThS. Lê Hoàng Long", 950000L)
        )
        ai.forEachIndexed { i, it ->
            val thumb = when (i) {
                0 -> "file:///android_asset/courses/course_ml.jpg"
                else -> localThumbnails[(i + 3) % localThumbnails.size]
            }
            list.add("cat_ai" to CourseSummary("ai_$i", it.first, thumb, it.second, null, 4.8f, 150 + i * 30, it.third))
        }

        return list
    }
}
