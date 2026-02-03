<template>
    <div class="card border-warning">
        <div class="card-header bg-warning text-dark d-flex justify-content-between">
            <h5 class="mb-0">Tạo Phiếu Xuất Kho</h5>
            <button class="btn btn-sm btn-dark" @click="$router.push('/xuat-kho')">
                <i class="fas fa-arrow-left"></i> Quay lại
            </button>
        </div>
        <div class="card-body">
            <div class="row mb-4">
                <div class="col-md-4">
                    <label class="form-label">Kho Xuất (*)</label>
                    <select class="form-select" v-model="phieuXuat.maKho" @change="resetSelection">
                        <option :value="null" disabled>-- Chọn Kho --</option>
                        <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label">Khách Hàng (*)</label>
                    <select class="form-select" v-model="phieuXuat.maDonVi">
                         <option :value="null" disabled>-- Chọn Khách Hàng --</option>
                         <option v-for="kh in listKhachHang" :key="kh.maDonVi" :value="kh.maDonVi">{{ kh.tenDonVi }}</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label">Ghi Chú</label>
                    <input type="text" class="form-control" v-model="phieuXuat.ghiChu" placeholder="Lý do xuất...">
                </div>
            </div>

            <div class="row g-3 bg-light p-3 rounded mb-3 border border-warning">
                <div class="col-12">
                    <div class="alert alert-info py-2 small">
                        <i class="fas fa-info-circle"></i> Chọn kho và sản phẩm để xem danh sách số Serial <b>đang tồn kho</b>.
                    </div>
                </div>

                <div class="col-md-4">
                    <label class="form-label">Sản Phẩm</label>
                    <select class="form-select" v-model="currentItem.maSP" @change="onChonSanPham">
                        <option value="" disabled>-- Chọn SP --</option>
                        <option v-for="sp in listSanPham" :key="sp.maSP" :value="sp.maSP">
                            {{ sp.tenSP }} ({{ sp.maSP }})
                        </option>
                    </select>
                </div>

                <div class="col-md-2">
                    <label class="form-label">Giá Bán</label>
                    <input type="number" class="form-control" v-model="currentItem.donGia">
                </div>

                <div class="col-md-1">
                    <label class="form-label">SL</label>
                    <input type="text" class="form-control fw-bold text-center bg-white" :value="selectedSerials.length" readonly disabled>
                </div>

                <div class="col-md-5">
                    <label class="form-label d-flex justify-content-between">
                        <span>Danh Sách Mã Máy (Tồn: {{ availableSerials.length }})</span>
                        <small class="text-primary" v-if="selectedSerials.length > 0">Đã chọn: {{ selectedSerials.length }}</small>
                    </label>
                    
                    <div class="dropdown">
                        <button class="btn btn-outline-secondary w-100 text-start d-flex justify-content-between align-items-center" 
                                type="button" data-bs-toggle="dropdown" aria-expanded="false" 
                                :disabled="!currentItem.maSP || !phieuXuat.maKho">
                            <span class="text-truncate">
                                {{ selectedSerials.length > 0 ? `Đã chọn ${selectedSerials.length} máy...` : '-- Chọn các máy cần xuất --' }}
                            </span>
                            <i class="fas fa-chevron-down"></i>
                        </button>
                        
                        <div class="dropdown-menu w-100 p-2" style="max-height: 300px; overflow-y: auto;">
                            <input type="text" class="form-control mb-2" v-model="searchText" placeholder="🔍 Tìm serial...">
                            
                            <div v-if="filteredSerials.length > 0">
                                <div class="form-check" v-for="seri in filteredSerials" :key="seri">
                                    <input class="form-check-input" type="checkbox" :value="seri" :id="seri" v-model="selectedSerials">
                                    <label class="form-check-label w-100" :for="seri" style="cursor: pointer;">
                                        {{ seri }}
                                    </label>
                                </div>
                            </div>
                            <div v-else class="text-center text-muted py-2 small">
                                Không tìm thấy máy phù hợp trong kho này.
                            </div>
                        </div>
                    </div>
                    
                    <div class="mt-2 d-flex flex-wrap gap-1">
                        <span v-for="s in selectedSerials" :key="s" class="badge bg-primary">
                            {{ s }} <i class="fas fa-times ms-1" style="cursor: pointer;" @click="removeSerial(s)"></i>
                        </span>
                    </div>
                </div>

                <div class="col-md-12 text-end">
                    <button class="btn btn-warning" @click="themDongChiTiet">
                        <i class="fas fa-plus-circle"></i> Thêm vào phiếu
                    </button>
                </div>
            </div>

            <table class="table table-bordered mt-3" v-if="listHienThi.length > 0">
                <thead class="table-secondary">
                    <tr>
                        <th>Sản Phẩm</th>
                        <th>Giá Bán</th>
                        <th>SL</th>
                        <th>Danh Sách Serial</th>
                        <th>Thành Tiền</th>
                        <th>Xóa</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(item, index) in listHienThi" :key="index">
                        <td>{{ getTenSP(item.maSP) }}</td>
                        <td>{{ formatCurrency(item.donGia) }}</td>
                        <td class="fw-bold text-center">{{ item.soLuong }}</td>
                        <td>
                            <span class="badge bg-secondary me-1" v-for="s in item.danhSachSeri" :key="s">{{ s }}</span>
                        </td>
                        <td>{{ formatCurrency(item.donGia * item.soLuong) }}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-danger" @click="listHienThi.splice(index, 1)">X</button>
                        </td>
                    </tr>
                </tbody>
            </table>

            <div class="mt-4 text-center">
                <button class="btn btn-primary px-5" @click="luuPhieuXuat" :disabled="listHienThi.length === 0">
                    <i class="fas fa-save"></i> Hoàn thành Xuất Kho
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue';
import api from '@/utils/axios'; 
import { useRouter } from 'vue-router';

const router = useRouter();
const listKho = ref([]);
const listDonVi = ref([]);
const listSanPham = ref([]);

const phieuXuat = ref({ maKho: null, maDonVi: null, ghiChu: '', chiTietPhieuXuat: [] });
const currentItem = ref({ maSP: '', donGia: 0 });
const listHienThi = ref([]);

// --- LOGIC MỚI: MULTI-SELECT ---
const availableSerials = ref([]); // Danh sách máy lấy từ API
const selectedSerials = ref([]);  // Các máy user tích chọn
const searchText = ref("");       // Text tìm kiếm

// 1. Filter danh sách dựa trên ô tìm kiếm
const filteredSerials = computed(() => {
    if (!searchText.value) return availableSerials.value;
    return availableSerials.value.filter(s => s.toLowerCase().includes(searchText.value.toLowerCase()));
});

// 2. Khi chọn SP, gọi API lấy danh sách máy tồn kho
const onChonSanPham = async () => {
    // Reset chọn cũ
    selectedSerials.value = [];
    availableSerials.value = [];
    
    if (!phieuXuat.value.maKho) {
        alert("Vui lòng chọn Kho Xuất trước!");
        currentItem.value.maSP = ""; // Reset SP nếu chưa chọn kho
        return;
    }

    try {
        // Gọi API: /api/kho/may-in/kha-dung?maSP=...&maKho=...
        const res = await api.get('/kho/may-in/kha-dung', {
            params: {
                maSP: currentItem.value.maSP,
                maKho: phieuXuat.value.maKho
            }
        });
        availableSerials.value = res.data;
    } catch (e) {
        console.error(e);
        alert("Không tải được danh sách máy tồn kho!");
    }
};

// 3. Reset khi đổi Kho
const resetSelection = () => {
    currentItem.value.maSP = "";
    availableSerials.value = [];
    selectedSerials.value = [];
    listHienThi.value = []; // Xóa luôn danh sách đã thêm vì khác kho
};

// 4. Xóa tag serial đã chọn
const removeSerial = (s) => {
    selectedSerials.value = selectedSerials.value.filter(item => item !== s);
};

// ------------------------------------------------

const listKhachHang = computed(() => {
    return listDonVi.value.filter(dv => {
        const loai = (dv.loaiDonVi && typeof dv.loaiDonVi === 'object') ? dv.loaiDonVi.loaiDonVi : dv.loaiDonVi;
        return loai === 2;
    });
});

const loadMasterData = async () => {
    try {
        const [k, d, s] = await Promise.all([
             api.get('/kho'),      
             api.get('/don-vi'),   
             api.get('/san-pham')  
        ]);
        listKho.value = k.data; 
        listDonVi.value = d.data; 
        listSanPham.value = s.data;
    } catch (e) { console.error(e); }
};

const themDongChiTiet = () => {
    if (!currentItem.value.maSP) return alert("Chưa chọn sản phẩm");
    if (selectedSerials.value.length === 0) return alert("Chưa chọn máy nào để xuất!");

    // Thêm vào danh sách hiển thị
    listHienThi.value.push({
        maSP: currentItem.value.maSP,
        donGia: currentItem.value.donGia,
        soLuong: selectedSerials.value.length, // SL tự động bằng số máy đã chọn
        danhSachSeri: [...selectedSerials.value] // Copy mảng
    });

    // Reset form item để chọn tiếp SP khác
    currentItem.value.maSP = "";
    currentItem.value.donGia = 0;
    selectedSerials.value = [];
    availableSerials.value = [];
};

const luuPhieuXuat = async () => {
    if (!phieuXuat.value.maKho || !phieuXuat.value.maDonVi) return alert("Vui lòng chọn Kho và Khách hàng");
    
    const payload = {
        maKho: phieuXuat.value.maKho,
        maDonVi: phieuXuat.value.maDonVi,
        ghiChu: phieuXuat.value.ghiChu,
        chiTietPhieuXuat: listHienThi.value
    };

    try {
        await api.post('/kho/xuat', payload);
        alert("Xuất kho thành công!");
        router.push('/xuat-kho');
    } catch (error) {
        const msg = error.response?.data?.message || error.response?.data || error.message;
        alert("Lỗi: " + msg);
    }
};

const getTenSP = (maSP) => listSanPham.value.find(s => s.maSP === maSP)?.tenSP || maSP;
const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

onMounted(() => loadMasterData());
</script>

<style scoped>
/* Tùy chỉnh thanh cuộn cho dropdown */
.dropdown-menu::-webkit-scrollbar {
    width: 6px;
}
.dropdown-menu::-webkit-scrollbar-thumb {
    background-color: #ccc; 
    border-radius: 4px;
}
</style>