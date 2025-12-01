package com.example.nutricook.utils

/**
 * Helper function để normalize và validate decimal input
 * Hỗ trợ cả dấu phẩy (,) và dấu chấm (.) cho decimal separator
 * GIỮ NGUYÊN dấu phẩy hoặc dấu chấm theo sở thích người dùng
 * Báo đỏ nếu bắt đầu bằng dấu decimal (".9", ",9") - bắt buộc phải nhập "0.9" hoặc "0,9"
 */
object DecimalInputHelper {
    
    /**
     * Normalize decimal input (cho onValueChange):
     * - Hỗ trợ cả dấu phẩy (,) và dấu chấm (.) - GIỮ NGUYÊN theo sở thích người dùng
     * - Chỉ cho phép số và 1 dấu decimal separator
     * - KHÔNG tự động thêm "0" ngay (để hiển thị error state)
     * - KHÔNG chuyển dấu phẩy thành dấu chấm (giữ nguyên)
     */
    fun normalizeDecimalInput(input: String): String {
        if (input.isBlank()) return ""
        
        // Cho phép số, dấu chấm và dấu phẩy
        val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
        
        // Chỉ cho phép 1 dấu decimal separator
        // Nếu có cả 2, giữ dấu đầu tiên xuất hiện
        val dotIndex = filtered.indexOf('.')
        val commaIndex = filtered.indexOf(',')
        
        val normalized = when {
            dotIndex >= 0 && commaIndex >= 0 -> {
                // Nếu có cả 2, giữ dấu xuất hiện trước
                if (dotIndex < commaIndex) {
                    filtered.replace(",", "") // Giữ dấu chấm
                } else {
                    filtered.replace(".", "") // Giữ dấu phẩy
                }
            }
            else -> filtered // Giữ nguyên dấu phẩy hoặc dấu chấm
        }
        
        // KHÔNG tự động thêm "0" ở đây - để hiển thị error state khi bắt đầu bằng dấu decimal
        return normalized
    }
    
    /**
     * Normalize và tự động thêm "0" (cho parse khi save):
     * - Tự động thêm "0" trước dấu decimal nếu bắt đầu bằng dấu đó
     * - Hỗ trợ cả dấu phẩy và dấu chấm
     */
    fun normalizeForParse(input: String): String {
        val normalized = normalizeDecimalInput(input)
        
        // Tự động thêm "0" trước dấu decimal nếu bắt đầu bằng dấu đó
        return when {
            normalized.startsWith(".") -> "0$normalized"
            normalized.startsWith(",") -> "0$normalized"
            normalized == "." -> "0."
            normalized == "," -> "0,"
            else -> normalized
        }
    }
    
    /**
     * Parse string thành Float, hỗ trợ cả dấu phẩy và dấu chấm
     * Tự động thêm "0" trước dấu decimal nếu cần
     * Chuyển dấu phẩy thành dấu chấm khi parse (vì Float.parseFloat() chỉ nhận dấu chấm)
     */
    fun parseToFloat(input: String): Float? {
        if (input.isBlank()) return null
        
        val normalized = normalizeForParse(input)
        // Chuyển dấu phẩy thành dấu chấm để parse (Float.parseFloat() chỉ nhận dấu chấm)
        val forParse = normalized.replace(",", ".")
        return forParse.toFloatOrNull()
    }
    
    /**
     * Kiểm tra giá trị có hợp lệ không (số dương)
     * Trả về false nếu bắt đầu bằng dấu decimal (".9", ",9") - bắt buộc phải nhập "0.9" hoặc "0,9"
     */
    fun isValid(value: String): Boolean {
        if (value.isBlank()) return true // Cho phép rỗng
        
        val trimmed = value.trim()
        
        // Kiểm tra nếu chỉ có dấu decimal (chưa có số) - không hợp lệ, hiển thị error
        if (trimmed == "." || trimmed == ",") {
            return false
        }
        
        // Filter để chỉ lấy số và dấu decimal
        val filtered = trimmed.filter { it.isDigit() || it == '.' || it == ',' }
        
        // Nếu sau khi filter chỉ còn dấu decimal (không có số) - không hợp lệ
        if (filtered == "." || filtered == ",") {
            return false
        }
        
        // QUAN TRỌNG: Kiểm tra nếu bắt đầu bằng dấu decimal (".9", ",9") - KHÔNG hợp lệ
        // Bắt buộc phải nhập "0.9" hoặc "0,9"
        val startsWithDecimal = filtered.startsWith(".") || filtered.startsWith(",")
        val hasDigitsAfter = filtered.drop(1).any { it.isDigit() }
        
        if (startsWithDecimal && hasDigitsAfter) {
            // Bắt đầu bằng dấu decimal và có số sau đó (".9", ",9") - KHÔNG hợp lệ
            // Bắt buộc phải nhập "0.9" hoặc "0,9"
            return false
        }
        
        // Kiểm tra nếu chỉ có dấu decimal ở đầu mà không có số sau đó
        if (startsWithDecimal && !hasDigitsAfter) {
            return false // Chỉ có dấu decimal, chưa có số - hiển thị error
        }
        
        // Parse và kiểm tra giá trị hợp lệ
        val floatValue = parseToFloat(value)
        return floatValue != null && floatValue >= 0
    }
}

