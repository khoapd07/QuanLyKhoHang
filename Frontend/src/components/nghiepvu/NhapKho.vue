<template>
    <div class="card shadow-sm">
        <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Quản lý Nhập Kho</h5>
            <div class="d-flex gap-2">
                <select v-if="isAdmin" class="form-select form-select-sm" style="width: 180px;" v-model="filterMaKho" @change="layDanhSach">
                    <option :value="0">-- Tất cả kho --</option>
                    <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                </select>

                <input type="text" class="form-control form-control-sm" v-model="searchQuery" placeholder="🔍 Tìm theo số phiếu..." style="width: 200px;">
                
                <router-link to="/nhap-kho/tao-moi" class="btn btn-light btn-sm fw-bold">
                    <i class="fas fa-plus"></i> Tạo Phiếu Mới
                </router-link>
            </div>
        </div>
        
        <div class="card-body">
            <div v-if="loading" class="text-center py-3">
                <div class="spinner-border text-primary" role="status"></div>
                <p>Đang tải dữ liệu...</p>
            </div>

            <div v-else>
                <table class="table table-hover table-bordered align-middle">
                    <thead class="table-light text-center">
                        <tr>
                            <th width="50px">STT</th>
                            <th>Số Phiếu</th>
                            <th>Ngày Nhập</th>
                            <th>Kho</th>
                            <th>Nhà Cung Cấp</th> 
                            <th width="25%">Chi tiết</th>
                            <th>Hiện Trạng (Còn/Tổng)</th>
                            <th>Giá Trị Tồn</th>
                            <th width="150px">Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, index) in paginatedData" :key="item.soPhieu">
                            <td class="text-center fw-bold text-secondary">
                                {{ (pagination.page * pagination.size) + index + 1 }}
                            </td>

                            <td class="fw-bold text-primary">{{ item.soPhieu }}</td>
                            <td>{{ formatDate(item.ngayNhap) }}</td>
                            <td>{{ item.tenKho }}</td>
                            <td class="fw-bold">{{ item.tenKhachHang || '---' }}</td>
                            <td>
                                <span v-for="(part, idx) in splitSummary(item.tomTatSanPham)" :key="idx" 
                                      class="badge bg-info text-dark me-1 mb-1">
                                    {{ part }}
                                </span>
                                <div v-if="item.ghiChu" class="small text-muted fst-italic mt-1">
                                    <i class="fas fa-comment-alt me-1"></i>{{ item.ghiChu }}
                                </div>
                            </td>
                            <td class="text-center">
                                <span class="fw-bold text-success fs-6">{{ item.soLuongConLai }}</span>
                                <span class="text-muted small"> / {{ item.tongSoLuongMay }}</span>
                                <div class="progress mt-1" style="height: 4px;">
                                    <div class="progress-bar bg-success" role="progressbar" 
                                         :style="{width: (item.soLuongConLai / item.tongSoLuongMay * 100) + '%'}" 
                                         aria-valuenow="25" aria-valuemin="0" aria-valuemax="100"></div>
                                </div>
                            </td>
                            <td class="text-end">
                                <div class="fw-bold text-danger">{{ formatCurrency(item.tienConLai) }}</div>
                                <small class="text-muted text-decoration-line-through">{{ formatCurrency(item.tongTien) }}</small>
                            </td>
                            <td class="text-center">
                                <button class="btn btn-sm btn-outline-info me-1" @click="moChiTiet(item.soPhieu)" title="Xem chi tiết"><i class="fas fa-eye"></i></button>
                                <button class="btn btn-sm btn-outline-warning me-1" @click="moModalSua(item)" title="Sửa ghi chú"><i class="fas fa-edit"></i></button>
                                
                                <span v-if="isYearLocked(item.ngayNhap)" class="text-muted ms-1" title="Năm đã chốt sổ"><i class="fas fa-lock"></i></span>
                                <button v-else-if="item.soLuongConLai === item.tongSoLuongMay" class="btn btn-sm btn-outline-danger" @click="xoaPhieu(item.soPhieu)" title="Xóa phiếu"><i class="fas fa-trash-alt"></i></button>
                                <button v-else class="btn btn-sm btn-secondary" disabled title="Không thể xóa vì đã có hàng xuất bán"><i class="fas fa-trash-alt"></i></button>
                            </td>
                        </tr>
                        <tr v-if="paginatedData.length === 0">
                            <td colspan="9" class="text-center text-muted">Không tìm thấy dữ liệu phù hợp</td>
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
        
        <NhapKhoChiTiet v-if="showModal" :soPhieu="selectedSoPhieu" @close="showModal = false" @update-success="layDanhSach" />

        <div v-if="showEditModal" class="modal d-block" style="background: rgba(0,0,0,0.5)">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header bg-warning">
                        <h5 class="modal-title">Cập nhật Phiếu: {{ editItem.soPhieu }}</h5>
                        <button type="button" class="btn-close" @click="showEditModal = false"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label">Ghi Chú</label>
                            <textarea class="form-control" rows="3" v-model="editItem.ghiChu"></textarea>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button class="btn btn-secondary" @click="showEditModal = false">Hủy</button>
                        <button class="btn btn-primary" @click="luuCapNhat">Lưu thay đổi</button>
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

const pagination = reactive({
    page: 0,
    size: 20,
    total: 0,
    totalPages: 0
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
    } catch (e) { 
        console.error("Lỗi data:", e); 
    } finally { loading.value = false; }
};

const filteredList = computed(() => {
    if (!searchQuery.value) return danhSachPhieu.value;
    const query = searchQuery.value.toLowerCase();
    return danhSachPhieu.value.filter(item => 
        item.soPhieu.toLowerCase().includes(query) || 
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

const moModalSua = (item) => {
    editItem.value = { ...item };
    showEditModal.value = true;
};

const luuCapNhat = async () => {
    try {
        const payload = { soPhieu: editItem.value.soPhieu, ghiChu: editItem.value.ghiChu };
        await api.put(`${API_URL}/${editItem.value.soPhieu}`, payload);
        alert("Cập nhật thành công!");
        showEditModal.value = false;
        layDanhSach(); 
    } catch (e) {
        alert("Lỗi cập nhật: " + (e.response?.data?.message || e.message));
    }
};

const xoaPhieu = async (soPhieu) => {
    if(!confirm('Cảnh báo: Xóa phiếu này sẽ xóa toàn bộ máy trong kho. Tiếp tục?')) return;
    try {
        await api.delete(`${API_URL}/${soPhieu}`);
        alert("Đã xóa thành công!");
        layDanhSach();
    } catch (e) { 
        alert("Lỗi xóa: " + (e.response?.data?.message || e.message)); 
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