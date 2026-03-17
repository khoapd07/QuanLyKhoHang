<template>
  <div class="content-wrapper">
    <div class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1 class="m-0"><i class="fas fa-file-excel text-success"></i> Import Excel Chốt Sổ</h1>
          </div>
        </div>
      </div>
    </div>

    <section class="content">
      <div class="container-fluid">
        <div class="card shadow-sm border-success">
          <div class="card-body p-4">
            
            <div class="alert alert-info small shadow-sm">
              <i class="fas fa-info-circle fw-bold"></i> Tính năng này cho phép bạn tải lên file <b>Báo Cáo Xuất Nhập Tồn</b>. Hệ thống sẽ tự động tính Tồn Cuối và lưu chốt sổ vào CSDL theo đúng Năm và Kho đã chọn.
            </div>
            
            <div class="row align-items-end mb-4 bg-light p-3 rounded border">
              <div class="col-md-3 mb-2 mb-md-0">
                <label class="form-label fw-bold small text-muted">Năm chốt sổ</label>
                <input type="number" class="form-control shadow-none border-secondary" v-model="selectedYear" placeholder="Nhập năm..." />
              </div>

              <div class="col-md-4 mb-2 mb-md-0">
                <label class="form-label fw-bold small text-muted">Kho áp dụng</label>
                <select class="form-control shadow-none border-secondary" v-model="selectedKho" :disabled="!isAdmin">
                  <option value="" disabled>-- Vui lòng chọn kho --</option> 
                  <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                </select>
              </div>

              <div class="col-md-5">
                <input type="file" ref="excelFileInput" class="d-none" accept=".xlsx, .xls, .csv" @change="uploadExcel" />
                <button type="button" class="btn btn-warning fw-bold w-100 shadow-sm" @click="triggerExcelUpload" :disabled="isImporting">
                  <i class="fas" :class="isImporting ? 'fa-spinner fa-spin' : 'fa-upload'"></i>
                  {{ isImporting ? 'Đang xử lý dữ liệu...' : 'Chọn File Excel & Lưu Tồn Kho' }}
                </button>
              </div>
            </div>
            
            <div class="col-sm-6 text-right mb-3 px-0">
              <router-link to="/bao-cao-ton-dau-nam" class="btn btn-sm btn-secondary font-weight-bold">
                <i class="fas fa-arrow-left"></i> Quay Lại Trang Chốt Sổ
              </router-link>
            </div>

            <div v-if="importedData.length > 0">
              
              <div class="d-flex justify-content-between align-items-center mb-3 border-bottom pb-2">
                <h6 class="text-success fw-bold m-0">
                  <i class="fas fa-check-circle"></i> Đã import thành công ({{ importedData.length }} sản phẩm)
                </h6>
                <button type="button" class="btn btn-sm btn-primary shadow-sm" @click="syncToDMTonkho" :disabled="isSyncing">
                  <i class="fas" :class="isSyncing ? 'fa-spinner fa-spin' : 'fa-database'"></i> 
                  {{ isSyncing ? 'Đang lưu...' : 'Lưu Tồn Cuối vào DM Tồn Kho' }}
                </button>
              </div>
              
              <div class="table-responsive" style="max-height: 500px;">
                <table class="table table-bordered table-striped table-head-fixed text-nowrap">
                  <thead>
                    <tr>
                      <th class="text-center" width="50">STT</th>
                      <th>Mã SP</th>
                      <th>Tên Sản Phẩm </th>
                      <th class="text-center">ĐVT</th>
                      <th class="text-right">Tồn Đầu</th>
                      <th class="text-right">Nhập</th>
                      <th class="text-right">Xuất</th>
                      <th class="text-right">Tồn Cuối</th>
                      <th class="text-right">Giá BQ</th>
                      <th class="text-right">Thành Tiền</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-if="paginatedData.length === 0">
                      <td colspan="10" class="text-center py-4 text-muted">Chưa có dữ liệu.</td>
                    </tr>
                    <tr v-for="(item, index) in paginatedData" :key="index">
                      <td class="text-center">{{ (pagination.page * pagination.size) + index + 1 }}</td>
                      <td class="text-primary font-weight-bold font-monospace">{{ item.maSP }}</td>
                      <td>{{ formatProductName(item.tenSP) }}</td>
                      <td class="text-center">{{ item.donvitinh }}</td>
                      <td class="text-right">{{ item.tonDau || 0 }}</td>
                      <td class="text-right text-success">+{{ item.nhapTrong || 0 }}</td>
                      <td class="text-right text-danger">-{{ item.xuatTrong || 0 }}</td>
                      <td class="text-right font-weight-bold bg-warning bg-opacity-25">{{ item.tonCuoi || 0 }}</td>
                      <td class="text-right">{{ formatCurrency(item.giaBQ || 0) }}</td>
                      <td class="text-right font-weight-bold text-primary">{{ formatCurrency(item.thanhTien || 0) }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <div class="bg-light border-top py-2" v-if="importedData.length > 0"
                style="border-bottom-left-radius: .25rem; border-bottom-right-radius: .25rem; box-shadow: 0 -1px 3px rgba(0,0,0,0.05);">
                <div class="d-flex justify-content-around text-center align-items-center"
                  style="font-size: 14px; min-height: 40px;">
                  <div class="flex-fill border-right">
                    <span class="text-muted mr-1">Tồn Đầu:</span>
                    <span class="font-weight-bold">{{ grandTotal.tdk }}</span>
                  </div>
                  <div class="flex-fill border-right">
                    <span class="text-success mr-1">Nhập:</span>
                    <span class="font-weight-bold">{{ grandTotal.ntk }}</span>
                  </div>
                  <div class="flex-fill border-right">
                    <span class="text-danger mr-1">Xuất:</span>
                    <span class="font-weight-bold">{{ grandTotal.xtk }}</span>
                  </div>
                  <div class="flex-fill border-right">
                    <span class="text-dark mr-1">Tồn Cuối:</span>
                    <span class="font-weight-bold text-warning" style="font-size: 1.1em;">{{ grandTotal.tck }}</span>
                  </div>
                  <div class="flex-fill">
                    <span class="text-primary mr-1">Tổng Tiền:</span>
                    <span class="font-weight-bold text-primary">{{ formatCurrency(grandTotal.tien) }}</span>
                  </div>
                </div>
              </div>

              <div class="d-flex justify-content-center mt-3 px-3 pb-3" v-if="pagination.totalPages > 1">
                <ul class="pagination pagination-sm m-0">
                  <li class="page-item" :class="{ disabled: pagination.page === 0 }">
                    <a class="page-link" href="#" @click.prevent="changePage(pagination.page - 1)">« Trước</a>
                  </li>
                  <li v-for="(page, index) in visiblePages" :key="index" class="page-item" :class="{ active: page === pagination.page + 1, disabled: page === '...' }">
                    <a class="page-link" href="#" @click.prevent="page !== '...' ? changePage(page - 1) : null">{{ page }}</a>
                  </li>
                  <li class="page-item" :class="{ disabled: pagination.page >= pagination.totalPages - 1 }">
                    <a class="page-link" href="#" @click.prevent="changePage(pagination.page + 1)">Sau »</a>
                  </li>
                </ul>
              </div>

            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import api from '@/utils/axios';
import Swal from 'sweetalert2';

const listKho = ref([]);
const selectedKho = ref(''); 
const selectedYear = ref(new Date().getFullYear());
const isAdmin = ref(false);
const excelFileInput = ref(null);
const isImporting = ref(false);
const importedData = ref([]);
const isSyncing = ref(false);

const grandTotal = computed(() => {
  return importedData.value.reduce((acc, item) => {
    acc.tdk += Number(item.tonDau || 0);
    acc.ntk += Number(item.nhapTrong || 0);
    acc.xtk += Number(item.xuatTrong || 0);
    acc.tck += Number(item.tonCuoi || 0);
    acc.tien += Number(item.thanhTien || 0);
    return acc;
  }, { tdk: 0, ntk: 0, xtk: 0, tck: 0, tien: 0 });
});

const formatProductName = (name) => {
  if (!name) return '';
  return name.toString()
    .replace(/\s*-\s*Bình thường\s*$/gi, '')
    .replace(/\s*-\s*Chưa xác định\s*$/gi, '')
    .replace(/\s*-\s*Mới\s*\(New\)/gi, ' - New')
    .replace(/\s*-\s*Mới/gi, ' - New')
    .replace(/\s*-\s*$/g, '')
    .trim();
};

const formatCurrency = (value) => {
  if (value === null || value === undefined || isNaN(value)) return '0';
  return new Intl.NumberFormat('vi-VN', { style: 'decimal', currency: 'VND' }).format(value);
};

const pagination = reactive({ page: 0, size: 20, total: 0, totalPages: 0 });
const paginatedData = ref([]);

const applyPagination = () => {
  const start = pagination.page * pagination.size;
  paginatedData.value = importedData.value.slice(start, start + pagination.size);
};

const changePage = (newPage) => {
  if (newPage >= 0 && newPage < pagination.totalPages) {
    pagination.page = newPage;
    applyPagination();
  }
};

const visiblePages = computed(() => {
  const total = pagination.totalPages;
  if (total === 0) return [];
  const current = pagination.page + 1;
  const range = [], rangeWithDots = [];
  let l;
  for (let i = 1; i <= total; i++) {
    if (i === 1 || i === total || (i >= current - 2 && i <= current + 2)) range.push(i);
  }
  for (let i of range) {
    if (l) {
      if (i - l === 2) rangeWithDots.push(l + 1);
      else if (i - l !== 1) rangeWithDots.push('...');
    }
    rangeWithDots.push(i);
    l = i;
  }
  return rangeWithDots;
});

const setupPhanQuyen = async () => {
  const role = localStorage.getItem('userRole');
  let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');
  if (!userMaKho) {
    try { userMaKho = JSON.parse(localStorage.getItem('userInfo') || '{}').maKho; } catch (e) {}
  }

  if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
    isAdmin.value = true;
    selectedKho.value = ''; 
  } else {
    isAdmin.value = false;
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
  if (!selectedYear.value || selectedYear.value < 2000) return Swal.fire('Lỗi', 'Vui lòng nhập Năm chốt sổ hợp lệ!', 'error');
  if (!selectedKho.value || selectedKho.value === "") return Swal.fire('Lỗi', 'Vui lòng chọn kho áp dụng!', 'error');

  if (excelFileInput.value) excelFileInput.value.click();
};

const uploadExcel = async (event) => {
  const file = event.target.files[0];
  if (!file) return;

  const formData = new FormData();
  formData.append('file', file);
  formData.append('maKho', selectedKho.value);
  formData.append('nam', selectedYear.value); 

  isImporting.value = true;
  importedData.value = [];
  paginatedData.value = [];

  try {
    const response = await api.post('/import/import-excel', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      timeout: 3600000 
    });

    importedData.value = response.data.danhSachChiTiet || response.data;
    pagination.total = importedData.value.length;
    pagination.totalPages = Math.ceil(pagination.total / pagination.size);
    pagination.page = 0;
    applyPagination();

    Swal.fire('Thành công', response.data.thongBao || 'Đọc file Excel thành công!', 'success');

  } catch (error) {
    console.error("Lỗi import file:", error);
    Swal.fire('Lỗi Import', 'Có lỗi khi đọc file Excel: ' + (error.response?.data?.message || error.message), 'error');
  } finally {
    isImporting.value = false;
    event.target.value = ''; 
  }
};

// --- HÀM ĐỒNG BỘ: ĐÃ THÊM VALIDATE MẬT KHẨU ---
const syncToDMTonkho = async () => {
  if (importedData.value.length === 0) return;

  const payload = importedData.value.map(item => ({
    maKho: selectedKho.value,
    nam: selectedYear.value,
    maSP: item.maSP,
    maTrangThai: item.maTrangThai,
    soLuong: Number(item.tonCuoi || 0),  // <-- Lấy Tồn Cuối của Excel gán cho CSDL
    giaTri: Number(item.thanhTien || 0)  // Gửi kèm giá trị tiền
  }));

  const { value: password } = await Swal.fire({
    title: 'Xác thực bảo mật',
    html: `Lưu số dư Tồn Cuối của <b>${payload.length}</b> sản phẩm vào DM Tồn Kho (Năm ${selectedYear.value}).<br><br>Vui lòng nhập mật khẩu tài khoản để xác nhận.`,
    input: 'password',
    inputPlaceholder: 'Nhập mật khẩu của bạn',
    inputAttributes: { autocapitalize: 'off', autocorrect: 'off' },
    icon: 'info',
    showCancelButton: true,
    confirmButtonColor: '#007bff',
    cancelButtonColor: '#6c757d',
    confirmButtonText: 'Đồng ý lưu',
    cancelButtonText: 'Hủy',
    inputValidator: (value) => {
      if (!value) return 'Mật khẩu không được để trống!'
    }
  });

  if (!password) return;

  isSyncing.value = true;
  try {
    let savedUsername = localStorage.getItem('hoTen') || localStorage.getItem('username') || localStorage.getItem('tenTaiKhoan') || '';
    try { const ui = JSON.parse(localStorage.getItem('userInfo') || '{}'); savedUsername = ui.hoTen || ui.username || ui.sub || savedUsername; } catch(e){}

    if (!savedUsername) {
      isSyncing.value = false;
      return Swal.fire('Lỗi phiên đăng nhập', 'Không tìm thấy tên tài khoản. Vui lòng đăng xuất và đăng nhập lại!', 'error');
    }

    const authRes = await api.post('/thong-ke/verify-password', { username: savedUsername, password: password });
    
    if (authRes.data.success) {
      // Gọi lên Endpoint mới sửa bên Spring Boot
      await api.post('/import/sync-import', payload);
      Swal.fire('Thành công', 'Đã chốt toàn bộ số lượng Tồn Cuối vào hệ thống!', 'success');
    } else {
      Swal.fire('Lỗi xác thực', 'Mật khẩu không chính xác. Không thể đồng bộ!', 'error');
    }
  } catch (error) {
    console.error("Lỗi đồng bộ:", error);
    Swal.fire('Lỗi', 'Không thể đồng bộ vào DM Tồn Kho: ' + (error.response?.data?.message || error.message), 'error');
  } finally {
    isSyncing.value = false;
  }
};

onMounted(async () => {
  const token = localStorage.getItem('token');
  if (!token) return;
  await setupPhanQuyen();
  await loadKho();
});
</script>

<style scoped>
.page-link { cursor: pointer; font-weight: 600; }
.page-item.active .page-link { background-color: #007bff; border-color: #007bff; color: white; }
.page-item.disabled .page-link { pointer-events: none; background-color: #fff; color: #6c757d; }
.table-head-fixed th { position: sticky; top: 0; background-color: #e2e3e5; z-index: 10; box-shadow: 0 1px 2px rgba(0,0,0,0.1); }
</style>