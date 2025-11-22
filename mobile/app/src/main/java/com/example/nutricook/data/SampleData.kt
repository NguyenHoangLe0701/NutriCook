package com.example.nutricook.data

import androidx.compose.ui.graphics.Color
import com.example.nutricook.R
import com.example.nutricook.view.recipes.ReviewItem
import com.example.nutricook.view.recipes.TodayRecipe
import com.example.nutricook.view.recipes.RecipeCategory
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant

object SampleData {

    val reviews = listOf(
        ReviewItem(
            userName = "Allrecipes Member",
            date = "07/09/2022",
            text = "I would recommend adding the vegetables in until the last three hours of cooking time. That way they won’t turn to mush.",
            liked = true,
            rating = 4,
            likes = 10
        ),
        ReviewItem(
            userName = "seany42o1",
            date = "06/12/2022",
            text = "The corned beef needs to be on low and then 9 hours works. Great recipe, I made it without the beer.",
            liked = false,
            rating = 4,
            likes = 5
        ),
        ReviewItem(
            userName = "vicki936",
            date = "03/22/2022",
            text = "I bought a corned beef brisket at Costco and didn’t want to toil with stove top directions so I found this crock pot recipe.",
            liked = false,
            rating = 4,
            likes = 5
        ),
        ReviewItem(
            userName = "John Shifflett",
            date = "04/02/2022",
            text = "Perfect recipe, made this numerous times and it always comes out delicious.",
            liked = false,
            rating = 4,
            likes = 5
        )
    )

    val categories = listOf(
        RecipeCategory(
            "Vietnamese Food",
            Color(0xFFD6F4E0),
            "Vegetarian Pho (Vietnamese Noodle Soup)",
            R.drawable.pho,
            3,
            5
        ),
        RecipeCategory(
            "Salmon Recipes",
            Color(0xFFFFEBD2),
            "7 Sheet Pan Salmon Recipes for Busy Weeknights",
            R.drawable.supcahoi,
            2,
            3
        ),
        RecipeCategory(
            "Chicken Recipes",
            Color(0xFFE6F1FF),
            "25 Pineapple Chicken Recipes for Sweet and Savory Dinners",
            R.drawable.ga,
            4,
            21
        )
    )

    val todayRecipes = listOf(
        TodayRecipe(
            name = "Slow-Cooker Corned Beef and Cabbage",
            description = "Cook this in your slow cooker all day and enjoy the tenderness!",
            rating = 4.5,
            imageRes = R.drawable.beefandcabbage,
            imageUrl = null,
            reviews = 1250,
            userName = null,
            createdAt = null
        ),
        TodayRecipe(
            name = "One-Pan White Cheddar Mac and Cheese",
            description = "If you can make boxed macaroni and cheese, you can make this!",
            rating = 4.5,
            imageRes = R.drawable.macandcheese,
            imageUrl = null,
            reviews = 980,
            userName = null,
            createdAt = null
        ),
        TodayRecipe(
            name = "Simple Macaroni and Cheese",
            description = "A super satisfying, quick and easy dinner.",
            rating = 3.4,
            imageRes = R.drawable.macaroniandcheese,
            imageUrl = null,
            reviews = 850,
            userName = null,
            createdAt = null
        ),
        TodayRecipe(
            name = "Marie's Easy Slow Cooker Pot Roast",
            description = "Moist and juicy pot roast with carrots, onion and potatoes.",
            rating = 4.6,
            imageRes = R.drawable.potroast,
            imageUrl = null,
            reviews = 2382,
            userName = null,
            createdAt = null
        )
    )

    // Food items by category from CategoriesScreen
    val vegetables = listOf(
        mapOf("name" to "Cà rốt", "calories" to "52 kcal"),
        mapOf("name" to "Khoai tây", "calories" to "104 kcal"),
        mapOf("name" to "Nghệ (bột)", "calories" to "354 kcal"),
        mapOf("name" to "Cà tím", "calories" to "24 kcal"),
        mapOf("name" to "Cà chua", "calories" to "18 kcal"),
        mapOf("name" to "Củ dền", "calories" to "26 kcal"),
        mapOf("name" to "Ngô", "calories" to "132 kcal"),
        mapOf("name" to "Ớt chuông", "calories" to "26 kcal"),
        mapOf("name" to "Hành tây", "calories" to "42 kcal")
    )

    val fruits = listOf(
        mapOf("name" to "Chuối", "calories" to "89 kcal"),
        mapOf("name" to "Bơ", "calories" to "130 kcal"),
        mapOf("name" to "Sung", "calories" to "74 kcal"),
        mapOf("name" to "Anh đào ngọt", "calories" to "63 kcal"),
        mapOf("name" to "Dừa", "calories" to "354 kcal"),
        mapOf("name" to "Nho", "calories" to "69 kcal"),
        mapOf("name" to "Sầu riêng", "calories" to "885 kcal"),
        mapOf("name" to "Thơm", "calories" to "48 kcal"),
        mapOf("name" to "Cam", "calories" to "47 kcal")
    )

    val fishAndSeafood = listOf(
        mapOf("name" to "Tôm", "calories" to "99 kcal"),
        mapOf("name" to "Cá hồi", "calories" to "208 kcal"),
        mapOf("name" to "Cua", "calories" to "97 kcal"),
        mapOf("name" to "Mực", "calories" to "92 kcal"),
        mapOf("name" to "Cá ngừ", "calories" to "132 kcal"),
        mapOf("name" to "Cá thu", "calories" to "205 kcal"),
        mapOf("name" to "Tôm hùm", "calories" to "89 kcal"),
        mapOf("name" to "Sò điệp", "calories" to "88 kcal"),
        mapOf("name" to "Cá tuyết", "calories" to "82 kcal"),
        mapOf("name" to "Hàu", "calories" to "68 kcal")
    )

    val meat = listOf(
        mapOf("name" to "Hàu", "calories" to "68 kcal")
    )

    // Ingredients by letter for IngredientBrowserScreen
    val allIngredients = mapOf(
        'A' to listOf("Apple (Táo)", "Avocado (Bơ)", "Artichoke (Atiso)"),
        'B' to listOf("Bánh mì", "Bánh bao", "Bánh quy", "Bánh ngọt", "Bắp", "Bí đỏ"),
        'C' to listOf("Cà chua", "Cà rốt", "Cá hồi", "Cải bó xôi", "Cơm", "Chanh"),
        'D' to listOf("Dưa leo", "Dưa hấu", "Dâu tây")
    )

    val mealTypes = listOf(
        "Khai vị", "Bữa sáng", "Món tráng miệng", "Bữa trưa",
        "Món chính", "Salad", "Món phụ", "Ăn nhẹ",
        "Súp", "Đồ uống", "Bánh ngọt", "Nước sốt & gia vị"
    )

    val dietTypes = listOf(
        "Không sữa", "Giàu đạm thực vật", "Không gluten",
        "Giàu chất xơ", "Ít calo", "Ít tinh bột"
    )

    // Cooking tips for RecipeGuidanceScreen
    val cookingTips = listOf(
        mapOf(
            "title" to "Cách nấu cơm ngon",
            "description" to "Tỷ lệ nước và gạo 1:1.5, để lửa nhỏ 15 phút",
            "category" to "Cơm"
        ),
        mapOf(
            "title" to "Cách luộc trứng hoàn hảo",
            "description" to "Nước sôi, thả trứng, đun 6-7 phút cho lòng đào",
            "category" to "Trứng"
        ),
        mapOf(
            "title" to "Cách ướp thịt mềm",
            "description" to "Dùng nước dứa hoặc giấm táo ướp 30 phút",
            "category" to "Thịt"
        ),
        mapOf(
            "title" to "Cách làm nước dùng trong",
            "description" to "Đun sôi, vớt bọt, thêm hành tây và gừng",
            "category" to "Nước dùng"
        )
    )

    // Calorie info for RecipeGuidanceScreen
    val calorieInfo = listOf(
        mapOf(
            "foodName" to "Thơm (Dứa)",
            "calories" to 48,
            "benefits" to listOf("Giàu vitamin C", "Hỗ trợ tiêu hóa", "Chống viêm", "Tăng cường miễn dịch")
        ),
        mapOf(
            "foodName" to "Chuối",
            "calories" to 89,
            "benefits" to listOf("Giàu kali", "Cung cấp năng lượng", "Tốt cho tim mạch", "Hỗ trợ cơ bắp")
        ),
        mapOf(
            "foodName" to "Cà rốt",
            "calories" to 52,
            "benefits" to listOf("Giàu beta-carotene", "Tốt cho mắt", "Chống oxy hóa", "Hỗ trợ da khỏe")
        )
    )
}
