<script setup>
import { ref, onMounted, reactive } from 'vue';
import axios from 'axios';
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH API ---
const API_URL = 'http://localhost:8080/api/loai-san-pham';

// --- STATE (Trạng thái) ---
const danhSach = ref([]);
const isLoading = ref(false);
const message = ref({ type: '', text: '' }); // Thông báo
const isEditMode = ref(false); // Cờ kiểm tra đang Thêm hay Sửa
let modalInstance = null; // Biến giữ Modal Bootstrap

// Model Form (Dựa theo Entity LoaiSanPham trong Java)
const form = reactive({
  maLoai: '',
  tenLoai: ''
});

// --- CÁC HÀM TƯƠNG TÁC API ---

// 1. Lấy danh sách (GET)
const loadData = async () => {
  isLoading.value = true;
  try {
    const response = await axios.get(API_URL);
    danhSach.value = response.data;
  } catch (error) {
    showMessage('danger', 'Không thể tải dữ liệu: ' + error.message);
  } finally {
    isLoading.value = false;
  }
};

// 2. Lưu dữ liệu (POST / PUT)
const saveData = async () => {
  // Validate cơ bản
  if (!form.tenLoai) {
    showMessage('warning', 'Vui lòng nhập tên loại!');
    return;
  }

  try {
    if (isEditMode.value) {
      // Cập nhật (PUT)
      await axios.put(`${API_URL}/${form.maLoai}`, form);
      showMessage('success', 'Cập nhật loại sản phẩm thành công!');
    } else {
      // Thêm mới (POST)
      await axios.post(API_URL, form);
      showMessage('success', 'Thêm mới loại sản phẩm thành công!');
    }
    closeModal();
    loadData(); // Load lại bảng
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.message;
    showMessage('danger', 'Lỗi lưu dữ liệu: ' + errorMsg);
  }
};

// 3. Xóa dữ liệu (DELETE)
const deleteData = async (id) => {
  if (!confirm(`Bạn có chắc chắn muốn xóa loại sản phẩm #${id} không?`)) return;

  try {
    await axios.delete(`${API_URL}/${id}`);
    showMessage('success', 'Đã xóa thành công!');
    loadData();
  } catch (error) {
    showMessage('danger', 'Không thể xóa (có thể dữ liệu đang được sử dụng): ' + error.message);
  }
};

// --- HÀM HỖ TRỢ GIAO DIỆN ---

// Hiển thị thông báo tạm thời
const showMessage = (type, text) => {
  message.value = { type, text };
  setTimeout(() => message.value = { type: '', text: '' }, 3000);
};

// Mở Modal Thêm mới
const openAddModal = () => {
  isEditMode.value = false;
  form.maLoai = '';
  form.tenLoai = '';
  
  const modalEl = document.getElementById('modalLoaiSP');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

// Mở Modal Sửa
const openEditModal = (item) => {
  isEditMode.value = true;
  form.maLoai = item.maLoai; // Copy dữ liệu vào form
  form.tenLoai = item.tenLoai;

  const modalEl = document.getElementById('modalLoaiSP');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

// Đóng Modal
const closeModal = () => {
  if (modalInstance) {
    modalInstance.hide();
    modalInstance = null;
  }
};

// --- KHỞI TẠO ---
onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4 border-bottom pb-2">
      <h3 class="text-primary"><i class="bi bi-tags"></i> Quản Lý Loại Sản Phẩm</h3>
      <button class="btn btn-primary" @click="openAddModal">
        <i class="bi bi-plus-lg"></i> Thêm Loại Mới
      </button>
    </div>

    <div v-if="message.text" :class="`alert alert-${message.type} alert-dismissible fade show`" role="alert">
      {{ message.text }}
      <button type="button" class="btn-close" @click="message.text = ''"></button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body p-0">
        <table class="table table-striped table-hover mb-0">
          <thead class="table-dark">
            <tr>
              <th class="text-center" style="width: 150px;">Mã Loại</th>
              <th>Tên Loại Sản Phẩm</th>
              <th class="text-center" style="width: 200px;">Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoading">
              <td colspan="3" class="text-center py-4">
                <div class="spinner-border text-primary" role="status"></div>
                <div class="mt-2">Đang tải dữ liệu...</div>
              </td>
            </tr>
            
            <tr v-else v-for="item in danhSach" :key="item.maLoai">
              <td class="text-center fw-bold">{{ item.maLoai }}</td>
              <td>{{ item.tenLoai }}</td>
              <td class="text-center">
                <button class="btn btn-sm btn-outline-warning me-2" @click="openEditModal(item)">
                  <i class="bi bi-pencil"></i> Sửa
                </button>
                <button class="btn btn-sm btn-outline-danger" @click="deleteData(item.maLoai)">
                  <i class="bi bi-trash"></i> Xóa
                </button>
              </td>
            </tr>

            <tr v-if="!isLoading && danhSach.length === 0">
              <td colspan="3" class="text-center text-muted py-3">Chưa có dữ liệu nào.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal fade" id="modalLoaiSP" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEditMode ? 'Cập Nhật Loại Sản Phẩm' : 'Thêm Loại Sản Phẩm Mới' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveData">
              
              

              <div class="mb-3">
                <label class="form-label">Tên Loại <span class="text-danger">*</span></label>
                <input 
                  v-model="form.tenLoai" 
                  type="text" 
                  class="form-control" 
                  required 
                  placeholder="Ví dụ: Điện thoại, Laptop..."
                >
              </div>

              <div class="text-end mt-4">
                <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">Đóng</button>
                <button type="submit" class="btn btn-primary">
                  {{ isEditMode ? 'Lưu Thay Đổi' : 'Tạo Mới' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>

<style scoped>
/* CSS tùy chỉnh cho component này */
.table th, .table td {
  vertical-align: middle;
}
</style>