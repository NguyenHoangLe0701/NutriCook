// app.js - Enhanced UI helpers for NutriCook Admin Dashboard
document.addEventListener('DOMContentLoaded', function () {
  // Initialize all components
  initSidebar();
  initDarkMode();
  initAnimations();
  initTooltips();
  initKeyboardShortcuts();
});

// Sidebar Management
function initSidebar() {
  const sidebarToggle = document.getElementById('sidebarToggle');
  const body = document.body;

  if (sidebarToggle) {
    sidebarToggle.addEventListener('click', function () {
      toggleSidebar();
    });
  }

  // Apply stored preference
  try {
    const saved = localStorage.getItem('sidebarCollapsed');
    if (saved === 'true') body.classList.add('sidebar-collapsed');
  } catch (e) {}

  // Close sidebar on Escape
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      const sidebar = document.querySelector('.sidebar');
      if (sidebar && sidebar.classList.contains('active')) {
        sidebar.classList.remove('active');
      }
    }
  });

  // Close mobile sidebar when clicking outside
  document.addEventListener('click', (e) => {
    const sidebar = document.querySelector('.sidebar');
    const toggle = document.getElementById('sidebarToggle');
    if (!sidebar) return;
    if (window.innerWidth <= 768) {
      if (!sidebar.contains(e.target) && toggle && !toggle.contains(e.target)) {
        sidebar.classList.remove('active');
      }
    }
  });
}

function toggleSidebar() {
  const sidebar = document.querySelector('.sidebar');
  const body = document.body;

  if (!sidebar) return;

  // On small screens use `active` to slide in; on large screens toggle collapsed
  if (window.innerWidth <= 768) {
    sidebar.classList.toggle('active');
  } else {
    body.classList.toggle('sidebar-collapsed');
    // Store preference in localStorage
    try {
      localStorage.setItem('sidebarCollapsed', body.classList.contains('sidebar-collapsed'));
    } catch (e) {}
  }
}

// Dark Mode Management
function initDarkMode() {
  const themeToggle = document.getElementById('themeToggle');
  if (!themeToggle) return;

  // Check for saved theme preference or default to light mode
  const savedTheme = localStorage.getItem('theme') || 'light';
  setTheme(savedTheme);

  themeToggle.addEventListener('click', () => {
    const currentTheme = document.documentElement.getAttribute('data-theme');
    const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
    setTheme(newTheme);
  });
}

function setTheme(theme) {
  document.documentElement.setAttribute('data-theme', theme);
  localStorage.setItem('theme', theme);

  const themeToggle = document.getElementById('themeToggle');
  if (themeToggle) {
    themeToggle.classList.toggle('dark', theme === 'dark');
  }

  // Update Chart.js theme if chart exists
  updateChartTheme(theme);
}

function updateChartTheme(theme) {
  const charts = Chart.instances;
  charts.forEach(chart => {
    const isDark = theme === 'dark';
    chart.options.plugins.legend.labels.color = isDark ? '#94a3b8' : '#64748b';
    chart.options.scales.x.ticks.color = isDark ? '#94a3b8' : '#64748b';
    chart.options.scales.y.ticks.color = isDark ? '#94a3b8' : '#64748b';
    chart.update();
  });
}

// Animation Management
function initAnimations() {
  // Add intersection observer for scroll animations
  const observerOptions = {
    threshold: 0.1,
    rootMargin: '0px 0px -50px 0px'
  };

  const observer = new IntersectionObserver((entries) => {
    entries.forEach(entry => {
      if (entry.isIntersecting) {
        entry.target.classList.add('animate-in');
      }
    });
  }, observerOptions);

  // Observe elements with animation classes
  document.querySelectorAll('.animate-on-scroll').forEach(el => {
    observer.observe(el);
  });

  // Add loading states
  addLoadingStates();
}

function addLoadingStates() {
  // Add skeleton loading to cards
  const cards = document.querySelectorAll('.stat-card');
  cards.forEach(card => {
    card.classList.add('loaded');
  });
}

// Tooltip Management
function initTooltips() {
  const tooltipElements = document.querySelectorAll('[data-tooltip]');

  tooltipElements.forEach(element => {
    element.addEventListener('mouseenter', showTooltip);
    element.addEventListener('mouseleave', hideTooltip);
  });
}

function showTooltip(e) {
  const tooltip = document.createElement('div');
  tooltip.className = 'tooltip';
  tooltip.textContent = e.target.getAttribute('data-tooltip');

  document.body.appendChild(tooltip);

  const rect = e.target.getBoundingClientRect();
  tooltip.style.left = rect.left + (rect.width / 2) - (tooltip.offsetWidth / 2) + 'px';
  tooltip.style.top = rect.top - tooltip.offsetHeight - 5 + 'px';

  setTimeout(() => tooltip.classList.add('visible'), 10);
}

function hideTooltip() {
  const tooltip = document.querySelector('.tooltip');
  if (tooltip) {
    tooltip.classList.remove('visible');
    setTimeout(() => tooltip.remove(), 200);
  }
}

// Keyboard Shortcuts
function initKeyboardShortcuts() {
  document.addEventListener('keydown', (e) => {
    // Ctrl/Cmd + K: Focus search
    if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
      e.preventDefault();
      const searchInput = document.querySelector('input[type="search"], input[placeholder*="Tìm"]');
      if (searchInput) {
        searchInput.focus();
      }
    }

    // Ctrl/Cmd + B: Toggle sidebar
    if ((e.ctrlKey || e.metaKey) && e.key === 'b') {
      e.preventDefault();
      toggleSidebar();
    }

    // Ctrl/Cmd + Shift + D: Toggle dark mode
    if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key === 'D') {
      e.preventDefault();
      const themeToggle = document.getElementById('themeToggle');
      if (themeToggle) {
        themeToggle.click();
      }
    }
  });
}

// Utility Functions
function showNotification(message, type = 'info') {
  const notification = document.createElement('div');
  notification.className = `notification notification-${type}`;
  notification.innerHTML = `
    <div class="notification-content">
      <i class="fa-solid ${getNotificationIcon(type)}"></i>
      <span>${message}</span>
    </div>
    <button class="notification-close" onclick="this.parentElement.remove()">
      <i class="fa-solid fa-times"></i>
    </button>
  `;

  document.body.appendChild(notification);

  // Auto remove after 5 seconds
  setTimeout(() => {
    if (notification.parentElement) {
      notification.remove();
    }
  }, 5000);
}

function getNotificationIcon(type) {
  const icons = {
    success: 'fa-check-circle',
    error: 'fa-exclamation-circle',
    warning: 'fa-exclamation-triangle',
    info: 'fa-info-circle'
  };
  return icons[type] || icons.info;
}

// Performance monitoring
function initPerformanceMonitoring() {
  if ('performance' in window && 'PerformanceObserver' in window) {
    // Monitor Largest Contentful Paint
    const lcpObserver = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const lastEntry = entries[entries.length - 1];
      console.log('LCP:', lastEntry.startTime);
    });
    lcpObserver.observe({ entryTypes: ['largest-contentful-paint'] });

    // Monitor First Input Delay
    const fidObserver = new PerformanceObserver((list) => {
      const entries = list.getEntries();
      entries.forEach((entry) => {
        console.log('FID:', entry.processingStart - entry.startTime);
      });
    });
    fidObserver.observe({ entryTypes: ['first-input'] });
  }
}

// Initialize performance monitoring
initPerformanceMonitoring();

// Export functions for global use
window.NutriCookAdmin = {
  toggleSidebar,
  showNotification,
  setTheme
};
