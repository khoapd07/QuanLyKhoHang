<template>
    <div class="card shadow-sm border-info">
        <div class="card-header bg-info text-white d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Lịch Sử Chuyển Kho</h5>
            <div class="d-flex gap-2">
                <input type="text" class="form-control form-control-sm" v-model="searchQuery" placeholder="🔍 Tìm theo mã phiếu..." style="width: 250px;">
                <router-link to="/chuyen-kho/tao-moi" class="btn btn-light btn-sm fw-bold">
                    <i class="fas fa-exchange-alt"></i> Tạo Phiếu Chuyển
                </router-link>
            </div>
        </div>
        
        <div class="card-body">
            <div v-if="loading" class="text-center py-3">
                <div class="spinner-border text-info"></div> <p>Đang tải dữ liệu...</p>
            </div>

            <div v-else>
                <table class="table table-hover table-bordered align-middle">
                    <thead class="table-light text-center">
                        <tr>
                            <th width="50px">STT</th>
                            <th>Số Phiếu</th>
                            <th>Ngày Chuyển</th>
                            <th>Kho Đi (Xuất)</th>
                            <th>Kho Đến (Nhập)</th>
                            <th width="35%">Chi tiết (Sản phẩm x SL)</th>
                            <th>Tổng SL</th>
                            <th>Thao tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, index) in paginatedData" :key="item.soPhieu">
                            <td class="text-center fw-bold text-secondary">
                                {{ (pagination.page * pagination.size) + index + 1 }}
                            </td>

                            <td class="fw-bold text-primary">{{ item.soPhieu }}</td>
                            <td>{{ formatDate(item.ngayChuyen) }}</td>
                            <td class="text-danger fw-bold">{{ item.tenKhoDi }} <i class="fas fa-arrow-right text-muted small"></i></td>
                            <td class="text-success fw-bold">{{ item.tenKhoDen }}</td>
                            <td>
                                <span v-for="(part, idx) in splitSummary(item.tomTatSanPham)" :key="idx" class="badge bg-secondary me-1 mb-1">{{ part }}</span>
                                <div v-if="item.ghiChu" class="small text-muted fst-italic mt-1">({{ item.ghiChu }})</div>
                            </td>
                            <td class="text-center fw-bold">{{ item.tongSoLuong }}</td>
                            
                            <td class="text-center">
                                <button class="btn btn-sm btn-outline-info me-1" @click="moChiTiet(item.soPhieu)" title="Xem chi tiết">
                                    <i class="fas fa-eye"></i>
                                </button>
                                <button class="btn btn-sm btn-outline-danger" @click="xoaPhieu(item.soPhieu)" title="Hủy phiếu chuyển">
                                    <i class="fas fa-trash-alt"></i>
                                </button>
                            </td>
                        </tr>
                        <tr v-if="paginatedData.length === 0"><td colspan="8" class="text-center text-muted">Không có dữ liệu.</td></tr>
                    </tbody>
                </table>

                <div class="d-flex justify-content-center mt-3" v-if="pagination.total > 0">
                    <ul class="pagination pagination-sm">
                        <li class="page-item" :class="{ disabled: pagination.page === 0 }"><a class="page-link" href="#" @click.prevent="changePage(pagination.page - 1)">«</a></li>
                        <li v-for="p in visiblePages" :key="p" class="page-item" :class="{ active: p === pagination.page + 1, disabled: p === '...' }"><a class="page-link" href="#" @click.prevent="p !== '...' ? changePage(p - 1) : null">{{ p }}</a></li>
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