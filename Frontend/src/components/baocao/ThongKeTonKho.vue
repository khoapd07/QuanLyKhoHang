<template>
  <div class="content-wrapper">
    <!-- Content Header (Page header) -->
    <div class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1 class="m-0">Báo Cáo Xuất Nhập Tồn</h1>
          </div>
          <div class="col-sm-6">
          </div>
        </div>
      </div>
    </div>
    <!-- /.content-header -->

    <!-- Main content -->
    <section class="content">
      <div class="container-fluid">
        <!-- Filter Card -->
        <div class="card card-primary card-outline">
          <div class="card-header">
            <h3 class="card-title">
              <i class="fas fa-filter"></i>
              Bộ lọc báo cáo
            </h3>
          </div>
          <div class="card-body">
            <div class="row">
              <div class="col-lg-4 col-md-6 col-12">
                <div class="form-group">
                  <label for="startDate">Từ ngày</label>
                  <input type="date" class="form-control" id="startDate" v-model="filters.startDate">
                </div>
              </div>
              <div class="col-lg-4 col-md-6 col-12">
                <div class="form-group">
                  <label for="endDate">Đến ngày</label>
                  <input type="date" class="form-control" id="endDate" v-model="filters.endDate">
                </div>
              </div>
              <div class="col-lg-4 col-md-6 col-12">
                <div class="form-group">
                  <label for="warehouse">Kho/Chi nhánh</label>
                  <select class="form-control" id="warehouse" v-model="filters.warehouseId">
                    <option value="0">Tất cả kho</option>
                    <option v-for="kho in khoList" :key="kho.id" :value="kho.id">{{ kho.name }}</option>
                  </select>
                </div>
              </div>
            </div>
          </div>
          <div class="card-footer">
            <button type="button" class="btn btn-primary" @click="fetchInventoryReport">
              <i class="fas fa-search"></i> Lọc báo cáo
            </button>
            <button type="button" class="btn btn-success float-right" @click="exportReport">
              <i class="fas fa-file-excel"></i> Xuất File
            </button>
          </div>
        </div>
        <!-- /.card -->

        <!-- Report Results Card -->
        <div class="card mt-4">
          <div class="card-header">
            <h3 class="card-title">{{ reportTitle }}</h3>
          </div>
          <!-- /.card-header -->
          <div class="card-body table-responsive">
            <table class="table table-bordered table-striped table-responsive-stack">
              <thead>
                <tr>
                  <th style="width: 10px">Stt</th>
                  <th>Mã Sản Phẩm</th>
                  <th>Tên Sản Phẩm</th>
                  <th>ĐVT</th>
                  <th>TĐK</th>
                  <th>NTK</th>
                  <th>XTK</th>
                  <th>TCK</th>
                  <th>Giá/BQ</th>
                  <th>Thành Tiền</th>
                </tr>
              </thead>
              <tbody>
                <!-- Skeleton Loader -->
                <template v-if="loading">
                  <tr v-for="n in 5" :key="`skel-${n}`">
                    <td v-for="m in 10" :key="m"><div class="skeleton-loader"></div></td>
                  </tr>
                </template>

                <!-- Data Rows -->
                <template v-else>
                  <tr v-if="reportData.length === 0">
                    <td colspan="10" class="text-center">Không có dữ liệu.</td>
                  </tr>
                  <tr v-for="(item, index) in reportData" :key="item.maSP">
                    <td data-label="Stt">{{ index + 1 }}</td>
                    <td data-label="Mã SP">{{ item.maSP }}</td>
                    <td data-label="Tên SP">{{ item.tenSP }}</td>
                    <td data-label="ĐVT">{{ item.dvt }}</td>
                    <td data-label="TĐK">{{ item.tonDauKySL }}</td>
                    <td data-label="NTK">{{ item.nhapTrongKySL }}</td>
                    <td data-label="XTK">{{ item.xuatTrongKySL }}</td>
                    <td data-label="TCK"><strong>{{ item.tonCuoiKySL }}</strong></td>
                    <td data-label="Giá/BQ">{{ formatCurrency(item.giaVon) }}</td>
                    <td data-label="Thành Tiền"><strong>{{ formatCurrency(item.tonCuoiKyGT) }}</strong></td>
                  </tr>
                </template>
              </tbody>
            </table>
          </div>
          <!-- /.card-body -->
        </div>
        <!-- /.card -->

      </div>
    </section>
    <!-- /.content -->
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import axios from 'axios'; // 1. Import axios

// --- CẤU HÌNH ---
// Nếu bạn chưa cấu hình base URL toàn cục, hãy dùng link đầy đủ
const API_URL = 'http://localhost:8080/api/thong-ke/xuat-nhap-ton';

// --- STATE MANAGEMENT ---
const filters = reactive({
  startDate: new Date('2025-01-01').toISOString().substr(0, 10), // Mặc định lấy từ đầu năm để test
  endDate: new Date().toISOString().substr(0, 10),
  warehouseId: 0, // Mặc định chọn kho 1 (Hà Nội)
});

// Danh sách kho (LƯU Ý: ID phải là số INT để khớp với DB)
const khoList = ref([
    { id: 1, name: 'Kho Tổng Hà Nội' },
    { id: 2, name: 'Kho Chi Nhánh HCM' }
]);

const reportData = ref([]);
const loading = ref(false);
const currentTenKho = ref(''); // Biến lưu tên kho server trả về

// Tiêu đề động
const reportTitle = computed(() => {
  return currentTenKho.value 
    ? `Kết quả tồn kho: ${currentTenKho.value}` 
    : 'Kết quả tồn kho';
});

// --- METHODS ---

const formatCurrency = (value) => {
  if (value === null || value === undefined) return '0 ₫';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

// HÀM GỌI API QUAN TRỌNG NHẤT
const fetchInventoryReport = async () => {
  loading.value = true;
  reportData.value = []; // Reset bảng

  try {
    // Gọi Axios
    const response = await axios.get(API_URL, {
      params: {
        maKho: filters.warehouseId,
        tuNgay: filters.startDate,
        denNgay: filters.endDate,
        loaiLoc: 0 // Mặc định lấy tất cả (bạn có thể thêm dropdown lọc trạng thái sau)
      }
    });

    // Xử lý dữ liệu trả về từ Spring Boot
    // Cấu trúc: { tenKho: "...", danhSachChiTiet: [...] }
    const data = response.data;

    currentTenKho.value = data.tenKho;         // Lấy tên kho hiển thị Header
    reportData.value = data.danhSachChiTiet;   // Lấy danh sách đổ vào bảng

    console.log("Dữ liệu tải về:", data);

  } catch (error) {
    console.error("Lỗi gọi API:", error);
    alert("Không thể tải báo cáo. Vui lòng kiểm tra Server!");
  } finally {
    loading.value = false;
  }
};

// Giả lập export Excel (Frontend only)
const exportReport = () => {
    // Bạn có thể cài thư viện 'xlsx' để xuất mảng reportData.value ra file
    alert(`Đang xuất ${reportData.value.length} dòng ra Excel...`);
};

// --- LIFECYCLE ---
onMounted(() => {
  // Tự động tải dữ liệu lần đầu
  fetchInventoryReport();
});
</script>

<style scoped>
/* Thêm một chút khoảng cách cho các nút trong card footer */
.card-footer .btn {
  margin-right: 5px;
}

/* Skeleton Loader CSS */
.skeleton-loader {
  width: 100%;
  height: 1.2em; /* Chiều cao tương đương dòng chữ */
  background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
  border-radius: 4px;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

/* Responsive Table CSS for Mobile */
@media screen and (max-width: 768px) {
  .table-responsive-stack thead {
    display: none;
  }
  .table-responsive-stack tr {
    display: block;
    margin-bottom: 1rem;
    border: 1px solid #dee2e6;
  }
  .table-responsive-stack td {
    display: block;
    text-align: right;
    border: none;
    border-bottom: 1px solid #dee2e6;
  }
  .table-responsive-stack td::before {
    content: attr(data-label);
    float: left;
    font-weight: bold;
    text-transform: uppercase;
  }
  .table-responsive-stack td:last-child {
    border-bottom: 0;
  }
}
</style>
