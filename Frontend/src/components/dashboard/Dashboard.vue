<template>
  <div class="dashboard-wrapper">
    <div class="container-limit">
      <header class="dashboard-header">
        <div class="header-title">
            <h1>Tổng Quan Kho Hàng</h1>
            <p class="subtitle">Thống kê hoạt động xuất nhập tồn theo thời gian thực (Năm {{ currentYear }})</p>
        </div>
        <div class="header-filter">
            <select v-model="selectedKho" @change="handleFilterChange" class="top-chart-filter" :disabled="!isAdmin">
                <option :value="0" v-if="isAdmin">Tất cả kho</option>
                <option v-for="k in khoList" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
            </select>
        </div>
      </header>

      <div class="stats-grid">
        <div class="stat-card total-stock">
          <div class="card-icon"><i class="fas fa-boxes"></i> 📦</div>
          <div class="card-info">
            <h3>Tổng Tồn Kho</h3>
            <p class="stat-value">{{ formatNumber(stats.totalStock) }}</p>
            <span class="stat-trend positive">Sản phẩm đang có trong kho</span>
          </div>
        </div>

        <div class="stat-card import-stock">
          <div class="card-icon">📥</div>
          <div class="card-info">
            <h3>Tổng Nhập Trong Tháng</h3>
            <p class="stat-value">{{ formatNumber(stats.importMonth) }}</p>
            <span class="sub-text">Gồm nhập trong tháng {{ currentMonth }}</span>
          </div>
        </div>

        <div class="stat-card export-stock">
          <div class="card-icon">📤</div>
          <div class="card-info">
            <h3>Tổng Xuất Trong Tháng</h3>
            <p class="stat-value">{{ formatNumber(stats.exportMonth) }}</p>
            <span class="sub-text">Gồm xuất trong tháng {{ currentMonth }}</span>
          </div>
        </div>
      </div>

      <div class="chart-section">
        <div class="chart-header">
          <h2>Biểu Đồ Xuất Nhập Tổng Quát</h2>
        </div>
        
        <div class="chart-container">
          <Bar v-if="loaded" :data="chartData" :options="chartOptions" />
          <div v-else class="loading-chart">Đang tải dữ liệu biểu đồ...</div>
        </div>
      </div>
    </div>
  </div> 
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import api from '@/utils/axios';

// Import Chart.js
import {
  Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale
} from 'chart.js';
import { Bar } from 'vue-chartjs';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

// --- STATE ---
const currentYear = new Date().getFullYear();
const currentMonth = new Date().getMonth() + 1; // Thêm biến currentMonth để hiển thị trên UI
const selectedKho = ref(0);
const khoList = ref([]);
const isAdmin = ref(false);

const stats = reactive({ totalStock: 0, importMonth: 0, exportMonth: 0 });

// State cho Chart Tổng Quan
const loaded = ref(false);
const chartData = ref({
  labels: ['Thg 1', 'Thg 2', 'Thg 3', 'Thg 4', 'Thg 5', 'Thg 6', 'Thg 7', 'Thg 8', 'Thg 9', 'Thg 10', 'Thg 11', 'Thg 12'],
  datasets: [
    { label: 'Tổng Nhận', backgroundColor: '#3b82f6', data: [], borderRadius: 4, barPercentage: 0.6, categoryPercentage: 0.8 },
    { label: 'Tổng Đi', backgroundColor: '#9ca3af', data: [], borderRadius: 4, barPercentage: 0.6, categoryPercentage: 0.8 }
  ]
});

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: { position: 'top' }, tooltip: { mode: 'index', intersect: false } },
  scales: { y: { beginAtZero: true, grid: { color: '#f3f4f6' }, ticks: { precision: 0 } }, x: { grid: { display: false } } }
};

const formatNumber = (num) => {
  if(!num) return '0';
  return new Intl.NumberFormat('vi-VN').format(num);
};

// --- LOGIC ---

const setupPhanQuyen = () => {
    const role = localStorage.getItem('userRole');
    let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');

    if (!userMaKho) {
        try {
            const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
            userMaKho = userInfo.maKho;
        } catch (e) {}
    }

    if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
        isAdmin.value = true;
        selectedKho.value = 0; 
    } else {
        isAdmin.value = false;
        const myKho = userMaKho ? parseInt(userMaKho) : 0;
        selectedKho.value = myKho;
    }
};

const loadKhoList = async () => {
    try {
        const res = await api.get('/kho');
        khoList.value = res.data;
    } catch (e) { console.error("Lỗi tải danh sách kho", e); }
}

// API Lấy dữ liệu Tổng quan (Card + Chart)
const fetchDashboardData = async () => {
  loaded.value = false;
  try {
    const response = await api.get('/dashboard/stats', {
        params: { maKho: selectedKho.value } 
    });
    const data = response.data;

    // Cập nhật số liệu 3 Card
    if (data.cards) {
        stats.totalStock = data.cards.totalStock;
        stats.importMonth = data.cards.importMonth;
        stats.exportMonth = data.cards.exportMonth;
    }

    // Cập nhật dữ liệu Biểu đồ
    if (data.chart && Array.isArray(data.chart)) {
        const importArr = new Array(12).fill(0);
        const exportArr = new Array(12).fill(0);

        data.chart.forEach(item => {
            const index = item.month - 1; 
            if (index >= 0 && index < 12) {
                importArr[index] = item.importQty;
                exportArr[index] = item.exportQty;
            }
        });

        chartData.value = {
            ...chartData.value,
            datasets: [
                { ...chartData.value.datasets[0], data: importArr },
                { ...chartData.value.datasets[1], data: exportArr }
            ]
        };
    }
    loaded.value = true;
  } catch (e) { 
      console.error("Lỗi tải dashboard:", e); 
  }
};

// Hàm xử lý khi đổi bộ lọc kho
const handleFilterChange = () => {
    fetchDashboardData();
}

onMounted(async () => {
  setupPhanQuyen();
  await loadKhoList();
  fetchDashboardData();
});
</script>

<style scoped>
/* --- BASE STYLES (Mặc định cho Desktop) --- */
.dashboard-wrapper {
  font-family: 'Inter', sans-serif;
  color: #1f2937;
  padding: 24px;
  background-color: #f9fafb; 
  min-height: 100vh;
  box-sizing: border-box; 
}

.container-limit {
  max-width: 1400px;
  margin: 0 auto;
}

/* CHỈNH SỬA HEADER ĐỂ CHỨA SELECT KHO BÊN PHẢI */
.dashboard-header { 
    margin-bottom: 32px; 
    display: flex;
    justify-content: space-between; /* Đẩy tiêu đề và nút lọc ra 2 bên */
    align-items: center;
}

.header-title h1 { font-size: 24px; font-weight: 700; margin: 0 0 8px 0; color: #111827; }
.header-title .subtitle { font-size: 14px; color: #6b7280; margin: 0; }

.top-chart-filter { 
    padding: 8px 16px; 
    border: 1px solid #d1d5db; 
    border-radius: 6px; 
    outline: none; 
    font-size: 14px; 
    background: white; 
    cursor: pointer;
    min-width: 200px;
    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.05);
}

.stats-grid { 
  display: grid; 
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); 
  gap: 24px; 
  margin-bottom: 32px; 
}

.stat-card { 
  background: #ffffff; 
  border-radius: 12px; 
  padding: 24px; 
  display: flex; 
  align-items: center; 
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1); 
  border: 1px solid #e5e7eb; 
  transition: transform 0.2s; 
}

.stat-card:hover { transform: translateY(-2px); box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1); }
.card-icon { width: 48px; height: 48px; border-radius: 10px; background-color: #eff6ff; display: flex; align-items: center; justify-content: center; font-size: 24px; margin-right: 16px; flex-shrink: 0; }
.card-info h3 { font-size: 14px; color: #6b7280; margin: 0 0 4px 0; font-weight: 600; text-transform: uppercase; }
.stat-value { font-size: 24px; font-weight: 700; color: #111827; margin: 0; }
.stat-trend { font-size: 12px; color: #10b981; margin-top: 4px; display: block; font-weight: 500; }
.sub-text { font-size: 12px; color: #9ca3af; }

.chart-section { background: #ffffff; border-radius: 12px; padding: 24px; box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1); border: 1px solid #e5e7eb; }
.chart-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 12px; }
.chart-header h2 { font-size: 18px; font-weight: 600; color: #111827; margin: 0; }

.chart-container { position: relative; height: 400px; width: 100%; }
.loading-chart { display: flex; align-items: center; justify-content: center; height: 100%; color: #6b7280; }

/* --- RESPONSIVE BREAKPOINTS --- */

@media (max-width: 1024px) {
  .stats-grid { gap: 16px; }
  .chart-container { height: 350px; }
}

@media (max-width: 768px) {
  .dashboard-wrapper { padding: 16px; }
  
  /* Nếu màn hình nhỏ, cho header rớt dòng */
  .dashboard-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 16px;
  }
  
  .top-chart-filter { width: 100%; }

  .chart-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .chart-header h2 { font-size: 16px; width: 100%; }
}

@media (max-width: 480px) {
  .dashboard-wrapper { padding: 12px; background-color: #f3f4f6; }
  .header-title h1 { font-size: 20px; }
  .stats-grid { grid-template-columns: 1fr; }
  .stat-card { padding: 16px; }
  .card-icon { width: 40px; height: 40px; font-size: 20px; }
  .chart-section { padding: 16px; }
  .chart-container { height: 280px; }
}
</style>