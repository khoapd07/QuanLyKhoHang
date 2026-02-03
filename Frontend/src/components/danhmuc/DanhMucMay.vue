<script setup>
import { ref, onMounted, reactive } from 'vue';
// [QUAN TRỌNG] Sử dụng api từ utils thay vì axios thường
import api from '@/utils/axios'; 
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH API ---
// BaseURL đã là /api trong axios.js
const API_URL = '/may-in'; 
const API_KHO = '/kho'; 

// --- STATE ---
const danhSachMay = ref([]);
const danhSachKho = ref([]);
const isLoading = ref(false);
const message = ref({ type: '', text: '' });
let modalInstance = null;

// Form Data
const form = reactive({
  maMay: '',
  tenSP: '',
  
  // --- THÔNG TIN GỐC (READ ONLY) ---
  tenHang: '',     
  tenLoai: '',     
  ngayTao: '',     
  soPhieuNhap: '', 
  // ----------------------------------

  soSeri: '',      
  trangThai: 1,    
  tonKho: true,    
  maKho: null      
});

// --- MAPPING TRẠNG THÁI VẬT LÝ ---
const TRANG_THAI_LIST = [
  { id: 1, text: 'Mới (New)', class: 'bg-success' },
  { id: 2, text: 'Like New', class: 'bg-info text-dark' },
  { id: 3, text: 'Hỏng', class: 'bg-danger' },
  { id: 4, text: 'Xác', class: 'bg-dark' },
  { id: 5, text: 'Thu hồi', class: 'bg-warning text-dark' },
  { id: 6, text: 'Nhập Khẩu', class: 'bg-primary' }
];

const getTrangThaiInfo = (id) => {
  return TRANG_THAI_LIST.find(t => t.id === id) || { text: 'Khác', class: 'bg-secondary' };
};

const formatDate = (dateString) => {
  if (!dateString) return '---';
  // Xử lý dạng mảng [yyyy, MM, dd] từ Java LocalDate/LocalDateTime
  if (Array.isArray(dateString)) {
      // Giả sử mảng trả về [2024, 1, 1, 10, 30]
      const [year, month, day, hour, minute] = dateString;
      return `${day}/${month}/${year} ${hour ? hour : ''}:${minute ? minute : ''}`;
  }
  const date = new Date(dateString);
  return date.toLocaleString('vi-VN', { hour12: false });
};

// --- API METHODS ---
const loadData = async () => {
  isLoading.value = true;
  try {
    // Sử dụng api.get để có Token
    const [resMay, resKho] = await Promise.all([
      api.get(API_URL),
      api.get(API_KHO)
    ]);
    danhSachMay.value = resMay.data;
    danhSachKho.value = resKho.data;
  } catch (error) {
    const msg = error.response?.data?.message || error.response?.data || error.message;
    showMessage('danger', 'Lỗi tải dữ liệu: ' + msg);
  } finally {
    isLoading.value = false;
  }
};

const openEditModal = (may) => {
  // 1. Map dữ liệu vào Form
  form.maMay = may.maMay;
  form.tenSP = may.sanPham?.tenSP || '---';
  
  // Logic truy cập lồng nhau chuẩn theo Entity mới
  form.tenHang = may.sanPham?.hangSanXuat?.tenHang || '---';
  form.tenLoai = may.sanPham?.loaiSanPham?.tenLoai || '---'; 

  form.ngayTao = formatDate(may.ngayTao);
  form.soPhieuNhap = may.soPhieuNhap || 'Không có';

  // 2. Thông tin cập nhật
  form.soSeri = may.soSeri || '';
  form.trangThai = may.trangThai || 1;
  form.tonKho = may.tonKho; 
  form.maKho = may.kho?.maKho || null;

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
      // Gửi object Kho để Backend map vào Entity MayIn
      kho: form.maKho ? { maKho: form.maKho } : null
    };
    
    // api.put
    await api.put(`${API_URL}/${form.maMay}`, payload);
    
    showMessage('success', 'Cập nhật thông tin thành công!');
    if (modalInstance) modalInstance.hide();
    loadData();
  } catch (error) {
    const msg = error.response?.data?.message || error.response?.data || error.message;
    showMessage('danger', 'Lỗi cập nhật: ' + msg);
  }
};

const deleteMay = async (id, seri) => {
  if (!confirm(`CẢNH BÁO: Bạn có chắc chắn muốn xóa vĩnh viễn máy ${seri || id}?`)) return;
  try {
    await api.delete(`${API_URL}/${id}`);
    showMessage('success', 'Đã xóa máy thành công!');
    loadData();
  } catch (error) {
    const msg = error.response?.data?.message || error.response?.data || error.message;
    showMessage('danger', 'Không thể xóa: ' + msg);
  }
};

const showMessage = (type, text) => {
  message.value = { type, text };
  setTimeout(() => message.value = { type: '', text: '' }, 3000);
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h3 class="text-primary"><i class="bi bi-qr-code"></i> Quản Lý Danh Mục Máy</h3>
      <button class="btn btn-sm btn-outline-secondary" @click="loadData">
        <i class="bi bi-arrow-clockwise"></i> Tải lại
      </button>
    </div>

    <div v-if="message.text" :class="`alert alert-${message.type} alert-dismissible fade show`">
      {{ message.text }}
      <button type="button" class="btn-close" @click="message.text = ''"></button>
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
                <th width="120px">Trạng Thái Kho</th> <th width="120px">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="isLoading">
                <td colspan="8" class="text-center py-3">Đang tải dữ liệu...</td>
              </tr>
              <tr v-else v-for="(may, index) in danhSachMay" :key="may.maMay">
                <td class="text-center">{{ index + 1 }}</td>
                <td class="fw-bold text-primary">{{ may.maMay || '(Trống)' }}</td>
                <td>
                  <div>{{ may.sanPham?.tenSP }}</div>
                  <small class="text-muted" v-if="may.sanPham?.hangSanXuat">
                    {{ may.sanPham.hangSanXuat.tenHang }}
                  </small>
                </td>
                
                <td class="text-center">
                  <span class="badge bg-light text-dark border">
                    {{ may.sanPham?.loaiSanPham?.tenLoai || '---' }}
                  </span>
                </td>

                <td>{{ may.kho?.tenKho || '---' }}</td>
                
                <td class="text-center">
                  <span :class="['badge', getTrangThaiInfo(may.trangThai).class]">
                    {{ getTrangThaiInfo(may.trangThai).text }}
                  </span>
                </td>

                <td class="text-center">
                    <span v-if="may.tonKho" class="badge bg-success">
                        Còn Hàng
                    </span>
                    <span v-else class="badge bg-secondary">
                        Đã Xuất
                    </span>
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
            </tbody>
          </table>
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
                  <label class="form-label">Tình Trạng (New/LikeNew...)</label>
                  <select v-model="form.trangThai" class="form-select">
                    <option v-for="tt in TRANG_THAI_LIST" :key="tt.id" :value="tt.id">
                      {{ tt.text }}
                    </option>
                  </select>
                </div>

                <div class="col-md-6">
                  <label class="form-label">Vị trí Kho</label>
                  <select v-model="form.maKho" class="form-select">
                    <option :value="null">-- Chưa xác định --</option>
                    <option v-for="k in danhSachKho" :key="k.maKho" :value="k.maKho">
                      {{ k.tenKho }}
                    </option>
                  </select>
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