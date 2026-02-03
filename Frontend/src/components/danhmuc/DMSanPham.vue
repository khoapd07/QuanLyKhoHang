<template>
  <div class="container-fluid mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h3 class="text-primary"><i class="fas fa-box-open"></i> Quản Lý Danh Mục Sản Phẩm</h3>
      <button class="btn btn-primary fw-bold" @click="openAddModal">
        <i class="fas fa-plus"></i> Thêm Sản Phẩm Mới
      </button>
    </div>

    <div v-if="alertMsg" :class="`alert alert-${alertType} alert-dismissible fade show`" role="alert">
      {{ alertMsg }}
      <button type="button" class="btn-close" @click="alertMsg = ''"></button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body p-0">
        <table class="table table-hover table-striped mb-0 align-middle">
          <thead class="table-dark">
            <tr>
              <th>Mã SP</th>
              <th>Tên Sản Phẩm</th>
              <th>ĐVT</th>
              <th>Hãng SX</th>
              <th>Mô Tả</th>
              <th class="text-center" width="150">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoading">
              <td colspan="6" class="text-center py-4">
                <div class="spinner-border text-primary" role="status"></div>
                <p class="mb-0 mt-2">Đang tải dữ liệu...</p>
              </td>
            </tr>
            <tr v-else-if="danhSach.length === 0">
              <td colspan="6" class="text-center py-4 text-muted">Chưa có sản phẩm nào.</td>
            </tr>
            <tr v-else v-for="sp in danhSach" :key="sp.maSP">
              <td class="fw-bold text-primary">{{ sp.maSP }}</td>
              <td class="fw-bold">{{ sp.tenSP }}</td>
              <td><span class="badge bg-info text-dark">{{ sp.donViTinh }}</span></td>
              
              <td class="text-success fw-bold">
                  {{ sp.hangSanXuat ? sp.hangSanXuat.tenHang : '---' }}
              </td>
              
              <td class="small text-muted">{{ sp.moTa }}</td>
              <td class="text-center">
                <button class="btn btn-sm btn-outline-warning me-2" @click="openEditModal(sp)" title="Sửa">
                  <i class="fas fa-edit"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger" @click="deleteData(sp.maSP)" title="Xóa">
                  <i class="fas fa-trash-alt"></i>
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal fade" id="modalSanPham" tabindex="-1" data-bs-backdrop="static" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header" :class="isEditMode ? 'bg-warning' : 'bg-primary text-white'">
            <h5 class="modal-title">
              {{ isEditMode ? 'Cập Nhật Sản Phẩm' : 'Thêm Sản Phẩm Mới' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          
          <div class="modal-body">
            <form @submit.prevent="saveData">
              <div class="mb-3">
                <label class="form-label">Mã Sản Phẩm <span class="text-danger">*</span></label>
                <input type="text" class="form-control" v-model="form.maSP" required 
                       :disabled="isEditMode" 
                       placeholder="VD: IP15, SS-S24...">
                <div class="form-text text-danger" v-if="isEditMode">Không thể sửa mã sản phẩm.</div>
              </div>

              <div class="mb-3">
                <label class="form-label">Tên Sản Phẩm <span class="text-danger">*</span></label>
                <input type="text" class="form-control" v-model="form.tenSP" required placeholder="Nhập tên sản phẩm">
              </div>

              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Đơn Vị Tính</label>
                  <input type="text" class="form-control" v-model="form.donViTinh" placeholder="Cái, Chiếc, Bộ...">
                </div>
                
                <div class="col-md-6 mb-3">
                  <label class="form-label">Hãng Sản Xuất</label>
                  <select class="form-select" v-model="form.maHang">
                      <option :value="null" disabled>-- Chọn Hãng --</option>
                      <option v-for="h in listHangSanXuat" :key="h.maHang" :value="h.maHang">
                          {{ h.tenHang }}
                      </option>
                  </select>
                </div>
              </div>

              <div class="mb-3">
                <label class="form-label">Mô Tả</label>
                <textarea class="form-control" v-model="form.moTa" rows="3" placeholder="Ghi chú thêm về sản phẩm"></textarea>
              </div>

              <div class="text-end">
                <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">Hủy</button>
                <button type="submit" class="btn" :class="isEditMode ? 'btn-warning' : 'btn-primary'">
                  <i class="fas fa-save"></i> Lưu Dữ Liệu
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import api from '@/utils/axios'; 
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH API ---
const API_URL = '/san-pham';
// Giả định API lấy danh sách hãng là /api/hang-san-xuat
// Nếu bạn chưa có API này, bạn cần tạo thêm HangSanXuatController hoặc dùng tạm API khác
const API_HANG_SX = '/hang-san-xuat'; 

// --- STATE ---
const danhSach = ref([]);
const listHangSanXuat = ref([]); // Danh sách hãng để bind vào Select
const isLoading = ref(false);
const isEditMode = ref(false);
const alertMsg = ref('');
const alertType = ref('success'); 

let modalInstance = null;

// Form Data (Lưu ý: dùng maHang để bind v-model cho select)
const form = reactive({
  maSP: '',
  tenSP: '',
  donViTinh: '',
  maHang: null, // Chỉ lưu ID hãng
  moTa: ''
});

// --- METHODS ---

// 1. Load Master Data (Danh sách hãng & Danh sách sản phẩm)
const loadData = async () => {
  isLoading.value = true;
  try {
    // Gọi song song 2 API
    const [resSP, resHang] = await Promise.all([
        api.get(API_URL),
        api.get(API_HANG_SX) // API lấy danh sách hãng
    ]);

    danhSach.value = resSP.data;
    listHangSanXuat.value = resHang.data;

  } catch (error) {
    showAlert('Lỗi tải dữ liệu: ' + (error.response?.data?.message || error.message), 'danger');
  } finally {
    isLoading.value = false;
  }
};

// 2. Lưu
const saveData = async () => {
  if (!form.maSP || !form.tenSP) {
    showAlert('Vui lòng nhập Mã và Tên sản phẩm!', 'warning');
    return;
  }

  // Chuẩn bị payload: Backend cần object HangSanXuat, nhưng form chỉ có maHang
  // Ta cần map lại: { ...form, hangSanXuat: { maHang: form.maHang } }
  const payload = {
      maSP: form.maSP,
      tenSP: form.tenSP,
      donViTinh: form.donViTinh,
      moTa: form.moTa,
      hangSanXuat: form.maHang ? { maHang: form.maHang } : null
  };

  try {
    if (isEditMode.value) {
      await api.put(`${API_URL}/${form.maSP}`, payload);
      showAlert('Cập nhật thành công!', 'success');
    } else {
      await api.post(API_URL, payload);
      showAlert('Thêm mới thành công!', 'success');
    }
    
    closeModal();
    loadData(); 
  } catch (error) {
    const msg = error.response?.data || error.message; 
    showAlert('Lỗi: ' + msg, 'danger');
  }
};

// 3. Xóa
const deleteData = async (id) => {
  if (!confirm(`Bạn có chắc muốn xóa sản phẩm [${id}] không?`)) return;

  try {
    await api.delete(`${API_URL}/${id}`);
    showAlert('Đã xóa thành công!', 'success');
    loadData();
  } catch (error) {
    const msg = error.response?.data || error.message;
    showAlert('Không thể xóa: ' + msg, 'danger');
  }
};

// --- HELPER FUNCTIONS ---

const openAddModal = () => {
  isEditMode.value = false;
  resetForm();
  showModal();
};

const openEditModal = (item) => {
  isEditMode.value = true;
  
  // Fill data vào form
  form.maSP = item.maSP;
  form.tenSP = item.tenSP;
  form.donViTinh = item.donViTinh;
  form.moTa = item.moTa;
  
  // Map Hãng SX: Lấy ID từ object hangSanXuat
  if (item.hangSanXuat) {
      form.maHang = item.hangSanXuat.maHang;
  } else {
      form.maHang = null;
  }

  showModal();
};

const showModal = () => {
  const modalEl = document.getElementById('modalSanPham');
  if (modalEl) {
    modalInstance = new bootstrap.Modal(modalEl);
    modalInstance.show();
  }
};

const closeModal = () => {
  if (modalInstance) {
    modalInstance.hide();
    modalInstance = null;
  }
};

const resetForm = () => {
  form.maSP = '';
  form.tenSP = '';
  form.donViTinh = '';
  form.maHang = null;
  form.moTa = '';
};

const showAlert = (msg, type) => {
  alertMsg.value = msg;
  alertType.value = type;
  setTimeout(() => alertMsg.value = '', 3000);
};

// --- LIFECYCLE ---
onMounted(() => {
  loadData();
});
</script>