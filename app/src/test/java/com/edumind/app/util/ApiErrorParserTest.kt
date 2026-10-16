package com.edumind.app.util

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test

class ApiErrorParserTest {

    @Test
    fun parseError_withStandardEduMindError_parsesCodeAndMessage() {
        val json = """
            {
                "error": {
                    "code": "EMAIL_ALREADY_EXISTS",
                    "message": "Email này đã được đăng ký. Vui lòng đăng nhập."
                }
            }
        """.trimIndent()
        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        val exception = ApiErrorParser.parseError(409, responseBody)

        assertEquals(409, exception.httpCode)
        assertEquals("EMAIL_ALREADY_EXISTS", exception.errorCode)
        assertEquals("Email này đã được đăng ký. Vui lòng đăng nhập.", exception.message)
    }

    @Test
    fun parseError_withFastApiDetailString_parsesDetailAsMessage() {
        val json = """{"detail": "Tài khoản hoặc mật khẩu không chính xác."}"""
        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        val exception = ApiErrorParser.parseError(401, responseBody)

        assertEquals(401, exception.httpCode)
        assertEquals("INVALID_CREDENTIALS", exception.errorCode)
        assertEquals("Tài khoản hoặc mật khẩu không chính xác.", exception.message)
    }

    @Test
    fun parseError_withRootMessageField_parsesMessage() {
        val json = """{"message": "Phiên đăng nhập đã hết hạn."}"""
        val responseBody = json.toResponseBody("application/json".toMediaTypeOrNull())

        val exception = ApiErrorParser.parseError(401, responseBody)

        assertEquals(401, exception.httpCode)
        assertEquals("INVALID_CREDENTIALS", exception.errorCode)
        assertEquals("Phiên đăng nhập đã hết hạn.", exception.message)
    }

    @Test
    fun parseError_withNullOrEmptyBody_usesDefaultFallback() {
        val exception = ApiErrorParser.parseError(429, null)

        assertEquals(429, exception.httpCode)
        assertEquals("RATE_LIMITED", exception.errorCode)
        assertEquals("Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau 15 phút.", exception.message)
    }

    @Test
    fun parseError_withHtmlOrInvalidJson_usesDefaultFallback() {
        val html = "<html><body>502 Bad Gateway</body></html>"
        val responseBody = html.toResponseBody("text/html".toMediaTypeOrNull())

        val exception = ApiErrorParser.parseError(502, responseBody)

        assertEquals(502, exception.httpCode)
        assertEquals("INTERNAL_ERROR", exception.errorCode)
        assertEquals("Hệ thống máy chủ đang bận. Vui lòng thử lại sau.", exception.message)
    }
}
