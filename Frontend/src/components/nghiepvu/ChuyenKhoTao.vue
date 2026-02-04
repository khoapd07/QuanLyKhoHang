<template>
    <div class="card border-info">
        <div class="card-header bg-info text-white d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Tạo Phiếu Chuyển Kho</h5>
            <button class="btn btn-sm btn-light text-info fw-bold" @click="$router.push('/chuyen-kho')">
                <i class="fas fa-arrow-left"></i> Quay lại
            </button>
        </div>
        <div class="card-body">
            <div class="row mb-4">
                <div class="col-md-4">
                    <label class="form-label fw-bold">Kho Đi (Xuất) (*)</label>
                    <select class="form-select" v-model="form.maKhoDi" @change="resetSelection">
                        <option :value="null" disabled>-- Chọn Kho Đi --</option>
                        <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label fw-bold">Kho Đến (Nhập) (*)</label>
                    <select class="form-select" v-model="form.maKhoDen">
                        <option :value="null" disabled>-- Chọn Kho Đến --</option>
                        <option v-for="k in listKho.filter(x => x.maKho !== form.maKhoDi)" 
                                :key="k.maKho" :value="k.maKho">
                            {{ k.tenKho }}
                        </option>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label">Ghi Chú</label>
                    <input type="text" class="form-control" v-model="form.ghiChu" placeholder="Ví dụ: Chuyển hàng cho chi nhánh...">
                </div>
            </div>

            <div class="card bg-light border-info mb-3" v-if="form.maKhoDi">
                <div class="card-body">
                    <div class="row g-3 align-items-end">
                        <div class="col-md-4">
                            <label class="form-label fw-bold">1. Chọn Sản Phẩm</label>
                            <select class="form-select" v-model="currentItem.maSP" @change="onChonSanPham">
                                <option value="" disabled>-- Chọn Sản Phẩm --</option>
                                <option v-for="sp in listSanPham" :key="sp.maSP" :value="sp.maSP">
                                    {{ sp.tenSP }} ({{ sp.maSP }})
                                </option>
                            </select>
                        </div>
                        
                        <div class="col-md-6">
                            <label class="form-label fw-bold d-flex justify-content-between">
                                <span>2. Chọn Mã Máy <span class="badge bg-secondary">Tồn: {{ availableSerials.length }}</span></span>
                                <span class="text-primary" v-if="selectedSerials.length > 0">Đang chọn: {{ selectedSerials.length }}</span>
                            </label>
                            
                            <div class="dropdown">
                                <button class="btn btn-outline-secondary w-100 text-start d-flex justify-content-between align-items-center bg-white" 
                                        type="button" data-bs-toggle="dropdown" 
                                        :disabled="!currentItem.maSP || availableSerials.length === 0">
                                    <span class="text-truncate">
                                        {{ selectedSerials.length > 0 ? `Đã chọn ${selectedSerials.length} máy` : (currentItem.maSP ? '-- Chọn các máy cần chuyển --' : '-- Vui lòng chọn SP trước --') }}
                                    </span>
                                    <i class="fas fa-chevron-down"></i>
                                </button>
                                
                                <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 400px; overflow-y: auto;">
                                    <input type="text" class="form-control mb-2" v-model="searchText" placeholder="🔍 Tìm serial (hoặc gõ Shift để chọn nhiều)...">
                                    
                                    <div class="d-flex justify-content-between align-items-center px-2 py-2 mb-2 bg-light rounded border" v-if="filteredSerials.length > 0">
                                        <div class="form-check mb-0">
                                            <input class="form-check-input" type="checkbox" id="selectAll" 
                                                   :checked="isAllSelected" 
                                                   @change="toggleSelectAll">
                                            <label class="form-check-label fw-bold cursor-pointer" for="selectAll">
                                                Chọn tất cả ({{ filteredSerials.length }})
                                            </label>
                                        </div>
                                    </div>

                                    <div v-if="filteredSerials.length > 0">
                                        <div class="form-check py-1 px-2 hover-bg" v-for="(s, index) in filteredSerials" :key="s">
                                            <input class="form-check-input" type="checkbox" 
                                                   :value="s" :id="s" 
                                                   v-model="selectedSerials"
                                                   @click="handleShiftClick($event, index)">
                                            
                                            <label class="form-check-label w-100 cursor-pointer" :for="s">
                                                {{ s }}
                                            </label>
                                        </div>
                                    </div>
                                    <div v-else class="text-center text-muted py-2 small">
                                        {{ currentItem.maSP ? 'Không tìm thấy mã máy nào.' : 'Hãy chọn sản phẩm trước.' }}
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-2">
                            <button class="btn btn-info text-white w-100 fw-bold" @click="themVaoDanhSach">
                                <i class="fas fa-plus-circle"></i> Thêm
                            </button>
                        </div>
                    </div>
                    
                    <div class="mt-2 d-flex flex-wrap gap-1" v-if="selectedSerials.length > 0">
                        <span v-for="s in selectedSerials" :key="s" class="badge bg-primary">
                            {{ s }} <i class="fas fa-times ms-1 cursor-pointer" @click="removeSerial(s)"></i>
                        </span>
                        <span class="badge bg-danger cursor-pointer" @click="selectedSerials = []" v-if="selectedSerials.length > 2">
                            Xóa hết
                        </span>
                    </div>
                </div>
            </div>

            <table class="table table-bordered table-striped mt-3" v-if="listHienThi.length > 0">
                <thead class="table-secondary text-center">
                    <tr>
                        <th>STT</th>
                        <th>Sản Phẩm</th>
                        <th>Số Lượng</th>
                        <th>Danh Sách Serial</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(item, index) in listHienThi" :key="index">
                        <td class="text-center">{{ index + 1 }}</td>
                        <td class="fw-bold text-primary">{{ getTenSP(item.maSP) }}</td>
                        <td class="fw-bold text-center">{{ item.danhSachSeri.length }}</td>
                        <td>
                            <div class="d-flex flex-wrap gap-1" style="max-height: 100px; overflow-y: auto;">
                                <span class="badge bg-secondary" v-for="s in item.danhSachSeri" :key="s">{{ s }}</span>
                            </div>
                        </td>
                        <td class="text-center">
                            <button class="btn btn-sm btn-outline-danger" @click="listHienThi.splice(index, 1)" title="Xóa dòng này">
                                <i class="fas fa-trash-alt"></i>
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>

            <div class="mt-4 text-center">
                <button class="btn btn-primary px-5 py-2 fw-bold" @click="luuPhieu" :disabled="listHienThi.length === 0">
                    <i class="fas fa-save me-2"></i> HOÀN THÀNH CHUYỂN KHO
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
let lastCheckedIndex = -1; // Biến lưu vị trí click gần nhất để xử lý Shift

// Hàm lấy data an toàn
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

// [LOGIC MỚI] Kiểm tra xem đã chọn hết danh sách chưa
const isAllSelected = computed(() => {
    if (filteredSerials.value.length === 0) return false;
    return filteredSerials.value.every(s => selectedSerials.value.includes(s));
});

// [LOGIC MỚI] Xử lý nút Chọn Tất Cả
const toggleSelectAll = (e) => {
    const isChecked = e.target.checked;
    if (isChecked) {
        // Thêm những cái chưa có trong filteredSerials vào selectedSerials
        filteredSerials.value.forEach(s => {
            if (!selectedSerials.value.includes(s)) {
                selectedSerials.value.push(s);
            }
        });
    } else {
        // Bỏ chọn những cái đang hiển thị
        selectedSerials.value = selectedSerials.value.filter(s => !filteredSerials.value.includes(s));
    }
};

// [LOGIC MỚI] Xử lý Shift + Click
const handleShiftClick = (event, index) => {
    // Nếu giữ phím Shift và đã từng click 1 cái trước đó
    if (event.shiftKey && lastCheckedIndex !== -1) {
        const start = Math.min(lastCheckedIndex, index);
        const end = Math.max(lastCheckedIndex, index);
        
        // Lấy danh sách serial nằm trong khoảng click
        const subList = filteredSerials.value.slice(start, end + 1);
        const isChecking = event.target.checked;

        subList.forEach(serial => {
            if (isChecking) {
                if (!selectedSerials.value.includes(serial)) selectedSerials.value.push(serial);
            } else {
                selectedSerials.value = selectedSerials.value.filter(s => s !== serial);
            }
        });
    }
    // Cập nhật vị trí click cuối cùng
    lastCheckedIndex = index;
};

const loadMaster = async () => {
    try {
        const [k, s] = await Promise.all([
            api.get('/kho'), 
            api.get('/san-pham')
        ]);
        listKho.value = getDataSafe(k); 
        listSanPham.value = getDataSafe(s);
    } catch(e) { console.error("Lỗi tải master data:", e); }
};

const onChonSanPham = async () => {
    selectedSerials.value = []; 
    availableSerials.value = [];
    searchText.value = "";
    lastCheckedIndex = -1;
    
    if (!form.maKhoDi) return alert("Vui lòng chọn Kho Đi trước!");

    try {
        const res = await api.get('/kho/may-in/kha-dung', { 
            params: { maSP: currentItem.maSP, maKho: form.maKhoDi } 
        });
        availableSerials.value = res.data;
    } catch(e) { console.error(e); alert("Lỗi tải danh sách máy tồn kho!"); }
};

const themVaoDanhSach = () => {
    if (!currentItem.maSP) return alert("Chưa chọn sản phẩm!");
    if (selectedSerials.value.length === 0) return alert("Chưa chọn máy nào!");

    listHienThi.value.push({ 
        maSP: currentItem.maSP, 
        danhSachSeri: [...selectedSerials.value] 
    });

    currentItem.maSP = ""; 
    selectedSerials.value = []; 
    availableSerials.value = [];
    searchText.value = "";
    lastCheckedIndex = -1;
};

const luuPhieu = async () => {
    if (!form.maKhoDi || !form.maKhoDen) return alert("Vui lòng chọn đủ Kho Đi và Kho Đến!");
    
    const allSerials = listHienThi.value.flatMap(x => x.danhSachSeri);
    const payload = {
        maKhoDi: form.maKhoDi,
        maKhoDen: form.maKhoDen,
        ghiChu: form.ghiChu,
        danhSachSerial: allSerials
    };
    
    try {
        await api.post('/kho/chuyen', payload);
        alert("Chuyển kho thành công!");
        router.push('/chuyen-kho');
    } catch(e) { 
        const msg = e.response?.data?.message || e.message;
        alert("Lỗi: " + msg); 
    }
};

const resetSelection = () => { 
    currentItem.maSP = ""; listHienThi.value = []; selectedSerials.value = []; availableSerials.value = []; 
};
const removeSerial = (s) => { selectedSerials.value = selectedSerials.value.filter(item => item !== s); };
const getTenSP = (id) => listSanPham.value.find(s => s.maSP === id)?.tenSP || id;

onMounted(() => loadMaster());
</script>

<style scoped>
.dropdown-menu::-webkit-scrollbar { width: 6px; }
.dropdown-menu::-webkit-scrollbar-thumb { background-color: #ccc; border-radius: 4px; }
.cursor-pointer { cursor: pointer; }
.hover-bg:hover { background-color: #f8f9fa; }
</style>