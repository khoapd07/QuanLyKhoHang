<script setup>
import { ref, onMounted, reactive } from 'vue';
import api from '@/utils/axios'; 
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH API ---
const API_URL = '/hinh-thuc-xuat'; 

// --- STATE ---
const danhSach = ref([]);
const isLoading = ref(false);
const message = ref({ type: '', text: '' }); 
const isEditMode = ref(false); 
let modalInstance = null; 

// Form Data sử dụng maHT và tenHT
const form = reactive({
  maHT: null, 
  tenHT: ''
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
  const tenInput = form.tenHT.trim();

  if (!tenInput) {
    showMessage('warning', 'Vui lòng nhập Tên Hình Thức Xuất!');
    return;
  }
  
  // Validate Trùng Tên trên Frontend
  const isExistName = danhSach.value.some(
      item => item.tenHT.toLowerCase().trim() === tenInput.toLowerCase() 
              && item.maHT !== form.maHT
  );

  if (isExistName) {
      showMessage('warning', `Tên hình thức "${tenInput}" đã tồn tại! Vui lòng chọn tên khác.`);
      return;
  }

  try {
    const payload = {
        tenHT: tenInput
    };

    if (isEditMode.value) {
      await api.put(`${API_URL}/${form.maHT}`, payload);
      showMessage('success', 'Cập nhật hình thức xuất thành công!');
    } else {
      await api.post(API_URL, payload);
      showMessage('success', 'Thêm mới hình thức xuất thành công!');
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
  if (!confirm(`Bạn có chắc chắn muốn xóa hình thức xuất "${ten}" không?`)) return;

  try {
    await api.delete(`${API_URL}/${id}`);
    showMessage('success', 'Đã xóa hình thức xuất thành công!');
    loadData(); 
  } catch (error) {
    // Thông báo lỗi nếu hình thức xuất đã có phiếu xuất sử dụng
    const msg = error.response?.data?.message || error.response?.data || 'Không thể xóa (dữ liệu đang được sử dụng trong Phiếu Xuất)';
    showMessage('danger', msg);
  }
};

// --- HELPER FUNCTIONS ---
const showMessage = (type, text) => {
  message.value = { type, text };
  setTimeout(() => message.value = { type: '', text: '' }, 4000);
};

const openAddModal = () => {
  isEditMode.value = false;
  form.maHT = null;
  form.tenHT = '';
  
  const modalEl = document.getElementById('modalHinhThucXuat');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

const openEditModal = (item) => {
  isEditMode.value = true;
  form.maHT = item.maHT;
  form.tenHT = item.tenHT;

  const modalEl = document.getElementById('modalHinhThucXuat');
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
      <h3 class="text-success"><i class="bi bi-box-arrow-right"></i> Quản Lý Hình Thức Xuất</h3>
      <div>
           <button class="btn btn-outline-secondary me-2" @click="loadData">
                <i class="bi bi-arrow-clockwise"></i> Tải Lại
            </button>
          <button class="btn btn-success" @click="openAddModal">
            <i class="bi bi-plus-lg"></i> Thêm Mới
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
                <th>Tên Hình Thức Xuất</th>
                <th class="text-center" style="width: 200px;">Thao Tác</th>
                </tr>
            </thead>
            <tbody>
                <tr v-if="isLoading">
                <td colspan="3" class="text-center py-4">
                    <div class="spinner-border text-success spinner-border-sm" role="status"></div>
                    <span class="ms-2">Đang tải dữ liệu...</span>
                </td>
                </tr>
                
                <tr v-else v-for="(item, index) in danhSach" :key="item.maHT">
                
                <td class="text-center fw-bold text-muted">{{ index + 1 }}</td>
                <td class="fw-medium text-success">{{ item.tenHT }}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-success me-2" @click="openEditModal(item)">
                        <i class="bi bi-pencil-square"></i> Sửa
                    </button>
                    <button class="btn btn-sm btn-outline-danger" @click="deleteData(item.maHT, item.tenHT)">
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
    </div>

    <div class="modal fade" id="modalHinhThucXuat" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header bg-light">
            <h5 class="modal-title text-success">
               <i class="bi bi-box-arrow-right"></i> {{ isEditMode ? 'Cập Nhật Hình Thức Xuất' : 'Thêm Hình Thức Xuất Mới' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveData">
              <div class="mb-3">
                <label class="form-label fw-bold">Tên Hình Thức <span class="text-danger">*</span></label>
                <input 
                  v-model="form.tenHT" 
                  type="text" 
                  class="form-control border-success" 
                  required 
                  placeholder="VD: Xuất bán, Xuất thanh lý..."
                  autofocus
                >
              </div>
              
              <div class="text-end mt-4 border-top pt-3">
                <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">Đóng</button>
                <button type="submit" class="btn btn-success px-4">
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