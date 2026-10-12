package com.edumind.app.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Định dạng số tiền Long sang chuỗi VND
 */
fun Long.toVnd(): String {
    if (this <= 0L) return "Miễn phí"
    val symbols = DecimalFormatSymbols(Locale("vi", "VN")).apply {
        groupingSeparator = '.'
    }
    val formatter = DecimalFormat("#,###", symbols)
    return "${formatter.format(this)}đ"
}
