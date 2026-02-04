<template>
    <div class="card shadow-sm border-warning">
        <div class="card-header bg-warning text-dark d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Quản lý Xuất Kho (Bán Hàng)</h5>
            <div class="d-flex gap-2">
                <select v-if="isAdmin" class="form-select form-select-sm" style="width: 180px;" v-model="filterMaKho" @change="layDanhSach">
                    <option :value="0">-- Tất cả kho --</option>
                    <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                </select>

                <input type="text" class="form-control form-control-sm border-dark" 
                       v-model="searchQuery" 
                       placeholder="🔍 Tìm theo số phiếu, khách hàng..." 
                       style="width: 250px;">
                
                <router-link to="/xuat-kho/tao-moi" class="btn btn-dark btn-sm fw-bold">
                    <i class="fas fa-plus"></i> Tạo Phiếu Xuất
                </router-link>
            </div>
        </div>
        
        <div class="card-body">
            <div v-if="loading" class="text-center py-3">
                <div class="spinner-border text-warning" role="status"></div>
                <p>Đang tải dữ liệu...</p>
            </div>

            <div v-else>
                <table class="table table-hover table-bordered align-middle">
                    <thead class="table-light text-center">
                        <tr>
                            <th width="50px">STT</th>
                            <th>Số Phiếu</th>
                            <th>Ngày Xuất</th>
                            <th>Kho Xuất</th>
                            <th>Khách Hàng</th>
                            <th width="30%">Chi tiết (Sản phẩm x SL)</th>
                            <th>Tổng SL</th>
                            <th>Tổng Tiền</th>
                            <th width="120px">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, index) in paginatedData" :key="item.soPhieu">
                            <td class="text-center fw-bold text-secondary">
                                {{ (pagination.page * pagination.size) + index + 1 }}
                            </td>

                            <td class="fw-bold text-primary">{{ item.soPhieu }}</td>
                            <td>{{ formatDate(item.ngayXuat) }}</td>
                            <td>{{ item.tenKho }}</td>
                            <td class="fw-bold">{{ item.tenKhachHang || '---' }}</td>

                            <td>
                                <span v-for="(part, idx) in splitSummary(item.tomTatSanPham)" :key="idx" 
                                      class="badge bg-warning text-dark me-1 mb-1">
                                    {{ part }}
                                </span>
                                <div v-if="item.ghiChu" class="small text-muted fst-italic mt-1">
                                    ({{ item.ghiChu }})
                                </div>
                            </td>
                            <td class="text-center fw-bold">{{ item.tongSoLuong }}</td>
                            <td class="text-end text-danger fw-bold">{{ formatCurrency(item.tongTien) }}</td>
                            
                            <td class="text-center">
                                <button class="btn btn-sm btn-outline-info me-1" @click="moChiTiet(item.soPhieu)" title="Xem chi tiết">
                                    <i class="fas fa-eye"></i>
                                </button>

                                <span v-if="isYearLocked(item.ngayXuat)" class="text-muted ms-1" title="Đã chốt sổ năm này">
                                    <i class="fas fa-lock"></i>
                                </span>
                                <button v-else class="btn btn-sm btn-outline-danger" @click="huyPhieu(item.soPhieu)">
                                    <i class="fas fa-undo"></i>
                                </button>
                            </td>
                        </tr>
                        <tr v-if="paginatedData.length === 0">
                            <td colspan="9" class="text-center text-muted">Không tìm thấy dữ liệu phù hợp.</td>
                        </tr>
                    </tbody>
                </table>

                <div class="d-flex justify-content-center mt-3 px-3 pb-3" v-if="pagination.total > 0">
                    <ul class="pagination pagination-sm m-0">
                        <li class="page-item" :class="{ disabled: pagination.page === 0 }">
                            <a class="page-link" href="#" @click.prevent="changePage(pagination.page - 1)">« Trước</a>
                        </li>
                        <li v-for="(page, index) in visiblePages" :key="index" class="page-item" :class="{ active: page === pagination.page + 1, disabled: page === '...' }">
                            <a class="page-link" href="#" @click.prevent="page !== '...' ? changePage(page - 1) : null">{{ page }}</a>
                        </li>
                        <li class="page-item" :class="{ disabled: pagination.page >= pagination.totalPages - 1 }">
                            <a class="page-link" href="#" @click.prevent="changePage(pagination.page + 1)">Sau »</a>
                        </li>
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

const pagination = reactive({
    page: 0, size: 20, total: 0, totalPages: 0
});

const setupPhanQuyen = async () => {
    const role = localStorage.getItem('userRole');
    let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');
    
    if (!userMaKho) {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        userMaKho = userInfo.maKho;
    }

    if (role === 'ADMIN') {
        isAdmin.value = true;
        filterMaKho.value = 0; 
        await loadDanhSachKho(); 
    } else {
        isAdmin.value = false;
        filterMaKho.value = userMaKho ? parseInt(userMaKho) : 0;
    }
};

const loadDanhSachKho = async () => {
    try {
        const res = await api.get('/kho');
        listKho.value = res.data;
    } catch (e) { console.error(e); }
};

const layDanhSach = async () => {
    loading.value = true;
    try {
        const params = {};
        if (filterMaKho.value && filterMaKho.value !== 0) {
            params.maKho = filterMaKho.value;
        }

        const res = await api.get(API_URL, { params });
        danhSachPhieu.value = res.data;
    } catch (error) {
        console.error(error);
    } finally {
        loading.value = false;
    }
};

const filteredList = computed(() => {
    if (!searchQuery.value) return danhSachPhieu.value;
    const query = searchQuery.value.toLowerCase();
    return danhSachPhieu.value.filter(item => 
        item.soPhieu.toLowerCase().includes(query) || 
        (item.tenKhachHang && item.tenKhachHang.toLowerCase().includes(query)) ||
        (item.ghiChu && item.ghiChu.toLowerCase().includes(query))
    );
});

watch(filteredList, (newVal) => {
    pagination.total = newVal.length;
    pagination.totalPages = Math.ceil(newVal.length / pagination.size);
    pagination.page = 0;
}, { immediate: true });

const paginatedData = computed(() => {
    const start = pagination.page * pagination.size;
    const end = start + pagination.size;
    return filteredList.value.slice(start, end);
});

const visiblePages = computed(() => {
    const total = pagination.totalPages;
    const current = pagination.page + 1; 
    const delta = 2;
    const range = [];
    for (let i = Math.max(2, current - delta); i <= Math.min(total - 1, current + delta); i++) {
        range.push(i);
    }
    if (current - delta > 2) range.unshift("...");
    if (current + delta < total - 1) range.push("...");
    range.unshift(1);
    if (total > 1) range.push(total);
    return range;
});

const changePage = (pageIndex) => {
    if (pageIndex >= 0 && pageIndex < pagination.totalPages) {
        pagination.page = pageIndex;
    }
};

const moChiTiet = (soPhieu) => {
    selectedSoPhieu.value = soPhieu;
    showModal.value = true;
};

const huyPhieu = async (soPhieu) => {
    if(!confirm(`CẢNH BÁO: Hủy phiếu ${soPhieu} sẽ hoàn trả toàn bộ máy về trạng thái Tồn Kho (Có thể bán lại). Tiếp tục?`)) return;
    try {
        await api.delete(`${API_URL}/${soPhieu}`);
        alert("Đã hủy phiếu và hoàn trả kho thành công!");
        layDanhSach();
    } catch (error) {
        const msg = error.response?.data?.message || error.response?.data || error.message;
        alert("Lỗi: " + msg);
    }
};

const isYearLocked = (dateInput) => {
    if (!dateInput) return false;
    let year = 0;
    if (Array.isArray(dateInput)) year = dateInput[0];
    else year = new Date(dateInput).getFullYear();
    return year < new Date().getFullYear();
};

const splitSummary = (str) => str ? str.split(', ') : [];

const formatDate = (dateArray) => {
    if (!dateArray) return '';
    if (Array.isArray(dateArray)) {
        const [year, month, day, hour, minute] = dateArray;
        const f = (n) => n < 10 ? '0' + n : n;
        return `${f(day)}/${f(month)}/${year} ${hour ? f(hour) + ':' + f(minute) : ''}`;
    }
    return new Date(dateArray).toLocaleString('vi-VN');
};

const formatCurrency = (v) => {
    if(!v) return '0 đ';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);
};

onMounted(async () => {
    await setupPhanQuyen();
    layDanhSach();
});
</script>