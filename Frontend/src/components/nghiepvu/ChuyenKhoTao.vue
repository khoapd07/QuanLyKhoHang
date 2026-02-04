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
                            <label class="form-label fw-bold">
                                2. Chọn Mã Máy 
                                <span v-if="availableSerials.length > 0" class="badge bg-success">Tồn: {{ availableSerials.length }}</span>
                                <span v-else class="badge bg-secondary">Tồn: 0</span>
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
                                
                                <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 300px; overflow-y: auto;">
                                    <input type="text" class="form-control mb-2" v-model="searchText" placeholder="🔍 Tìm serial nhanh...">
                                    
                                    <div v-if="filteredSerials.length > 0">
                                        <div class="form-check py-1" v-for="s in filteredSerials" :key="s">
                                            <input class="form-check-input" type="checkbox" :value="s" :id="s" v-model="selectedSerials">
                                            <label class="form-check-label w-100" :for="s" style="cursor: pointer;">
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
                            {{ s }} <i class="fas fa-times ms-1" style="cursor: pointer;" @click="removeSerial(s)"></i>
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
                            <div class="d-flex flex-wrap gap-1">
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

// Item đang thao tác
const currentItem = reactive({ maSP: '' });
const listHienThi = ref([]);

// Logic Multi-select
const availableSerials = ref([]); // Danh sách máy lấy từ API
const selectedSerials = ref([]);  // Danh sách máy đang tích chọn
const searchText = ref("");

// Lọc serial theo ô tìm kiếm
const filteredSerials = computed(() => {
    if (!searchText.value) return availableSerials.value;
    return availableSerials.value.filter(s => s.toLowerCase().includes(searchText.value.toLowerCase()));
});

// Load dữ liệu ban đầu
const loadMaster = async () => {
    try {
        const [k, s] = await Promise.all([
            api.get('/kho'), 
            api.get('/san-pham')
        ]);
        
        listKho.value = k.data; 
        
        // [FIX LỖI QUAN TRỌNG] Kiểm tra nếu API trả về dạng Page (có .content) hay List thường
        if (s.data && s.data.content && Array.isArray(s.data.content)) {
            listSanPham.value = s.data.content; // Trường hợp có phân trang
        } else {
            listSanPham.value = s.data; // Trường hợp list thường
        }
    } catch(e) { 
        console.error("Lỗi tải master data:", e); 
    }
};

// Khi chọn sản phẩm -> Gọi API lấy máy tồn kho
const onChonSanPham = async () => {
    // Reset lựa chọn cũ
    selectedSerials.value = []; 
    availableSerials.value = [];
    
    if (!form.maKhoDi) {
        alert("Vui lòng chọn Kho Đi trước!");
        currentItem.maSP = "";
        return;
    }

    try {
        // Gọi API lấy máy tồn của Sản phẩm X tại Kho Y
        const res = await api.get('/kho/may-in/kha-dung', { 
            params: { 
                maSP: currentItem.maSP, 
                maKho: form.maKhoDi 
            } 
        });
        availableSerials.value = res.data;
    } catch(e) { 
        console.error(e);
        alert("Lỗi tải danh sách máy tồn kho!"); 
    }
};

// Thêm dòng vào bảng bên dưới
const themVaoDanhSach = () => {
    if (!currentItem.maSP) return alert("Chưa chọn sản phẩm!");
    if (selectedSerials.value.length === 0) return alert("Chưa chọn máy nào để chuyển!");

    // Thêm vào danh sách hiển thị
    listHienThi.value.push({ 
        maSP: currentItem.maSP, 
        danhSachSeri: [...selectedSerials.value] 
    });

    // Reset để chọn tiếp SP khác
    currentItem.maSP = ""; 
    selectedSerials.value = []; 
    availableSerials.value = [];
    searchText.value = "";
};

// Gửi dữ liệu về Server
const luuPhieu = async () => {
    if (!form.maKhoDi || !form.maKhoDen) return alert("Vui lòng chọn đủ Kho Đi và Kho Đến!");
    
    // Gom tất cả serial từ các dòng thành 1 mảng duy nhất
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

// Reset khi đổi Kho Đi
const resetSelection = () => { 
    currentItem.maSP = ""; 
    listHienThi.value = []; 
    selectedSerials.value = []; 
    availableSerials.value = [];
};

// Bỏ tích 1 serial
const removeSerial = (s) => {
    selectedSerials.value = selectedSerials.value.filter(item => item !== s);
};

// Helper lấy tên SP
const getTenSP = (id) => listSanPham.value.find(s => s.maSP === id)?.tenSP || id;

onMounted(() => loadMaster());
</script>

<style scoped>
/* Thanh cuộn đẹp cho dropdown */
.dropdown-menu::-webkit-scrollbar { width: 6px; }
.dropdown-menu::-webkit-scrollbar-thumb { background-color: #ccc; border-radius: 4px; }
</style>