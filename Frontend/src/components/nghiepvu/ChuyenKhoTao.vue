<template>
    <div class="card border-info shadow-sm">
        <div class="card-header bg-info text-white d-flex justify-content-between align-items-center py-2">
            <h6 class="mb-0 fw-bold header-title"><i class="fas fa-exchange-alt me-1"></i> TẠO PHIẾU CHUYỂN</h6>
            <button class="btn btn-sm btn-light text-info fw-bold back-btn" @click="$router.push('/chuyen-kho')">
                <i class="fas fa-arrow-left"></i> Quay lại
            </button>
        </div>
        <div class="card-body p-2">
            <div class="bg-light border rounded p-2 mb-3">
                <div class="row g-2">
                    <div class="col-12 col-md-4 custom-col">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">KHO ĐI (XUẤT) (*)</label>
                        <select class="form-select form-select-sm shadow-none text-truncate" 
                                v-model="form.maKhoDi" @change="resetSelection">
                            <option :value="null" disabled>-- Chọn Kho Đi --</option>
                            <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                        </select>
                    </div>
                    <div class="col-12 col-md-4 custom-col">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">KHO ĐẾN (NHẬP) (*)</label>
                        <select class="form-select form-select-sm shadow-none text-truncate" 
                                v-model="form.maKhoDen">
                            <option :value="null" disabled>-- Chọn Kho Đến --</option>
                            <option v-for="k in listKho.filter(x => x.maKho !== form.maKhoDi)" 
                                    :key="k.maKho" :value="k.maKho">
                                {{ k.tenKho }}
                            </option>
                        </select>
                    </div>
                    <div class="col-12 col-md-4 custom-col">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">GHI CHÚ</label>
                        <input type="text" class="form-control form-control-sm shadow-none" 
                               v-model="form.ghiChu" placeholder="...">
                    </div>
                </div>
            </div>

            <div class="card border-info mb-3 shadow-sm" v-if="form.maKhoDi">
                <div class="card-body p-2 bg-white">
                    <div class="row g-2 align-items-end">
                        <div class="col-12 col-md-4">
                            <label class="form-label small mb-0 fw-bold small-label">1. SẢN PHẨM</label>
                            <select class="form-select form-select-sm text-truncate" 
                                    v-model="currentItem.maSP" @change="onChonSanPham" style="width: 100%;">
                                <option value="" disabled>-- Chọn Sản Phẩm --</option>
                                <option v-for="sp in listSanPham" :key="sp.maSP" :value="sp.maSP">
                                    {{ sp.tenSP }} ({{ sp.maSP }})
                                </option>
                            </select>
                        </div>
                        
                        <div class="col-12 col-md-6">
                            <label class="form-label small mb-0 fw-bold d-flex justify-content-between small-label">
                                <span>2. MÃ MÁY <span class="badge bg-secondary">{{ availableSerials.length }}</span></span>
                                <small class="text-primary" v-if="selectedSerials.length > 0">Chọn: {{ selectedSerials.length }}</small>
                            </label>
                            
                            <div class="dropdown">
                                <button class="btn btn-outline-secondary btn-sm w-100 text-start d-flex justify-content-between align-items-center bg-white text-truncate" 
                                        type="button" data-bs-toggle="dropdown" 
                                        :disabled="!currentItem.maSP || availableSerials.length === 0"
                                        style="height: 31px;">
                                    <span class="text-truncate">
                                        {{ selectedSerials.length > 0 ? `Đã chọn ${selectedSerials.length} máy` : (currentItem.maSP ? '-- Chọn các máy --' : '-- Chọn SP trước --') }}
                                    </span>
                                    <i class="fas fa-chevron-down ms-1"></i>
                                </button>
                                
                                <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 350px; overflow-y: auto;">
                                    <input type="text" class="form-control form-control-sm mb-2" v-model="searchText" placeholder="🔍 Tìm serial (Shift để chọn nhiều)...">
                                    
                                    <div class="d-flex align-items-center px-2 py-1 mb-1 bg-light rounded border" v-if="filteredSerials.length > 0">
                                        <input class="form-check-input m-0 me-2" type="checkbox" id="selectAll" 
                                               :checked="isAllSelected" @change="toggleSelectAll" style="cursor: pointer;">
                                        <label class="form-check-label small fw-bold cursor-pointer w-100" for="selectAll">
                                            Chọn tất cả ({{ filteredSerials.length }})
                                        </label>
                                    </div>

                                    <div v-if="filteredSerials.length > 0">
                                        <div class="d-flex align-items-center py-1 px-2 hover-bg rounded" v-for="(s, index) in filteredSerials" :key="s">
                                            <input class="form-check-input m-0 me-2" type="checkbox" :value="s" :id="s" 
                                                   v-model="selectedSerials" @click="handleShiftClick($event, index)" style="cursor: pointer;">
                                            <label class="form-check-label w-100 small cursor-pointer text-break" :for="s">{{ s }}</label>
                                        </div>
                                    </div>
                                    <div v-else class="text-center text-muted py-2 small">Không có máy.</div>
                                </div>
                            </div>
                        </div>

                        <div class="col-12 col-md-2">
                            <button class="btn btn-info text-white btn-sm w-100 fw-bold btn-add-fix" @click="themVaoDanhSach">
                                <i class="fas fa-plus-circle"></i> Thêm
                            </button>
                        </div>
                    </div>
                    
                    <div class="mt-2 d-flex flex-wrap gap-1" v-if="selectedSerials.length > 0">
                        <span v-for="s in selectedSerials" :key="s" class="badge bg-primary" style="font-size: 10px;">
                            {{ s }} <i class="fas fa-times ms-1 cursor-pointer" @click="removeSerial(s)"></i>
                        </span>
                        <span class="badge bg-danger cursor-pointer" @click="selectedSerials = []" v-if="selectedSerials.length > 5" style="font-size: 10px;">
                            Xóa hết
                        </span>
                    </div>
                </div>
            </div>

            <div class="table-responsive border rounded bg-white d-none d-md-block" v-if="listHienThi.length > 0">
                <table class="table table-sm table-bordered align-middle mb-0 small" style="font-size: 13px;">
                    <thead class="table-secondary text-center">
                        <tr>
                            <th width="40px">STT</th>
                            <th>Sản Phẩm</th>
                            <th width="80px">SL</th>
                            <th>Danh Sách Serial</th>
                            <th width="50px">Xóa</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, index) in listHienThi" :key="index">
                            <td class="text-center">{{ index + 1 }}</td>
                            <td class="fw-bold text-primary">{{ getTenSP(item.maSP) }}</td>
                            <td class="fw-bold text-center">{{ item.danhSachSeri.length }}</td>
                            <td>
                                <div class="d-flex flex-wrap gap-1" style="max-height: 80px; overflow-y: auto;">
                                    <span class="badge bg-secondary" style="font-size: 10px;" v-for="s in item.danhSachSeri" :key="s">{{ s }}</span>
                                </div>
                            </td>
                            <td class="text-center p-0">
                                <button class="btn btn-link btn-sm text-danger" @click="listHienThi.splice(index, 1)">
                                    <i class="fas fa-trash-alt"></i>
                                </button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="d-md-none" v-if="listHienThi.length > 0">
                <div v-for="(item, index) in listHienThi" :key="index" class="card mb-2 border shadow-sm">
                    <div class="card-body p-2">
                        <div class="d-flex justify-content-between align-items-start mb-2">
                            <div>
                                <span class="badge bg-info me-1">{{ index + 1 }}</span>
                                <span class="fw-bold text-primary small">{{ getTenSP(item.maSP) }}</span>
                            </div>
                            <button class="btn btn-sm btn-outline-danger border-0 py-0" @click="listHienThi.splice(index, 1)">
                                <i class="fas fa-trash-alt"></i>
                            </button>
                        </div>
                        
                        <div class="d-flex justify-content-between align-items-center mb-2 small">
                            <span class="text-muted">Số lượng chuyển:</span>
                            <span class="fw-bold">{{ item.danhSachSeri.length }}</span>
                        </div>

                        <div class="bg-light p-2 rounded border" style="max-height: 100px; overflow-y: auto;">
                            <div class="d-flex flex-wrap gap-1">
                                <span class="badge bg-secondary" style="font-size: 9px;" v-for="s in item.danhSachSeri" :key="s">{{ s }}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="mt-3 text-center">
                <button class="btn btn-primary px-5 fw-bold shadow-sm" @click="luuPhieu" :disabled="listHienThi.length === 0" style="font-size: 13px;">
                    <i class="fas fa-save me-1"></i> HOÀN THÀNH
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed, reactive } from 'vue';
import api from '@/utils/axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const listKho = ref([]);
const listSanPham = ref([]);
const form = reactive({ maKhoDi: null, maKhoDen: null, ghiChu: '' });

const currentItem = reactive({ maSP: '' });
const listHienThi = ref([]);

// Logic Multi-select
const availableSerials = ref([]); 
const selectedSerials = ref([]);  
const searchText = ref("");
let lastCheckedIndex = -1;

const getDataSafe = (res) => {
    if(!res || !res.data) return [];
    if(res.data.content && Array.isArray(res.data.content)) return res.data.content;
    if(Array.isArray(res.data)) return res.data;
    return [];
}

const filteredSerials = computed(() => {
    if (!searchText.value) return availableSerials.value;
    return availableSerials.value.filter(s => s.toLowerCase().includes(searchText.value.toLowerCase()));
});

const isAllSelected = computed(() => {
    if (filteredSerials.value.length === 0) return false;
    return filteredSerials.value.every(s => selectedSerials.value.includes(s));
});

const toggleSelectAll = (e) => {
    const isChecked = e.target.checked;
    if (isChecked) {
        filteredSerials.value.forEach(s => { if (!selectedSerials.value.includes(s)) selectedSerials.value.push(s); });
    } else {
        selectedSerials.value = selectedSerials.value.filter(s => !filteredSerials.value.includes(s));
    }
};

const handleShiftClick = (event, index) => {
    if (event.shiftKey && lastCheckedIndex !== -1) {
        const start = Math.min(lastCheckedIndex, index);
        const end = Math.max(lastCheckedIndex, index);
        const subList = filteredSerials.value.slice(start, end + 1);
        const isChecking = event.target.checked;
        subList.forEach(serial => {
            if (isChecking) { if (!selectedSerials.value.includes(serial)) selectedSerials.value.push(serial); } 
            else { selectedSerials.value = selectedSerials.value.filter(s => s !== serial); }
        });
    }
    lastCheckedIndex = index;
};

const loadMaster = async () => {
    try {
        const [k, s] = await Promise.all([api.get('/kho'), api.get('/san-pham')]);
        listKho.value = getDataSafe(k); listSanPham.value = getDataSafe(s);
    } catch(e) { console.error("Lỗi tải master data:", e); }
};

const onChonSanPham = async () => {
    selectedSerials.value = []; availableSerials.value = []; searchText.value = ""; lastCheckedIndex = -1;
    if (!form.maKhoDi) return alert("Vui lòng chọn Kho Đi trước!");
    try {
        const res = await api.get('/kho/may-in/kha-dung', { params: { maSP: currentItem.maSP, maKho: form.maKhoDi } });
        availableSerials.value = res.data;
    } catch(e) { console.error(e); alert("Lỗi tải danh sách máy tồn kho!"); }
};

const themVaoDanhSach = () => {
    if (!currentItem.maSP) return alert("Chưa chọn sản phẩm!");
    if (selectedSerials.value.length === 0) return alert("Chưa chọn máy nào!");
    listHienThi.value.push({ maSP: currentItem.maSP, danhSachSeri: [...selectedSerials.value] });
    currentItem.maSP = ""; selectedSerials.value = []; availableSerials.value = []; searchText.value = ""; lastCheckedIndex = -1;
};

const luuPhieu = async () => {
    if (!form.maKhoDi || !form.maKhoDen) return alert("Vui lòng chọn đủ Kho Đi và Kho Đến!");
    const allSerials = listHienThi.value.flatMap(x => x.danhSachSeri);
    const payload = { maKhoDi: form.maKhoDi, maKhoDen: form.maKhoDen, ghiChu: form.ghiChu, danhSachSerial: allSerials };
    try {
        await api.post('/kho/chuyen', payload);
        alert("Chuyển kho thành công!"); router.push('/chuyen-kho');
    } catch(e) { const msg = e.response?.data?.message || e.message; alert("Lỗi: " + msg); }
};

const resetSelection = () => { currentItem.maSP = ""; listHienThi.value = []; selectedSerials.value = []; availableSerials.value = []; };
const removeSerial = (s) => { selectedSerials.value = selectedSerials.value.filter(item => item !== s); };
const getTenSP = (id) => listSanPham.value.find(s => s.maSP === id)?.tenSP || id;

onMounted(() => loadMaster());
</script>

<style scoped>
/* PC CSS */
@media (min-width: 768px) {
    .btn-add-fix { height: 31px; display: flex; align-items: center; justify-content: center; }
}

/* MOBILE CSS */
@media (max-width: 767px) {
    .header-title { font-size: 13px !important; }
    .back-btn { font-size: 11px !important; }
    .small-label { font-size: 10px !important; margin-bottom: 0 !important; }
    
    .form-select-sm, .form-control-sm {
        font-size: 12px !important;
        height: 30px !important;
        width: 100% !important;
        max-width: 100% !important;
    }
    .custom-col { width: 100%; margin-bottom: 5px; }
}

.dropdown-menu::-webkit-scrollbar { width: 6px; }
.dropdown-menu::-webkit-scrollbar-thumb { background-color: #ccc; border-radius: 4px; }
.cursor-pointer { cursor: pointer; }
.hover-bg:hover { background-color: #f8f9fa; }
</style>