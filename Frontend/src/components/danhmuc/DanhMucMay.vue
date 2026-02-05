<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import api from '@/utils/axios'; 
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH ---
const API_URL = '/may-in'; 
const API_KHO = '/kho'; 
const API_TRANG_THAI = '/trang-thai'; 
const API_SAN_PHAM = '/san-pham/list'; // [MỚI] API lấy danh sách sản phẩm để lọc

// --- STATE ---
const danhSachMay = ref([]); 
const danhSachKho = ref([]);
const danhSachTrangThai = ref([]);
const danhSachSanPham = ref([]); // [MỚI] List sản phẩm cho dropdown
const isLoading = ref(false);
const message = ref({ type: '', text: '' });
let modalInstance = null;

// State phân quyền & Bộ lọc
const isAdmin = ref(false);
const filterMaKho = ref(0);     // 0 = Tất cả
const filterMaSP = ref("");     // "" = Tất cả [MỚI]
const filterTrangThai = ref(0); // 0 = Tất cả [MỚI]

// --- STATE PHÂN TRANG ---
const currentPage = ref(0);
const itemsPerPage = ref(20); 
const totalPages = ref(0);
const totalElements = ref(0);

// --- COMPUTED: Pagination (Giữ nguyên) ---
const visiblePages = computed(() => {
    const total = totalPages.value;
    const current = currentPage.value + 1;
    const delta = 2; 
    const range = [];
    const rangeWithDots = [];
    let l;
    for (let i = 1; i <= total; i++) {
        if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) {
            range.push(i);
        }
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

const paginationInfo = computed(() => ({
    total: totalElements.value,
    page: currentPage.value,
    totalPages: totalPages.value
}));

// --- FORM DATA & HELPERS ---
const form = reactive({
  maMay: '', tenSP: '', tenHang: '', tenLoai: '', ngayTao: '', soPhieuNhap: '', 
  soSeri: '', trangThai: 1, tonKho: true, maKho: null      
});

const getBadgeClass = (id) => {
    switch (id) {
        case 1: return 'bg-success';
        case 2: return 'bg-info text-dark';
        case 3: return 'bg-danger';
        case 4: return 'bg-dark';
        case 5: return 'bg-warning text-dark';
        case 6: return 'bg-primary';
        default: return 'bg-secondary';
    }
};

const getTenTrangThai = (id) => {
    const tt = danhSachTrangThai.value.find(t => t.maTrangThai === id);
    return tt ? tt.tenTrangThai : 'Không xác định';
};

const formatDate = (dateString) => {
  if (!dateString) return '---';
  if (Array.isArray(dateString)) {
      const [year, month, day, hour, minute] = dateString;
      return `${day}/${month}/${year} ${hour ? hour : ''}:${minute ? minute : ''}`;
  }
  return new Date(dateString).toLocaleString('vi-VN', { hour12: false });
};

// --- LOGIC PHÂN QUYỀN ---
const setupPhanQuyen = async () => {
    const role = localStorage.getItem('userRole');
    let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');
    
    if (!userMaKho) {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        userMaKho = userInfo.maKho;
    }

    if (role === 'ADMIN') {
        isAdmin.value = true;
        filterMaKho.value = 0; 
    } else {
        isAdmin.value = false;
        filterMaKho.value = userMaKho ? parseInt(userMaKho) : 0;
    }
};

// --- API METHODS ---

// 1. Load Data (Cập nhật logic gửi params)
const loadData = async (page = 0) => {
  isLoading.value = true;
  try {
    const params = { page: page, size: itemsPerPage.value };
    
    // [MỚI] Gán các tham số lọc vào request
    if (filterMaKho.value && filterMaKho.value !== 0) params.maKho = filterMaKho.value;
    if (filterMaSP.value && filterMaSP.value !== "") params.maSP = filterMaSP.value;
    if (filterTrangThai.value && filterTrangThai.value !== 0) params.trangThai = filterTrangThai.value;

    // Load song song: Máy In + Các danh mục (nếu chưa có)
    const [resMay, resKho, resTrangThai, resSP] = await Promise.all([
      api.get(API_URL, { params }),
      danhSachKho.value.length === 0 ? api.get(API_KHO) : { data: danhSachKho.value },
      danhSachTrangThai.value.length === 0 ? api.get(API_TRANG_THAI) : { data: danhSachTrangThai.value },
      danhSachSanPham.value.length === 0 ? api.get(API_SAN_PHAM) : { data: danhSachSanPham.value } // [MỚI]
    ]);

    if (resMay.data) {
        console.log("Dữ liệu API trả về:", resMay.data);
        const data = resMay.data;

        // Gán content (danh sách máy)
        danhSachMay.value = data.content || []; 

        // Gán thông tin phân trang (Lấy từ object 'page' bên trong)
        if (data.page) {
            totalPages.value = data.page.totalPages || 0;
            totalElements.value = data.page.totalElements || 0; 
            currentPage.value = data.page.number || 0;
        } else {
            // Fallback nếu API trả về cấu trúc phẳng (standard Spring Page)
            totalPages.value = data.totalPages || 0;
            totalElements.value = data.totalElements || 0;
            currentPage.value = (typeof data.number === 'number') ? data.number : 0;
        }
    }
    
    if(resKho.data) danhSachKho.value = resKho.data;
    if(resTrangThai.data) danhSachTrangThai.value = resTrangThai.data;
    if(resSP.data) danhSachSanPham.value = resSP.data; // [MỚI]

  } catch (error) {
    const msg = error.response?.data?.message || error.message;
    showMessage('danger', 'Lỗi tải dữ liệu: ' + msg);
  } finally {
    isLoading.value = false;
  }
};

const changePage = (page) => {
    if (page >= 0 && page < totalPages.value) {
        loadData(page); 
    }
};

// [MỚI] Hàm reset bộ lọc
const resetFilters = () => {
    if(isAdmin.value) filterMaKho.value = 0;
    filterMaSP.value = "";
    filterTrangThai.value = 0;
    loadData(0);
};

const openEditModal = (may) => {
  form.maMay = may.maMay;
  form.tenSP = may.tenSP || '---';
  form.tenHang = may.tenHang || '---';
  form.tenLoai = may.tenLoai || '---';
  form.ngayTao = formatDate(may.ngayTao);
  form.soPhieuNhap = may.soPhieuNhap || 'Không có';
  form.soSeri = may.soSeri || '';
  form.trangThai = may.trangThai || 1;
  form.tonKho = may.tonKho; 
  form.maKho = may.maKho || null;

  const modalEl = document.getElementById('modalChiTietMay');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

const saveChanges = async () => {
  try {
    const payload = {
      maMay: form.maMay,
      soSeri: form.soSeri,
      trangThai: form.trangThai,
      tonKho: form.tonKho,
      kho: form.maKho ? { maKho: form.maKho } : null
    };
    await api.put(`${API_URL}/${form.maMay}`, payload);
    showMessage('success', 'Cập nhật thành công!');
    if (modalInstance) modalInstance.hide();
    loadData(currentPage.value); 
  } catch (error) {
    showMessage('danger', 'Lỗi cập nhật: ' + error.message);
  }
};

const deleteMay = async (id, seri) => {
  if (!confirm(`Xóa máy ${seri || id}?`)) return;
  try {
    await api.delete(`${API_URL}/${id}`);
    showMessage('success', 'Đã xóa thành công!');
    loadData(0); 
  } catch (error) {
    showMessage('danger', 'Không thể xóa: ' + error.message);
  }
};

const showMessage = (type, text) => {
  message.value = { type, text };
  setTimeout(() => message.value = { type: '', text: '' }, 3000);
};

onMounted(async () => {
  await setupPhanQuyen();
  loadData(0);
});
</script>

<template>
  <div class="container mt-4">
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4 gap-3">
      <h3 class="text-primary mb-0"><i class="bi bi-qr-code"></i> Quản Lý Danh Mục Máy</h3>
      
      <div class="d-flex flex-wrap gap-2 align-items-center bg-light p-2 rounded border shadow-sm">
          <span class="fw-bold text-secondary small me-1"><i class="bi bi-funnel-fill"></i> Bộ lọc:</span>
          
          <select v-if="isAdmin" class="form-select form-select-sm" style="width: 160px;" v-model="filterMaKho" @change="loadData(0)">
              <option :value="0">-- Tất cả kho --</option>
              <option v-for="k in danhSachKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
          </select>

          <select class="form-select form-select-sm" style="width: 160px;" v-model="filterMaSP" @change="loadData(0)">
              <option value="">-- Tất cả SP --</option>
              <option v-for="sp in danhSachSanPham" :key="sp.maSP" :value="sp.maSP">{{ sp.tenSP }}</option>
          </select>

          <select class="form-select form-select-sm" style="width: 150px;" v-model="filterTrangThai" @change="loadData(0)">
              <option :value="0">-- Tất cả T.Thái --</option>
              <option v-for="tt in danhSachTrangThai" :key="tt.maTrangThai" :value="tt.maTrangThai">
                  {{ tt.tenTrangThai }}
              </option>
          </select>

          <button class="btn btn-sm btn-outline-danger" @click="resetFilters" title="Xóa bộ lọc">
            <i class="bi bi-x-lg"></i>
          </button>

          <button class="btn btn-sm btn-outline-secondary" @click="loadData(currentPage)">
            <i class="bi bi-arrow-clockwise"></i>
          </button>
      </div>
    </div>

    <div v-if="message.text" :class="`alert alert-${message.type} alert-dismissible fade show`">
      {{ message.text }} <button type="button" class="btn-close" @click="message.text = ''"></button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-bordered table-hover mb-0 align-middle">
            <thead class="table-dark text-center">
              <tr>
                <th width="50px">STT</th>
                <th>Mã máy</th>
                <th>Tên Sản Phẩm</th>
                <th>Loại SP</th> 
                <th>Kho</th>
                <th width="120px">Tình Trạng</th>
                <th width="120px">TT Kho</th> <th width="120px">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="isLoading">
                <td colspan="8" class="text-center py-3">
                    <div class="spinner-border text-primary spinner-border-sm" role="status"></div> Đang tải...
                </td>
              </tr>
              <tr v-else v-for="(may, index) in danhSachMay" :key="may.maMay">
                
                <td class="text-center">{{ ((currentPage || 0) * itemsPerPage) + index + 1 }}</td>
                <td class="fw-bold text-primary">{{ may.maMay }}</td>
                <td>
                  <div>{{ may.tenSP }}</div> <small class="text-muted">{{ may.tenHang }}</small> </td>
                <td class="text-center">
                    <span class="badge bg-light text-dark border">{{ may.tenLoai || '---' }}</span> </td>
                <td>{{ may.tenKho || '---' }}</td> 
                
                <td class="text-center">
                  <span :class="['badge', getBadgeClass(may.trangThai)]">
                    {{ getTenTrangThai(may.trangThai) }}
                  </span>
                </td>

                <td class="text-center">
                    <span v-if="may.tonKho" class="badge bg-success">Còn Hàng</span>
                    <span v-else class="badge bg-secondary">Đã Xuất</span>
                </td>

                <td class="text-center">
                  <button class="btn btn-sm btn-outline-primary me-2" @click="openEditModal(may)">
                    <i class="bi bi-pencil-square"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteMay(may.maMay, may.soSeri)">
                    <i class="bi bi-trash"></i>
                  </button>
                </td>
              </tr>
              <tr v-if="!isLoading && (!danhSachMay || danhSachMay.length === 0)">
                 <td colspan="8" class="text-center py-3">Chưa có dữ liệu.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      <div class="card-footer bg-white border-top-0">
          <div class="d-flex justify-content-center mt-3 px-3 pb-3" v-if="paginationInfo.total > 0">
                <ul class="pagination pagination-sm m-0">
                    <li class="page-item" :class="{ disabled: paginationInfo.page === 0 }">
                        <a class="page-link" href="#" @click.prevent="changePage(paginationInfo.page - 1)">« Trước</a>
                    </li>
                    <li v-for="(page, index) in visiblePages" :key="index" 
                        class="page-item" 
                        :class="{ active: page === paginationInfo.page + 1, disabled: page === '...' }">
                        <a class="page-link" href="#" @click.prevent="page !== '...' ? changePage(page - 1) : null">
                            {{ page }}
                        </a>
                    </li>
                    <li class="page-item" :class="{ disabled: paginationInfo.page >= paginationInfo.totalPages - 1 }">
                        <a class="page-link" href="#" @click.prevent="changePage(paginationInfo.page + 1)">Sau »</a>
                    </li>
                </ul>
          </div>
          <div class="text-center text-muted small mt-1" v-if="paginationInfo.total > 0">
              Hiển thị {{ ((currentPage || 0) * itemsPerPage) + 1 }} - {{ Math.min(((currentPage || 0) + 1) * itemsPerPage, paginationInfo.total) }} 
              trong tổng {{ paginationInfo.total }} máy
          </div>
      </div>
    </div>
    
    <div class="modal fade" id="modalChiTietMay" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
         <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header bg-light">
            <h5 class="modal-title text-primary">
              <i class="bi bi-info-circle-fill"></i> Chi Tiết & Cập Nhật Máy
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveChanges">
              
              <h6 class="text-uppercase fw-bold text-secondary mb-3 small border-bottom pb-1">Thông tin gốc</h6>
              <div class="row g-3 mb-4">
                <div class="col-md-4">
                  <label class="form-label text-muted small">Mã Máy</label>
                  <input type="text" class="form-control bg-light" :value="form.maMay" disabled readonly>
                </div>
                <div class="col-md-4">
                  <label class="form-label text-muted small">Phiếu Nhập</label>
                  <div class="input-group">
                    <span class="input-group-text bg-light"><i class="bi bi-file-earmark-text"></i></span>
                    <input type="text" class="form-control bg-light fw-bold" :value="form.soPhieuNhap" disabled readonly>
                  </div>
                </div>
                <div class="col-md-4">
                  <label class="form-label text-muted small">Ngày Tạo</label>
                  <input type="text" class="form-control bg-light" :value="form.ngayTao" disabled readonly>
                </div>

                <div class="col-md-4">
                  <label class="form-label text-muted small">Sản Phẩm</label>
                  <input type="text" class="form-control bg-light fw-bold" :value="form.tenSP" disabled readonly>
                </div>
                <div class="col-md-4">
                  <label class="form-label text-muted small">Hãng SX</label>
                  <input type="text" class="form-control bg-light fw-bold" :value="form.tenHang" disabled readonly>
                </div>
                <div class="col-md-4">
                  <label class="form-label text-muted small">Loại Sản Phẩm</label>
                  <input type="text" class="form-control bg-light fw-bold text-primary" :value="form.tenLoai" disabled readonly>
                </div>
              </div>

              <h6 class="text-uppercase fw-bold text-secondary mb-3 small border-bottom pb-1">Cập nhật hiện trạng</h6>
              <div class="row g-3">
                <div class="col-12">
                  <label class="form-label fw-bold">Số Seri <span class="text-danger">*</span></label>
                  <input v-model="form.soSeri" type="text" class="form-control border-primary" placeholder="Nhập số seri thực tế...">
                </div>

                <div class="col-md-6">
                  <label class="form-label">Tình Trạng (Từ DB)</label>
                  <select v-model="form.trangThai" class="form-select">
                    <option v-for="tt in danhSachTrangThai" :key="tt.maTrangThai" :value="tt.maTrangThai">
                      {{ tt.tenTrangThai }}
                    </option>
                  </select>
                </div>

                <div class="col-md-6">
                  <label class="form-label">Vị trí Kho</label>
                  <select v-model="form.maKho" class="form-select bg-light" disabled>
                    <option :value="null">-- Chưa xác định --</option>
                    <option v-for="k in danhSachKho" :key="k.maKho" :value="k.maKho">
                      {{ k.tenKho }}
                    </option>
                  </select>
                  <div class="form-text text-muted small fst-italic">
                      <i class="bi bi-info-circle"></i> Vị trí kho được cập nhật tự động qua Phiếu Nhập/Xuất.
                  </div>
                </div>

                <div class="col-12 mt-3">
                    <div class="form-check form-switch p-3 border rounded bg-white">
                        <input class="form-check-input" type="checkbox" id="switchTonKho" v-model="form.tonKho">
                        <label class="form-check-label fw-bold ms-2" for="switchTonKho">
                            <span v-if="form.tonKho" class="text-success">Trạng thái: ĐANG CÓ TRONG KHO</span>
                            <span v-else class="text-secondary">Trạng thái: ĐÃ XUẤT / KHÔNG CÓ TRONG KHO</span>
                        </label>
                        <div class="form-text text-muted fst-italic ms-1">
                            <i class="bi bi-info-circle"></i> Chỉ chỉnh sửa mục này khi cần điều chỉnh số liệu kho thủ công.
                        </div>
                    </div>
                </div>
              </div>

              <div class="text-end border-top pt-3 mt-4">
                <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">Đóng</button>
                <button type="submit" class="btn btn-primary px-4">
                  <i class="bi bi-save"></i> Lưu Thay Đổi
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>