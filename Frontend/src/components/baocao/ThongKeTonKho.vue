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
                    <option value="">Tất cả kho</option>
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
                  <tr v-for="(item, index) in reportData" :key="item.productId">
                    <td data-label="Stt">{{ index + 1 }}</td>
                    <td data-label="Mã SP">{{ item.productCode }}</td>
                    <td data-label="Tên SP">{{ item.productName }}</td>
                    <td data-label="ĐVT">{{ item.dvt }}</td>
                    <td data-label="TĐK">{{ item.openingStock }}</td>
                    <td data-label="NTK">{{ item.importStock }}</td>
                    <td data-label="XTK">{{ item.exportStock }}</td>
                    <td data-label="TCK"><strong>{{ item.closingStock }}</strong></td>
                    <td data-label="Giá/BQ">{{ formatCurrency(item.averagePrice) }}</td>
                    <td data-label="Thành Tiền"><strong>{{ formatCurrency(item.totalValue) }}</strong></td>
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
// Giả sử bạn có một service để gọi API
// import apiService from '@/services/apiService'; 

// --- STATE MANAGEMENT ---

// Dữ liệu cho bộ lọc
const filters = reactive({
  startDate: new Date().toISOString().substr(0, 10),
  endDate: new Date().toISOString().substr(0, 10),
  warehouseId: '',
});

// Danh sách kho để hiển thị trong dropdown (sẽ được lấy từ API)
const khoList = ref([
    { id: 'KHO-HN', name: 'Kho Hà Nội' },
    { id: 'KHO-HCM', name: 'Kho TP.Hồ Chí Minh' }
]);

// Dữ liệu báo cáo
const reportData = ref([]);
const loading = ref(false);

// Tiêu đề động cho bảng kết quả
const reportTitle = computed(() => {
  if (filters.warehouseId) {
    const selectedKho = khoList.value.find(kho => kho.id === filters.warehouseId);
    return `Kết quả tồn kho: ${selectedKho?.name || 'Không rõ'}`;
  }
  return 'Kết quả tồn kho: Tất cả kho';
});

// --- Dữ liệu mẫu ---
const sampleData = [
  { 
    productId: 'SP001', 
    productCode: 'IP15PM',
    productName: 'iPhone 15 Pro Max',
    dvt: 'Cái',
    warehouseId: 'KHO-HN',
    warehouseName: 'Kho Hà Nội',
    openingStock: 100,
    importStock: 50,
    exportStock: 30,
    averagePrice: 25000000,
    get closingStock() { return this.openingStock + this.importStock - this.exportStock },
    get totalValue() { return this.closingStock * this.averagePrice }
  },
  { 
    productId: 'SP002', 
    productCode: 'SS24UL',
    productName: 'Samsung Galaxy S24 Ultra',
    dvt: 'Cái',
    warehouseId: 'KHO-HCM',
    warehouseName: 'Kho TP.Hồ Chí Minh',
    openingStock: 80,
    importStock: 20,
    exportStock: 40,
    averagePrice: 22500000,
    get closingStock() { return this.openingStock + this.importStock - this.exportStock },
    get totalValue() { return this.closingStock * this.averagePrice }
  },
  { 
    productId: 'SP003', 
    productCode: 'DELL-XPS',
    productName: 'Dell XPS 15',
    dvt: 'Bộ',
    warehouseId: 'KHO-HN',
    warehouseName: 'Kho Hà Nội',
    openingStock: 50,
    importStock: 10,
    exportStock: 5,
    averagePrice: 45000000,
    get closingStock() { return this.openingStock + this.importStock - this.exportStock },
    get totalValue() { return this.closingStock * this.averagePrice }
  },
  { 
    productId: 'SP004', 
    productCode: 'MBA-M3',
    productName: 'Macbook Air M3',
    dvt: 'Cái',
    warehouseId: 'KHO-HCM',
    warehouseName: 'Kho TP.Hồ Chí Minh',
    openingStock: 30,
    importStock: 15,
    exportStock: 10,
    averagePrice: 28000000,
    get closingStock() { return this.openingStock + this.importStock - this.exportStock },
    get totalValue() { return this.closingStock * this.averagePrice }
  },
  { 
    productId: 'SP005', 
    productCode: 'SONY-PS5',
    productName: 'Sony Playstation 5',
    dvt: 'Bộ',
    warehouseId: 'KHO-HN',
    warehouseName: 'Kho Hà Nội',
    openingStock: 60,
    importStock: 25,
    exportStock: 15,
    averagePrice: 12500000,
    get closingStock() { return this.openingStock + this.importStock - this.exportStock },
    get totalValue() { return this.closingStock * this.averagePrice }
  },
];


// --- METHODS ---

/**
 * Định dạng số thành tiền tệ VNĐ.
 */
const formatCurrency = (value) => {
  if (value === null || value === undefined) return '';
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

/**
 * Giả lập việc gọi API để lấy dữ liệu báo cáo tồn kho.
 * Trong thực tế, bạn sẽ thay thế hàm này bằng lời gọi `axios` đến backend.
 */
const fetchInventoryReport = async () => {
  console.log('Đang lọc báo cáo với các tiêu chí:', filters);
  loading.value = true;
  
  // Giả lập độ trễ mạng
  await new Promise(resolve => setTimeout(resolve, 1000));

  // Lọc dữ liệu mẫu (đây chỉ là ví dụ, logic thực tế sẽ ở backend)
  reportData.value = sampleData.filter(item => {
    if (filters.warehouseId && item.warehouseName !== khoList.value.find(k => k.id === filters.warehouseId)?.name) {
        return false;
    }
    return true;
  });

  console.log('Tải dữ liệu thành công:', reportData.value);
  loading.value = false;
};

/**
 * Lấy danh sách kho từ API.
 */
const fetchWarehouses = async () => {
    // try {
    //   const response = await apiService.get('/warehouses');
    //   khoList.value = response.data;
    // } catch (error) {
    //   console.error("Không thể tải danh sách kho:", error);
    // }
    console.log("Đã tải danh sách kho (dữ liệu mẫu).");
};

/**
 * Xử lý việc xuất báo cáo ra file Excel.
 * Cần một thư viện như 'xlsx' hoặc gọi API backend để xử lý.
 */
const exportReport = () => {
  alert('Chức năng xuất Excel đang được phát triển!');
  // Ví dụ sử dụng một thư viện client-side:
  // import * as XLSX from 'xlsx';
  // const worksheet = XLSX.utils.json_to_sheet(reportData.value);
  // const workbook = XLSX.utils.book_new();
  // XLSX.utils.book_append_sheet(workbook, worksheet, "TonKho");
  // XLSX.writeFile(workbook, "BaoCaoTonKho.xlsx");
};

// --- LIFECYCLE HOOKS ---

// Tải dữ liệu cần thiết khi component được tạo
onMounted(() => {
  fetchWarehouses();
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
