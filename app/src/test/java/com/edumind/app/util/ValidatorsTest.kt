package com.edumind.app.util

import org.junit.Assert.*
import org.junit.Test

class ValidatorsTest {

    // --- FULL NAME ---

    @Test
    fun validateFullName_empty_returnsError() {
        val result = Validators.validateFullName("   ")
        assertFalse(result.isValid)
        assertEquals("Vui lòng nhập họ và tên.", result.errorMessage)
    }

    @Test
    fun validateFullName_tooShort_returnsError() {
        val result = Validators.validateFullName("A")
        assertFalse(result.isValid)
        assertEquals("Họ và tên phải có ít nhất 2 ký tự.", result.errorMessage)
    }

    @Test
    fun validateFullName_valid_returnsValid() {
        val result = Validators.validateFullName("Trần Lê Đức Lợi")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    // --- EMAIL ---

    @Test
    fun validateEmail_empty_returnsError() {
        val result = Validators.validateEmail("")
        assertFalse(result.isValid)
        assertEquals("Vui lòng nhập địa chỉ email.", result.errorMessage)
    }

    @Test
    fun validateEmail_invalidFormat_returnsError() {
        assertFalse(Validators.validateEmail("invalid-email").isValid)
        assertFalse(Validators.validateEmail("user@").isValid)
        assertFalse(Validators.validateEmail("@edumind.com").isValid)
        assertFalse(Validators.validateEmail("user@domain").isValid)
    }

    @Test
    fun validateEmail_validFormat_returnsValid() {
        assertTrue(Validators.validateEmail("student@edumind.edu.vn").isValid)
        assertTrue(Validators.validateEmail("user.name123@gmail.com").isValid)
    }

    // --- PASSWORD ---

    @Test
    fun validatePassword_empty_returnsError() {
        val result = Validators.validatePassword("")
        assertFalse(result.isValid)
        assertEquals("Vui lòng nhập mật khẩu.", result.errorMessage)
    }

    @Test
    fun validatePassword_short_returnsError() {
        val result = Validators.validatePassword("Abc@1")
        assertFalse(result.isValid)
    }

    @Test
    fun validatePassword_missingUppercase_returnsError() {
        val result = Validators.validatePassword("edumind@2026")
        assertFalse(result.isValid)
    }

    @Test
    fun validatePassword_missingLowercase_returnsError() {
        val result = Validators.validatePassword("EDUMIND@2026")
        assertFalse(result.isValid)
    }

    @Test
    fun validatePassword_missingDigit_returnsError() {
        val result = Validators.validatePassword("EduMind@Pass")
        assertFalse(result.isValid)
    }

    @Test
    fun validatePassword_missingSpecialChar_returnsError() {
        val result = Validators.validatePassword("EduMind2026")
        assertFalse(result.isValid)
    }

    @Test
    fun validatePassword_strongPassword_returnsValid() {
        assertTrue(Validators.validatePassword("EduMind@2026").isValid)
        assertTrue(Validators.validatePassword("Admin@1234").isValid)
    }

    // --- CONFIRM PASSWORD ---

    @Test
    fun validateConfirmPassword_mismatch_returnsError() {
        val result = Validators.validateConfirmPassword("EduMind@2026", "Different@123")
        assertFalse(result.isValid)
        assertEquals("Mật khẩu xác nhận không trùng khớp.", result.errorMessage)
    }

    @Test
    fun validateConfirmPassword_match_returnsValid() {
        val result = Validators.validateConfirmPassword("EduMind@2026", "EduMind@2026")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }

    // --- PASSWORD CHECKLIST CRITERIA ---

    @Test
    fun passwordCriteriaHelpers_checkCorrectly() {
        assertTrue(Validators.hasMinLength("12345678"))
        assertFalse(Validators.hasMinLength("1234567"))

        assertTrue(Validators.hasUppercase("aBc"))
        assertFalse(Validators.hasUppercase("abc"))

        assertTrue(Validators.hasLowercase("AbC"))
        assertFalse(Validators.hasLowercase("ABC"))

        assertTrue(Validators.hasDigit("pass1"))
        assertFalse(Validators.hasDigit("pass"))

        assertTrue(Validators.hasSpecialChar("pass@"))
        assertFalse(Validators.hasSpecialChar("pass123"))
    }

    // --- OTP VALIDATION ---

    @Test
    fun validateOtp_empty_returnsError() {
        val result = Validators.validateOtp("")
        assertFalse(result.isValid)
        assertEquals("Vui lòng nhập mã xác thực.", result.errorMessage)
    }

    @Test
    fun validateOtp_not6Digits_returnsError() {
        assertFalse(Validators.validateOtp("12345").isValid)
        assertFalse(Validators.validateOtp("1234567").isValid)
    }

    @Test
    fun validateOtp_containsNonDigits_returnsError() {
        val result = Validators.validateOtp("12345a")
        assertFalse(result.isValid)
        assertEquals("Mã xác thực chỉ chứa chữ số.", result.errorMessage)
    }

    @Test
    fun validateOtp_valid6Digits_returnsValid() {
        val result = Validators.validateOtp("123456")
        assertTrue(result.isValid)
        assertNull(result.errorMessage)
    }
}
