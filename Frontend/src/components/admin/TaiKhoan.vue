<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import api from '@/utils/axios'; 
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH API ---
const API_USER = '/admin/tai-khoan'; 
const API_KHO = '/chi-nhanh'; // [Lưu ý] Dùng API /chi-nhanh để lấy list kho (cần backend có api getAllList)

// --- STATE ---
const danhSachUser = ref([]);
const danhSachKho = ref([]);
const isLoading = ref(false);
const message = ref({ type: '', text: '' });
const isEditMode = ref(false);
let modalInstance = null;

// --- STATE PHÂN TRANG ---
const currentPage = ref(0);
const itemsPerPage = ref(20); 
const totalPages = ref(0);
const totalElements = ref(0);

// --- COMPUTED: TÍNH TOÁN NÚT TRANG ---
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

// Danh sách Vai trò
const danhSachVaiTro = [
  { id: 1, ten: 'Admin (Quản trị viên)' },
  { id: 2, ten: 'Staff (Nhân viên kho)' }
];

// Form Model
const form = reactive({
  maTaiKhoan: null, tenTaiKhoan: '', password: '', maVaitro: 2, maKho: null
});

// --- API METHODS ---

// 1. Tải dữ liệu (Users + Kho)
const loadData = async (page = 0) => {
  isLoading.value = true;
  try {
    const [resUsers, resKho] = await Promise.all([
      api.get(API_USER, { params: { page: page, size: itemsPerPage.value } }),
      // Nếu đã có danh sách kho thì không load lại
      danhSachKho.value.length === 0 ? api.get(API_KHO) : { data: { content: danhSachKho.value } }
    ]);
    
    // Xử lý dữ liệu User (Phân trang)
    const data = resUsers.data;
    if(data) {
        // Ưu tiên lấy content từ cấu trúc Page
        danhSachUser.value = data.content || [];
        
        if (data.page) { // Cấu trúc lồng
            totalPages.value = data.page.totalPages || 0;
            totalElements.value = data.page.totalElements || 0;
            currentPage.value = data.page.number || 0;
        } else { // Cấu trúc phẳng
            totalPages.value = data.totalPages || 0;
            totalElements.value = data.totalElements || 0;
            currentPage.value = (typeof data.number === 'number') ? data.number : 0;
        }
    } else {
        danhSachUser.value = [];
        totalElements.value = 0;
    }

    // Xử lý dữ liệu Kho (Có thể API kho trả về Page hoặc List, cần handle cả 2)
    const khoData = resKho.data;
    if (Array.isArray(khoData)) {
        danhSachKho.value = khoData;
    } else if (khoData && khoData.content) {
        danhSachKho.value = khoData.content;
    }

  } catch (error) {
    const msg = error.response?.data?.message || error.message;
    showMessage('danger', 'Lỗi tải dữ liệu: ' + msg);
  } finally {
    isLoading.value = false;
  }
};

const changePage = (page) => {
    if (page >= 0 && page < totalPages.value) {
        loadData(page);
    }
};

// 2. Lưu dữ liệu
const saveData = async () => {
  if (!isEditMode.value && (!form.tenTaiKhoan || !form.password)) {
    showMessage('warning', 'Vui lòng nhập tên tài khoản và mật khẩu!');
    return;
  }

  const payload = {
    maVaitro: form.maVaitro,
    maKho: form.maKho
  };

  if (!isEditMode.value) {
    payload.tenTaiKhoan = form.tenTaiKhoan;
    payload.password = form.password;
  }

  try {
    if (isEditMode.value) {
      await api.put(`${API_USER}/${form.maTaiKhoan}`, payload);
      showMessage('success', 'Cập nhật tài khoản thành công!');
    } else {
      await api.post(API_USER, payload);
      showMessage('success', 'Tạo tài khoản mới thành công!');
    }
    closeModal();
    loadData(currentPage.value); 
  } catch (error) {
    const msg = error.response?.data?.message || error.response?.data || error.message;
    showMessage('danger', 'Lỗi lưu dữ liệu: ' + msg);
  }
};

// 3. Xóa tài khoản
const deleteData = async (id, name) => {
  if (!confirm(`Bạn có chắc muốn xóa tài khoản [${name}]?`)) return;
  try {
    await api.delete(`${API_USER}/${id}`);
    showMessage('success', 'Đã xóa tài khoản thành công!');
    loadData(0);
  } catch (error) {
    const msg = error.response?.data?.message || error.message;
    showMessage('danger', 'Không thể xóa: ' + msg);
  }
};

// --- HELPER FUNCTIONS ---
const showMessage = (type, text) => {
  message.value = { type, text };
  setTimeout(() => message.value = { type: '', text: '' }, 4000);
};

const getTenKho = (khoInput) => {
  if (!khoInput) return '---';
  if (typeof khoInput === 'object' && khoInput.tenKho) return khoInput.tenKho;
  const found = danhSachKho.value.find(k => k.maKho === khoInput);
  return found ? found.tenKho : `Kho #${khoInput}`;
};

const getTenVaiTro = (roleInput) => {
    let id = roleInput;
    if (typeof roleInput === 'object' && roleInput?.maVaitro) {
        id = roleInput.maVaitro;
    }
    const found = danhSachVaiTro.find(r => r.id === id);
    return found ? found.ten : 'Không xác định';
};

const openAddModal = () => {
  isEditMode.value = false;
  form.maTaiKhoan = null; form.tenTaiKhoan = ''; form.password = ''; form.maVaitro = 2; 
  form.maKho = danhSachKho.value.length > 0 ? danhSachKho.value[0].maKho : null;
  const modalEl = document.getElementById('modalTaiKhoan');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

const openEditModal = (item) => {
  isEditMode.value = true;
  form.maTaiKhoan = item.maTaiKhoan;
  form.tenTaiKhoan = item.tenTaiKhoan; 
  form.password = ''; 
  form.maVaitro = (typeof item.maVaitro === 'object') ? item.maVaitro.maVaitro : item.maVaitro;
  
  if (item.kho && typeof item.kho === 'object') {
     form.maKho = item.kho.maKho;
  } else {
     form.maKho = item.maKho;
  }

  const modalEl = document.getElementById('modalTaiKhoan');
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
  loadData(0);
});
</script>

<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4 border-bottom pb-2">
      <h3 class="text-primary"><i class="bi bi-person-badge"></i> Quản Lý Tài Khoản</h3>
      <div>
           <button class="btn btn-outline-secondary me-2" @click="loadData(currentPage)">
                <i class="bi bi-arrow-clockwise"></i>
            </button>
          <button class="btn btn-primary" @click="openAddModal">
            <i class="bi bi-person-plus-fill"></i> Tạo Tài Khoản
          </button>
      </div>
    </div>

    <div v-if="message.text" :class="`alert alert-${message.type} alert-dismissible fade show`">
      {{ message.text }}
      <button type="button" class="btn-close" @click="message.text = ''"></button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover table-bordered mb-0 align-middle">
            <thead class="table-dark">
                <tr>
                <th class="text-center" width="80px">STT</th>
                <th class="text-center" width="5%">ID</th>
                <th width="20%">Tên Đăng Nhập</th>
                <th width="20%">Vai Trò</th>
                <th>Thuộc Kho</th>
                <th class="text-center" width="15%">Thao Tác</th>
                </tr>
            </thead>
            <tbody>
                <tr v-if="isLoading">
                <td colspan="6" class="text-center py-4">
                    <div class="spinner-border text-primary spinner-border-sm" role="status"></div> Đang tải dữ liệu...
                </td>
                </tr>

                <tr v-else v-for="(user, index) in danhSachUser" :key="user.maTaiKhoan">
                <td class="text-center">
                    {{ ((currentPage || 0) * itemsPerPage) + index + 1 }}
                </td>
                <td class="text-center fw-bold">{{ user.maTaiKhoan }}</td>
                <td class="text-primary fw-medium">{{ user.tenTaiKhoan }}</td>
                <td>
                    <span :class="['badge', (user.vaiTro?.maVaitro === 1 || user.maVaitro === 1) ? 'bg-danger' : 'bg-info text-dark']">
                    {{ getTenVaiTro(user.vaiTro || user.maVaitro) }}
                    </span>
                </td>
                <td>
                    <i class="bi bi-geo-alt-fill text-danger"></i> {{ getTenKho(user.kho || user.maKho) }}
                </td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-warning me-2" @click="openEditModal(user)" title="Sửa vai trò/kho">
                    <i class="bi bi-pencil-square"></i>
                    </button>
                    <button 
                    class="btn btn-sm btn-outline-danger" 
                    @click="deleteData(user.maTaiKhoan, user.tenTaiKhoan)"
                    :disabled="user.tenTaiKhoan === 'admin'"
                    title="Xóa tài khoản"
                    >
                    <i class="bi bi-trash"></i>
                    </button>
                </td>
                </tr>
                
                <tr v-if="!isLoading && (!danhSachUser || danhSachUser.length === 0)">
                <td colspan="6" class="text-center py-3">Chưa có tài khoản nào.</td>
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
              trong tổng {{ paginationInfo.total }} tài khoản
          </div>
      </div>
    </div>

    <div class="modal fade" id="modalTaiKhoan" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEditMode ? 'Cập Nhật Tài Khoản' : 'Thêm Tài Khoản Mới' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveData">
              
              <div class="mb-3">
                <label class="form-label">Tên Đăng Nhập <span class="text-danger" v-if="!isEditMode">*</span></label>
                <input 
                  v-model="form.tenTaiKhoan" 
                  type="text" 
                  class="form-control" 
                  :disabled="isEditMode"
                  required
                  placeholder="Nhập tên đăng nhập..."
                >
                <div class="form-text" v-if="isEditMode">Không thể đổi tên đăng nhập.</div>
              </div>

              <div class="mb-3" v-if="!isEditMode">
                <label class="form-label">Mật Khẩu <span class="text-danger">*</span></label>
                <input 
                  v-model="form.password" 
                  type="password" 
                  class="form-control" 
                  required
                  placeholder="Nhập mật khẩu..."
                >
              </div>

              <div class="mb-3">
                <label class="form-label">Vai Trò</label>
                <select v-model="form.maVaitro" class="form-select">
                  <option v-for="role in danhSachVaiTro" :key="role.id" :value="role.id">
                    {{ role.ten }}
                  </option>
                </select>
              </div>

              <div class="mb-3">
                <label class="form-label">Chi Nhánh Kho Làm Việc</label>
                <select v-model="form.maKho" class="form-select" required>
                  <option :value="null" disabled>-- Chọn Kho --</option>
                  <option v-for="kho in danhSachKho" :key="kho.maKho" :value="kho.maKho">
                    {{ kho.tenKho }}
                  </option>
                </select>
              </div>

              <div class="text-end mt-4">
                <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">Hủy</button>
                <button type="submit" class="btn btn-primary">
                  {{ isEditMode ? 'Lưu Thay Đổi' : 'Tạo Tài Khoản' }}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>