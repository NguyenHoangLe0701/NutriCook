// chart.js
document.addEventListener('DOMContentLoaded', () => {
  const ctx = document.getElementById('categoryChart');
  if (!ctx) return;

  new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['Rau củ', 'Trái cây', 'Hải sản', 'Thịt'],
      datasets: [{
        data: [35, 25, 20, 20],
        backgroundColor: ['#10B981', '#F59E0B', '#EF4444', '#3B82F6'],
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