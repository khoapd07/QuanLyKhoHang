<script setup>
import { ref, onMounted, reactive } from 'vue';
import axios from 'axios';
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH API ---
const API_URL = 'http://localhost:8080/api/hang-san-xuat';

// --- TRẠNG THÁI (STATE) ---
const danhSach = ref([]);
const isLoading = ref(false);
const message = ref({ type: '', text: '' });
const isEditMode = ref(false);
let modalInstance = null;

// Form Model (Khớp với Entity HangSanXuat)
const form = reactive({
  maHang: null,
  tenHang: ''
});

// --- CÁC HÀM XỬ LÝ API ---

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

// 2. Lưu (Thêm mới hoặc Cập nhật)
const saveData = async () => {
  if (!form.tenHang.trim()) {
    showMessage('warning', 'Vui lòng nhập tên hãng sản xuất!');
    return;
  }

  try {
    if (isEditMode.value) {
      // Cập nhật (PUT)
      await axios.put(`${API_URL}/${form.maHang}`, form);
      showMessage('success', 'Cập nhật thành công!');
    } else {
      // Thêm mới (POST) - ID tự tăng nên backend tự xử lý
      await axios.post(API_URL, form);
      showMessage('success', 'Thêm mới thành công!');
    }
    closeModal();
    loadData();
  } catch (error) {
    showMessage('danger', 'Lỗi lưu dữ liệu: ' + (error.response?.data?.message || error.message));
  }
};

// 3. Xóa
const deleteData = async (id) => {
  if (!confirm('Bạn có chắc chắn muốn xóa hãng này không?')) return;

  try {
    await axios.delete(`${API_URL}/${id}`);
    showMessage('success', 'Đã xóa hãng sản xuất!');
    loadData();
  } catch (error) {
    showMessage('danger', 'Không thể xóa (có thể hãng này đang có sản phẩm): ' + error.message);
  }
};

// --- HÀM HỖ TRỢ GIAO DIỆN ---

const showMessage = (type, text) => {
  message.value = { type, text };
  setTimeout(() => message.value = { type: '', text: '' }, 3000);
};

const openAddModal = () => {
  isEditMode.value = false;
  form.maHang = null;
  form.tenHang = '';
  
  const modalEl = document.getElementById('modalHangSX');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

const openEditModal = (item) => {
  isEditMode.value = true;
  form.maHang = item.maHang;
  form.tenHang = item.tenHang;

  const modalEl = document.getElementById('modalHangSX');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

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
      <h3 class="text-primary"><i class="bi bi-buildings"></i> Quản Lý Hãng Sản Xuất</h3>
      <button class="btn btn-primary" @click="openAddModal">
        <i class="bi bi-plus-lg"></i> Thêm Hãng Mới
      </button>
    </div>

    <div v-if="message.text" :class="`alert alert-${message.type} alert-dismissible fade show`">
      {{ message.text }}
      <button type="button" class="btn-close" @click="message.text = ''"></button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body p-0">
        <table class="table table-hover mb-0">
          <thead class="table-dark">
            <tr>
              <th class="text-center" width="10%">ID</th>
              <th>Tên Hãng Sản Xuất</th>
              <th class="text-center" width="20%">Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoading">
              <td colspan="3" class="text-center py-4">Đang tải dữ liệu...</td>
            </tr>
            
            <tr v-else v-for="item in danhSach" :key="item.maHang">
              <td class="text-center fw-bold text-muted">{{ item.maHang }}</td>
              <td class="fw-medium">{{ item.tenHang }}</td>
              <td class="text-center">
                <button class="btn btn-sm btn-outline-primary me-2" @click="openEditModal(item)">
                  <i class="bi bi-pencil-square"></i> Sửa
                </button>
                <button class="btn btn-sm btn-outline-danger" @click="deleteData(item.maHang)">
                  <i class="bi bi-trash"></i> Xóa
                </button>
              </td>
            </tr>

            <tr v-if="!isLoading && danhSach.length === 0">
              <td colspan="3" class="text-center text-muted py-3">Chưa có dữ liệu hãng sản xuất.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal fade" id="modalHangSX" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEditMode ? 'Cập Nhật Hãng' : 'Thêm Hãng Sản Xuất' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveData">
              
              <div class="mb-3" v-if="isEditMode">
                <label class="form-label">Mã Hãng</label>
                <input type="text" class="form-control" :value="form.maHang" disabled>
              </div>

              <div class="mb-3">
                <label class="form-label">Tên Hãng <span class="text-danger">*</span></label>
                <input 
                  v-model="form.tenHang" 
                  type="text" 
                  class="form-control" 
                  required 
                  placeholder="Ví dụ: Canon, HP, Apple..."
                >
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