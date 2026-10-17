package com.edumind.app.repository

import com.edumind.app.data.model.response.Category
import com.edumind.app.data.model.response.CourseSummary
import com.edumind.app.data.model.response.PagedResponse
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeCourseRepository @Inject constructor() : CourseRepository {

    var simulateError: Boolean = false

    private val categories = listOf(
        Category(id = "cat_all", name = "Tất cả", courseCount = 45),
        Category(id = "cat_prog", name = "Lập trình", courseCount = 15),
        Category(id = "cat_design", name = "Thiết kế", courseCount = 10),
        Category(id = "cat_biz", name = "Kinh doanh", courseCount = 8),
        Category(id = "cat_data", name = "Dữ liệu", courseCount = 6),
        Category(id = "cat_ai", name = "AI/ML", courseCount = 6)
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
        delay(500) // Giả lập độ trễ mạng thực tế

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

    private fun generateDummyCourses(): List<Pair<String, CourseSummary>> {
        val list = mutableListOf<Pair<String, CourseSummary>>()

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
            list.add("cat_prog" to CourseSummary("p_$i", it.first, "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=600", it.second, null, (4.5f + (i % 5) * 0.1f).coerceAtMost(5.0f), 120 + i * 25, it.third))
        }

        val design = listOf(
            Triple("UI/UX Design cho người mới", "ThS. Trần Minh Châu", 0L),
            Triple("Figma Masterclass: Design System", "ThS. Trần Minh Châu", 699000L),
            Triple("Thiết kế Mobile App Material Design 3", "KS. Lê Thị Mai", 550000L),
            Triple("Typography & Hệ phối màu chuyên nghiệp", "ThS. Trần Minh Châu", 350000L),
            Triple("Adobe Illustrator từ cơ bản đến ứng dụng", "KS. Hoàng Văn Nam", 490000L),
            Triple("Motion Design với Lottie", "KS. Lê Thị Mai", 650000L),
            Triple("Thiết kế 3D căn bản với Blender", "KS. Ngô Đình Huy", 750000L),
            Triple("Xây dựng Portfolio & Case Study", "ThS. Trần Minh Châu", 0L),
            Triple("Visual Design: Bố cục & Nhịp điệu", "KS. Lê Thị Mai", 450000L),
            Triple("Design Thinking trong sản phẩm số", "TS. Hoàng Phúc", 590000L)
        )
        design.forEachIndexed { i, it ->
            list.add("cat_design" to CourseSummary("d_$i", it.first, "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=600", it.second, null, (4.4f + (i % 6) * 0.1f).coerceAtMost(5.0f), 85 + i * 18, it.third))
        }

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
            list.add("cat_biz" to CourseSummary("b_$i", it.first, "https://images.unsplash.com/photo-1460925895917-afdab827c52f?w=600", it.second, null, 4.6f, 95 + i * 14, it.third))
        }

        val data = listOf(
            Triple("Data Science với Python và Pandas", "TS. Vũ Hải Đăng", 599000L),
            Triple("SQL cho phân tích dữ liệu kinh doanh", "ThS. Trần Đức Cương", 0L),
            Triple("Xây dựng Dashboard trực quan Power BI", "ThS. Trần Đức Cương", 699000L),
            Triple("Thống kê ứng dụng trong Data Science", "TS. Vũ Hải Đăng", 450000L),
            Triple("Data Engineering căn bản Apache Spark", "KS. Nguyễn Hải Nam", 890000L),
            Triple("Xử lý dữ liệu lớn với AWS Big Data", "KS. Nguyễn Hải Nam", 990000L)
        )
        data.forEachIndexed { i, it ->
            list.add("cat_data" to CourseSummary("dt_$i", it.first, "https://images.unsplash.com/photo-1551288049-bebda4e38f71?w=600", it.second, null, 4.7f, 110 + i * 20, it.third))
        }

        val ai = listOf(
            Triple("Machine Learning cơ bản và ứng dụng", "ThS. Lê Hoàng Long", 699000L),
            Triple("Deep Learning PyTorch Computer Vision", "ThS. Lê Hoàng Long", 990000L),
            Triple("Xử lý ngôn ngữ tự nhiên Transformers", "TS. Nguyễn Kiều Oanh", 890000L),
            Triple("Prompt Engineering & LLMs thực chiến", "TS. Nguyễn Kiều Oanh", 0L),
            Triple("Xây dựng AI Agent với LangChain", "KS. Trần Quốc Bảo", 790000L),
            Triple("Triển khai mô hình AI MLOps", "ThS. Lê Hoàng Long", 950000L)
        )
        ai.forEachIndexed { i, it ->
            list.add("cat_ai" to CourseSummary("ai_$i", it.first, "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600", it.second, null, 4.8f, 150 + i * 30, it.third))
        }

        return list
    }
}
