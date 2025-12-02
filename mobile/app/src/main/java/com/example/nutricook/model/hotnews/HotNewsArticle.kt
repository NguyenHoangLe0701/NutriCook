package com.example.nutricook.model.hotnews

import com.example.nutricook.model.user.User

data class HotNewsArticle(
    val id: String = "",
    val title: String = "",
    val content: String = "", // Nội dung bài đăng
    val category: String = "",
    val author: User = User(),
    val thumbnailUrl: String? = null,
    val link: String? = null, // Optional link
    val createdAt: Long = System.currentTimeMillis(),
    // Thêm các trường tương tác giống Post
    val likes: List<String> = emptyList(),
    val saves: List<String> = emptyList(),
    val commentCount: Int = 0
) {
    // Helper để format date theo định dạng tiếng Việt (dd/MM/yyyy)
    fun getFormattedDate(): String {
        val date = java.util.Date(createdAt)
        val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
        return sdf.format(date)
    }
    
    // Helper để dịch category từ tiếng Anh sang tiếng Việt
    fun getTranslatedCategory(): String {
        return when (category.trim()) {
            "Food News and Trends", "Food News And Trends" -> "Tin tức và xu hướng ẩm thực"
            "Kitchen Tips" -> "Mẹo bếp núc"
            "Healthy Eating" -> "Ăn uống lành mạnh"
            "Recipes" -> "Công thức nấu ăn"
            "Nutrition" -> "Dinh dưỡng"
            "Cooking Techniques" -> "Kỹ thuật nấu ăn"
            "Food Safety" -> "An toàn thực phẩm"
            "Meal Planning" -> "Lập kế hoạch bữa ăn"
            else -> category // Nếu không khớp, trả về category gốc
        }
    }
    
    // Helper properties cho UI
    val likeCount: Int
        get() = likes.size
    
    // Hàm kiểm tra xem user hiện tại có lưu bài này không
    fun isSaved(currentUid: String?): Boolean {
        return currentUid != null && saves.contains(currentUid)
    }
}

