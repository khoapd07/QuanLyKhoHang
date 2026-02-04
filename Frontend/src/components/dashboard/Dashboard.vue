<template>
  <div class="dashboard-wrapper">
    <header class="dashboard-header">
      <h1>Tổng Quan Kho Hàng</h1>
      <p class="subtitle">Thống kê hoạt động xuất nhập tồn theo thời gian thực (Năm {{ currentYear }})</p>
    </header>

    <div class="stats-grid">
      <div class="stat-card total-stock">
        <div class="card-icon">
          <i class="fas fa-boxes"></i> 📦
        </div>
        <div class="card-info">
          <h3>Tổng Tồn Kho</h3>
          <p class="stat-value">{{ formatNumber(stats.totalStock) }}</p>
          <span class="stat-trend positive">
            Sản phẩm đang có trong kho
          </span>
        </div>
      </div>

      <div class="stat-card import-stock">
        <div class="card-icon">
          📥
        </div>
        <div class="card-info">
          <h3>Nhập Tháng Này</h3>
          <p class="stat-value">{{ formatNumber(stats.importMonth) }}</p>
          <span class="sub-text">Lượt nhập từ ngày 1 đến nay</span>
        </div>
      </div>

      <div class="stat-card export-stock">
        <div class="card-icon">
          📤
        </div>
        <div class="card-info">
          <h3>Xuất Tháng Này</h3>
          <p class="stat-value">{{ formatNumber(stats.exportMonth) }}</p>
          <span class="sub-text">Lượt xuất từ ngày 1 đến nay</span>
        </div>
      </div>
    </div>

    <div class="chart-section">
      <div class="chart-header">
        <h2>Biểu Đồ Xuất Nhập 12 Tháng</h2>
        <select v-model="selectedKho" @change="fetchDashboardData" class="chart-filter" :disabled="!isAdmin">
            <option :value="0" v-if="isAdmin">Tất cả kho</option>
            <option v-for="k in khoList" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
        </select>
      </div>
      
      <div class="chart-container">
        <Bar 
          v-if="loaded" 
          :data="chartData" 
          :options="chartOptions" 
        />
        <div v-else class="loading-chart">Đang tải dữ liệu biểu đồ...</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue';
import api from '@/utils/axios';

// Import Chart.js components
import {
  Chart as ChartJS,
  Title,
  Tooltip,
  Legend,
  BarElement,
  CategoryScale,
  LinearScale
} from 'chart.js';
import { Bar } from 'vue-chartjs';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

// --- STATE ---
const currentYear = new Date().getFullYear();
const loaded = ref(false);
const selectedKho = ref(0);
const khoList = ref([]);
const isAdmin = ref(false); // [NEW] Biến kiểm tra quyền Admin

const stats = reactive({
  totalStock: 0,
  importMonth: 0,
  exportMonth: 0
});

const chartData = ref({
  labels: [ 'Thg 1', 'Thg 2', 'Thg 3', 'Thg 4', 'Thg 5', 'Thg 6', 'Thg 7', 'Thg 8', 'Thg 9', 'Thg 10', 'Thg 11', 'Thg 12' ],
  datasets: [
    {
      label: 'Nhập kho',
      backgroundColor: '#3b82f6',
      data: [],
      borderRadius: 4,
      barPercentage: 0.6,
      categoryPercentage: 0.8
    },
    {
      label: 'Xuất kho',
      backgroundColor: '#9ca3af',
      data: [],
      borderRadius: 4,
      barPercentage: 0.6,
      categoryPercentage: 0.8
    }
  ]
});

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'top' },
    tooltip: { mode: 'index', intersect: false }
  },
  scales: {
    y: {
      beginAtZero: true,
      grid: { color: '#f3f4f6' },
      ticks: { precision: 0 }
    },
    x: { grid: { display: false } }
  }
};

const formatNumber = (num) => {
  if(!num) return '0';
  return new Intl.NumberFormat('vi-VN').format(num);
};

// --- LOGIC PHÂN QUYỀN ---
const setupPhanQuyen = () => {
    const role = localStorage.getItem('userRole');
    let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');
    
    // Fallback: Lấy maKho từ userInfo JSON nếu key lẻ không có
    if (!userMaKho) {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        userMaKho = userInfo.maKho;
    }

    if (role === 'ADMIN') {
        isAdmin.value = true;
        selectedKho.value = 0; // Admin mặc định xem tất cả
    } else {
        isAdmin.value = false;
        // Staff buộc phải xem kho của mình
        if (userMaKho) {
            selectedKho.value = parseInt(userMaKho);
        } else {
            // Trường hợp lỗi (Staff không có kho), có thể set 0 hoặc xử lý khác
            // Ở đây tạm set 0, nhưng backend sẽ chặn nếu logic backend chặt chẽ
            selectedKho.value = 0; 
        }
    }
};

// --- API CALLS ---
const loadKhoList = async () => {
    try {
        const res = await api.get('/kho');
        khoList.value = res.data;
    } catch (e) {
        console.error("Lỗi tải danh sách kho", e);
    }
}

const fetchDashboardData = async () => {
  loaded.value = false;
  try {
    const response = await api.get('/dashboard/stats', {
        params: { 
            maKho: selectedKho.value,
            nam: currentYear 
        }
    });
    
    const data = response.data;

    // Update Cards
    if (data.cards) {
        stats.totalStock = data.cards.totalStock;
        stats.importMonth = data.cards.importMonth;
        stats.exportMonth = data.cards.exportMonth;
    }

    // Update Chart
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

onMounted(async () => {
  setupPhanQuyen(); // Thiết lập quyền trước
  await loadKhoList(); // Tải danh sách kho (để hiển thị tên trong dropdown nếu cần)
  fetchDashboardData(); // Tải dữ liệu dashboard
});
</script>

<style scoped>
/* Reset & Font cơ bản */
.dashboard-wrapper {
  font-family: 'Inter', sans-serif;
  color: #1f2937;
  padding: 24px;
  background-color: #f9fafb; 
  min-height: 100vh;
}

/* Header */
.dashboard-header {
  margin-bottom: 32px;
}
.dashboard-header h1 {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #111827;
}
.subtitle {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

/* Stats Cards */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
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

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  background-color: #eff6ff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 16px;
}

.card-info h3 {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 4px 0;
  font-weight: 600;
  text-transform: uppercase;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.stat-trend {
  font-size: 12px;
  color: #10b981; /* Xanh lá */
  margin-top: 4px;
  display: block;
  font-weight: 500;
}
.sub-text {
    font-size: 12px;
    color: #9ca3af;
}

/* Chart Section */
.chart-section {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  border: 1px solid #e5e7eb;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: #111827;
  margin: 0;
}

.chart-filter {
    padding: 6px 12px;
    border: 1px solid #d1d5db;
    border-radius: 6px;
    outline: none;
    font-size: 14px;
}

.chart-container {
  position: relative;
  height: 400px;
  width: 100%;
}

.loading-chart {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 100%;
    color: #6b7280;
}

@media (max-width: 640px) {
  .dashboard-wrapper { padding: 16px; }
  .stats-grid { grid-template-columns: 1fr; }
  .chart-container { height: 300px; }
}
</style>