<script setup>
import { ref, onMounted, reactive } from 'vue';
import axios from 'axios';
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH API ---
const API_URL = 'http://localhost:8080/api/don-vi';

// --- TRẠNG THÁI (STATE) ---
const danhSach = ref([]);
const isLoading = ref(false);
const message = ref({ type: '', text: '' });
const isEditMode = ref(false);
let modalInstance = null;

// Form Data
const form = reactive({
  maDonVi: '',
  tenDonVi: '',
  soDienThoai: '',
  email: '',
  diaChi: '',
  loaiDonVi: 1 // Mặc định 1 (Ví dụ: 1 là NCC, 2 là Khách hàng - tuỳ dữ liệu mẫu của bạn)
});

// Danh sách loại đơn vị (Tạm thời hardcode, nếu có API LoaiDonVi thì gọi API đổ vào đây)
const listLoaiDonVi = [
  { id: 1, ten: 'Nhà Cung Cấp' },
  { id: 2, ten: 'Khách Hàng' },
  { id: 3, ten: 'Đối Tác Khác' }
];

// --- API METHODS ---

// 1. Lấy danh sách
const loadData = async () => {
  isLoading.value = true;
  try {
    const response = await axios.get(API_URL);
    danhSach.value = response.data;
  } catch (error) {
    showMessage('danger', 'Lỗi tải dữ liệu: ' + error.message);
  } finally {
    isLoading.value = false;
  }
};

// 2. Lưu (Thêm / Sửa)
const saveData = async () => {
  // Validate cơ bản
  if (!form.maDonVi || !form.tenDonVi) {
    showMessage('warning', 'Vui lòng nhập Mã đơn vị và Tên đơn vị!');
    return;
  }

  try {
    if (isEditMode.value) {
      // Cập nhật (PUT)
      await axios.put(`${API_URL}/${form.maDonVi}`, form);
      showMessage('success', 'Cập nhật thông tin thành công!');
    } else {
      // Thêm mới (POST)
      await axios.post(API_URL, form);
      showMessage('success', 'Thêm mới đơn vị thành công!');
    }
    closeModal();
    loadData();
  } catch (error) {
    // Lấy thông báo lỗi từ Backend (dòng return ResponseEntity.badRequest().body(...))
    const msg = error.response?.data || error.message;
    showMessage('danger', 'Lỗi: ' + msg);
  }
};

// 3. Xóa
const deleteData = async (id) => {
  if (!confirm(`Bạn có chắc chắn muốn xóa đơn vị [${id}] không?`)) return;

  try {
    await axios.delete(`${API_URL}/${id}`);
    showMessage('success', 'Đã xóa thành công!');
    loadData();
  } catch (error) {
    const msg = error.response?.data || error.message;
    showMessage('danger', 'Không thể xóa: ' + msg);
  }
};

// --- HELPER FUNCTIONS ---

// Helper hiển thị tên loại đơn vị từ ID
const getTenLoai = (id) => {
  const loai = listLoaiDonVi.find(item => item.id === id);
  return loai ? loai.ten : id;
};

const showMessage = (type, text) => {
  message.value = { type, text };
  setTimeout(() => message.value = { type: '', text: '' }, 4000);
};

const openAddModal = () => {
  isEditMode.value = false;
  // Reset form
  form.maDonVi = '';
  form.tenDonVi = '';
  form.soDienThoai = '';
  form.email = '';
  form.diaChi = '';
  form.loaiDonVi = 1;

  const modalEl = document.getElementById('modalDonVi');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

const openEditModal = (item) => {
  isEditMode.value = true;
  // Fill data
  form.maDonVi = item.maDonVi;
  form.tenDonVi = item.tenDonVi;
  form.soDienThoai = item.soDienThoai;
  form.email = item.email;
  form.diaChi = item.diaChi;
  form.loaiDonVi = item.loaiDonVi;

  const modalEl = document.getElementById('modalDonVi');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

const closeModal = () => {
  if (modalInstance) {
    modalInstance.hide();
    modalInstance = null;
  }
};

// --- LIFECYCLE ---
onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4 border-bottom pb-2">
      <h3 class="text-primary"><i class="bi bi-people-fill"></i> Quản Lý Đối Tác (Đơn Vị)</h3>
      <button class="btn btn-primary" @click="openAddModal">
        <i class="bi bi-person-plus-fill"></i> Thêm Đơn Vị
      </button>
    </div>

    <div v-if="message.text" :class="`alert alert-${message.type} alert-dismissible fade show`">
      {{ message.text }}
      <button type="button" class="btn-close" @click="message.text = ''"></button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-striped mb-0 align-middle">
            <thead class="table-dark">
              <tr>
                <th>Mã ĐV</th>
                <th>Tên Đơn Vị</th>
                <th>Liên Hệ</th>
                <th>Loại</th>
                <th>Địa Chỉ</th>
                <th class="text-center" style="min-width: 150px;">Thao Tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="isLoading">
                <td colspan="6" class="text-center py-4">Đang tải dữ liệu...</td>
              </tr>

              <tr v-else v-for="item in danhSach" :key="item.maDonVi">
                <td class="fw-bold text-primary">{{ item.maDonVi }}</td>
                <td class="fw-medium">{{ item.tenDonVi }}</td>
                <td>
                  <div v-if="item.soDienThoai"><i class="bi bi-telephone"></i> {{ item.soDienThoai }}</div>
                  <div v-if="item.email" class="small text-muted"><i class="bi bi-envelope"></i> {{ item.email }}</div>
                </td>
                <td>
                  <span class="badge bg-info text-dark">{{ getTenLoai(item.loaiDonVi) }}</span>
                </td>
                <td class="small">{{ item.diaChi }}</td>
                <td class="text-center">
                  <button class="btn btn-sm btn-outline-primary me-2" @click="openEditModal(item)">
                    <i class="bi bi-pencil-square"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteData(item.maDonVi)">
                    <i class="bi bi-trash"></i>
                  </button>
                </td>
              </tr>

              <tr v-if="!isLoading && danhSach.length === 0">
                <td colspan="6" class="text-center text-muted py-3">Chưa có dữ liệu đơn vị.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="modal fade" id="modalDonVi" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEditMode ? 'Cập Nhật Thông Tin' : 'Thêm Đơn Vị Mới' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveData">
              <div class="row g-3">
                
                <div class="col-md-6">
                  <label class="form-label">Mã Đơn Vị <span class="text-danger">*</span></label>
                  <input 
                    v-model="form.maDonVi" 
                    type="text" 
                    class="form-control" 
                    required 
                    :disabled="isEditMode"
                    placeholder="VD: NCC01, KH02..."
                  >
                  <div class="form-text" v-if="!isEditMode">Mã không được trùng và không thể sửa sau khi tạo.</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label">Loại Đơn Vị</label>
                  <select v-model="form.loaiDonVi" class="form-select">
                    <option v-for="loai in listLoaiDonVi" :key="loai.id" :value="loai.id">
                      {{ loai.ten }}
                    </option>
                  </select>
                </div>

                <div class="col-12">
                  <label class="form-label">Tên Đơn Vị <span class="text-danger">*</span></label>
                  <input v-model="form.tenDonVi" type="text" class="form-control" required placeholder="Nhập tên công ty hoặc khách hàng">
                </div>

                <div class="col-md-6">
                  <label class="form-label">Số Điện Thoại</label>
                  <input v-model="form.soDienThoai" type="text" class="form-control" placeholder="09xxxxxxx">
                </div>

                <div class="col-md-6">
                  <label class="form-label">Email</label>
                  <input v-model="form.email" type="email" class="form-control" placeholder="email@example.com">
                </div>

                <div class="col-12">
                  <label class="form-label">Địa Chỉ</label>
                  <textarea v-model="form.diaChi" class="form-control" rows="2" placeholder="Địa chỉ chi tiết..."></textarea>
                </div>

              </div>

              <div class="text-end mt-4">
                <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">Hủy</button>
                <button type="submit" class="btn btn-primary">
                  {{ isEditMode ? 'Lưu Thay Đổi' : 'Thêm Mới' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>