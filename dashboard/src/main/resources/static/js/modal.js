// modal.js
function openModal(id) {
  document.getElementById(id).classList.add('active');
}

function closeModal(id) {
  document.getElementById(id).classList.remove('active');
}

// Đóng modal khi click ngoài
document.addEventListener('click', (e) => {
  if (e.target.classList.contains('modal')) {
    e.target.classList.remove('active');
  }
});


/**
 * Hiển thị ảnh xem trước khi người dùng chọn file
 * @param {Event} event - Sự kiện 'change' từ input file
 * @param {string} previewId - ID của thẻ <img> để hiển thị ảnh
 */
function previewImage(event, previewId) {
  const input = event.target;
  const preview = document.getElementById(previewId);
  
  if (input.files && input.files[0]) {
    const reader = new FileReader();
    
    reader.onload = function(e) {
      preview.src = e.target.result;
    };
    
    reader.readAsDataURL(input.files[0]);
  } else {
    // Đặt lại ảnh placeholder nếu không chọn file
    preview.src = '/img/food-placeholder.png'; 
  }
}