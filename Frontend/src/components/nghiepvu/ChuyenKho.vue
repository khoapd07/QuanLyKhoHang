<template>
    <div class="card shadow-sm border-info">
        <div class="card-header bg-info text-white p-2">
            <div class="d-flex flex-column flex-md-row justify-content-between align-items-center gap-2">
                <h6 class="mb-0 fw-bold text-nowrap"><i class="fas fa-history me-1"></i> LỊCH SỬ CHUYỂN KHO</h6>
                
                <div class="d-flex gap-1 w-100 w-md-auto justify-content-end header-toolbar">
                    <input type="text" class="form-control form-control-sm my-input" 
                           v-model="searchQuery" 
                           placeholder="🔍 Tìm mã phiếu...">
                    
                    <router-link to="/chuyen-kho/tao-moi" class="btn btn-light btn-sm fw-bold d-flex align-items-center btn-fix-height">
                        <i class="fas fa-plus me-1"></i> Tạo Phiếu
                    </router-link>
                </div>
            </div>
        </div>
        
        <div class="card-body p-2">
            <div v-if="loading" class="text-center py-3">
                <div class="spinner-border spinner-border-sm text-info"></div> <span class="small ms-2">Đang tải...</span>
            </div>

            <div v-else>
                <div class="table-responsive d-none d-md-block">
                    <table class="table table-hover table-bordered align-middle small mb-0">
                        <thead class="table-light text-center">
                            <tr>
                                <th width="50px">STT</th>
                                <th>Số Phiếu</th>
                                <th>Ngày Chuyển</th>
                                <th>Kho Đi (Xuất)</th>
                                <th>Kho Đến (Nhập)</th>
                                <th width="35%">Chi tiết</th>
                                <th>Tổng SL</th>
                                <th width="150px">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(item, index) in paginatedData" :key="item.soPhieu">
                                <td class="text-center text-secondary">{{ (pagination.page * pagination.size) + index + 1 }}</td>
                                <td class="fw-bold text-primary">{{ item.soPhieu }}</td>
                                <td>{{ formatDate(item.ngayChuyen) }}</td>
                                <td class="text-danger fw-bold">{{ item.tenKhoDi }} <i class="fas fa-arrow-right text-muted small"></i></td>
                                <td class="text-success fw-bold">{{ item.tenKhoDen }}</td>
                                <td>
                                    <span v-for="(part, idx) in splitSummary(item.tomTatSanPham)" :key="idx" 
                                          class="badge bg-secondary me-1 border mb-1 text-wrap text-start">
                                        {{ part }}
                                    </span>
                                    <div v-if="item.ghiChu" class="text-muted fst-italic text-truncate" style="max-width: 200px;">
                                        ({{ item.ghiChu }})
                                    </div>
                                </td>
                                <td class="text-center fw-bold">{{ item.tongSoLuong }}</td>
                                <td class="text-center">
                                    <div class="btn-group btn-group-sm">
                                        <button class="btn btn-outline-info" @click="moChiTiet(item.soPhieu)" title="Xem">
                                            <i class="fas fa-eye"></i> CT
                                        </button>
                                        <button class="btn btn-outline-danger" @click="xoaPhieu(item.soPhieu)" title="Hủy">
                                            <i class="fas fa-trash-alt"></i> Hủy
                                        </button>
                                    </div>
                                </td>
                            </tr>
                            <tr v-if="paginatedData.length === 0"><td colspan="8" class="text-center text-muted">Không có dữ liệu.</td></tr>
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
                                <small class="text-muted" style="font-size: 11px;">{{ formatDate(item.ngayChuyen) }}</small>
                            </div>

                            <div class="mb-2 small" style="font-size: 12px; line-height: 1.4;">
                                <div class="text-danger fw-bold text-truncate">
                                    <i class="fas fa-sign-out-alt me-1"></i> Từ: {{ item.tenKhoDi }}
                                </div>
                                <div class="text-success fw-bold text-truncate">
                                    <i class="fas fa-sign-in-alt me-1"></i> Đến: {{ item.tenKhoDen }}
                                </div>
                            </div>

                            <div class="bg-light rounded p-2 mb-2 product-summary">
                                <div v-for="(part, idx) in splitSummary(item.tomTatSanPham)" :key="idx" class="fw-bold text-dark mb-1">
                                    • {{ part }}
                                </div>
                                <div v-if="item.ghiChu" class="text-muted fst-italic mt-1" style="font-size: 11px;">
                                    📝 {{ item.ghiChu }}
                                </div>
                            </div>

                            <div class="d-flex justify-content-between align-items-end">
                                <div style="font-size: 11px;">
                                    Tổng SL: <span class="badge bg-info text-dark">{{ item.tongSoLuong }}</span>
                                </div>
                                
                                <div class="btn-group btn-group-sm">
                                    <button class="btn btn-outline-info px-2" @click="moChiTiet(item.soPhieu)">CT</button>
                                    <button class="btn btn-outline-danger px-2" @click="xoaPhieu(item.soPhieu)">Hủy</button>
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
        <ChuyenKhoChiTiet v-if="showModal" :soPhieu="selectedSoPhieu" @close="showModal = false" />
    </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed, watch } from 'vue';
import api from '@/utils/axios';
import ChuyenKhoChiTiet from './ChuyenKhoChiTiet.vue';

const listData = ref([]);
const loading = ref(false);
const showModal = ref(false);
const selectedSoPhieu = ref(null);
const searchQuery = ref("");
const pagination = reactive({ page: 0, size: 20, total: 0, totalPages: 0 });

const filteredList = computed(() => {
    if (!searchQuery.value) return listData.value;
    const q = searchQuery.value.toLowerCase();
    return listData.value.filter(i => i.soPhieu.toLowerCase().includes(q));
});

watch(filteredList, (val) => {
    pagination.total = val.length;
    pagination.totalPages = Math.ceil(val.length / pagination.size);
    pagination.page = 0;
}, { immediate: true });

const paginatedData = computed(() => filteredList.value.slice(pagination.page * pagination.size, (pagination.page + 1) * pagination.size));
const visiblePages = computed(() => {
    let pages = [];
    for(let i=1; i<=pagination.totalPages; i++) pages.push(i);
    return pages.slice(0, 5); 
});

const changePage = (p) => { if(p >= 0 && p < pagination.totalPages) pagination.page = p; };

const loadData = async () => {
    loading.value = true;
    try {
        const res = await api.get('/kho/chuyen');
        listData.value = res.data;
    } catch (e) { console.error(e); } finally { loading.value = false; }
};

const xoaPhieu = async (soPhieu) => {
    if(!confirm(`Bạn có chắc muốn HỦY phiếu chuyển ${soPhieu}?\nCác máy trong phiếu này sẽ được trả về kho cũ.`)) return;
    try {
        await api.delete(`/kho/chuyen/${soPhieu}`);
        alert("Đã hủy phiếu chuyển thành công!");
        loadData(); 
    } catch (e) {
        const msg = e.response?.data?.message || e.message;
        alert("Lỗi: " + msg);
    }
};

const moChiTiet = (id) => { selectedSoPhieu.value = id; showModal.value = true; };
const splitSummary = (s) => s ? s.split(', ') : [];
const formatDate = (d) => { if(!d) return ''; if(Array.isArray(d)) return `${d[2]}/${d[1]}/${d[0]} ${d[3]}:${d[4]}`; return new Date(d).toLocaleString('vi-VN'); };

onMounted(() => loadData());
</script>

<style scoped>
/* 1. CSS CHO MÁY TÍNH (Web) */
@media (min-width: 768px) {
    .btn-fix-height {
        height: 31px; /* Cao bằng ô input */
        display: flex; align-items: center; justify-content: center;
    }
    .my-input { max-width: 250px; }
}

/* 2. CSS CHO ĐIỆN THOẠI (Mobile) */
@media (max-width: 767px) {
    .header-toolbar { flex-direction: column; width: 100%; }
    .my-input { width: 100% !important; max-width: 100% !important; font-size: 13px; margin-bottom: 5px; }
    .btn-fix-height { width: 100%; justify-content: center; margin-top: 5px; height: 35px; }
    
    .mobile-card { border-radius: 8px; overflow: hidden; }
    .product-summary { font-size: 12px; }
}
</style>