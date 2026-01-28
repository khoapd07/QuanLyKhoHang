<script setup>
import { ref, onMounted, reactive } from 'vue';
import axios from 'axios';
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH ---
const API_URL = 'http://localhost:8080/api/chi-nhanh'; 

// --- STATE ---
const danhSach = ref([]);
const isLoading = ref(false);
const message = ref({ type: '', text: '' }); // Thông báo xanh/đỏ
const isEditMode = ref(false);

// Form data (Model)
const form = reactive({
  maKho: null,
  tenKho: '',
  diaChi: ''
});

// Biến giữ instance Modal của Bootstrap
let modalInstance = null;

// --- API METHODS ---

// 1. Lấy danh sách (GET)
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

// 2. Lưu dữ liệu (POST hoặc PUT)
const saveData = async () => {
  if (!form.tenKho) {
    showMessage('warning', 'Vui lòng nhập tên chi nhánh!');
    return;
  }

  try {
    if (isEditMode.value) {
      // Sửa (PUT)
      await axios.put(`${API_URL}/${form.maKho}`, form);
      showMessage('success', 'Cập nhật thành công!');
    } else {
      // Thêm mới (POST) - Lưu ý: Backend ID tự tăng hay nhập tay? 
      // Nếu ID tự tăng thì bỏ maKho, nếu nhập tay thì cần input maKho.
      // Code này giả định ID tự tăng hoặc Backend xử lý.
      await axios.post(API_URL, form);
      showMessage('success', 'Thêm mới thành công!');
    }
    closeModal();
    loadData(); // Tải lại bảng
  } catch (error) {
    showMessage('danger', 'Lỗi lưu dữ liệu: ' + (error.response?.data?.message || error.message));
  }
};

// 3. Xóa (DELETE)
const deleteData = async (id) => {
  if (!confirm('Bạn có chắc muốn xóa chi nhánh này?')) return;
  
  try {
    await axios.delete(`${API_URL}/${id}`);
    showMessage('success', 'Đã xóa thành công!');
    loadData();
  } catch (error) {
    showMessage('danger', 'Không thể xóa (có thể kho đang chứa hàng hóa): ' + error.message);
  }
};

// --- HELPER FUNCTIONS ---

const showMessage = (type, text) => {
  message.value = { type, text };
  setTimeout(() => message.value = { type: '', text: '' }, 3000);
};

const openAddModal = () => {
  isEditMode.value = false;
  // Reset form
  form.maKho = null;
  form.tenKho = '';
  form.diaChi = '';
  
  const modalEl = document.getElementById('modalChiNhanh');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

const openEditModal = (item) => {
  isEditMode.value = true;
  // Fill data
  form.maKho = item.maKho;
  form.tenKho = item.tenKho;
  form.diaChi = item.diaChi;

  const modalEl = document.getElementById('modalChiNhanh');
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
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h3 class="text-primary"><i class="bi bi-shop"></i> Quản Lý Chi Nhánh Kho</h3>
      <button class="btn btn-primary" @click="openAddModal">
        + Thêm Chi Nhánh
      </button>
    </div>

    <div v-if="message.text" :class="`alert alert-${message.type}`" role="alert">
      {{ message.text }}
    </div>

    <div class="card shadow-sm">
      <div class="card-body p-0">
        <table class="table table-hover table-bordered mb-0">
          <thead class="table-light">
            <tr>
              <th class="text-center" width="10%">Mã Kho</th>
              <th width="35%">Tên Chi Nhánh</th>
              <th>Địa Chỉ</th>
              <th class="text-center" width="15%">Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoading">
              <td colspan="4" class="text-center py-3">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else v-for="kho in danhSach" :key="kho.maKho">
              <td class="text-center fw-bold">#{{ kho.maKho }}</td>
              <td>{{ kho.tenKho }}</td>
              <td>{{ kho.diaChi }}</td>
              <td class="text-center">
                <button class="btn btn-sm btn-outline-primary me-2" @click="openEditModal(kho)">
                  Sửa
                </button>
                <button class="btn btn-sm btn-outline-danger" @click="deleteData(kho.maKho)">
                  Xóa
                </button>
              </td>
            </tr>
            <tr v-if="!isLoading && danhSach.length === 0">
              <td colspan="4" class="text-center text-muted">Chưa có dữ liệu.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal fade" id="modalChiNhanh" tabindex="-1" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEditMode ? 'Cập Nhật Chi Nhánh' : 'Thêm Chi Nhánh Mới' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveData">
              
              <div class="mb-3" v-if="isEditMode">
                 <label class="form-label">Mã Kho</label>
                 <input type="text" class="form-control" :value="form.maKho" disabled>
              </div>

              <div class="mb-3">
                <label class="form-label">Tên Chi Nhánh <span class="text-danger">*</span></label>
                <input v-model="form.tenKho" type="text" class="form-control" required placeholder="Ví dụ: Chi Nhánh Cầu Giấy">
              </div>

              <div class="mb-3">
                <label class="form-label">Địa Chỉ</label>
                <textarea v-model="form.diaChi" class="form-control" rows="3" placeholder="Nhập địa chỉ cụ thể..."></textarea>
              </div>

              <div class="text-end mt-4">
                <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">Hủy</button>
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
/* Tùy chỉnh CSS nếu cần */
.table th {
    vertical-align: middle;
}
</style>