package com.example.nutricook.view.recipes

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nutricook.utils.VitaminDetails

@Composable
fun VitaminDetailsDialog(
    vitaminDetails: VitaminDetails,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        color = Color(0xFFF9FAFB),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Chi tiết các loại vitamin (% DV)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E),
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Danh sách các loại vitamin
            val vitamins = listOf(
                "Vitamin A" to vitaminDetails.vitaminA,
                "Vitamin B1 (Thiamin)" to vitaminDetails.vitaminB1,
                "Vitamin B2 (Riboflavin)" to vitaminDetails.vitaminB2,
                "Vitamin B3 (Niacin)" to vitaminDetails.vitaminB3,
                "Vitamin B6" to vitaminDetails.vitaminB6,
                "Vitamin B9 (Folate)" to vitaminDetails.vitaminB9,
                "Vitamin B12" to vitaminDetails.vitaminB12,
                "Vitamin C" to vitaminDetails.vitaminC,
                "Vitamin D" to vitaminDetails.vitaminD,
                "Vitamin E" to vitaminDetails.vitaminE,
                "Vitamin K" to vitaminDetails.vitaminK
            )
            
            vitamins.forEach { (name, value) ->
                if (value > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            fontSize = 13.sp,
                            color = Color(0xFF4B5563)
                        )
                        Surface(
                            color = Color(0xFF00BFA5).copy(alpha = 0.1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${String.format("%.1f", value)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF00BFA5),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            // Hiển thị thông báo nếu không có vitamin nào
            if (vitamins.all { it.second == 0.0 }) {
                Text(
                    text = "Không có thông tin chi tiết về vitamin",
                    fontSize = 12.sp,
                    color = Color(0xFF9CA3AF),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

