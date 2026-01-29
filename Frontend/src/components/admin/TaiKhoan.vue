<script setup>
import { ref, onMounted, reactive, computed } from 'vue';
import axios from 'axios';
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH API ---
const API_USER = 'http://localhost:8080/api/admin/tai-khoan';
const API_KHO = 'http://localhost:8080/api/chi-nhanh'; // Dùng lại API Kho đã làm trước đó

// --- STATE ---
const danhSachUser = ref([]);
const danhSachKho = ref([]);
const isLoading = ref(false);
const message = ref({ type: '', text: '' });
const isEditMode = ref(false);
let modalInstance = null;

// Danh sách Vai trò (Cứng vì ít thay đổi, hoặc có thể gọi API nếu có)
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
    // Gọi song song cả 2 API để tiết kiệm thời gian
    const [resUsers, resKho] = await Promise.all([
      axios.get(API_USER),
      axios.get(API_KHO)
    ]);
    
    danhSachUser.value = resUsers.data;
    danhSachKho.value = resKho.data;
  } catch (error) {
    showMessage('danger', 'Lỗi tải dữ liệu: ' + (error.response?.data || error.message));
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
  // Controller yêu cầu: maVaitro, maKho (dạng ID Integer)
  const payload = {
    maVaitro: form.maVaitro,
    maKho: form.maKho
  };

  if (!isEditMode.value) {
    // Nếu thêm mới thì gửi thêm username/pass
    payload.tenTaiKhoan = form.tenTaiKhoan;
    payload.password = form.password;
  }

  try {
    if (isEditMode.value) {
      // API Update: PUT /api/admin/tai-khoan/{id}
      await axios.put(`${API_USER}/${form.maTaiKhoan}`, payload);
      showMessage('success', 'Cập nhật tài khoản thành công!');
    } else {
      // API Create: POST /api/admin/tai-khoan
      await axios.post(API_USER, payload);
      showMessage('success', 'Tạo tài khoản mới thành công!');
    }
    closeModal();
    loadData(); // Tải lại bảng
  } catch (error) {
    const msg = error.response?.data || error.message;
    showMessage('danger', 'Lỗi lưu dữ liệu: ' + msg);
  }
};

// 3. Xóa tài khoản
const deleteData = async (id, name) => {
  if (!confirm(`Bạn có chắc muốn xóa tài khoản [${name}]? Hành động này không thể hoàn tác.`)) return;

  try {
    await axios.delete(`${API_USER}/${id}`);
    showMessage('success', 'Đã xóa tài khoản thành công!');
    loadData();
  } catch (error) {
    showMessage('danger', 'Không thể xóa: ' + (error.response?.data || error.message));
  }
};

// --- HELPER FUNCTIONS ---

const showMessage = (type, text) => {
  message.value = { type, text };
  setTimeout(() => message.value = { type: '', text: '' }, 4000);
};

// Hàm lấy tên Kho từ ID (để hiển thị trên bảng)
const getTenKho = (khoObjectOrId) => {
  // Backend có thể trả về object {maKho: 1, tenKho: '...'} hoặc chỉ số ID
  if (!khoObjectOrId) return '---';
  
  // Trường hợp trả về Object
  if (typeof khoObjectOrId === 'object' && khoObjectOrId.tenKho) {
    return khoObjectOrId.tenKho;
  }
  
  // Trường hợp trả về ID, tìm trong danhSachKho
  const found = danhSachKho.value.find(k => k.maKho === khoObjectOrId);
  return found ? found.tenKho : `Kho #${khoObjectOrId}`;
};

// Hàm lấy tên Vai trò
const getTenVaiTro = (roleObjOrId) => {
  let id = roleObjOrId;
  if (typeof roleObjOrId === 'object' && roleObjOrId?.maVaitro) {
    id = roleObjOrId.maVaitro;
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
  form.maKho = danhSachKho.value.length > 0 ? danhSachKho.value[0].maKho : null;

  const modalEl = document.getElementById('modalTaiKhoan');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

// Mở Modal Sửa
const openEditModal = (item) => {
  isEditMode.value = true;
  form.maTaiKhoan = item.maTaiKhoan;
  form.tenTaiKhoan = item.tenTaiKhoan; // Chỉ để hiển thị, không gửi đi update
  form.password = ''; // Không cần fill pass cũ
  
  // Xử lý map dữ liệu từ Item vào Form
  // Nếu backend trả về object Kho/Vaitro thì lấy ID, nếu trả về ID thì lấy thẳng
  form.maVaitro = (typeof item.vaiTro === 'object') ? item.vaiTro.maVaitro : item.maVaitro;
  form.maKho = (typeof item.kho === 'object') ? item.kho.maKho : item.maKho;

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