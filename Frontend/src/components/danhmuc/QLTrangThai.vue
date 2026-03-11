<script setup>
import { ref, onMounted, reactive } from 'vue';
import api from '@/utils/axios'; 
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH API ---
const API_URL = '/trang-thai'; 

// --- STATE ---
const danhSach = ref([]);
const isLoading = ref(false);
const message = ref({ type: '', text: '' }); 
const isEditMode = ref(false); 
let modalInstance = null; 

// Form Data
const form = reactive({
  maTrangThai: null, // Đổi thành null
  tenTrangThai: ''
});

// --- API METHODS ---

// 1. Lấy danh sách
const loadData = async () => {
  isLoading.value = true;
  try {
    const response = await api.get(API_URL);
    danhSach.value = response.data || [];
  } catch (error) {
    const msg = error.response?.data?.message || error.message;
    showMessage('danger', 'Không thể tải dữ liệu: ' + msg);
  } finally {
    isLoading.value = false;
  }
};

// 2. Lưu (Thêm/Sửa)
const saveData = async () => {
  const tenInput = form.tenTrangThai.trim();

  // Validate Rỗng
  if (!tenInput) {
    showMessage('warning', 'Vui lòng nhập Tên Trạng Thái!');
    return;
  }
  
  // Validate Trùng Tên trên Frontend (Bỏ qua chính nó nếu đang Edit)
  const isExistName = danhSach.value.some(
      item => item.tenTrangThai.toLowerCase().trim() === tenInput.toLowerCase() 
              && item.maTrangThai !== form.maTrangThai
  );

  if (isExistName) {
      showMessage('warning', `Tên trạng thái "${tenInput}" đã tồn tại! Vui lòng chọn tên khác.`);
      return;
  }

  try {
    // Không gửi maTrangThai lên nếu là Thêm Mới để DB tự sinh
    const payload = {
        tenTrangThai: tenInput
    };

    if (isEditMode.value) {
      await api.put(`${API_URL}/${form.maTrangThai}`, payload);
      showMessage('success', 'Cập nhật trạng thái thành công!');
    } else {
      await api.post(API_URL, payload);
      showMessage('success', 'Thêm mới trạng thái thành công!');
    }
    closeModal();
    loadData(); 
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.response?.data || error.message;
    showMessage('danger', 'Lỗi lưu dữ liệu: ' + errorMsg);
  }
};

// 3. Xóa
const deleteData = async (id, ten) => {
  if (!confirm(`Bạn có chắc chắn muốn xóa trạng thái "${ten}" không?`)) return;

  try {
    await api.delete(`${API_URL}/${id}`);
    showMessage('success', 'Đã xóa thành công!');
    loadData(); 
  } catch (error) {
    const msg = error.response?.data?.message || error.response?.data || 'Không thể xóa (dữ liệu đang được sử dụng)';
    showMessage('danger', msg);
  }
};

// --- HELPER FUNCTIONS ---
const showMessage = (type, text) => {
  message.value = { type, text };
  setTimeout(() => message.value = { type: '', text: '' }, 3000);
};

const openAddModal = () => {
  isEditMode.value = false;
  form.maTrangThai = null;
  form.tenTrangThai = '';
  
  const modalEl = document.getElementById('modalTrangThai');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

const openEditModal = (item) => {
  isEditMode.value = true;
  form.maTrangThai = item.maTrangThai;
  form.tenTrangThai = item.tenTrangThai;

  const modalEl = document.getElementById('modalTrangThai');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

const closeModal = () => {
  if (modalInstance) {
    modalInstance.hide();
    modalInstance = null;
  }
};

onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4 border-bottom pb-2">
      <h3 class="text-primary"><i class="bi bi-activity"></i> Quản Lý Trạng Thái Máy</h3>
      <div>
           <button class="btn btn-outline-secondary me-2" @click="loadData">
                <i class="bi bi-arrow-clockwise"></i> Tải Lại
            </button>
          <button class="btn btn-primary" @click="openAddModal">
            <i class="bi bi-plus-lg"></i> Thêm Trạng Thái
          </button>
      </div>
    </div>

    <div v-if="message.text" :class="`alert alert-${message.type} alert-dismissible fade show shadow-sm`" role="alert">
      {{ message.text }}
      <button type="button" class="btn-close" @click="message.text = ''"></button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive"> 
            <table class="table table-striped table-hover mb-0 align-middle">
            <thead class="table-dark">
                <tr>
                <th class="text-center" style="width: 100px;">STT</th>
                <th>Tên Trạng Thái</th>
                <th class="text-center" style="width: 200px;">Thao Tác</th>
                </tr>
            </thead>
            <tbody>
                <tr v-if="isLoading">
                <td colspan="3" class="text-center py-4">
                    <div class="spinner-border text-primary spinner-border-sm" role="status"></div>
                    <span class="ms-2">Đang tải dữ liệu...</span>
                </td>
                </tr>
                
                <tr v-else v-for="(item, index) in danhSach" :key="item.maTrangThai">
                
                <td class="text-center fw-bold text-muted">{{ index + 1 }}</td>
                <td class="fw-medium text-primary">{{ item.tenTrangThai }}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-primary me-2" @click="openEditModal(item)">
                        <i class="bi bi-pencil-square"></i> Sửa
                    </button>
                    <button class="btn btn-sm btn-outline-danger" @click="deleteData(item.maTrangThai, item.tenTrangThai)">
                        <i class="bi bi-trash"></i> Xóa
                    </button>
                </td>
                </tr>

                <tr v-if="!isLoading && danhSach.length === 0">
                    <td colspan="3" class="text-center text-muted py-3">Chưa có dữ liệu trạng thái nào.</td>
                </tr>
            </tbody>
            </table>
        </div>
      </div>
    </div>

    <div class="modal fade" id="modalTrangThai" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-light">
            <h5 class="modal-title text-primary">
               <i class="bi bi-activity"></i> {{ isEditMode ? 'Cập Nhật Trạng Thái' : 'Thêm Trạng Thái Mới' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveData">
              <div class="mb-3">
                <label class="form-label fw-bold">Tên Trạng Thái <span class="text-danger">*</span></label>
                <input 
                  v-model="form.tenTrangThai" 
                  type="text" 
                  class="form-control border-primary" 
                  required 
                  placeholder="VD: Mới, Đang sử dụng, Lỗi..."
                  autofocus
                >
              </div>
              
              <div class="text-end mt-4 border-top pt-3">
                <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">Đóng</button>
                <button type="submit" class="btn btn-primary px-4">
                   <i class="bi bi-save"></i> {{ isEditMode ? 'Lưu Thay Đổi' : 'Tạo Mới' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>