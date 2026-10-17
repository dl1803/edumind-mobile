package com.edumind.app.data.model.response

import com.google.gson.annotations.SerializedName

/**
 * Model phân trang
 */
data class PagedResponse<T>(
    @SerializedName("data")
    val data: List<T>,
    @SerializedName("total")
    val total: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("page_size")
    val pageSize: Int,
    @SerializedName("total_pages")
    val totalPages: Int
)
