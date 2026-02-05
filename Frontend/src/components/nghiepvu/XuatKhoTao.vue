<template>
    <div class="card border-warning shadow-sm">
        <div class="card-header bg-warning text-dark py-2 d-flex justify-content-between align-items-center">
            <h5 class="mb-0 fw-bold header-title"><i class="fas fa-plus-circle me-1"></i> TẠO PHIẾU XUẤT</h5>
            <button class="btn btn-sm btn-dark fw-bold py-0 px-2 back-btn" @click="$router.push('/xuat-kho')">
                <i class="fas fa-arrow-left"></i> Quay lại
            </button>
        </div>
        <div class="card-body p-2">
            <div class="bg-light border rounded p-2 mb-3">
                <div class="row g-2">
                    <div class="col-12 col-md-4">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">KHO XUẤT (*)</label>
                        <select class="form-select form-select-sm shadow-none" v-model="phieuXuat.maKho" @change="resetSelection" :disabled="!isAdmin">
                            <option :value="null" disabled>-- Chọn Kho --</option>
                            <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                        </select>
                    </div>
                    <div class="col-12 col-md-4">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">KHÁCH HÀNG (*)</label>
                        <select class="form-select form-select-sm shadow-none" v-model="phieuXuat.maDonVi">
                             <option :value="null" disabled>-- Chọn Khách --</option>
                             <option v-for="kh in listKhachHang" :key="kh.maDonVi" :value="kh.maDonVi">{{ kh.tenDonVi }}</option>
                        </select>
                    </div>
                    <div class="col-12 col-md-4">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">GHI CHÚ</label>
                        <input type="text" class="form-control form-control-sm shadow-none" v-model="phieuXuat.ghiChu" placeholder="...">
                    </div>
                </div>
            </div>

            <div class="card border-warning mb-3 shadow-sm">
                <div class="card-body p-2 bg-white">
                    <div class="alert alert-info py-1 px-2 mb-2 small" style="font-size: 11px;">
                        <i class="fas fa-info-circle"></i> Chọn kho & SP để xem máy tồn.
                    </div>

                    <div class="row g-2 align-items-end">
                        <div class="col-12 col-md-4">
                            <label class="form-label small mb-0 fw-bold small-label">1. SẢN PHẨM</label>
                            <select class="form-select form-select-sm text-truncate" v-model="currentItem.maSP" @change="onChonSanPham" style="width: 100%;">
                                <option value="" disabled>-- Chọn SP --</option>
                                <option v-for="sp in listSanPham" :key="sp.maSP" :value="sp.maSP">{{ sp.tenSP }} ({{ sp.maSP }})</option>
                            </select>
                        </div>

                        <div class="col-4 col-md-2">
                            <label class="form-label small mb-0 fw-bold small-label">GIÁ BÁN</label>
                            <input type="number" class="form-control form-control-sm" v-model="currentItem.donGia">
                        </div>

                        <div class="col-8 col-md-4">
                            <label class="form-label small mb-0 fw-bold d-flex justify-content-between small-label">
                                <span>2. SERIAL MÁY <span class="badge bg-secondary">{{ availableSerials.length }}</span></span>
                                <small class="text-primary" v-if="selectedSerials.length > 0">Chọn: {{ selectedSerials.length }}</small>
                            </label>
                            
                            <div class="dropdown">
                                <button class="btn btn-outline-secondary btn-sm w-100 text-start d-flex justify-content-between align-items-center bg-white text-truncate" 
                                        type="button" data-bs-toggle="dropdown" 
                                        :disabled="!currentItem.maSP || !phieuXuat.maKho"
                                        style="height: 31px;">
                                    <span class="text-truncate">
                                        {{ selectedSerials.length > 0 ? `Đã chọn ${selectedSerials.length}...` : 'Chọn máy...' }}
                                    </span>
                                    <i class="fas fa-chevron-down ms-1"></i>
                                </button>
                                
                                <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 300px; overflow-y: auto;">
                                    <input type="text" class="form-control form-control-sm mb-2" v-model="searchText" placeholder="🔍 Tìm...">
                                    
                                    <div v-if="filteredSerials.length > 0">
                                        <div class="d-flex align-items-center px-1 py-1 bg-light border mb-1">
                                            <input class="form-check-input me-2" type="checkbox" id="selectAll" :checked="isAllSelected" @change="toggleSelectAll">
                                            <label class="form-check-label small fw-bold w-100" for="selectAll">Chọn tất cả</label>
                                        </div>
                                        <div class="form-check py-1 px-1 hover-bg" v-for="(seri, index) in filteredSerials" :key="seri">
                                            <input class="form-check-input" type="checkbox" :value="seri" :id="seri" v-model="selectedSerials" @click="handleShiftClick($event, index)">
                                            <label class="form-check-label w-100 small cursor-pointer" :for="seri">{{ seri }}</label>
                                        </div>
                                    </div>
                                    <div v-else class="text-center text-muted py-2 small">Không có máy.</div>
                                </div>
                            </div>
                        </div>

                        <div class="col-12 col-md-2">
                            <button class="btn btn-warning btn-sm w-100 fw-bold btn-add-fix" @click="themDongChiTiet">
                                <i class="fas fa-plus-circle me-1"></i> Thêm
                            </button>
                        </div>
                    </div>
                    
                    <div class="mt-2 d-flex flex-wrap gap-1" v-if="selectedSerials.length > 0">
                        <span v-for="s in selectedSerials" :key="s" class="badge bg-primary" style="font-size: 10px;">
                            {{ s }} <i class="fas fa-times ms-1 cursor-pointer" @click="removeSerial(s)"></i>
                        </span>
                        <span class="badge bg-danger cursor-pointer" @click="selectedSerials = []" v-if="selectedSerials.length > 5" style="font-size: 10px;">Xóa hết</span>
                    </div>
                </div>
            </div>

            <div class="table-responsive border rounded bg-white" v-if="listHienThi.length > 0">
                <table class="table table-sm table-bordered align-middle mb-0 small" style="font-size: 11px;">
                    <thead class="table-secondary text-center">
                        <tr>
                            <th>Sản Phẩm</th>
                            <th>Giá</th>
                            <th width="30px">SL</th>
                            <th>Serials</th>
                            <th>Tiền</th>
                            <th width="30px">#</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, index) in listHienThi" :key="index">
                            <td class="fw-bold text-primary text-break" style="min-width: 100px;">{{ getTenSP(item.maSP) }}</td>
                            <td class="text-end">{{ formatCurrency(item.donGia) }}</td>
                            <td class="fw-bold text-center">{{ item.soLuong }}</td>
                            <td>
                                <div class="d-flex flex-wrap gap-1" style="max-height: 60px; overflow-y: auto;">
                                    <span class="badge bg-secondary p-1" style="font-size: 9px;" v-for="s in item.danhSachSeri" :key="s">{{ s }}</span>
                                </div>
                            </td>
                            <td class="text-end fw-bold">{{ formatCurrency(item.donGia * item.soLuong) }}</td>
                            <td class="text-center p-0">
                                <button class="btn btn-link btn-sm text-danger" @click="listHienThi.splice(index, 1)"><i class="fas fa-times"></i></button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="mt-3 text-center">
                <button class="btn btn-primary px-5 btn-sm fw-bold shadow-sm" @click="luuPhieuXuat" :disabled="listHienThi.length === 0" style="font-size: 13px;">
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
const listDonVi = ref([]);
const listSanPham = ref([]);
const phieuXuat = ref({ maKho: null, maDonVi: null, ghiChu: '', chiTietPhieuXuat: [] });
const currentItem = ref({ maSP: '', donGia: 0 });
const listHienThi = ref([]);
const availableSerials = ref([]); 
const selectedSerials = ref([]);  
const searchText = ref("");       
const isAdmin = ref(false);
let lastCheckedIndex = -1;

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
const getDataSafe = (response) => {
    if (!response || !response.data) return [];
    if (response.data.content && Array.isArray(response.data.content)) return response.data.content;
    if (Array.isArray(response.data)) return response.data;
    return [];
};
const onChonSanPham = async () => {
    selectedSerials.value = []; availableSerials.value = []; searchText.value = ""; lastCheckedIndex = -1; 
    if (!phieuXuat.value.maKho) { alert("Vui lòng chọn Kho Xuất trước!"); currentItem.value.maSP = ""; return; }
    try {
        const res = await api.get('/kho/may-in/kha-dung', { params: { maSP: currentItem.value.maSP, maKho: phieuXuat.value.maKho } });
        availableSerials.value = res.data;
    } catch (e) { console.error(e); alert("Không tải được danh sách máy tồn kho!"); }
};
const resetSelection = () => { currentItem.value.maSP = ""; availableSerials.value = []; selectedSerials.value = []; listHienThi.value = []; lastCheckedIndex = -1; };
const removeSerial = (s) => { selectedSerials.value = selectedSerials.value.filter(item => item !== s); };
const listKhachHang = computed(() => {
    if (!Array.isArray(listDonVi.value)) return [];
    return listDonVi.value.filter(dv => {
        const loai = (dv.loaiDonVi && typeof dv.loaiDonVi === 'object') ? dv.loaiDonVi.loaiDonVi : dv.loaiDonVi;
        return loai === 2;
    });
});
const loadMasterData = async () => {
    try {
        const [k, d, s] = await Promise.all([api.get('/kho'), api.get('/don-vi'), api.get('/san-pham')]);
        listKho.value = getDataSafe(k); listDonVi.value = getDataSafe(d); listSanPham.value = getDataSafe(s);
        const role = localStorage.getItem('userRole');
        let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');
        if (!userMaKho) { const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}'); userMaKho = userInfo.maKho; }
        if (role === 'ADMIN') { isAdmin.value = true; } else { isAdmin.value = false; if (userMaKho) phieuXuat.value.maKho = parseInt(userMaKho); else if (listKho.value.length > 0) phieuXuat.value.maKho = listKho.value[0].maKho; }
    } catch (e) { console.error(e); }
};
const themDongChiTiet = () => {
    if (!currentItem.value.maSP) return alert("Chưa chọn sản phẩm");
    if (selectedSerials.value.length === 0) return alert("Chưa chọn máy nào để xuất!");
    listHienThi.value.push({ maSP: currentItem.value.maSP, donGia: currentItem.value.donGia, soLuong: selectedSerials.value.length, danhSachSeri: [...selectedSerials.value] });
    currentItem.value.maSP = ""; currentItem.value.donGia = 0; selectedSerials.value = []; availableSerials.value = []; searchText.value = ""; lastCheckedIndex = -1;
};
const luuPhieuXuat = async () => {
    if (!phieuXuat.value.maKho || !phieuXuat.value.maDonVi) return alert("Vui lòng chọn Kho và Khách hàng");
    try {
        await api.post('/kho/xuat', { maKho: phieuXuat.value.maKho, maDonVi: phieuXuat.value.maDonVi, ghiChu: phieuXuat.value.ghiChu, chiTietPhieuXuat: listHienThi.value });
        alert("Xuất kho thành công!"); router.push('/xuat-kho');
    } catch (error) { const msg = error.response?.data?.message || error.response?.data || error.message; alert("Lỗi: " + msg); }
};
const getTenSP = (maSP) => listSanPham.value.find(s => s.maSP === maSP)?.tenSP || maSP;
const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
onMounted(() => loadMasterData());
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
}
.dropdown-menu::-webkit-scrollbar { width: 6px; }
.dropdown-menu::-webkit-scrollbar-thumb { background-color: #ccc; border-radius: 4px; }
.cursor-pointer { cursor: pointer; }
.hover-bg:hover { background-color: #f8f9fa; }
</style>