package com.edumind.app.util

import com.edumind.app.data.model.response.ApiErrorWrapper
import com.google.gson.Gson
import com.google.gson.JsonParser
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
        val jsonString = try {
            errorBody?.string()
        } catch (_: Exception) {
            null
        }

        if (jsonString.isNullOrBlank()) {
            return defaultException(httpCode)
        }

        return try {
            val errorWrapper = gson.fromJson(jsonString, ApiErrorWrapper::class.java)
            val errorDetail = errorWrapper?.error
            if (errorDetail != null && errorDetail.code.isNotBlank() && errorDetail.message.isNotBlank()) {
                AppException(
                    httpCode = httpCode,
                    errorCode = errorDetail.code,
                    message = errorDetail.message
                )
            } else {
                parseFallback(httpCode, jsonString)
            }
        } catch (_: Exception) {
            parseFallback(httpCode, jsonString)
        }
    }

    private fun parseFallback(httpCode: Int, jsonString: String): AppException {
        return try {
            val element = JsonParser.parseString(jsonString)
            if (element.isJsonObject) {
                val jsonObject = element.asJsonObject
                val detail = when {
                    jsonObject.has("detail") && jsonObject.get("detail").isJsonPrimitive ->
                        jsonObject.get("detail").asString
                    jsonObject.has("message") && jsonObject.get("message").isJsonPrimitive ->
                        jsonObject.get("message").asString
                    else -> null
                }
                val code = when {
                    jsonObject.has("code") && jsonObject.get("code").isJsonPrimitive ->
                        jsonObject.get("code").asString
                    else -> getDefaultErrorCode(httpCode)
                }
                AppException(
                    httpCode = httpCode,
                    errorCode = code,
                    message = detail?.takeIf { it.isNotBlank() } ?: getDefaultErrorMessage(httpCode)
                )
            } else {
                defaultException(httpCode)
            }
        } catch (_: Exception) {
            defaultException(httpCode)
        }
    }

    private fun getDefaultErrorCode(httpCode: Int): String {
        return when (httpCode) {
            400 -> "VALIDATION_ERROR"
            401 -> "INVALID_CREDENTIALS"
            403 -> "FORBIDDEN"
            404 -> "NOT_FOUND"
            409 -> "EMAIL_ALREADY_EXISTS"
            429 -> "RATE_LIMITED"
            else -> "INTERNAL_ERROR"
        }
    }

    private fun getDefaultErrorMessage(httpCode: Int): String {
        return when (httpCode) {
            400 -> "Yêu cầu không hợp lệ. Vui lòng kiểm tra lại thông tin."
            401 -> "Tài khoản hoặc mật khẩu không chính xác."
            403 -> "Tài khoản không có quyền truy cập hoặc chưa được xác thực."
            404 -> "Không tìm thấy tài nguyên yêu cầu."
            409 -> "Email hoặc dữ liệu này đã được sử dụng."
            429 -> "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau 15 phút."
            500, 502, 503, 504 -> "Hệ thống máy chủ đang bận. Vui lòng thử lại sau."
            else -> "Đã xảy ra lỗi hệ thống (Mã: $httpCode)"
        }
    }

    private fun defaultException(httpCode: Int): AppException {
        return AppException(
            httpCode = httpCode,
            errorCode = getDefaultErrorCode(httpCode),
            message = getDefaultErrorMessage(httpCode)
        )
    }
}
