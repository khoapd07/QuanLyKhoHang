<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import api from '@/utils/axios'; 
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH ---
const API_URL = '/chi-nhanh'; 

// --- STATE ---
const danhSach = ref([]);
const isLoading = ref(false);
const message = ref({ type: '', text: '' }); 
const isEditMode = ref(false);
let modalInstance = null;

// --- STATE PHÂN TRANG (Server-side) ---
const currentPage = ref(0);
const itemsPerPage = ref(20); // 20 kho/trang
const totalPages = ref(0);
const totalElements = ref(0);

// --- COMPUTED: TÍNH TOÁN THANH PHÂN TRANG ---
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

// Form data (Model)
const form = reactive({
  maKho: null, tenKho: '', diaChi: ''
});

// --- API METHODS ---

// 1. Lấy danh sách (GET) - Có phân trang
const loadData = async (page = 0) => {
  isLoading.value = true;
  try {
    const response = await api.get(API_URL, {
        params: { page: page, size: itemsPerPage.value }
    });
    
    // [SỬA] Xử lý an toàn dữ liệu trả về để tránh lỗi STT
    if (response.data) {
        danhSach.value = response.data.content || [];
        totalPages.value = response.data.totalPages || 0;
        totalElements.value = response.data.totalElements || 0;
        // Nếu backend trả về null/undefined thì gán = 0
        currentPage.value = (typeof response.data.number === 'number') ? response.data.number : 0;
    }

  } catch (error) {
    const msg = error.response?.data?.message || error.message;
    showMessage('danger', 'Lỗi tải dữ liệu: ' + msg);
  } finally {
    isLoading.value = false;
  }
};

// Chuyển trang
const changePage = (page) => {
    if (page >= 0 && page < totalPages.value) {
        loadData(page);
    }
};

// 2. Lưu dữ liệu
const saveData = async () => {
  if (!form.tenKho) {
    showMessage('warning', 'Vui lòng nhập tên chi nhánh!');
    return;
  }

  try {
    if (isEditMode.value) {
      await api.put(`${API_URL}/${form.maKho}`, form);
      showMessage('success', 'Cập nhật thành công!');
    } else {
      await api.post(API_URL, form);
      showMessage('success', 'Thêm mới thành công!');
    }
    closeModal();
    loadData(currentPage.value); // Load lại trang hiện tại
  } catch (error) {
    const msg = error.response?.data?.message || error.response?.data || error.message;
    showMessage('danger', 'Lỗi lưu dữ liệu: ' + msg);
  }
};

// 3. Xóa
const deleteData = async (id) => {
  if (!confirm('Bạn có chắc muốn xóa chi nhánh này?')) return;
  
  try {
    await api.delete(`${API_URL}/${id}`);
    showMessage('success', 'Đã xóa thành công!');
    loadData(0); // Xóa xong về trang đầu
  } catch (error) {
    const msg = error.response?.data?.message || error.response?.data || error.message;
    showMessage('danger', 'Không thể xóa (có thể kho đang chứa hàng hóa): ' + msg);
  }
};

// --- HELPER FUNCTIONS ---
const showMessage = (type, text) => {
  message.value = { type, text };
  setTimeout(() => message.value = { type: '', text: '' }, 3000);
};

const openAddModal = () => {
  isEditMode.value = false;
  form.maKho = null; form.tenKho = ''; form.diaChi = '';
  
  const modalEl = document.getElementById('modalChiNhanh');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

const openEditModal = (item) => {
  isEditMode.value = true;
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
  loadData(0);
});
</script>

<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3 border-bottom pb-2">
      <h3 class="text-primary"><i class="bi bi-shop"></i> Quản Lý Chi Nhánh Kho</h3>
      <div>
           <button class="btn btn-outline-secondary me-2" @click="loadData(currentPage)">
                <i class="bi bi-arrow-clockwise"></i>
            </button>
          <button class="btn btn-primary" @click="openAddModal">
            <i class="bi bi-plus-lg"></i> Thêm Chi Nhánh
          </button>
      </div>
    </div>

    <div v-if="message.text" :class="`alert alert-${message.type} alert-dismissible fade show`" role="alert">
      {{ message.text }}
      <button type="button" class="btn-close" @click="message.text = ''"></button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive"> <table class="table table-hover table-striped mb-0 align-middle">
            <thead class="table-dark">
                <tr>
                <th class="text-center" width="80px">STT</th> <th class="text-center" width="10%">Mã Kho</th>
                <th width="35%">Tên Chi Nhánh</th>
                <th>Địa Chỉ</th>
                <th class="text-center" width="15%">Thao Tác</th>
                </tr>
            </thead>
            <tbody>
                <tr v-if="isLoading">
                <td colspan="5" class="text-center py-4">
                    <div class="spinner-border text-primary spinner-border-sm" role="status"></div>
                    <span class="ms-2">Đang tải dữ liệu...</span>
                </td>
                </tr>
                <tr v-else v-for="(kho, index) in danhSach" :key="kho.maKho">
                
                <td class="text-center">
                    {{ ((currentPage || 0) * itemsPerPage) + index + 1 }}
                </td>

                <td class="text-center fw-bold text-muted">#{{ kho.maKho }}</td>
                <td class="fw-medium text-primary">{{ kho.tenKho }}</td>
                <td>{{ kho.diaChi }}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-primary me-2" @click="openEditModal(kho)">
                    <i class="bi bi-pencil-square"></i> Sửa
                    </button>
                    <button class="btn btn-sm btn-outline-danger" @click="deleteData(kho.maKho)">
                    <i class="bi bi-trash"></i> Xóa
                    </button>
                </td>
                </tr>
                <tr v-if="!isLoading && danhSach && danhSach.length === 0">
                <td colspan="5" class="text-center text-muted py-3">Chưa có dữ liệu.</td>
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
                        <a class="page-link" href="#" 
                            @click.prevent="page !== '...' ? changePage(page - 1) : null">
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
              trong tổng {{ paginationInfo.total }} kho
          </div>
      </div>
    </div>

    <div class="modal fade" id="modalChiNhanh" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
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
                 <label class="form-label text-muted">Mã Kho</label>
                 <input type="text" class="form-control" :value="form.maKho" disabled readonly>
              </div>

              <div class="mb-3">
                <label class="form-label">Tên Chi Nhánh <span class="text-danger">*</span></label>
                <input 
                  v-model="form.tenKho" 
                  type="text" 
                  class="form-control" 
                  required 
                  placeholder="Ví dụ: Chi Nhánh Cầu Giấy"
                  autofocus
                >
              </div>

              <div class="mb-3">
                <label class="form-label">Địa Chỉ</label>
                <textarea v-model="form.diaChi" class="form-control" rows="3" placeholder="Nhập địa chỉ cụ thể..."></textarea>
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