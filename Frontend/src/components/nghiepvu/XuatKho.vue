<template>
    <div class="card shadow-sm border-warning">
        <div class="card-header bg-warning text-dark p-2">
            <div class="d-flex flex-column flex-md-row justify-content-between align-items-center gap-2">
                <h6 class="mb-0 fw-bold text-nowrap"><i class="bi bi-box-arrow-right me-1"></i> QUẢN LÝ XUẤT KHO</h6>
                
                <div class="d-flex gap-1 w-100 w-md-auto justify-content-end header-toolbar">
                    <select v-if="isAdmin" class="form-select form-select-sm my-select" style="max-width: 180px;" v-model="filterMaKho" @change="layDanhSach">
                        <option :value="0">-- Tất cả kho --</option>
                        <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                    </select>

                    <input type="text" class="form-control form-control-sm my-input" style="max-width: 200px;" 
                           v-model="searchQuery" 
                           placeholder="🔍 Tìm phiếu...">
                    
                    <router-link to="/xuat-kho/tao-moi" class="btn btn-dark btn-sm fw-bold d-flex align-items-center btn-fix-height">
                        <i class="fas fa-plus me-1"></i> Tạo
                    </router-link>
                </div>
            </div>
        </div>
        
        <div class="card-body p-2">
            <div v-if="loading" class="text-center py-3">
                <div class="spinner-border spinner-border-sm text-warning" role="status"></div>
                <span class="ms-2 small">Đang tải...</span>
            </div>

            <div v-else>
                <div class="table-responsive d-none d-md-block">
                    <table class="table table-hover table-bordered align-middle small mb-0">
                        <thead class="table-light text-center">
                            <tr>
                                <th width="40px">STT</th>
                                <th>Số Phiếu</th>
                                <th>Ngày Xuất</th>
                                <th>Kho Xuất</th>
                                <th>Khách Hàng</th>
                                <th width="25%">Chi tiết</th>
                                <th>Tổng SL</th>
                                <th>Tổng Tiền</th>
                                <th width="150px">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(item, index) in paginatedData" :key="item.soPhieu">
                                <td class="text-center text-secondary">{{ (pagination.page * pagination.size) + index + 1 }}</td>
                                <td class="fw-bold text-primary">{{ item.soPhieu }}</td>
                                <td>{{ formatDate(item.ngayXuat) }}</td>
                                <td>{{ item.tenKho }}</td>
                                <td class="fw-bold text-truncate" style="max-width: 150px;">{{ item.tenKhachHang || '---' }}</td>
                                <td>
                                    <span v-for="(part, idx) in splitSummary(item.tomTatSanPham).slice(0, 4)" :key="idx" 
                                        class="badge bg-warning text-dark me-1 border border-warning mb-1 text-wrap text-start">
                                        {{ part }}
                                    </span>
                                    <span v-if="splitSummary(item.tomTatSanPham).length > 4" 
                                        class="badge bg-secondary text-white me-1 mb-1">
                                        +{{ splitSummary(item.tomTatSanPham).length - 4 }} nữa...
                                    </span>
                                    <div v-if="item.ghiChu" class="text-muted fst-italic text-truncate" style="max-width: 150px;">
                                        ({{ item.ghiChu }})
                                    </div>
                                </td>
                                <td class="text-center fw-bold">{{ item.tongSoLuong }}</td>
                                <td class="text-end text-danger fw-bold">{{ formatCurrency(item.tongTien) }}</td>
                                <td class="text-center">
                                    <div class="btn-group btn-group-sm">
                                        <button class="btn btn-outline-info" @click="moChiTiet(item.soPhieu)" title="Xem">
                                            <i class="fas fa-eye"></i> Chi tiết
                                        </button>
                                        <button v-if="!isYearLocked(item.ngayXuat)" class="btn btn-outline-danger" @click="huyPhieu(item.soPhieu)">
                                            <i class="fas fa-undo"></i> Xóa
                                        </button>
                                        <button v-else class="btn btn-secondary disabled">
                                            <i class="fas fa-lock"></i> Xóa
                                        </button>
                                    </div>
                                </td>
                            </tr>
                            <tr v-if="paginatedData.length === 0">
                                <td colspan="9" class="text-center text-muted">Không tìm thấy dữ liệu phù hợp.</td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="d-md-none">
                    <div v-for="(item, index) in paginatedData" :key="item.soPhieu" class="card mb-2 shadow-sm border-0 mobile-card">
                        <div class="card-body p-2">
                            <div class="d-flex justify-content-between align-items-center mb-1 border-bottom pb-1">
                                <div>
                                    <span class="badge bg-secondary me-1" style="font-size: 10px;">#{{ (pagination.page * pagination.size) + index + 1 }}</span>
                                    <span class="fw-bold text-primary">{{ item.soPhieu }}</span>
                                </div>
                                <small class="text-muted" style="font-size: 11px;">{{ formatDate(item.ngayXuat) }}</small>
                            </div>

                            <div class="mb-2" style="font-size: 12px;">
                                <div class="text-truncate fw-bold text-dark mb-1">
                                    <i class="bi bi-shop me-1 text-muted"></i> {{ item.tenKho }}
                                </div>
                                <div class="text-truncate text-muted">
                                    <i class="bi bi-person-badge me-1"></i> {{ item.tenKhachHang }}
                                </div>
                            </div>

                            <div class="bg-light rounded p-2 mb-2 product-summary">
                                <div v-for="(part, idx) in splitSummary(item.tomTatSanPham).slice(0, 4)" :key="idx" class="fw-bold text-dark mb-1">
                                    • {{ part }}
                                </div>
                                <div v-if="splitSummary(item.tomTatSanPham).length > 4" class="fw-bold text-secondary mb-1" style="font-size: 11px;">
                                    • ... và {{ splitSummary(item.tomTatSanPham).length - 4 }} sản phẩm khác
                                </div>
                                <div v-if="item.ghiChu" class="text-muted fst-italic mt-1" style="font-size: 11px;">
                                    📝 {{ item.ghiChu }}
                                </div>
                            </div>

                            <div class="d-flex justify-content-between align-items-end">
                                <div style="font-size: 11px;">
                                    <div>SL: <span class="fw-bold text-primary">{{ item.tongSoLuong }}</span></div>
                                    <div>Tiền: <span class="fw-bold text-danger">{{ formatCurrency(item.tongTien) }}</span></div>
                                </div>
                                
                                <div class="btn-group btn-group-sm">
                                    <button class="btn btn-outline-info px-2" @click="moChiTiet(item.soPhieu)">Chi Tiết</button>
                                    <button v-if="!isYearLocked(item.ngayXuat)" 
                                            class="btn btn-outline-danger px-2" @click="huyPhieu(item.soPhieu)">Xóa</button>
                                    <button v-else class="btn btn-secondary px-2 disabled"><i class="fas fa-lock"></i></button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div v-if="paginatedData.length === 0" class="text-center text-muted small py-2">Không có dữ liệu.</div>
                </div>

                <div class="d-flex justify-content-center mt-2 px-1" v-if="pagination.total > 0">
                    <ul class="pagination pagination-sm m-0">
                        <li class="page-item" :class="{ disabled: pagination.page === 0 }"><a class="page-link" href="#" @click.prevent="changePage(pagination.page - 1)">«</a></li>
                        <li class="page-item disabled"><span class="page-link text-dark">Trang {{ pagination.page + 1 }}/{{ pagination.totalPages }}</span></li>
                        <li class="page-item" :class="{ disabled: pagination.page >= pagination.totalPages - 1 }"><a class="page-link" href="#" @click.prevent="changePage(pagination.page + 1)">»</a></li>
                    </ul>
                </div>
            </div>
        </div>

        <XuatKhoChiTiet v-if="showModal" :soPhieu="selectedSoPhieu" @close="showModal = false" />
    </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue';
import api from '@/utils/axios'; 
import XuatKhoChiTiet from './XuatKhoChiTiet.vue';

const API_URL = '/kho/xuat'; 
const danhSachPhieu = ref([]);
const listKho = ref([]); 
const loading = ref(false);
const showModal = ref(false);
const selectedSoPhieu = ref(null);
const searchQuery = ref("");
const isAdmin = ref(false);
const filterMaKho = ref(0); 
const pagination = reactive({ page: 0, size: 20, total: 0, totalPages: 0 });

// Logic giữ nguyên
const setupPhanQuyen = async () => {
    const role = localStorage.getItem('userRole');
    let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');
    if (!userMaKho) { const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}'); userMaKho = userInfo.maKho; }
    if (role === 'ADMIN') { isAdmin.value = true; filterMaKho.value = 0; await loadDanhSachKho(); } 
    else { isAdmin.value = false; filterMaKho.value = userMaKho ? parseInt(userMaKho) : 0; }
};
const loadDanhSachKho = async () => { try { const res = await api.get('/kho'); listKho.value = res.data; } catch (e) { console.error(e); } };
const layDanhSach = async () => {
    loading.value = true;
    try {
        const params = {}; if (filterMaKho.value && filterMaKho.value !== 0) params.maKho = filterMaKho.value;
        const res = await api.get(API_URL, { params }); danhSachPhieu.value = res.data;
    } catch (error) { console.error(error); } finally { loading.value = false; }
};
const filteredList = computed(() => {
    if (!searchQuery.value) return danhSachPhieu.value;
    const query = searchQuery.value.toLowerCase();
    return danhSachPhieu.value.filter(item => item.soPhieu.toLowerCase().includes(query) || (item.tenKhachHang && item.tenKhachHang.toLowerCase().includes(query)) || (item.ghiChu && item.ghiChu.toLowerCase().includes(query)));
});
watch(filteredList, (newVal) => { pagination.total = newVal.length; pagination.totalPages = Math.ceil(newVal.length / pagination.size); pagination.page = 0; }, { immediate: true });
const paginatedData = computed(() => { const start = pagination.page * pagination.size; const end = start + pagination.size; return filteredList.value.slice(start, end); });
const changePage = (pageIndex) => { if (pageIndex >= 0 && pageIndex < pagination.totalPages) pagination.page = pageIndex; };
const moChiTiet = (soPhieu) => { selectedSoPhieu.value = soPhieu; showModal.value = true; };
const huyPhieu = async (soPhieu) => { if(!confirm(`CẢNH BÁO: Hủy phiếu ${soPhieu} sẽ hoàn trả toàn bộ máy về trạng thái Tồn Kho (Có thể bán lại). Tiếp tục?`)) return; try { await api.delete(`${API_URL}/${soPhieu}`); alert("Đã hủy phiếu và hoàn trả kho thành công!"); layDanhSach(); } catch (error) { alert("Lỗi: " + (error.response?.data?.message || error.message)); } };
const isYearLocked = (dateInput) => { if (!dateInput) return false; let year = Array.isArray(dateInput) ? dateInput[0] : new Date(dateInput).getFullYear(); return year < new Date().getFullYear(); };
const splitSummary = (str) => str ? str.split(', ') : [];
const formatDate = (dateArray) => { if (!dateArray) return ''; if (Array.isArray(dateArray)) { const f = (n) => n < 10 ? '0' + n : n; return `${f(dateArray[2])}/${f(dateArray[1])}/${dateArray[0]}`; } return new Date(dateArray).toLocaleDateString('vi-VN'); };
const formatCurrency = (v) => { if(!v) return '0 đ'; return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v); };
onMounted(async () => { await setupPhanQuyen(); layDanhSach(); });
</script>

<style scoped>
/* 1. CSS CHO MÁY TÍNH (Web) */
@media (min-width: 768px) {
    .btn-fix-height {
        height: 31px; /* Chiều cao chuẩn của input-sm */
        display: flex; align-items: center; justify-content: center;
    }
}

/* 2. CSS CHO ĐIỆN THOẠI (Mobile) */
@media (max-width: 767px) {
    .header-toolbar { flex-direction: column; width: 100%; }
    .my-select, .my-input { width: 100% !important; max-width: 100% !important; font-size: 13px; margin-bottom: 5px; }
    .btn-fix-height { width: 100%; justify-content: center; margin-top: 5px; height: 35px; }
    
    .mobile-card { border-radius: 8px; overflow: hidden; }
    .product-summary { font-size: 12px; }
}
</style>