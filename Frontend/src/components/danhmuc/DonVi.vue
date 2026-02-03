<script setup>
import { ref, onMounted, reactive } from 'vue';
// [QUAN TRỌNG] Dùng api từ utils
import api from '@/utils/axios'; 
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH API ---
// Kiểm tra DonViController: @RequestMapping("/api/don-vi")
const API_URL = '/don-vi'; 

// --- TRẠNG THÁI (STATE) ---
const danhSach = ref([]);
const isLoading = ref(false);
const message = ref({ type: '', text: '' });
const isEditMode = ref(false);
let modalInstance = null;

// Form Data
const form = reactive({
  maDonVi: '',
  tenDonVi: '',
  soDienThoai: '',
  email: '',
  diaChi: '',
  loaiDonVi: 1 // Mặc định ID=1
});

// Danh sách loại đơn vị (Mapping cứng theo DB)
const listLoaiDonVi = [
  { id: 1, ten: 'Nhà Cung Cấp' },
  { id: 2, ten: 'Khách Hàng' },
  { id: 3, ten: 'Đối Tác Khác' }
];

// --- API METHODS ---

// 1. Lấy danh sách
const loadData = async () => {
  isLoading.value = true;
  try {
    const response = await api.get(API_URL);
    danhSach.value = response.data;
  } catch (error) {
    const msg = error.response?.data?.message || error.message;
    showMessage('danger', 'Lỗi tải dữ liệu: ' + msg);
  } finally {
    isLoading.value = false;
  }
};

// 2. Lưu (Thêm / Sửa)
const saveData = async () => {
  // Validate cơ bản
  if (!form.maDonVi || !form.tenDonVi) {
    showMessage('warning', 'Vui lòng nhập Mã đơn vị và Tên đơn vị!');
    return;
  }

  // [SỬA LỖI] Vì Entity DonVi bên Backend khai báo là 'Integer loaiDonVi',
  // nên ta gửi nguyên form (đang chứa số 1, 2...) là đúng.
  // KHÔNG được bọc thành object { loaiDonVi: ... } nữa.
  const payload = { ...form };

  try {
    if (isEditMode.value) {
      await api.put(`${API_URL}/${form.maDonVi}`, payload);
      showMessage('success', 'Cập nhật thông tin thành công!');
    } else {
      await api.post(API_URL, payload);
      showMessage('success', 'Thêm mới đơn vị thành công!');
    }
    closeModal();
    loadData();
  } catch (error) {
    // In chi tiết lỗi ra console để kiểm tra (F12)
    console.error("API Error:", error);

    // Lấy thông báo lỗi
    let msg = "Lỗi không xác định";
    if (error.response) {
        // Ưu tiên lấy message từ body response của Backend
        msg = error.response.data?.message || error.response.data || error.message;
        
        // Nếu là lỗi 403 -> Do quyền
        if (error.response.status === 403) msg = "Bạn không có quyền thực hiện (Cần Admin)";
        // Nếu là lỗi 400 -> Do dữ liệu sai định dạng
        if (error.response.status === 400) msg = "Dữ liệu gửi lên không hợp lệ (Kiểm tra mã trùng hoặc kiểu dữ liệu)";
    }
    
    showMessage('danger', 'Lỗi: ' + msg);
  }
};

// 3. Xóa
const deleteData = async (id) => {
  if (!confirm(`Bạn có chắc chắn muốn xóa đơn vị [${id}] không?`)) return;

  try {
    await api.delete(`${API_URL}/${id}`);
    showMessage('success', 'Đã xóa thành công!');
    loadData();
  } catch (error) {
    const msg = error.response?.data?.message || error.response?.data || error.message;
    showMessage('danger', 'Không thể xóa (có thể đơn vị này đang có giao dịch): ' + msg);
  }
};

// --- HELPER FUNCTIONS ---

// Helper hiển thị tên loại đơn vị
const getTenLoai = (loaiInput) => {
  let id = loaiInput;
  // Nếu Backend trả về object {loaiDonVi: 1, tenLoai: '...'}
  if (typeof loaiInput === 'object' && loaiInput?.loaiDonVi) {
      id = loaiInput.loaiDonVi;
  }
  const loai = listLoaiDonVi.find(item => item.id === id);
  return loai ? loai.ten : 'Khác';
};

const showMessage = (type, text) => {
  message.value = { type, text };
  setTimeout(() => message.value = { type: '', text: '' }, 4000);
};

const openAddModal = () => {
  isEditMode.value = false;
  // Reset form
  form.maDonVi = '';
  form.tenDonVi = '';
  form.soDienThoai = '';
  form.email = '';
  form.diaChi = '';
  form.loaiDonVi = 1;

  const modalEl = document.getElementById('modalDonVi');
  modalInstance = new bootstrap.Modal(modalEl);
  modalInstance.show();
};

const openEditModal = (item) => {
  isEditMode.value = true;
  // Fill data
  form.maDonVi = item.maDonVi;
  form.tenDonVi = item.tenDonVi;
  form.soDienThoai = item.soDienThoai;
  form.email = item.email;
  form.diaChi = item.diaChi;
  
  // Xử lý map LoaiDonVi từ item vào form (quan trọng)
  if (item.loaiDonVi && typeof item.loaiDonVi === 'object') {
      form.loaiDonVi = item.loaiDonVi.loaiDonVi;
  } else {
      form.loaiDonVi = item.loaiDonVi || 1;
  }

  const modalEl = document.getElementById('modalDonVi');
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
      <h3 class="text-primary"><i class="bi bi-people-fill"></i> Quản Lý Đối Tác (Đơn Vị)</h3>
      <button class="btn btn-primary" @click="openAddModal">
        <i class="bi bi-person-plus-fill"></i> Thêm Đơn Vị
      </button>
    </div>

    <div v-if="message.text" :class="`alert alert-${message.type} alert-dismissible fade show`">
      {{ message.text }}
      <button type="button" class="btn-close" @click="message.text = ''"></button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
          <table class="table table-hover table-striped mb-0 align-middle">
            <thead class="table-dark">
              <tr>
                <th>Mã ĐV</th>
                <th>Tên Đơn Vị</th>
                <th>Liên Hệ</th>
                <th>Loại</th>
                <th>Địa Chỉ</th>
                <th class="text-center" style="min-width: 150px;">Thao Tác</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="isLoading">
                <td colspan="6" class="text-center py-4">Đang tải dữ liệu...</td>
              </tr>

              <tr v-else v-for="item in danhSach" :key="item.maDonVi">
                <td class="fw-bold text-primary">{{ item.maDonVi }}</td>
                <td class="fw-medium">{{ item.tenDonVi }}</td>
                <td>
                  <div v-if="item.soDienThoai"><i class="bi bi-telephone"></i> {{ item.soDienThoai }}</div>
                  <div v-if="item.email" class="small text-muted"><i class="bi bi-envelope"></i> {{ item.email }}</div>
                </td>
                <td>
                  <span class="badge bg-info text-dark">{{ getTenLoai(item.loaiDonVi) }}</span>
                </td>
                <td class="small">{{ item.diaChi }}</td>
                <td class="text-center">
                  <button class="btn btn-sm btn-outline-primary me-2" @click="openEditModal(item)">
                    <i class="bi bi-pencil-square"></i>
                  </button>
                  <button class="btn btn-sm btn-outline-danger" @click="deleteData(item.maDonVi)">
                    <i class="bi bi-trash"></i>
                  </button>
                </td>
              </tr>

              <tr v-if="!isLoading && danhSach.length === 0">
                <td colspan="6" class="text-center text-muted py-3">Chưa có dữ liệu đơn vị.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="modal fade" id="modalDonVi" tabindex="-1" aria-hidden="true" data-bs-backdrop="static">
      <div class="modal-dialog modal-lg">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">
              {{ isEditMode ? 'Cập Nhật Thông Tin' : 'Thêm Đơn Vị Mới' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <form @submit.prevent="saveData">
              <div class="row g-3">
                
                <div class="col-md-6">
                  <label class="form-label">Mã Đơn Vị <span class="text-danger">*</span></label>
                  <input 
                    v-model="form.maDonVi" 
                    type="text" 
                    class="form-control" 
                    required 
                    :disabled="isEditMode"
                    placeholder="VD: NCC01, KH02..."
                  >
                  <div class="form-text" v-if="!isEditMode">Mã không được trùng và không thể sửa sau khi tạo.</div>
                </div>

                <div class="col-md-6">
                  <label class="form-label">Loại Đơn Vị</label>
                  <select v-model="form.loaiDonVi" class="form-select">
                    <option v-for="loai in listLoaiDonVi" :key="loai.id" :value="loai.id">
                      {{ loai.ten }}
                    </option>
                  </select>
                </div>

                <div class="col-12">
                  <label class="form-label">Tên Đơn Vị <span class="text-danger">*</span></label>
                  <input v-model="form.tenDonVi" type="text" class="form-control" required placeholder="Nhập tên công ty hoặc khách hàng">
                </div>

                <div class="col-md-6">
                  <label class="form-label">Số Điện Thoại</label>
                  <input v-model="form.soDienThoai" type="text" class="form-control" placeholder="09xxxxxxx">
                </div>

                <div class="col-md-6">
                  <label class="form-label">Email</label>
                  <input v-model="form.email" type="email" class="form-control" placeholder="email@example.com">
                </div>

                <div class="col-12">
                  <label class="form-label">Địa Chỉ</label>
                  <textarea v-model="form.diaChi" class="form-control" rows="2" placeholder="Địa chỉ chi tiết..."></textarea>
                </div>

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