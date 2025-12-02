package com.example.nutricook.utils

/**
 * Enum định nghĩa các đơn vị đo lường cho nguyên liệu
 */
enum class IngredientUnit(
    val displayName: String,
    val abbreviation: String,
    val conversionToGrams: (Double) -> Double // Hàm chuyển đổi sang gram
) {
    // Đơn vị khối lượng
    GRAMS("Gram", "g", { it }), // 1g = 1g
    KILOGRAMS("Kilogram", "kg", { it * 1000.0 }), // 1kg = 1000g
    
    // Đơn vị thể tích (cho chất lỏng)
    MILLILITERS("Milliliter", "ml", { it }), // 1ml nước ≈ 1g (giả định)
    LITERS("Liter", "l", { it * 1000.0 }), // 1l = 1000ml
    
    // Đơn vị đếm
    PIECES("Quả/Cái", "quả", { 
        // Chuyển đổi dựa trên loại nguyên liệu
        // Ví dụ: 1 quả trứng ≈ 50-60g, 1 quả chanh ≈ 50-80g
        // Trung bình: 1 quả = 60g (ước tính hợp lý cho hầu hết trái cây và trứng)
        it * 60.0 
    }),
    
    // Đơn vị khác
    CUPS("Cốc", "cốc", { it * 240.0 }), // 1 cốc ≈ 240ml
    TABLESPOONS("Thìa canh", "thìa canh", { it * 15.0 }), // 1 thìa canh ≈ 15ml
    TEASPOONS("Thìa cà phê", "thìa cà phê", { it * 5.0 }), // 1 thìa cà phê ≈ 5ml
    
    // Đơn vị đặc biệt
    SLICES("Lát", "lát", { it * 25.0 }), // 1 lát ≈ 25g (bánh mì, phô mai, thịt nguội)
    CLOVES("Tép", "tép", { it * 3.0 }), // 1 tép tỏi ≈ 3g (đúng)
    BULBS("Củ", "củ", { it * 80.0 }); // 1 củ ≈ 80g (trung bình cho hành, khoai tây, cà rốt: 50-150g)
    
    /**
     * Chuyển đổi giá trị từ đơn vị này sang gram
     */
    fun toGrams(value: Double): Double {
        return conversionToGrams(value)
    }
    
    companion object {
        /**
         * Lấy đơn vị mặc định dựa trên tên nguyên liệu
         */
        fun getDefaultUnit(ingredientName: String): IngredientUnit {
            val name = ingredientName.lowercase()
            return when {
                // Chất lỏng (ưu tiên cao nhất)
                name.contains("nước") || 
                name.contains("sữa") || 
                name.contains("dầu") || 
                name.contains("giấm") || 
                name.contains("nước mắm") ||
                name.contains("nước tương") -> MILLILITERS
                
                // Đếm theo quả/cái (ưu tiên cao)
                name.contains("trứng") -> PIECES
                name.contains("chanh") -> PIECES
                name.contains("quả") -> PIECES
                name.contains("cái") -> PIECES
                
                // Đếm theo củ (các củ cụ thể)
                name.contains("hành củ") || name.contains("củ hành") -> BULBS
                name.contains("khoai tây") -> BULBS
                name.contains("khoai lang") -> BULBS
                name.contains("cà rốt") -> BULBS
                name.contains("củ cải") -> BULBS
                name.contains("tỏi") && name.contains("củ") -> BULBS
                
                // Bột, đường, gia vị (ưu tiên cao)
                name.contains("bột") || 
                name.contains("đường") || 
                name.contains("muối") ||
                name.contains("tiêu") -> GRAMS
                
                // Thịt, cá, hải sản (ưu tiên cao)
                name.contains("thịt") || 
                name.contains("cá") || 
                name.contains("tôm") ||
                name.contains("cua") -> GRAMS
                
                // Rau xanh, rau củ (dùng gram)
                name.contains("rau") && !name.contains("củ") -> GRAMS
                name.contains("bắp cải") -> GRAMS
                
                // Các củ khác (nếu chưa được xử lý ở trên)
                name.contains("củ") -> BULBS
                
                // Mặc định
                else -> GRAMS
            }
        }
    }
}

