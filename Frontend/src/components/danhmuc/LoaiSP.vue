<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import api from '@/utils/axios'; 
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH API ---
const API_URL = '/loai-san-pham';

// --- STATE ---
const danhSach = ref([]);
const isLoading = ref(false);
const message = ref({ type: '', text: '' }); 
const isEditMode = ref(false); 
let modalInstance = null; 

// --- STATE PHÂN TRANG (Server-side) ---
const currentPage = ref(0);
const itemsPerPage = ref(20); // 20 item/trang
const totalPages = ref(0);
const totalElements = ref(0);

// --- COMPUTED: TÍNH TOÁN PHÂN TRANG ---
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

// Model Form
const form = reactive({
  maLoai: '',
  tenLoai: ''
});

// --- CÁC HÀM TƯƠNG TÁC API ---

// 1. Lấy danh sách (GET) - Có phân trang
const loadData = async (page = 0) => {
  isLoading.value = true;
  try {
    const response = await api.get(API_URL, {
        params: { page: page, size: itemsPerPage.value }
    });
    
    // Cập nhật State từ Page<LoaiSanPham>
    danhSach.value = response.data.content;
    totalPages.value = response.data.totalPages;
    totalElements.value = response.data.totalElements;
    currentPage.value = response.data.number;
    
  } catch (error) {
    const msg = error.response?.data?.message || error.message;
    showMessage('danger', 'Không thể tải dữ liệu: ' + msg);
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

// 2. Lưu dữ liệu (POST / PUT)
const saveData = async () => {
  if (!form.tenLoai.trim()) {
    showMessage('warning', 'Vui lòng nhập tên loại sản phẩm!');
    return;
  }

  try {
    if (isEditMode.value) {
      await api.put(`${API_URL}/${form.maLoai}`, form);
      showMessage('success', 'Cập nhật loại sản phẩm thành công!');
    } else {
      await api.post(API_URL, form);
      showMessage('success', 'Thêm mới loại sản phẩm thành công!');
    }
    closeModal();
    loadData(currentPage.value); // Load lại trang hiện tại
  } catch (error) {
    const errorMsg = error.response?.data?.message || error.response?.data || error.message;
    showMessage('danger', 'Lỗi lưu dữ liệu: ' + errorMsg);
  }
};

// 3. Xóa dữ liệu (DELETE)
const deleteData = async (id) => {
  if (!confirm(`Bạn có chắc chắn muốn xóa loại sản phẩm #${id} không?`)) return;

  try {
    await api.delete(`${API_URL}/${id}`);
    showMessage('success', 'Đã xóa thành công!');
    loadData(0); // Xóa xong về trang đầu
  } catch (error) {
    const msg = error.response?.data?.message || 'Không thể xóa (có thể dữ liệu đang được sử dụng)';
    showMessage('danger', msg);
  }
};

// --- HÀM HỖ TRỢ GIAO DIỆN ---
const showMessage = (type, text) => {
  message.value = { type, text };
  setTimeout(() => message.value = { type: '', text: '' }, 3000);
};

const openAddModal = () => {
  isEditMode.value = false;
  form.maLoai = '';
  form.tenLoai = '';
  
  const modalEl = document.getElementById('modalLoaiSP');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

const openEditModal = (item) => {
  isEditMode.value = true;
  form.maLoai = item.maLoai;
  form.tenLoai = item.tenLoai;

  const modalEl = document.getElementById('modalLoaiSP');
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
  loadData(0);
});
</script>

<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4 border-bottom pb-2">
      <h3 class="text-primary"><i class="bi bi-tags"></i> Quản Lý Loại Sản Phẩm</h3>
      <div>
           <button class="btn btn-outline-secondary me-2" @click="loadData(currentPage)">
                <i class="bi bi-arrow-clockwise"></i>
            </button>
          <button class="btn btn-primary" @click="openAddModal">
            <i class="bi bi-plus-lg"></i> Thêm Loại Mới
          </button>
      </div>
    </div>

    <div v-if="message.text" :class="`alert alert-${message.type} alert-dismissible fade show`" role="alert">
      {{ message.text }}
      <button type="button" class="btn-close" @click="message.text = ''"></button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body p-0">
        <table class="table table-striped table-hover mb-0 align-middle">
          <thead class="table-dark">
            <tr>
              <th class="text-center" style="width: 80px;">STT</th>
              <th class="text-center" style="width: 150px;">Mã Loại</th>
              <th>Tên Loại Sản Phẩm</th>
              <th class="text-center" style="width: 200px;">Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoading">
              <td colspan="4" class="text-center py-4">
                <div class="spinner-border text-primary spinner-border-sm" role="status"></div>
                <span class="ms-2">Đang tải dữ liệu...</span>
              </td>
            </tr>
            
            <tr v-else v-for="(item, index) in danhSach" :key="item.maLoai">
              <td class="text-center">{{ (currentPage * itemsPerPage) + index + 1 }}</td>
              <td class="text-center fw-bold">{{ item.maLoai }}</td>
              <td class="fw-medium">{{ item.tenLoai }}</td>
              <td class="text-center">
                <button class="btn btn-sm btn-outline-primary me-2" @click="openEditModal(item)">
                  <i class="bi bi-pencil-square"></i> Sửa
                </button>
                <button class="btn btn-sm btn-outline-danger" @click="deleteData(item.maLoai)">
                  <i class="bi bi-trash"></i> Xóa
                </button>
              </td>
            </tr>

            <tr v-if="!isLoading && danhSach.length === 0">
              <td colspan="4" class="text-center text-muted py-3">Chưa có dữ liệu nào.</td>
            </tr>
          </tbody>
        </table>
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
              Hiển thị {{ (currentPage * itemsPerPage) + 1 }} - {{ Math.min((currentPage + 1) * itemsPerPage, paginationInfo.total) }} 
              trong tổng {{ paginationInfo.total }} loại
          </div>
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
              
              <div class="mb-3" v-if="isEditMode">
                <label class="form-label text-muted">Mã Loại</label>
                <input type="text" class="form-control" :value="form.maLoai" disabled readonly>
              </div>

              <div class="mb-3">
                <label class="form-label">Tên Loại <span class="text-danger">*</span></label>
                <input 
                  v-model="form.tenLoai" 
                  type="text" 
                  class="form-control" 
                  required 
                  placeholder="Ví dụ: Điện thoại, Laptop..."
                  autofocus
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