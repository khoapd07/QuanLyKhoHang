<template>
    <div class="card shadow-sm border-primary">
        <div class="card-header bg-primary text-white p-2">
            <div class="d-flex flex-column flex-md-row justify-content-between align-items-center gap-2">
                <h6 class="mb-0 fw-bold"><i class="bi bi-box-seam me-1"></i> QUẢN LÝ NHẬP KHO</h6>

                <div class="d-flex gap-1 w-100 w-md-auto justify-content-end header-toolbar">
                    <select v-if="isAdmin" class="form-select form-select-sm my-select" v-model="filterMaKho" @change="layDanhSach">
                        <option :value="0">-- Tất cả kho --</option>
                        <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                    </select>

                    <router-link to="/nhap-kho/tao-moi" class="btn btn-light btn-sm fw-bold d-flex align-items-center btn-fix-height">
                        <i class="fas fa-plus me-1"></i> Tạo Mới
                    </router-link>
                </div>
            </div>
        </div>

        <div class="card-body p-2">
            <div class="bg-light border rounded p-2 mb-2">
                <div class="row g-2">
                    <div class="col-md-3">
                        <input type="text" class="form-control form-control-sm border-primary" v-model="filters.keyword" placeholder="🔍 Tìm mã phiếu, ghi chú...">
                    </div>
                    
                    <div class="col-md-3">
                        <div class="dropdown">
                            <button class="form-select form-select-sm text-start bg-white border-primary shadow-none" type="button" data-bs-toggle="dropdown">
                                <span class="text-truncate">{{ filters.maHT || '-- Lọc theo Hình thức --' }}</span>
                            </button>
                            <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 300px; overflow-y: auto;">
                                <input type="text" class="form-control form-control-sm mb-2 border-primary" v-model="searchFilterHT" placeholder="🔍 Tìm hình thức..." @click.stop>
                                <button type="button" class="dropdown-item small py-2 border-bottom text-wrap hover-bg" @click="filters.maHT = ''">-- Tất cả --</button>
                                <button type="button" class="dropdown-item small py-2 border-bottom text-wrap hover-bg" 
                                        v-for="ht in filteredListHT" :key="ht.maHT" @click="filters.maHT = ht.tenHT">
                                    {{ ht.tenHT }}
                                </button>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-3">
                        <div class="dropdown">
                            <button class="form-select form-select-sm text-start bg-white border-primary shadow-none" type="button" data-bs-toggle="dropdown">
                                <span class="text-truncate">{{ filters.tenKhach || '-- Lọc theo Nhà cung cấp --' }}</span>
                            </button>
                            <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 300px; overflow-y: auto;">
                                <input type="text" class="form-control form-control-sm mb-2 border-primary" v-model="searchFilterNcc" placeholder="🔍 Tìm nhà cung cấp..." @click.stop>
                                <button type="button" class="dropdown-item small py-2 border-bottom text-wrap hover-bg" @click="filters.tenKhach = ''">-- Tất cả --</button>
                                <button type="button" class="dropdown-item small py-2 border-bottom text-wrap hover-bg" 
                                        v-for="ncc in filteredListNcc" :key="ncc.maDonVi" @click="filters.tenKhach = ncc.tenDonVi">
                                    {{ ncc.tenDonVi }}
                                </button>
                            </div>
                        </div>
                    </div>
                    
                    <div class="col-md-3">
                        <div class="dropdown">
                            <button class="form-select form-select-sm text-start bg-white border-primary shadow-none" type="button" data-bs-toggle="dropdown">
                                <span class="text-truncate">{{ filters.maSP || '-- Lọc theo Model máy --' }}</span>
                            </button>
                            <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 300px; overflow-y: auto;">
                                <input type="text" class="form-control form-control-sm mb-2 border-primary" v-model="searchFilterSP" placeholder="🔍 Tìm Model máy..." @click.stop>
                                <button type="button" class="dropdown-item small py-2 border-bottom text-wrap hover-bg" @click="filters.maSP = ''">-- Tất cả --</button>
                                <button type="button" class="dropdown-item small py-2 border-bottom text-wrap hover-bg" 
                                        v-for="sp in filteredListSP" :key="sp.maSP" @click="filters.maSP = sp.maSP">
                                    <strong>{{ sp.maSP }}</strong> - {{ sp.tenSP }}
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

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
                                <th width="20%">Hình Thức Nhập</th>
                                <th>Hiện Trạng</th>
                                <th>Giá Trị</th>
                                <th width="180px">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(item, index) in paginatedData" :key="item.soPhieu">
                                <td class="text-center text-secondary">{{ (pagination.page * pagination.size) + index + 1 }}</td>
                                <td class="fw-bold text-primary">{{ item.soPhieu }}</td>
                                <td>{{ formatDate(item.ngayNhap) }}</td>
                                <td>{{ item.tenKho }}</td>
                                <td class="fw-bold">{{ item.tenKhachHang || '-' }}</td>
                                <td class="text-center">
                                    <span class="fw-bold text-success">{{ item.tenHinhThuc || '---' }}</span>
                                    <div v-if="item.ghiChu" class="text-muted fst-italic text-truncate mt-1" style="max-width: 150px; margin: 0 auto;">
                                        ({{ item.ghiChu }})
                                    </div>
                                </td>
                                <td class="text-center">
                                    <span class="fw-bold text-success">{{ item.soLuongConLai }}</span>/{{ item.tongSoLuongMay }}
                                </td>
                                <td class="text-end fw-bold text-danger">{{ formatCurrency(item.tienConLai) }}</td>
                                <td class="text-center">
                                    <div class="btn-group btn-group-sm">
                                        <button class="btn btn-outline-info" @click="moChiTiet(item.soPhieu)" title="Xem chi tiết">
                                            Chi tiết
                                        </button>
                                        <button
                                            v-if="!isYearLocked(item.ngayNhap) && item.soLuongConLai === item.tongSoLuongMay"
                                            class="btn btn-outline-danger" @click="xoaPhieu(item.soPhieu)" title="Xóa phiếu">
                                            Xóa
                                        </button>
                                    </div>
                                </td>
                            </tr>
                            <tr v-if="paginatedData.length === 0"><td colspan="9" class="text-center text-muted py-3">Không tìm thấy dữ liệu phù hợp với bộ lọc.</td></tr>
                        </tbody>
                    </table>
                </div>

                <div class="d-md-none">
                    <div v-for="(item, index) in paginatedData" :key="item.soPhieu" class="card mb-2 shadow-sm border-0 mobile-card">
                        <div class="card-body p-2">
                            <div class="d-flex justify-content-between align-items-center mb-1 border-bottom pb-1">
                                <div>
                                    <span class="badge bg-secondary me-1">#{{ (pagination.page * pagination.size) + index + 1 }}</span>
                                    <span class="fw-bold text-primary">{{ item.soPhieu }}</span>
                                </div>
                                <small class="text-muted" style="font-size: 11px;">{{ formatDate(item.ngayNhap) }}</small>
                            </div>

                            <div class="mb-2" style="font-size: 12px;">
                                <div class="text-truncate fw-bold text-dark mb-1">
                                    <i class="bi bi-shop me-1 text-muted"></i> {{ item.tenKho }}
                                </div>
                                <div class="text-truncate text-muted">
                                    <i class="bi bi-truck me-1"></i> {{ item.tenKhachHang }}
                                </div>
                                <div class="text-truncate text-success fw-bold mt-1">
                                    <i class="bi bi-tag-fill me-1 text-muted"></i> {{ item.tenHinhThuc || '---' }}
                                </div>
                            </div>

                            <div class="bg-light rounded p-2 mb-2 product-summary">
                                <div v-if="item.ghiChu" class="text-muted fst-italic" style="font-size: 11px;">
                                    📝 {{ item.ghiChu }}
                                </div>
                                <div v-else class="text-muted fst-italic" style="font-size: 11px;">
                                    Không có ghi chú
                                </div>
                            </div>

                            <div class="d-flex justify-content-between align-items-end">
                                <div style="font-size: 11px;">
                                    <div>SL: <span class="fw-bold text-success">{{ item.soLuongConLai }}/{{ item.tongSoLuongMay }}</span></div>
                                    <div>Tồn: <span class="fw-bold text-danger">{{ formatCurrency(item.tienConLai) }}</span></div>
                                </div>

                                <div class="btn-group btn-group-sm">
                                    <button class="btn btn-outline-info px-2" @click="moChiTiet(item.soPhieu)">
                                        Chi tiết
                                    </button>
                                    <button
                                        v-if="!isYearLocked(item.ngayNhap) && item.soLuongConLai === item.tongSoLuongMay"
                                        class="btn btn-outline-danger px-2" @click="xoaPhieu(item.soPhieu)">
                                        Xóa
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div v-if="paginatedData.length === 0" class="text-center text-muted small py-3">Không có dữ liệu.</div>
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

        <NhapKhoChiTiet v-if="showModal" :soPhieu="selectedSoPhieu" @close="showModal = false" @update-success="layDanhSach" />

    </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive, watch } from 'vue';
import api from '@/utils/axios';
import NhapKhoChiTiet from './NhapKhoChiTiet.vue';

const API_URL = '/kho/nhap';
const danhSachPhieu = ref([]);
const listKho = ref([]);
const listSanPham = ref([]);
const listTrangThai = ref([]);
const listDonVi = ref([]);
const listHinhThuc = ref([]);

const showModal = ref(false);
const selectedSoPhieu = ref(null);
const loading = ref(false);
const isAdmin = ref(false);
const filterMaKho = ref(0);
const pagination = reactive({ page: 0, size: 20, total: 0, totalPages: 0 });

// BỘ LỌC ĐA NĂNG
const filters = reactive({
    keyword: '',
    maHT: '',
    tenKhach: '',
    maSP: ''
});

// BIẾN TÌM KIẾM TRONG DROPDOWN LỌC
const searchFilterHT = ref('');
const searchFilterNcc = ref('');
const searchFilterSP = ref('');

const listNhaCungCap = computed(() => {
    if (!Array.isArray(listDonVi.value)) return [];
    return listDonVi.value.filter(dv => {
        const loai = (dv.loaiDonVi && typeof dv.loaiDonVi === 'object') ? dv.loaiDonVi.loaiDonVi : dv.loaiDonVi;
        return loai === 1; // NCC
    });
});

const filteredListHT = computed(() => {
    if (!searchFilterHT.value) return listHinhThuc.value;
    return listHinhThuc.value.filter(ht => ht.tenHT.toLowerCase().includes(searchFilterHT.value.toLowerCase()));
});

const filteredListNcc = computed(() => {
    if (!searchFilterNcc.value) return listNhaCungCap.value;
    return listNhaCungCap.value.filter(ncc => ncc.tenDonVi.toLowerCase().includes(searchFilterNcc.value.toLowerCase()));
});

const filteredListSP = computed(() => {
    if (!searchFilterSP.value) return listSanPham.value;
    const kw = searchFilterSP.value.toLowerCase();
    return listSanPham.value.filter(sp => sp.tenSP.toLowerCase().includes(kw) || sp.maSP.toLowerCase().includes(kw));
});


const getDataSafe = (res) => (res?.data?.content && Array.isArray(res.data.content)) ? res.data.content : (Array.isArray(res?.data) ? res.data : []);

const setupPhanQuyen = async () => {
    const role = localStorage.getItem('userRole');
    let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');
    if (!userMaKho) { const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}'); userMaKho = userInfo.maKho; }
    if (role === 'ADMIN') { isAdmin.value = true; filterMaKho.value = 0; }
    else { isAdmin.value = false; filterMaKho.value = userMaKho ? parseInt(userMaKho) : 0; }
    
    try {
        const [k, sp, tt, d, ht] = await Promise.all([
            api.get('/kho'),
            api.get('/san-pham/list'),
            api.get('/trang-thai'),
            api.get('/don-vi?size=1000'),
            api.get('/hinh-thuc-nhap')
        ]);
        listKho.value = getDataSafe(k);
        listSanPham.value = getDataSafe(sp); 
        listTrangThai.value = getDataSafe(tt);
        listDonVi.value = getDataSafe(d);
        listHinhThuc.value = getDataSafe(ht);
        await layDanhSach();
    } catch(e) { console.error("Lỗi load danh mục:", e); }
};

const layDanhSach = async () => {
    loading.value = true;
    try {
        const params = {}; if (filterMaKho.value && filterMaKho.value !== 0) params.maKho = filterMaKho.value;
        const res = await api.get(API_URL, { params }); danhSachPhieu.value = res.data;
    } catch (e) { console.error("Lỗi data:", e); } finally { loading.value = false; }
};

// LOGIC LỌC PHIẾU
const filteredList = computed(() => {
    let result = danhSachPhieu.value;
    
    if (filters.keyword) {
        const kw = filters.keyword.toLowerCase();
        result = result.filter(item => item.soPhieu.toLowerCase().includes(kw) || (item.ghiChu && item.ghiChu.toLowerCase().includes(kw)));
    }
    if (filters.maHT) {
        result = result.filter(item => item.tenHinhThuc === filters.maHT);
    }
    if (filters.tenKhach) {
        result = result.filter(item => item.tenKhachHang === filters.tenKhach);
    }
    if (filters.maSP) {
        result = result.filter(item => item.tomTatSanPham && item.tomTatSanPham.includes(`[${filters.maSP}]`));
    }
    return result;
});

watch(filteredList, (newVal) => { pagination.total = newVal.length; pagination.totalPages = Math.ceil(newVal.length / pagination.size); pagination.page = 0; }, { immediate: true });
const paginatedData = computed(() => { const start = pagination.page * pagination.size; const end = start + pagination.size; return filteredList.value.slice(start, end); });
const changePage = (pageIndex) => { if (pageIndex >= 0 && pageIndex < pagination.totalPages) pagination.page = pageIndex; };
const moChiTiet = (soPhieu) => { selectedSoPhieu.value = soPhieu; showModal.value = true; };
const xoaPhieu = async (soPhieu) => { if (!confirm('Cảnh báo: Xóa phiếu này sẽ xóa toàn bộ máy trong kho. Tiếp tục?')) return; try { await api.delete(`${API_URL}/${soPhieu}`); alert("Đã xóa thành công!"); layDanhSach(); } catch (e) { alert("Lỗi xóa: " + (e.response?.data?.message || e.message)); } };
const isYearLocked = (dateInput) => { if (!dateInput) return false; let year = Array.isArray(dateInput) ? dateInput[0] : new Date(dateInput).getFullYear(); return year < new Date().getFullYear(); };
const formatDate = (dateArray) => { if (!dateArray) return ''; if (Array.isArray(dateArray)) { const f = (n) => n < 10 ? '0' + n : n; return `${f(dateArray[2])}/${f(dateArray[1])}/${dateArray[0]}`; } return new Date(dateArray).toLocaleDateString('vi-VN'); };
const formatCurrency = (v) => { if (!v) return '0 đ'; return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND', maximumFractionDigits: 0 }).format(v); };

onMounted(() => { setupPhanQuyen(); });
</script>

<style scoped>
.dropdown-toggle::after { display: none !important; }
@media (min-width: 768px) { .btn-fix-height { height: 31px; display: flex; align-items: center; justify-content: center; white-space: nowrap; } .my-select { max-width: 200px; } .my-input { max-width: 200px; } }
@media (max-width: 767px) { .header-toolbar { flex-direction: column; width: 100%; } .my-select, .my-input { width: 100% !important; font-size: 13px; margin-bottom: 5px; } .btn-fix-height { width: 100%; justify-content: center; margin-top: 5px; height: 35px; } .mobile-card { border-radius: 8px; overflow: hidden; } .product-summary { font-size: 12px; } }
</style>