<template>
    <div class="card shadow-sm">
        <div class="card-header bg-success text-white py-2 d-flex justify-content-between align-items-center">
            <h6 class="mb-0 fw-bold header-title"><i class="fas fa-download me-1"></i> NHẬP KHO MỚI</h6>
            <button class="btn btn-sm btn-light text-success fw-bold py-0 px-2 back-btn" @click="$router.push('/nhap-kho')">
                <i class="fas fa-arrow-left"></i> Quay lại
            </button>
        </div>
        <div class="card-body p-2">
            <div class="bg-light border rounded p-2 mb-3">
                <div class="row g-2">
                    <div class="col-12 col-md-4 custom-col">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">KHO NHẬP (*)</label>
                        <select class="form-select form-select-sm shadow-none" v-model="phieuNhap.maKho" :disabled="!isAdmin">
                            <option :value="null" disabled>-- Chọn Kho --</option>
                            <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                        </select>
                    </div>
                    <div class="col-12 col-md-4 custom-col">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">NHÀ CUNG CẤP (*)</label>
                        <select class="form-select form-select-sm shadow-none" v-model="phieuNhap.maDonVi">
                            <option value="" disabled>-- Chọn NCC --</option>
                            <option v-for="ncc in listNhaCungCap" :key="ncc.maDonVi" :value="ncc.maDonVi">{{ ncc.tenDonVi }}</option>
                        </select>
                    </div>
                    <div class="col-12 col-md-4 custom-col">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">GHI CHÚ</label>
                        <input type="text" class="form-control form-control-sm shadow-none" v-model="phieuNhap.ghiChu" placeholder="...">
                    </div>
                </div>
            </div>

            <div class="card border-success mb-3 shadow-sm">
                <div class="card-body p-2 bg-white">
                    <div class="row g-2 align-items-end">
                        <div class="col-12 col-md-5">
                            <label class="form-label fw-bold small-label">SẢN PHẨM</label>
                            <select class="form-select form-select-sm" v-model="currentItem.maSP">
                                <option value="" disabled>-- Chọn sản phẩm --</option>
                                <option v-for="sp in listSanPham" :key="sp.maSP" :value="sp.maSP">
                                    {{ sp.tenSP }} ({{ sp.maSP }})
                                </option>
                            </select>
                        </div>
                        
                        <div class="col-4 col-md-2">
                            <label class="form-label fw-bold small-label">GIÁ</label>
                            <input type="number" class="form-control form-control-sm" v-model="currentItem.donGia" min="0">
                        </div>
                        <div class="col-3 col-md-2">
                            <label class="form-label fw-bold small-label">SL</label>
                            <input type="number" class="form-control form-control-sm text-center" v-model="currentItem.soLuong" min="1">
                        </div>
                        
                        <div class="col-5 col-md-2">
                            <label class="form-label fw-bold small-label">LOẠI</label>
                            <select class="form-select form-select-sm" v-model="currentItem.trangThai">
                                <option v-for="tt in listTrangThai" :key="tt.maTrangThai" :value="tt.maTrangThai">
                                    {{ tt.tenTrangThai }}
                                </option>
                            </select>
                        </div>

                        <div class="col-12 col-md-1">
                            <button class="btn btn-success btn-sm w-100 fw-bold btn-add-fix" @click="themDong">
                                <i class="fas fa-plus">Thêm</i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="table-responsive border rounded bg-white" v-if="listHienThi.length > 0">
                <table class="table table-sm table-bordered align-middle mb-0 small" style="font-size: 13px;">
                    <thead class="table-light text-center">
                        <tr>
                            <th>Sản Phẩm</th>
                            <th width="100px">Trạng Thái</th>
                            <th>Giá</th>
                            <th width="50px">SL</th>
                            <th>Tiền</th>
                            <th width="40px">#</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, index) in listHienThi" :key="index">
                            <td class="text-break">{{ getTenSP(item.maSP) }}</td>
                            
                            <td class="text-center">{{ getTenTrangThai(item.trangThai) }}</td>
                            
                            <td class="text-end">{{ formatCurrency(item.donGia) }}</td>
                            <td class="text-center fw-bold">{{ item.soLuong }}</td>
                            <td class="text-end text-primary">{{ formatCurrency(item.donGia * item.soLuong) }}</td>
                            <td class="text-center p-0">
                                <button class="btn btn-link btn-sm text-danger" @click="listHienThi.splice(index, 1)">
                                    <i class="fas fa-trash-alt"></i>
                                </button>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <div class="mt-3 text-center">
                <button class="btn btn-primary px-5 fw-bold shadow-sm" @click="luuPhieu" :disabled="listHienThi.length === 0">
                    <i class="fas fa-save me-2"></i> LƯU PHIẾU
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/utils/axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const listKho = ref([]);
const listSanPham = ref([]);
const listDonVi = ref([]); 
const listTrangThai = ref([]); // [MỚI] List trạng thái từ API

const phieuNhap = ref({ maKho: null, maDonVi: '', ghiChu: '' });
const currentItem = ref({ maSP: '', donGia: 0, soLuong: 1, trangThai: 1 }); // Mặc định ID 1 (Mới)
const listHienThi = ref([]);
const isAdmin = ref(false);

const getDataSafe = (res) => (res?.data?.content && Array.isArray(res.data.content)) ? res.data.content : (Array.isArray(res?.data) ? res.data : []);

const listNhaCungCap = computed(() => {
    if (!Array.isArray(listDonVi.value)) return [];
    return listDonVi.value.filter(dv => {
        const loai = (dv.loaiDonVi && typeof dv.loaiDonVi === 'object') ? dv.loaiDonVi.loaiDonVi : dv.loaiDonVi;
        return loai === 1;
    });
});

const loadData = async () => {
    try {
        // [CẬP NHẬT] Gọi thêm API /trang-thai
        const [k, s, d, tt] = await Promise.all([
            api.get('/kho'), 
            api.get('/san-pham'), 
            api.get('/don-vi'),
            api.get('/trang-thai')
        ]);
        
        listKho.value = getDataSafe(k);
        listSanPham.value = getDataSafe(s);
        listDonVi.value = getDataSafe(d);
        listTrangThai.value = getDataSafe(tt); // Lưu list trạng thái

        const role = localStorage.getItem('userRole');
        let userMaKho = localStorage.getItem('maKho') || (JSON.parse(localStorage.getItem('userInfo') || '{}').maKho);
        if (role === 'ADMIN') { isAdmin.value = true; } 
        else { isAdmin.value = false; if (userMaKho) phieuNhap.value.maKho = parseInt(userMaKho); else if (listKho.value.length > 0) phieuNhap.value.maKho = listKho.value[0].maKho; }
    } catch (e) { console.error(e); }
};

const themDong = () => {
    if(!currentItem.value.maSP) return alert("Chọn sản phẩm!");
    if(currentItem.value.soLuong <= 0) return alert("SL > 0");
    listHienThi.value.push({...currentItem.value});
    currentItem.value.soLuong = 1;
};

const luuPhieu = async () => {
    if(!phieuNhap.value.maKho || !phieuNhap.value.maDonVi) return alert("Chọn Kho & NCC!");
    if(listHienThi.value.length === 0) return alert("Chưa có SP!");
    try {
        await api.post('/kho/nhap', { maKho: phieuNhap.value.maKho, maDonVi: phieuNhap.value.maDonVi, ghiChu: phieuNhap.value.ghiChu, chiTietPhieuNhap: listHienThi.value });
        alert("Thành công!"); router.push('/nhap-kho');
    } catch (e) { alert("Lỗi: " + (e.response?.data?.message || e.message)); }
};

const getTenSP = (ma) => listSanPham.value.find(s => s.maSP === ma)?.tenSP || ma;

// [MỚI] Hàm lấy tên trạng thái từ ID
const getTenTrangThai = (id) => {
    const tt = listTrangThai.value.find(t => t.maTrangThai === id);
    return tt ? tt.tenTrangThai : id;
};

const formatCurrency = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);
onMounted(() => loadData());
</script>

<style scoped>
/* CSS PC */
@media (min-width: 768px) {
    .btn-add-fix {
        height: 31px; display: flex; align-items: center; justify-content: center;
    }
}

/* CSS MOBILE */
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
</style>