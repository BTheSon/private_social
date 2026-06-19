package com.example.backend.domain.contact

object PhoneNumberNormalizer {
    private const val DEFAULT_COUNTRY_CODE = "84"

    /**
     * Chuẩn hóa số điện thoại về dạng quốc tế không dấu: ví dụ +84901234567
     */
    fun normalize(rawNumber: String): String {
        // 1. Loại bỏ toàn bộ ký tự không phải số hoặc dấu +
        var cleaned = rawNumber.replace(Regex("[^0-9+]"), "")

        if (cleaned.isBlank()) return ""

        // 2. Xử lý các trường hợp định dạng phổ biến tại VN
        cleaned = when {
            // Đã có mã quốc gia dạng +84...
            cleaned.startsWith("+$DEFAULT_COUNTRY_CODE") -> cleaned

            // Dạng 0084...
            cleaned.startsWith("00$DEFAULT_COUNTRY_CODE") ->
                "+" + cleaned.removePrefix("00")

            // Dạng nội địa bắt đầu bằng 0 (ví dụ 0901234567)
            cleaned.startsWith("0") ->
                "+$DEFAULT_COUNTRY_CODE" + cleaned.removePrefix("0")

            // Dạng thiếu số 0 đầu (ví dụ 901234567)
            cleaned.length in 9..10 && !cleaned.startsWith("+") ->
                "+$DEFAULT_COUNTRY_CODE$cleaned"

            else -> cleaned
        }

        // 3. Validate cơ bản: số hợp lệ phải có ít nhất 10-11 số sau dấu +
        val digitsOnly = cleaned.removePrefix("+")
        if (digitsOnly.length < 9 || digitsOnly.length > 15) return ""

        return cleaned
    }
}