<script setup>
import { ref, onMounted, reactive } from 'vue';
// [QUAN TRỌNG] Import instance 'api' đã cấu hình Interceptor thay vì 'axios' thường
import api from '@/utils/axios'; 
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH API ---
// Lưu ý: baseURL đã là 'http://localhost:8080/api' trong file axios.js
const API_USER = '/admin/tai-khoan'; 
const API_KHO = '/kho'; 

// --- STATE ---
const danhSachUser = ref([]);
const danhSachKho = ref([]);
const isLoading = ref(false);
const message = ref({ type: '', text: '' });
const isEditMode = ref(false);
let modalInstance = null;

// Danh sách Vai trò (Map theo Logic DB: 1=Admin, 2=Staff)
const danhSachVaiTro = [
  { id: 1, ten: 'Admin (Quản trị viên)' },
  { id: 2, ten: 'Staff (Nhân viên kho)' }
];

// Form Model
const form = reactive({
  maTaiKhoan: null,
  tenTaiKhoan: '',
  password: '',
  maVaitro: 2, // Mặc định là Staff
  maKho: null
});

// --- API METHODS ---

// 1. Tải dữ liệu (Users + Kho)
const loadData = async () => {
  isLoading.value = true;
  try {
    // Gọi bằng 'api' để có Token
    const [resUsers, resKho] = await Promise.all([
      api.get(API_USER),
      api.get(API_KHO)
    ]);
    
    danhSachUser.value = resUsers.data;
    danhSachKho.value = resKho.data;
  } catch (error) {
    // Xử lý lỗi chuẩn (nếu hết hạn token axios.js đã lo, đây chỉ lo lỗi data)
    const msg = error.response?.data?.message || error.response?.data || error.message;
    showMessage('danger', 'Lỗi tải dữ liệu: ' + msg);
  } finally {
    isLoading.value = false;
  }
};

// 2. Lưu dữ liệu (Thêm / Sửa)
const saveData = async () => {
  // Validate cơ bản
  if (!isEditMode.value && (!form.tenTaiKhoan || !form.password)) {
    showMessage('warning', 'Vui lòng nhập tên tài khoản và mật khẩu!');
    return;
  }

  // Chuẩn bị payload gửi đi
  const payload = {
    maVaitro: form.maVaitro,
    maKho: form.maKho ? { maKho: form.maKho } : null // Gửi dạng Object nếu Backend yêu cầu Entity
    // Hoặc nếu Backend chỉ nhận ID: maKho: form.maKho
  };

  if (!isEditMode.value) {
    // Nếu thêm mới thì gửi thêm username/pass
    payload.tenTaiKhoan = form.tenTaiKhoan;
    payload.password = form.password;
  }

  try {
    if (isEditMode.value) {
      // API Update: PUT /tai-khoan/{id}
      await api.put(`${API_USER}/${form.maTaiKhoan}`, payload);
      showMessage('success', 'Cập nhật tài khoản thành công!');
    } else {
      // API Create: POST /tai-khoan
      await api.post(API_USER, payload);
      showMessage('success', 'Tạo tài khoản mới thành công!');
    }
    closeModal();
    loadData(); // Tải lại bảng
  } catch (error) {
    const msg = error.response?.data?.message || error.response?.data || error.message;
    showMessage('danger', 'Lỗi lưu dữ liệu: ' + msg);
  }
};

// 3. Xóa tài khoản
const deleteData = async (id, name) => {
  if (!confirm(`Bạn có chắc muốn xóa tài khoản [${name}]? Hành động này không thể hoàn tác.`)) return;

  try {
    await api.delete(`${API_USER}/${id}`);
    showMessage('success', 'Đã xóa tài khoản thành công!');
    loadData();
  } catch (error) {
    const msg = error.response?.data?.message || error.response?.data || error.message;
    showMessage('danger', 'Không thể xóa: ' + msg);
  }
};

// --- HELPER FUNCTIONS ---

const showMessage = (type, text) => {
  message.value = { type, text };
  setTimeout(() => message.value = { type: '', text: '' }, 4000);
};

// Hàm lấy tên Kho từ ID (để hiển thị trên bảng)
const getTenKho = (khoInput) => {
  // Trường hợp backend trả về null
  if (!khoInput) return '---';

  // Trường hợp backend trả về Object Kho {maKho: 1, tenKho: '...'}
  if (typeof khoInput === 'object' && khoInput.tenKho) {
    return khoInput.tenKho;
  }
  
  // Trường hợp backend trả về ID (số), tìm trong danhSachKho đã tải
  const found = danhSachKho.value.find(k => k.maKho === khoInput);
  return found ? found.tenKho : `Kho #${khoInput}`;
};

// Hàm lấy tên Vai trò
const getTenVaiTro = (roleInput) => {
    // Backend trả về số (1, 2) hoặc Object
    let id = roleInput;
    if (typeof roleInput === 'object' && roleInput?.maVaitro) {
        id = roleInput.maVaitro;
    }
    const found = danhSachVaiTro.find(r => r.id === id);
    return found ? found.ten : 'Không xác định';
};

// Mở Modal Thêm
const openAddModal = () => {
  isEditMode.value = false;
  form.maTaiKhoan = null;
  form.tenTaiKhoan = '';
  form.password = '';
  form.maVaitro = 2; // Mặc định Staff
  
  // Tự chọn kho đầu tiên nếu có
  form.maKho = danhSachKho.value.length > 0 ? danhSachKho.value[0].maKho : null;

  const modalEl = document.getElementById('modalTaiKhoan');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

// Mở Modal Sửa
const openEditModal = (item) => {
  isEditMode.value = true;
  form.maTaiKhoan = item.maTaiKhoan;
  form.tenTaiKhoan = item.tenTaiKhoan; 
  form.password = ''; // Reset pass khi mở form sửa
  
  // Map dữ liệu vào form (xử lý cả trường hợp trả về Object hoặc ID)
  form.maVaitro = (typeof item.maVaitro === 'object') ? item.maVaitro.maVaitro : item.maVaitro;
  
  // Kiểm tra item.kho (hoặc item.maKho) tùy JSON backend trả về
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

// --- LIFECYCLE ---
onMounted(() => {
  loadData();
});
</script>

<template>
  <div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4 border-bottom pb-2">
      <h3 class="text-primary"><i class="bi bi-person-badge"></i> Quản Lý Tài Khoản</h3>
      <button class="btn btn-primary" @click="openAddModal">
        <i class="bi bi-person-plus-fill"></i> Tạo Tài Khoản
      </button>
    </div>

    <div v-if="message.text" :class="`alert alert-${message.type} alert-dismissible fade show`">
      {{ message.text }}
      <button type="button" class="btn-close" @click="message.text = ''"></button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body p-0">
        <table class="table table-hover table-bordered mb-0 align-middle">
          <thead class="table-dark">
            <tr>
              <th class="text-center" width="5%">ID</th>
              <th width="20%">Tên Đăng Nhập</th>
              <th width="20%">Vai Trò</th>
              <th>Thuộc Kho</th>
              <th class="text-center" width="15%">Thao Tác</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoading">
              <td colspan="5" class="text-center py-4">Đang tải dữ liệu...</td>
            </tr>

            <tr v-else v-for="user in danhSachUser" :key="user.maTaiKhoan">
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
            
            <tr v-if="!isLoading && danhSachUser.length === 0">
              <td colspan="5" class="text-center py-3">Chưa có tài khoản nào.</td>
            </tr>
          </tbody>
        </table>
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