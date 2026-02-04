<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import api from '@/utils/axios'; 
import * as bootstrap from 'bootstrap';

// --- CẤU HÌNH API ---
const API_URL = '/san-pham';
const API_HANG_SX = '/hang-san-xuat'; 
const API_LOAI_SP = '/loai-san-pham'; 

// --- STATE ---
const danhSach = ref([]);
const listHangSanXuat = ref([]); 
const listLoaiSanPham = ref([]); 
const isLoading = ref(false);
const isEditMode = ref(false);
const alertMsg = ref('');
const alertType = ref('success'); 

let modalInstance = null;

// --- STATE PHÂN TRANG (Server-side) ---
const currentPage = ref(0);
const itemsPerPage = ref(20); 
const totalPages = ref(0);
const totalElements = ref(0);

// --- COMPUTED ---
const visiblePages = computed(() => {
    const total = totalPages.value;
    const current = currentPage.value + 1;
    const delta = 2; 
    const range = [];
    const rangeWithDots = [];
    let l;

    for (let i = 1; i <= total; i++) {
        if (i === 1 || i === total || (i >= current - delta && i <= current + delta)) range.push(i);
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

// Form Data
const form = reactive({
  maSP: '', tenSP: '', donViTinh: '', maHang: null, maLoai: null, moTa: ''
});

// --- METHODS ---

// 1. Load Data
const loadData = async (page = 0) => {
  isLoading.value = true;
  try {
    const [resSP, resHang, resLoai] = await Promise.all([
        api.get(API_URL, { params: { page: page, size: itemsPerPage.value } }),
        listHangSanXuat.value.length === 0 ? api.get(API_HANG_SX) : { data: listHangSanXuat.value },
        listLoaiSanPham.value.length === 0 ? api.get(API_LOAI_SP) : { data: listLoaiSanPham.value }
    ]);

    // Update danh sách sản phẩm
    if (resSP.data) {
        danhSach.value = resSP.data.content || [];
        totalPages.value = resSP.data.totalPages || 0;
        totalElements.value = resSP.data.totalElements || 0;
        // [SỬA] Đảm bảo lấy đúng page number để tính STT
        currentPage.value = (typeof resSP.data.number === 'number') ? resSP.data.number : 0;
    }

    // Update danh sách hãng
    const rawHang = resHang.data;
    if (Array.isArray(rawHang)) listHangSanXuat.value = rawHang;
    else if (rawHang?.content) listHangSanXuat.value = rawHang.content;

    // Update danh sách loại
    const rawLoai = resLoai.data;
    if (Array.isArray(rawLoai)) listLoaiSanPham.value = rawLoai;
    else if (rawLoai?.content) listLoaiSanPham.value = rawLoai.content;

  } catch (error) {
    showAlert('Lỗi tải dữ liệu: ' + (error.response?.data?.message || error.message), 'danger');
  } finally {
    isLoading.value = false;
  }
};

const changePage = (page) => {
    if (page >= 0 && page < totalPages.value) loadData(page);
};

// 2. Lưu
const saveData = async () => {
  if (!form.maSP || !form.tenSP) {
    showAlert('Vui lòng nhập Mã và Tên sản phẩm!', 'warning');
    return;
  }

  const payload = {
      maSP: form.maSP,
      tenSP: form.tenSP,
      donViTinh: form.donViTinh,
      moTa: form.moTa,
      hangSanXuat: form.maHang ? { maHang: form.maHang } : null,
      loaiSanPham: form.maLoai ? { maLoai: form.maLoai } : null 
  };

  try {
    if (isEditMode.value) {
      await api.put(`${API_URL}/${form.maSP}`, payload);
      showAlert('Cập nhật thành công!', 'success');
    } else {
      await api.post(API_URL, payload);
      showAlert('Thêm mới thành công!', 'success');
    }
    closeModal();
    loadData(currentPage.value); 
  } catch (error) {
    const msg = error.response?.data || error.message; 
    showAlert('Lỗi: ' + msg, 'danger');
  }
};

// 3. Xóa
const deleteData = async (id) => {
  if (!confirm(`Bạn có chắc muốn xóa sản phẩm [${id}] không?`)) return;
  try {
    await api.delete(`${API_URL}/${id}`);
    showAlert('Đã xóa thành công!', 'success');
    loadData(0); 
  } catch (error) {
    const msg = error.response?.data || error.message;
    showAlert('Không thể xóa: ' + msg, 'danger');
  }
};

// --- HELPER FUNCTIONS ---

const openAddModal = () => {
  isEditMode.value = false;
  resetForm();
  showModal();
};

const openEditModal = (item) => {
  isEditMode.value = true;
  form.maSP = item.maSP;
  form.tenSP = item.tenSP;
  form.donViTinh = item.donViTinh;
  form.moTa = item.moTa;
  form.maHang = item.hangSanXuat ? item.hangSanXuat.maHang : null;
  form.maLoai = item.loaiSanPham ? item.loaiSanPham.maLoai : null; 
  showModal();
};

const showModal = () => {
  const modalEl = document.getElementById('modalSanPham');
  if (modalEl) {
    modalInstance = new bootstrap.Modal(modalEl);
    modalInstance.show();
  }
};

const closeModal = () => {
  if (modalInstance) {
    modalInstance.hide();
    modalInstance = null;
  }
};

const resetForm = () => {
  form.maSP = ''; form.tenSP = ''; form.donViTinh = ''; 
  form.maHang = null; form.maLoai = null; 
  form.moTa = '';
};

const showAlert = (msg, type) => {
  alertMsg.value = msg;
  alertType.value = type;
  setTimeout(() => alertMsg.value = '', 3000);
};

onMounted(() => {
  loadData(0);
});
</script>

<template>
  <div class="container-fluid mt-4">
    <div class="d-flex justify-content-between align-items-center mb-3">
      <h3 class="text-primary"><i class="fas fa-box-open"></i> Quản Lý Danh Mục Sản Phẩm</h3>
      <div>
           <button class="btn btn-outline-secondary me-2 fw-bold" @click="loadData(currentPage)">
                <i class="fas fa-sync-alt"></i> Tải lại
            </button>
          <button class="btn btn-primary fw-bold" @click="openAddModal">
            <i class="fas fa-plus"></i> Thêm Sản Phẩm Mới
          </button>
      </div>
    </div>

    <div v-if="alertMsg" :class="`alert alert-${alertType} alert-dismissible fade show`" role="alert">
      {{ alertMsg }}
      <button type="button" class="btn-close" @click="alertMsg = ''"></button>
    </div>

    <div class="card shadow-sm">
      <div class="card-body p-0">
        <div class="table-responsive">
            <table class="table table-hover table-striped mb-0 align-middle">
            <thead class="table-dark text-center">
                <tr>
                <th width="50px">STT</th> <th>Mã SP</th>
                <th>Tên Sản Phẩm</th>
                <th>ĐVT</th>
                <th>Hãng SX</th>
                <th>Loại SP</th> 
                <th>Mô Tả</th>
                <th class="text-center" width="150">Thao tác</th>
                </tr>
            </thead>
            <tbody>
                <tr v-if="isLoading">
                <td colspan="8" class="text-center py-4">
                    <div class="spinner-border text-primary" role="status"></div>
                    <p class="mb-0 mt-2">Đang tải dữ liệu...</p>
                </td>
                </tr>
                
                <tr v-else v-for="(sp, index) in danhSach" :key="sp.maSP">
                
                <td class="text-center">
                    {{ ((currentPage || 0) * itemsPerPage) + index + 1 }}
                </td>

                <td class="fw-bold text-primary">{{ sp.maSP }}</td>
                <td class="fw-bold">{{ sp.tenSP }}</td>
                <td class="text-center"><span class="badge bg-info text-dark">{{ sp.donViTinh }}</span></td>
                
                <td class="text-success fw-bold text-center">
                    {{ sp.hangSanXuat ? sp.hangSanXuat.tenHang : '---' }}
                </td>

                <td class="text-center"> 
                    <span class="badge bg-secondary">{{ sp.loaiSanPham ? sp.loaiSanPham.tenLoai : '---' }}</span>
                </td>
                
                <td class="small text-muted">{{ sp.moTa }}</td>
                <td class="text-center">
                    <button class="btn btn-sm btn-outline-warning me-2" @click="openEditModal(sp)" title="Sửa">
                    <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" @click="deleteData(sp.maSP)" title="Xóa">
                    <i class="fas fa-trash-alt"></i>
                    </button>
                </td>
                </tr>
                 <tr v-if="!isLoading && danhSach.length === 0">
                    <td colspan="8" class="text-center py-4 text-muted">Chưa có sản phẩm nào.</td>
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
              trong tổng {{ paginationInfo.total }} sản phẩm
          </div>
      </div>
    </div>

    <div class="modal fade" id="modalSanPham" tabindex="-1" data-bs-backdrop="static" aria-hidden="true">
      <div class="modal-dialog">
        <div class="modal-content">
          <div class="modal-header" :class="isEditMode ? 'bg-warning' : 'bg-primary text-white'">
            <h5 class="modal-title">
              {{ isEditMode ? 'Cập Nhật Sản Phẩm' : 'Thêm Sản Phẩm Mới' }}
            </h5>
            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
          </div>
          
          <div class="modal-body">
            <form @submit.prevent="saveData">
              <div class="mb-3">
                <label class="form-label">Mã Sản Phẩm <span class="text-danger">*</span></label>
                <input type="text" class="form-control" v-model="form.maSP" required 
                       :disabled="isEditMode" 
                       placeholder="VD: IP15, SS-S24...">
                <div class="form-text text-danger" v-if="isEditMode">Không thể sửa mã sản phẩm.</div>
              </div>

              <div class="mb-3">
                <label class="form-label">Tên Sản Phẩm <span class="text-danger">*</span></label>
                <input type="text" class="form-control" v-model="form.tenSP" required placeholder="Nhập tên sản phẩm">
              </div>

              <div class="row">
                <div class="col-md-6 mb-3">
                  <label class="form-label">Hãng Sản Xuất</label>
                  <select class="form-select" v-model="form.maHang">
                      <option :value="null" disabled>-- Chọn Hãng --</option>
                      <option v-for="h in listHangSanXuat" :key="h.maHang" :value="h.maHang">
                          {{ h.tenHang }}
                      </option>
                  </select>
                </div>
                
                <div class="col-md-6 mb-3"> 
                  <label class="form-label">Loại Sản Phẩm</label>
                  <select class="form-select" v-model="form.maLoai">
                      <option :value="null" disabled>-- Chọn Loại --</option>
                      <option v-for="l in listLoaiSanPham" :key="l.maLoai" :value="l.maLoai">
                          {{ l.tenLoai }}
                      </option>
                  </select>
                </div>
              </div>

              <div class="mb-3">
                  <label class="form-label">Đơn Vị Tính</label>
                  <input type="text" class="form-control" v-model="form.donViTinh" placeholder="Cái, Chiếc, Bộ...">
              </div>

              <div class="mb-3">
                <label class="form-label">Mô Tả</label>
                <textarea class="form-control" v-model="form.moTa" rows="3" placeholder="Ghi chú thêm về sản phẩm"></textarea>
              </div>

              <div class="text-end">
                <button type="button" class="btn btn-secondary me-2" data-bs-dismiss="modal">Hủy</button>
                <button type="submit" class="btn" :class="isEditMode ? 'btn-warning' : 'btn-primary'">
                  <i class="fas fa-save"></i> Lưu Dữ Liệu
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>