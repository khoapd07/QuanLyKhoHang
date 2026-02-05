<template>
    <div class="card shadow-sm">
        <div class="card-header bg-primary text-white p-2">
            <div class="d-flex flex-column flex-md-row justify-content-between align-items-center gap-2">
                <h6 class="mb-0 fw-bold"><i class="bi bi-box-seam me-1"></i> QUẢN LÝ NHẬP KHO</h6>

                <div class="d-flex gap-1 w-100 w-md-auto justify-content-end header-toolbar">
                    <select v-if="isAdmin" class="form-select form-select-sm my-select" v-model="filterMaKho"
                        @change="layDanhSach">
                        <option :value="0">-- Tất cả kho --</option>
                        <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                    </select>

                    <input type="text" class="form-control form-control-sm my-input" v-model="searchQuery"
                        placeholder="🔍 Tìm phiếu...">

                    <router-link to="/nhap-kho/tao-moi"
                        class="btn btn-light btn-sm fw-bold d-flex align-items-center btn-fix-height">
                        <i class="fas fa-plus me-1"></i> Tạo
                    </router-link>
                </div>
            </div>
        </div>

        <div class="card-body p-2">
            <div v-if="loading" class="text-center py-3">
                <div class="spinner-border spinner-border-sm text-primary"></div>
            </div>

            <div v-else>
                <div class="table-responsive d-none d-md-block">
                    <table class="table table-hover table-bordered align-middle small mb-0">
                        <thead class="table-light text-center">
                            <tr>
                                <th width="40px">STT</th>
                                <th>Số Phiếu</th>
                                <th>Ngày Nhập</th>
                                <th>Kho</th>
                                <th>Nhà Cung Cấp</th>
                                <th width="20%">Chi tiết</th>
                                <th>Hiện Trạng</th>
                                <th>Giá Trị</th>
                                <th width="180px">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(item, index) in paginatedData" :key="item.soPhieu">
                                <td class="text-center text-secondary">{{ (pagination.page * pagination.size) + index +
                                    1 }}</td>
                                <td class="fw-bold text-primary">{{ item.soPhieu }}</td>
                                <td>{{ formatDate(item.ngayNhap) }}</td>
                                <td>{{ item.tenKho }}</td>
                                <td class="fw-bold">{{ item.tenKhachHang || '-' }}</td>
                                <td>
                                    <div v-for="(part, idx) in splitSummary(item.tomTatSanPham)" :key="idx"
                                        class="badge bg-info text-dark me-1 border border-info mb-1">{{ part }}</div>
                                    <div v-if="item.ghiChu" class="text-muted fst-italic text-truncate"
                                        style="max-width: 150px;">{{ item.ghiChu }}</div>
                                </td>
                                <td class="text-center">
                                    <span class="fw-bold text-success">{{ item.soLuongConLai }}</span>/{{
                                    item.tongSoLuongMay }}
                                </td>
                                <td class="text-end fw-bold text-danger">{{ formatCurrency(item.tienConLai) }}</td>
                                <td class="text-center">
                                    <div class="btn-group btn-group-sm">
                                        <button class="btn btn-outline-info" @click="moChiTiet(item.soPhieu)"
                                            title="Xem chi tiết">
                                            Chi tiết
                                        </button>
                                        <button class="btn btn-outline-warning" @click="moModalSua(item)"
                                            title="Sửa ghi chú">
                                            Sửa
                                        </button>
                                        <button
                                            v-if="!isYearLocked(item.ngayNhap) && item.soLuongConLai === item.tongSoLuongMay"
                                            class="btn btn-outline-danger" @click="xoaPhieu(item.soPhieu)"
                                            title="Xóa phiếu">
                                            Xóa
                                        </button>
                                    </div>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="d-md-none">
                    <div v-for="(item, index) in paginatedData" :key="item.soPhieu"
                        class="card mb-2 shadow-sm border-0 mobile-card">
                        <div class="card-body p-2">
                            <div class="d-flex justify-content-between align-items-center mb-1 border-bottom pb-1">
                                <div>
                                    <span class="badge bg-secondary me-1">#{{ (pagination.page * pagination.size) +
                                        index + 1 }}</span>
                                    <span class="fw-bold text-primary">{{ item.soPhieu }}</span>
                                </div>
                                <small class="text-muted" style="font-size: 11px;">{{ formatDate(item.ngayNhap)
                                    }}</small>
                            </div>

                            <div class="mb-2" style="font-size: 12px;">
                                <div class="text-truncate fw-bold text-dark mb-1">
                                    <i class="bi bi-shop me-1 text-muted"></i> {{ item.tenKho }}
                                </div>
                                <div class="text-truncate text-muted">
                                    <i class="bi bi-truck me-1"></i> {{ item.tenKhachHang }}
                                </div>
                            </div>

                            <div class="bg-light rounded p-2 mb-2 product-summary">
                                <div v-for="(part, idx) in splitSummary(item.tomTatSanPham)" :key="idx"
                                    class="fw-bold text-dark mb-1">
                                    • {{ part }}
                                </div>
                                <div v-if="item.ghiChu" class="text-muted fst-italic mt-1" style="font-size: 11px;">
                                    📝 {{ item.ghiChu }}
                                </div>
                            </div>

                            <div class="d-flex justify-content-between align-items-end">
                                <div style="font-size: 11px;">
                                    <div>SL: <span class="fw-bold text-success">{{ item.soLuongConLai }}/{{
                                            item.tongSoLuongMay }}</span></div>
                                    <div>Tồn: <span class="fw-bold text-danger">{{ formatCurrency(item.tienConLai)
                                            }}</span></div>
                                </div>

                                <div class="btn-group btn-group-sm">
                                    <button class="btn btn-outline-info px-2" @click="moChiTiet(item.soPhieu)"
                                        title="Xem chi tiết">
                                        Chi tiết
                                    </button>

                                    <button class="btn btn-outline-warning px-2" @click="moModalSua(item)"
                                        title="Sửa ghi chú">
                                        Sửa
                                    </button>

                                    <button
                                        v-if="!isYearLocked(item.ngayNhap) && item.soLuongConLai === item.tongSoLuongMay"
                                        class="btn btn-outline-danger px-2" @click="xoaPhieu(item.soPhieu)"
                                        title="Xóa phiếu">
                                        Xóa
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div v-if="paginatedData.length === 0" class="text-center text-muted small py-2">Không có dữ liệu.
                    </div>
                </div>

                <div class="d-flex justify-content-center mt-2 px-1" v-if="pagination.total > 0">
                    <ul class="pagination pagination-sm m-0">
                        <li class="page-item" :class="{ disabled: pagination.page === 0 }"><a class="page-link" href="#"
                                @click.prevent="changePage(pagination.page - 1)">«</a></li>
                        <li class="page-item disabled"><span class="page-link text-dark">Trang {{ pagination.page + 1
                                }}/{{ pagination.totalPages }}</span></li>
                        <li class="page-item" :class="{ disabled: pagination.page >= pagination.totalPages - 1 }"><a
                                class="page-link" href="#" @click.prevent="changePage(pagination.page + 1)">»</a></li>
                    </ul>
                </div>
            </div>
        </div>

        <NhapKhoChiTiet v-if="showModal" :soPhieu="selectedSoPhieu" @close="showModal = false"
            @update-success="layDanhSach" />

        <div v-if="showEditModal" class="modal d-block" style="background: rgba(0,0,0,0.5)">
            <div class="modal-dialog modal-dialog-centered modal-sm">
                <div class="modal-content">
                    <div class="modal-header bg-warning p-2">
                        <h6 class="modal-title small fw-bold">Sửa Ghi Chú</h6>
                        <button type="button" class="btn-close small" @click="showEditModal = false"></button>
                    </div>
                    <div class="modal-body p-2">
                        <textarea class="form-control form-control-sm" rows="3" v-model="editItem.ghiChu"></textarea>
                    </div>
                    <div class="modal-footer p-1">
                        <button class="btn btn-secondary btn-sm" @click="showEditModal = false">Hủy</button>
                        <button class="btn btn-primary btn-sm" @click="luuCapNhat">Lưu</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive, watch } from 'vue';
import api from '@/utils/axios';
import NhapKhoChiTiet from './NhapKhoChiTiet.vue';

const API_URL = '/kho/nhap';
const danhSachPhieu = ref([]);
const listKho = ref([]);
const showModal = ref(false);
const showEditModal = ref(false);
const selectedSoPhieu = ref(null);
const loading = ref(false);
const editItem = ref({ soPhieu: '', ghiChu: '' });
const searchQuery = ref("");
const isAdmin = ref(false);
const filterMaKho = ref(0);
const pagination = reactive({ page: 0, size: 20, total: 0, totalPages: 0 });

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
    } catch (e) { console.error("Lỗi data:", e); } finally { loading.value = false; }
};
const filteredList = computed(() => {
    if (!searchQuery.value) return danhSachPhieu.value;
    const query = searchQuery.value.toLowerCase();
    return danhSachPhieu.value.filter(item => item.soPhieu.toLowerCase().includes(query) || (item.ghiChu && item.ghiChu.toLowerCase().includes(query)));
});
watch(filteredList, (newVal) => { pagination.total = newVal.length; pagination.totalPages = Math.ceil(newVal.length / pagination.size); pagination.page = 0; }, { immediate: true });
const paginatedData = computed(() => { const start = pagination.page * pagination.size; const end = start + pagination.size; return filteredList.value.slice(start, end); });
const changePage = (pageIndex) => { if (pageIndex >= 0 && pageIndex < pagination.totalPages) pagination.page = pageIndex; };
const moChiTiet = (soPhieu) => { selectedSoPhieu.value = soPhieu; showModal.value = true; };
const moModalSua = (item) => { editItem.value = { ...item }; showEditModal.value = true; };
const luuCapNhat = async () => { try { await api.put(`${API_URL}/${editItem.value.soPhieu}`, { soPhieu: editItem.value.soPhieu, ghiChu: editItem.value.ghiChu }); alert("Cập nhật thành công!"); showEditModal.value = false; layDanhSach(); } catch (e) { alert("Lỗi: " + (e.response?.data?.message || e.message)); } };
const xoaPhieu = async (soPhieu) => { if (!confirm('Cảnh báo: Xóa phiếu này sẽ xóa toàn bộ máy trong kho. Tiếp tục?')) return; try { await api.delete(`${API_URL}/${soPhieu}`); alert("Đã xóa thành công!"); layDanhSach(); } catch (e) { alert("Lỗi xóa: " + (e.response?.data?.message || e.message)); } };
const isYearLocked = (dateInput) => { if (!dateInput) return false; let year = Array.isArray(dateInput) ? dateInput[0] : new Date(dateInput).getFullYear(); return year < new Date().getFullYear(); };
const splitSummary = (str) => str ? str.split(', ') : [];
const formatDate = (dateArray) => { if (!dateArray) return ''; if (Array.isArray(dateArray)) { const f = (n) => n < 10 ? '0' + n : n; return `${f(dateArray[2])}/${f(dateArray[1])}/${dateArray[0]}`; } return new Date(dateArray).toLocaleDateString('vi-VN'); };
const formatCurrency = (v) => { if (!v) return '0 đ'; return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(v); };
onMounted(async () => { await setupPhanQuyen(); layDanhSach(); });
</script>

<style scoped>
/* -----------------------------------------------------------
   1. CSS CHO MÁY TÍNH (PC/LAPTOP - Màn hình > 768px)
   ----------------------------------------------------------- */
@media (min-width: 768px) {
    .btn-fix-height {
        height: 31px;
        /* Chiều cao chuẩn của form-control-sm trong Bootstrap */
        display: flex;
        align-items: center;
        justify-content: center;
        white-space: nowrap;
        /* Ngăn chữ bị xuống dòng */
    }

    .my-select {
        max-width: 200px;
    }

    .my-input {
        max-width: 200px;
    }
}

/* -----------------------------------------------------------
   2. CSS CHO ĐIỆN THOẠI (MOBILE - Màn hình < 768px)
   ----------------------------------------------------------- */
@media (max-width: 767px) {

    /* Chuyển toolbar thành cột dọc để không bị tràn */
    .header-toolbar {
        flex-direction: column;
        width: 100%;
    }

    /* Input và Select full chiều rộng màn hình */
    .my-select,
    .my-input {
        width: 100% !important;
        font-size: 13px;
        margin-bottom: 5px;
    }

    /* Nút Tạo cũng full chiều rộng và cách ra một chút */
    .btn-fix-height {
        width: 100%;
        justify-content: center;
        margin-top: 5px;
        height: 35px;
        /* Cao hơn xíu cho dễ bấm trên điện thoại */
    }

    /* Bo góc thẻ card trên mobile */
    .mobile-card {
        border-radius: 8px;
        overflow: hidden;
    }

    .product-summary {
        font-size: 12px;
    }
}
</style>