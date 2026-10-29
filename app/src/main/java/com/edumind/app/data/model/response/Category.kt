package com.edumind.app.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * Model danh mục khóa học
 */
data class Category(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("course_count")
    val courseCount: Int = 0
)

data class CategoryListResponse(
    @SerializedName("data")
    val data: List<Category> = emptyList()
)
