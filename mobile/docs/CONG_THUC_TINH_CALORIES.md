# 📊 CÔNG THỨC TÍNH CALORIES VÀ DINH DƯỠNG - NUTRICOOK

## 📋 MỤC LỤC

1. [Công thức tính Calories từ Macronutrients](#1-công-thức-tính-calories-từ-macronutrients)
2. [Công thức tính Dinh dưỡng từ Nguyên liệu](#2-công-thức-tính-dinh-dưỡng-từ-nguyên-liệu)
3. [Công thức chuyển đổi Đơn vị](#3-công-thức-chuyển-đổi-đơn-vị)
4. [Công thức tính % Daily Value](#4-công-thức-tính-daily-value)
5. [Công thức tính Calories cho Món ăn](#5-công-thức-tính-calories-cho-món-ăn)
6. [Công thức tính Calories theo Khẩu phần](#6-công-thức-tính-calories-theo-khẩu-phần)
7. [Công thức tính Calories từ Gemini AI](#7-công-thức-tính-calories-từ-gemini-ai)
8. [Công thức tính Calories đốt cháy (Exercise)](#8-công-thức-tính-calories-đốt-cháy-exercise)
9. [Công thức tính BMR và TDEE](#9-công-thức-tính-bmr-và-tdee)
10. [Ví dụ Tính toán Thực tế](#10-ví-dụ-tính-toán-thực-tế)

---

## 1. CÔNG THỨC TÍNH CALORIES TỪ MACRONUTRIENTS

### 1.1. Công thức Cơ bản

Calories được tính từ 3 macronutrients chính:

```
Calories = (Protein × 4) + (Carbohydrates × 4) + (Fat × 9)
```

**Trong đó:**
- **Protein**: 4 calories/gram
- **Carbohydrates**: 4 calories/gram  
- **Fat**: 9 calories/gram

### 1.2. Công thức Chi tiết

```
Total Calories = (Protein_g × 4) + (Carbs_g × 4) + (Fat_g × 9) + (Alcohol_g × 7)
```

**Ví dụ:**
- Protein: 20g → 20 × 4 = 80 calories
- Carbs: 30g → 30 × 4 = 120 calories
- Fat: 10g → 10 × 9 = 90 calories
- **Tổng**: 80 + 120 + 90 = **290 calories**

### 1.3. Công thức Tính Calories từ Thực phẩm

Khi có thông tin dinh dưỡng trên 100g:

```
Calories_thực_tế = (Calories_trên_100g / 100) × Số_lượng_gram
```

**Ví dụ:**
- Gạo: 130 calories/100g
- Sử dụng: 200g
- **Calories = (130 / 100) × 200 = 260 calories**

---

## 2. CÔNG THỨC TÍNH DINH DƯỠNG TỪ NGUYÊN LIỆU

### 2.1. Công thức Tổng quát

Tính dinh dưỡng từ danh sách nguyên liệu:

```
Dinh_dưỡng_tổng = Σ(Dinh_dưỡng_nguyên_liệu_i)
```

### 2.2. Công thức Chi tiết cho từng Nguyên liệu

**Bước 1: Parse số lượng từ string**
```
quantity = parseQuantity(quantityStr)
```
- Hỗ trợ: "2", "1.5", "1/2", "1 1/2", "200"

**Bước 2: Chuyển đổi đơn vị sang gram**
```
quantityInGrams = unit.toGrams(quantityInUnits)
```

**Bước 3: Tính multiplier**
```
multiplier = quantityInGrams / 100.0
```

**Bước 4: Tính dinh dưỡng**
```
Calories = (Calories_per_100g × multiplier)
Protein = (Protein_per_100g × multiplier)
Fat = (Fat_per_100g × multiplier)
Carbs = (Carbs_per_100g × multiplier)
Cholesterol = (Cholesterol_per_100g × multiplier)
Sodium = (Sodium_per_100g × multiplier)
```

### 2.3. Công thức Tổng hợp cho Tất cả Nguyên liệu

```
Total_Calories = Σ(Calories_i)
Total_Protein = Σ(Protein_i)
Total_Fat = Σ(Fat_i)
Total_Carbs = Σ(Carbs_i)
Total_Cholesterol = Σ(Cholesterol_i)
Total_Sodium = Σ(Sodium_i)
```

### 2.4. Công thức Chia theo Khẩu phần

```
Calories_per_serving = Total_Calories / servings
Protein_per_serving = Total_Protein / servings
Fat_per_serving = Total_Fat / servings
Carbs_per_serving = Total_Carbs / servings
```

**Ví dụ:**
- Tổng calories: 1200
- Số khẩu phần: 4
- **Calories/khẩu phần = 1200 / 4 = 300 calories**

---

## 3. CÔNG THỨC CHUYỂN ĐỔI ĐƠN VỊ

### 3.1. Chuyển đổi sang Gram

Các đơn vị được chuyển đổi sang gram như sau:

| Đơn vị | Chuyển đổi sang Gram |
|--------|---------------------|
| **Gram (g)** | 1g = 1g |
| **Kilogram (kg)** | 1kg = 1000g |
| **Milliliter (ml)** | 1ml = 1g (cho nước và chất lỏng) |
| **Liter (l)** | 1l = 1000ml = 1000g |
| **Quả trứng** | 1 quả = 60g |
| **Cốc** | 1 cốc = 240ml = 240g |
| **Thìa canh** | 1 thìa = 15ml = 15g |
| **Thìa cà phê** | 1 thìa = 5ml = 5g |
| **Lát** | 1 lát = 25g (tùy loại) |
| **Tép** | 1 tép = 5g (tỏi) |

### 3.2. Công thức Chuyển đổi

```
quantityInGrams = quantityInUnits × conversionFactor
```

**Ví dụ:**
- 2 quả trứng = 2 × 60 = 120g
- 500ml nước = 500 × 1 = 500g
- 1.5kg thịt = 1.5 × 1000 = 1500g

---

## 4. CÔNG THỨC TÍNH % DAILY VALUE

### 4.1. Công thức Cơ bản

```
% Daily Value = (Giá_trị_thực_tế / Giá_trị_khuyến_nghị) × 100
```

### 4.2. Giá trị Khuyến nghị Hàng ngày (FDA)

| Dinh dưỡng | Giá trị Khuyến nghị |
|-----------|---------------------|
| **Calories** | 2000 kcal/ngày |
| **Fat** | 65g/ngày |
| **Carbohydrates** | 300g/ngày |
| **Protein** | 50g/ngày |
| **Cholesterol** | 300mg/ngày |
| **Sodium** | 2300mg/ngày |
| **Vitamin** | 100% DV |

### 4.3. Công thức Chi tiết

```
% Calories = (Calories / 2000) × 100
% Fat = (Fat_g / 65) × 100
% Carbs = (Carbs_g / 300) × 100
% Protein = (Protein_g / 50) × 100
% Cholesterol = (Cholesterol_mg / 300) × 100
% Sodium = (Sodium_mg / 2300) × 100
```

**Lưu ý**: % Daily Value được giới hạn tối đa 100%

```
% Daily Value = min((Giá_trị / Khuyến_nghị) × 100, 100)
```

**Ví dụ:**
- Calories: 500 kcal
- **% Daily Value = (500 / 2000) × 100 = 25%**

---

## 5. CÔNG THỨC TÍNH CALORIES CHO MÓN ĂN

### 5.1. Công thức Tổng quát

```
Calories_món_ăn = Σ(Calories_nguyên_liệu_i)
```

### 5.2. Công thức Chi tiết

**Bước 1: Tính calories cho từng nguyên liệu**
```
Calories_i = (Calories_per_100g_i / 100) × quantityInGrams_i
```

**Bước 2: Tổng hợp tất cả nguyên liệu**
```
Total_Calories = Σ(Calories_i)
```

**Bước 3: Chia theo khẩu phần (nếu có)**
```
Calories_per_serving = Total_Calories / servings
```

### 5.3. Ví dụ Tính toán

**Món: Cơm trắng với thịt gà**

**Nguyên liệu:**
1. Gạo: 200g (130 calories/100g)
   - Calories = (130 / 100) × 200 = 260 kcal

2. Thịt gà: 150g (165 calories/100g)
   - Calories = (165 / 100) × 150 = 247.5 kcal

3. Dầu ăn: 10g (900 calories/100g)
   - Calories = (900 / 100) × 10 = 90 kcal

**Tổng calories:**
```
Total = 260 + 247.5 + 90 = 597.5 kcal
```

**Nếu chia 2 phần:**
```
Calories_per_serving = 597.5 / 2 = 298.75 kcal
```

---

## 6. CÔNG THỨC TÍNH CALORIES THEO KHẨU PHẦN

### 6.1. Công thức Cơ bản

```
Calories_khẩu_phần = Calories_tổng / số_khẩu_phần
```

### 6.2. Công thức Tính lại khi Thay đổi Khẩu phần

```
Calories_mới = Calories_cũ × (số_khẩu_phần_mới / số_khẩu_phần_cũ)
```

**Ví dụ:**
- Calories ban đầu: 600 kcal (cho 2 phần)
- Muốn tính cho 3 phần:
  - **Calories_mới = 600 × (3 / 2) = 900 kcal**

### 6.3. Công thức Tính Calories cho Số lượng Cụ thể

```
Calories_thực_tế = Calories_per_100g × (số_lượng_gram / 100)
```

**Ví dụ:**
- Calories/100g: 150 kcal
- Sử dụng: 250g
- **Calories = 150 × (250 / 100) = 375 kcal**

---

## 7. CÔNG THỨC TÍNH CALORIES TỪ GEMINI AI

### 7.1. Quy trình Tính toán

Gemini AI sử dụng prompt engineering để tính calories từ tên món ăn:

**Input:**
```
Tên món ăn: "Cá ngừ 200gr"
```

**Prompt gửi đến Gemini:**
```
"Tính calories và dinh dưỡng cho: [tên món ăn]
Trả về JSON format:
{
  'calories': số_calories,
  'protein': số_gram,
  'fat': số_gram,
  'carb': số_gram
}"
```

**Output từ Gemini:**
```json
{
  "calories": 200,
  "protein": 40.0,
  "fat": 5.0,
  "carb": 0.0
}
```

### 7.2. Công thức Parse Kết quả

```
Calories = parseFloat(gemini_response.calories)
Protein = parseFloat(gemini_response.protein)
Fat = parseFloat(gemini_response.fat)
Carb = parseFloat(gemini_response.carb)
```

### 7.3. Validation

```
if (Calories > 0 && Calories <= 10000) {
    // Hợp lệ
} else {
    // Lỗi: Giá trị không hợp lệ
}
```

---

## 8. CÔNG THỨC TÍNH CALORIES ĐỐT CHÁY (EXERCISE)

### 8.1. Công thức Cơ bản

```
Calories_đốt_cháy = MET × weight_kg × duration_hours
```

**Trong đó:**
- **MET** (Metabolic Equivalent of Task): Hệ số chuyển hóa
- **weight_kg**: Cân nặng (kg)
- **duration_hours**: Thời gian tập (giờ)

### 8.2. Công thức Chi tiết

```
Calories_đốt_cháy = (MET × 3.5 × weight_kg) / 200 × duration_minutes
```

**Hoặc đơn giản hóa:**
```
Calories_đốt_cháy = MET × weight_kg × (duration_minutes / 60)
```

### 8.3. Bảng MET cho các Hoạt động

| Hoạt động | MET |
|----------|-----|
| Đi bộ chậm (3 km/h) | 2.0 |
| Đi bộ nhanh (5 km/h) | 3.5 |
| Chạy bộ (8 km/h) | 8.0 |
| Chạy nhanh (10 km/h) | 10.0 |
| Đạp xe (16 km/h) | 6.0 |
| Bơi lội | 6.0 |
| Yoga | 2.5 |
| Gym (nặng) | 6.0 |

### 8.4. Ví dụ Tính toán

**Thông tin:**
- Cân nặng: 70kg
- Hoạt động: Chạy bộ (MET = 8.0)
- Thời gian: 30 phút = 0.5 giờ

**Tính toán:**
```
Calories = 8.0 × 70 × 0.5 = 280 calories
```

---

## 9. CÔNG THỨC TÍNH BMR VÀ TDEE

### 9.1. BMR (Basal Metabolic Rate) - Tỷ lệ Chuyển hóa Cơ bản

#### 9.1.1. Công thức Mifflin-St Jeor (Chính xác nhất)

**Cho Nam:**
```
BMR = (10 × weight_kg) + (6.25 × height_cm) - (5 × age_years) + 5
```

**Cho Nữ:**
```
BMR = (10 × weight_kg) + (6.25 × height_cm) - (5 × age_years) - 161
```

#### 9.1.2. Công thức Harris-Benedict (Cũ hơn)

**Cho Nam:**
```
BMR = 88.362 + (13.397 × weight_kg) + (4.799 × height_cm) - (5.677 × age_years)
```

**Cho Nữ:**
```
BMR = 447.593 + (9.247 × weight_kg) + (3.098 × height_cm) - (4.330 × age_years)
```

### 9.2. TDEE (Total Daily Energy Expenditure) - Tổng Năng lượng Tiêu thụ Hàng ngày

```
TDEE = BMR × Activity_Factor
```

### 9.3. Activity Factor (Hệ số Hoạt động)

| Mức độ Hoạt động | Activity Factor | Mô tả |
|----------------|----------------|-------|
| **Sedentary** | 1.2 | Ít vận động, làm việc văn phòng |
| **Lightly Active** | 1.375 | Tập thể dục nhẹ 1-3 lần/tuần |
| **Moderately Active** | 1.55 | Tập thể dục vừa 3-5 lần/tuần |
| **Very Active** | 1.725 | Tập thể dục nặng 6-7 lần/tuần |
| **Extra Active** | 1.9 | Tập thể dục rất nặng, lao động chân tay |

### 9.4. Ví dụ Tính toán

**Thông tin:**
- Giới tính: Nam
- Cân nặng: 75kg
- Chiều cao: 175cm
- Tuổi: 30
- Mức độ hoạt động: Moderately Active (1.55)

**Tính BMR:**
```
BMR = (10 × 75) + (6.25 × 175) - (5 × 30) + 5
BMR = 750 + 1093.75 - 150 + 5
BMR = 1698.75 kcal/ngày
```

**Tính TDEE:**
```
TDEE = 1698.75 × 1.55 = 2633.06 kcal/ngày
```

**Calories Target:**
- Để giảm cân: TDEE - 500 = 2133 kcal/ngày
- Để tăng cân: TDEE + 500 = 3133 kcal/ngày
- Để duy trì: TDEE = 2633 kcal/ngày

---

## 10. VÍ DỤ TÍNH TOÁN THỰC TẾ

### 10.1. Ví dụ 1: Tính Calories cho Món Phở Bò

**Nguyên liệu:**
1. Bánh phở: 200g (110 calories/100g)
   - Calories = (110 / 100) × 200 = 220 kcal

2. Thịt bò: 100g (250 calories/100g)
   - Calories = (250 / 100) × 100 = 250 kcal

3. Nước dùng: 500ml (20 calories/100ml)
   - Calories = (20 / 100) × 500 = 100 kcal

4. Hành, rau thơm: 50g (30 calories/100g)
   - Calories = (30 / 100) × 50 = 15 kcal

**Tổng:**
```
Total Calories = 220 + 250 + 100 + 15 = 585 kcal
```

### 10.2. Ví dụ 2: Tính Calories cho Công thức Nấu ăn

**Công thức: Gà Kho Gừng (4 phần)**

**Nguyên liệu:**
1. Thịt gà: 500g (165 calories/100g)
   - Calories = (165 / 100) × 500 = 825 kcal

2. Gừng: 20g (80 calories/100g)
   - Calories = (80 / 100) × 20 = 16 kcal

3. Nước mắm: 30ml (20 calories/100ml)
   - Calories = (20 / 100) × 30 = 6 kcal

4. Đường: 20g (387 calories/100g)
   - Calories = (387 / 100) × 20 = 77.4 kcal

5. Dầu ăn: 15g (900 calories/100g)
   - Calories = (900 / 100) × 15 = 135 kcal

**Tổng:**
```
Total Calories = 825 + 16 + 6 + 77.4 + 135 = 1059.4 kcal
```

**Calories/khẩu phần:**
```
Calories_per_serving = 1059.4 / 4 = 264.85 kcal
```

### 10.3. Ví dụ 3: Tính % Daily Value

**Thông tin:**
- Calories: 500 kcal
- Protein: 25g
- Fat: 20g
- Carbs: 60g

**Tính % Daily Value:**
```
% Calories = (500 / 2000) × 100 = 25%
% Protein = (25 / 50) × 100 = 50%
% Fat = (20 / 65) × 100 = 30.77%
% Carbs = (60 / 300) × 100 = 20%
```

### 10.4. Ví dụ 4: Tính Calories đốt cháy khi Tập thể dục

**Thông tin:**
- Cân nặng: 65kg
- Hoạt động: Chạy bộ (MET = 8.0)
- Thời gian: 45 phút = 0.75 giờ

**Tính toán:**
```
Calories = 8.0 × 65 × 0.75 = 390 calories
```

---

## 11. CÔNG THỨC TÍNH CALORIES NET (Calories Thuần)

### 11.1. Công thức Cơ bản

```
Calories_Net = Calories_nạp_vào - Calories_đốt_cháy
```

### 11.2. Công thức Chi tiết

```
Calories_Net = (Calories_ăn_sáng + Calories_ăn_trưa + Calories_ăn_tối + Calories_ăn_vặt) - (Calories_BMR + Calories_tập_thể_dục)
```

### 11.3. Ví dụ

**Calories nạp vào:**
- Sáng: 400 kcal
- Trưa: 600 kcal
- Tối: 500 kcal
- Vặt: 100 kcal
- **Tổng: 1600 kcal**

**Calories đốt cháy:**
- BMR: 1500 kcal
- Tập thể dục: 300 kcal
- **Tổng: 1800 kcal**

**Calories Net:**
```
Calories_Net = 1600 - 1800 = -200 kcal (Thâm hụt - Giảm cân)
```

---

## 12. CÔNG THỨC TÍNH MACRONUTRIENTS RATIO

### 12.1. Công thức Tính % từ Calories

```
% Protein = (Protein_g × 4 / Total_Calories) × 100
% Carbs = (Carbs_g × 4 / Total_Calories) × 100
% Fat = (Fat_g × 9 / Total_Calories) × 100
```

### 12.2. Ví dụ

**Thông tin:**
- Calories: 2000 kcal
- Protein: 150g (150 × 4 = 600 kcal)
- Carbs: 200g (200 × 4 = 800 kcal)
- Fat: 66.7g (66.7 × 9 = 600 kcal)

**Tính %:**
```
% Protein = (600 / 2000) × 100 = 30%
% Carbs = (800 / 2000) × 100 = 40%
% Fat = (600 / 2000) × 100 = 30%
```

---

## 13. LƯU Ý QUAN TRỌNG

### 13.1. Độ chính xác

- Các công thức trên là **ước tính** dựa trên giá trị trung bình
- Giá trị thực tế có thể khác tùy thuộc vào:
  - Cách chế biến (nấu, chiên, hấp)
  - Nguồn gốc thực phẩm
  - Độ tươi của nguyên liệu

### 13.2. Làm tròn

- Calories thường được làm tròn đến **số nguyên gần nhất**
- Macronutrients được làm tròn đến **1 chữ số thập phân**

### 13.3. Validation

- Calories: 0 - 10000 kcal
- Protein: 0 - 1000g
- Fat: 0 - 1000g
- Carbs: 0 - 2000g

### 13.4. Xử lý Lỗi

- Nếu thiếu thông tin: Sử dụng giá trị mặc định hoặc bỏ qua
- Nếu giá trị không hợp lệ: Hiển thị cảnh báo cho người dùng

---

## 14. TÀI LIỆU THAM KHẢO

1. **FDA Daily Values**: https://www.fda.gov/food/nutrition-facts-label/daily-value-nutrition-and-supplement-facts-labels
2. **Mifflin-St Jeor Equation**: https://www.ncbi.nlm.nih.gov/pubmed/15883556
3. **MET Values**: https://sites.google.com/site/compendiumofphysicalactivities/
4. **Atwater System**: Hệ thống tính calories từ macronutrients (4-4-9)

---

*Tài liệu này được tạo dựa trên codebase của dự án NutriCook*
*Cập nhật: 2025*

