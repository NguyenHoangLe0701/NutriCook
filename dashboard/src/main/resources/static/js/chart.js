// chart.js - Phân bố hệ thống
document.addEventListener('DOMContentLoaded', () => {
  const ctx = document.getElementById('categoryChart');
  if (!ctx) return;

  // Lấy dữ liệu từ window.dashboardData (set bởi Thymeleaf)
  const data = window.dashboardData || {};
  const userCount = parseInt(data.userCount || 0);
  const foodCount = parseInt(data.foodCount || 0);
  const categoryCount = parseInt(data.categoryCount || 0);
  const recipeCount = parseInt(data.recipeCount || 0);

  // Tính tổng để chuẩn hóa thành phần trăm
  const total = userCount + foodCount + categoryCount + recipeCount;
  
  let userPercent = 0, foodPercent = 0, categoryPercent = 0, recipePercent = 0;
  
  if (total > 0) {
    userPercent = Math.round((userCount / total) * 100);
    foodPercent = Math.round((foodCount / total) * 100);
    categoryPercent = Math.round((categoryCount / total) * 100);
    recipePercent = Math.round((recipeCount / total) * 100);
  }

  // Cập nhật hiển thị phần trăm
  const userPercentEl = document.getElementById('userPercent');
  const foodPercentEl = document.getElementById('foodPercent');
  const categoryPercentEl = document.getElementById('categoryPercent');
  const recipePercentEl = document.getElementById('recipePercent');
  
  if (userPercentEl) userPercentEl.textContent = userPercent + '%';
  if (foodPercentEl) foodPercentEl.textContent = foodPercent + '%';
  if (categoryPercentEl) categoryPercentEl.textContent = categoryPercent + '%';
  if (recipePercentEl) recipePercentEl.textContent = recipePercent + '%';

  // Tạo chart với dữ liệu thực tế
  new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['Tổng người dùng', 'Số nguyên liệu', 'Danh mục', 'Các món ăn'],
      datasets: [{
        data: [userCount, foodCount, categoryCount, recipeCount],
        backgroundColor: ['#3B82F6', '#10B981', '#F59E0B', '#A855F7'],
        borderWidth: 4,
        borderColor: '#fff',
        hoverOffset: 8
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'bottom', labels: { padding: 20 } }
      }
    }
  });
});