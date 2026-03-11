<template>
  <div class="card shadow-sm border-success">
    <div class="card-header bg-success text-white p-2 d-flex justify-content-between align-items-center">
      <h6 class="mb-0 fw-bold"><i class="fas fa-file-excel me-1"></i> NHẬP DỮ LIỆU TỪ EXCEL (TẠO PHIẾU TỰ ĐỘNG)</h6>
    </div>
    
    <div class="card-body p-3">
      <div class="alert alert-info small shadow-sm">
        <i class="fas fa-info-circle fw-bold"></i> Tính năng này cho phép bạn import file "Báo Cáo Xuất Nhập Tồn" định dạng <b>.xlsx</b>. Hệ thống sẽ tự động đọc dữ liệu và sinh ra các <b>Phiếu Nhập</b>, <b>Phiếu Xuất</b> tương ứng vào cơ sở dữ liệu.
      </div>

      <div class="row align-items-end mb-4 bg-light p-3 rounded border">
        <div class="col-md-3 mb-2 mb-md-0">
          <label class="form-label fw-bold small text-muted">Kho áp dụng</label>
          <select class="form-select form-select-sm shadow-none border-secondary" v-model="selectedKho" :disabled="!isAdmin">
            <option value="" disabled>-- Vui lòng chọn kho --</option> 
            <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
          </select>
        </div>
        
        <div class="col-md-4 mb-2 mb-md-0">
          <label class="form-label fw-bold small text-muted">Ngày Nhập/Xuất (Tùy chỉnh)</label>
          <input type="datetime-local" class="form-control form-control-sm shadow-none border-secondary" v-model="selectedDate" />
        </div>

        <div class="col-md-5">
          <input type="file" ref="excelFileInput" class="d-none" accept=".xlsx, .xls" @change="uploadExcel" />
          <button type="button" class="btn btn-warning btn-sm fw-bold w-100 shadow-sm" @click="triggerExcelUpload" :disabled="isImporting">
            <i class="fas" :class="isImporting ? 'fa-spinner fa-spin' : 'fa-upload'"></i>
            {{ isImporting ? 'Đang xử lý dữ liệu...' : 'Chọn File Excel & Import' }}
          </button>
        </div>
      </div>

      <div v-if="importedData.length > 0">
        <h6 class="text-success fw-bold mb-3 border-bottom pb-2">
          <i class="fas fa-check-circle"></i> Chi tiết dữ liệu đã Import thành công ({{ importedData.length }} sản phẩm)
        </h6>
        <div class="table-responsive">
          <table class="table table-bordered table-striped table-hover text-nowrap small align-middle">
            <thead class="table-secondary text-center">
              <tr>
                <th width="50">STT</th>
                <th>Mã SP</th>
                <th class="text-start">Tên Sản Phẩm</th>
                <th>ĐVT</th>
                <th class="text-primary">Tồn Đầu</th>
                <th class="text-success">Nhập</th>
                <th class="text-danger">Xuất</th>
                <th class="text-warning text-dark">Tồn Cuối</th>
              </tr>
            </thead>
            <tbody class="text-center">
              <tr v-for="(item, index) in importedData" :key="index">
                <td>{{ index + 1 }}</td>
                <td class="fw-bold font-monospace">{{ item.maSP }}</td>
                <td class="text-start">{{ item.tenSP }}</td>
                <td>{{ item.donvitinh }}</td>
                <td class="fw-bold text-primary">{{ item.tonDau }}</td>
                <td class="fw-bold text-success">+{{ item.nhapTrong }}</td>
                <td class="fw-bold text-danger">-{{ item.xuatTrong }}</td>
                <td class="fw-bold bg-warning bg-opacity-25">{{ item.tonCuoi }}</td>
              </tr>

              <tr class="table-dark fw-bold text-center">
                <td colspan="4" class="text-end text-uppercase pe-3">Tổng cộng:</td>
                <td class="text-primary fs-6">{{ grandTotal.tdk }}</td>
                <td class="text-success fs-6">+{{ grandTotal.ntk }}</td>
                <td class="text-danger fs-6">-{{ grandTotal.xtk }}</td>
                <td class="text-warning fs-6">{{ grandTotal.tck }}</td>
              </tr>

            </tbody>
          </table>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/utils/axios';

const listKho = ref([]);
const selectedKho = ref(''); // Đổi 0 thành chuỗi rỗng '' để tránh lỗi khi so sánh với option value
const selectedDate = ref(''); // Biến lưu ngày tự chọn
const isAdmin = ref(false);
const excelFileInput = ref(null);
const isImporting = ref(false);
const importedData = ref([]);

const grandTotal = computed(() => {
  return importedData.value.reduce((acc, item) => {
    acc.tdk += Number(item.tonDau) || 0;
    acc.ntk += Number(item.nhapTrong) || 0;
    acc.xtk += Number(item.xuatTrong) || 0;
    acc.tck += Number(item.tonCuoi) || 0;
    return acc;
  }, { tdk: 0, ntk: 0, xtk: 0, tck: 0 });
});

const setupPhanQuyen = async () => {
  const role = localStorage.getItem('userRole');
  let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');
  
  if (!userMaKho) {
    try {
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
      userMaKho = userInfo.maKho;
    } catch (e) { }
  }

  if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
    isAdmin.value = true;
    selectedKho.value = ''; // Admin được quyền tự chọn kho
  } else {
    isAdmin.value = false;
    // Nhân viên: Tự động gán cứng mã kho của nhân viên đó
    selectedKho.value = userMaKho ? parseInt(userMaKho) : ''; 
  }
};

const loadKho = async () => {
  try {
    const res = await api.get('/kho');
    listKho.value = res.data;
  } catch (e) {
    console.error("Lỗi tải kho:", e);
  }
};

const triggerExcelUpload = () => {
  // Check nếu rỗng thì báo lỗi
  if (!selectedKho.value || selectedKho.value === "") {
    alert("Vui lòng chọn kho áp dụng trước khi Import file!");
    return; 
  }

  if (excelFileInput.value) {
    excelFileInput.value.click();
  }
};

const uploadExcel = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append('file', file);
  // Đẩy thẳng mã kho đã được fix cứng (hoặc admin đã chọn) xuống API
  formData.append('maKho', selectedKho.value);
  
  if (selectedDate.value) {
    formData.append('ngayNhap', selectedDate.value);
  }

  isImporting.value = true;
  importedData.value = [];

  try {
    const response = await api.post('/import/import-excel', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      timeout: 3600000 
    });

    importedData.value = response.data.danhSachChiTiet || response.data;
    
    if (response.data.thongBao) {
      alert(response.data.thongBao);
    } else {
      alert("Import dữ liệu thành công!");
    }

  } catch (error) {
    console.error("Lỗi import file:", error);
    alert("Có lỗi khi đọc file Excel: " + (error.response?.data?.message || error.response?.data || error.message));
  } finally {
    isImporting.value = false;
    event.target.value = ''; 
  }
};

onMounted(async () => {
  const token = localStorage.getItem('token');
  if (!token) return;
  await setupPhanQuyen();
  await loadKho();
});
</script>//