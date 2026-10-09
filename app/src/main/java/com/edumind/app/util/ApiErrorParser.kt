package com.edumind.app.util

import com.edumind.app.data.model.response.ApiErrorWrapper
import com.google.gson.Gson
import okhttp3.ResponseBody
import java.io.IOException

class AppException(
    val httpCode: Int,
    val errorCode: String,
    override val message: String
) : Exception(message)

class NetworkException(
    override val message: String = "Không thể kết nối đến máy chủ. Vui lòng kiểm tra kết nối mạng."
) : IOException(message)

object ApiErrorParser {
    private val gson = Gson()

    fun parseError(httpCode: Int, errorBody: ResponseBody?): AppException {
        return try {
            val jsonString = errorBody?.string()
            if (!jsonString.isNullOrBlank()) {
                val errorWrapper = gson.fromJson(jsonString, ApiErrorWrapper::class.java)
                AppException(
                    httpCode = httpCode,
                    errorCode = errorWrapper.error.code,
                    message = errorWrapper.error.message
                )
            } else {
                AppException(httpCode, "UNKNOWN_ERROR", "Đã xảy ra lỗi máy chủ (Mã: $httpCode)")
            }
        } catch (e: Exception) {
            AppException(httpCode, "PARSING_ERROR", "Không thể đọc phản hồi từ hệ thống ($httpCode)")
        }
    }
}
