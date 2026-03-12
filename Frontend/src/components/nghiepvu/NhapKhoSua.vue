<template>
    <div class="card border-warning shadow-sm">
        <div class="card-header bg-warning text-dark py-2 d-flex justify-content-between align-items-center">
            <h6 class="mb-0 fw-bold header-title"><i class="fas fa-edit me-1"></i> SỬA PHIẾU NHẬP: {{ soPhieu }}</h6>
            <button class="btn btn-sm btn-dark fw-bold py-0 px-2 back-btn" @click="$router.push('/nhap-kho')">
                <i class="fas fa-arrow-left"></i> Hủy / Quay lại
            </button>
        </div>
        <div class="card-body p-2" v-if="!loadingInit">
            <div class="bg-light border rounded p-2 mb-3">
                <div class="row g-2">
                    <div class="col-12 col-md-3 custom-col">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">KHO NHẬP</label>
                        <input type="text" class="form-control form-control-sm bg-white" :value="tenKho" disabled>
                    </div>
                    <div class="col-12 col-md-3 custom-col">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">NHÀ CUNG CẤP</label>
                        <input type="text" class="form-control form-control-sm bg-white" :value="tenNCC" disabled>
                    </div>
                    <div class="col-12 col-md-3 custom-col">
                        <label class="form-label small mb-0 fw-bold text-danger small-label">NGÀY NHẬP (Sửa)</label>
                        <input type="datetime-local" class="form-control form-control-sm border-danger" v-model="phieuNhap.ngayTaoPhieu">
                    </div>
                    <div class="col-12 col-md-3 custom-col">
                        <label class="form-label small mb-0 fw-bold text-danger small-label">GHI CHÚ (Sửa)</label>
                        <input type="text" class="form-control form-control-sm border-danger" v-model="phieuNhap.ghiChu" placeholder="...">
                    </div>
                </div>
            </div>

            <div class="card border-warning mb-3 shadow-sm">
                <div class="card-body p-2 bg-white">
                    <div class="row g-2 align-items-end">
                        <div class="col-12 col-md-5">
                            <label class="form-label fw-bold small-label">BỔ SUNG SẢN PHẨM</label>
                            <div class="dropdown">
                                <button class="form-select form-select-sm shadow-none text-start" 
                                        type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    <span class="text-truncate">
                                        {{ currentItem.maSP ? getTenSP(currentItem.maSP) : '-- Chọn sản phẩm (Gõ để tìm) --' }}
                                    </span>
                                </button>
                                <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 350px; overflow-y: auto;">
                                    <input type="text" class="form-control form-control-sm mb-2 shadow-none border-warning" 
                                           v-model="searchProductText" placeholder="🔍 Nhập mã hoặc tên sản phẩm..." @click.stop>
                                    <div v-if="filteredProducts.length > 0">
                                        <button type="button" class="dropdown-item small py-2 border-bottom text-wrap hover-bg" 
                                                v-for="sp in filteredProducts" :key="sp.maSP" 
                                                @click="selectProduct(sp.maSP)">
                                            <strong class="text-primary">{{ sp.maSP }}</strong> - {{ sp.tenSP }}
                                        </button>
                                    </div>
                                </div>
                            </div>
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
                                <option v-for="tt in listTrangThai" :key="tt.maTrangThai" :value="tt.maTrangThai">{{ tt.tenTrangThai }}</option>
                            </select>
                        </div>
                        <div class="col-12 col-md-1">
                            <button class="btn btn-warning btn-sm w-100 fw-bold btn-add-fix" @click="themDong">Thêm</button>
                        </div>
                    </div>
                </div>
            </div>

            <div class="table-responsive border rounded bg-white">
                <table class="table table-sm table-bordered align-middle mb-0 small" style="font-size: 13px;">
                    <thead class="table-warning text-center">
                        <tr>
                            <th>Sản Phẩm</th>
                            <th width="120px">Trạng Thái</th>
                            <th width="120px">Đơn Giá</th>
                            <th width="80px">SL</th>
                            <th width="150px">Thành Tiền</th>
                            <th width="100px">Thao Tác</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, index) in listHienThi" :key="index">
                            <td class="text-break fw-bold text-primary">{{ getTenSP(item.maSP) }}</td>
                            
                            <td class="text-center">
                                <select v-if="editIndex === index" class="form-select form-select-sm border-danger" v-model="editItemData.trangThai">
                                    <option v-for="tt in listTrangThai" :key="tt.maTrangThai" :value="tt.maTrangThai">{{ tt.tenTrangThai }}</option>
                                </select>
                                <span v-else>{{ getTenTrangThai(item.trangThai) }}</span>
                            </td>
                            
                            <td class="text-end">
                                <input v-if="editIndex === index" type="number" class="form-control form-control-sm text-end border-danger" v-model="editItemData.donGia" min="0">
                                <span v-else>{{ formatCurrency(item.donGia) }}</span>
                            </td>
                            
                            <td class="text-center">
                                <input v-if="editIndex === index" type="number" class="form-control form-control-sm text-center border-danger" v-model="editItemData.soLuong" min="1">
                                <span v-else class="fw-bold">{{ item.soLuong }}</span>
                            </td>
                            
                            <td class="text-end text-danger fw-bold">
                                {{ formatCurrency((editIndex === index ? editItemData.donGia : item.donGia) * (editIndex === index ? editItemData.soLuong : item.soLuong)) }}
                            </td>
                            
                            <td class="text-center p-1">
                                <div v-if="editIndex === index" class="btn-group btn-group-sm w-100">
                                    <button class="btn btn-success px-1" @click="luuSuaDong(index)"><i class="fas fa-check"></i></button>
                                    <button class="btn btn-secondary px-1" @click="editIndex = -1"><i class="fas fa-times"></i></button>
                                </div>
                                <div v-else class="btn-group btn-group-sm w-100">
                                    <button class="btn btn-outline-warning px-1" @click="batDauSuaDong(index)"><i class="fas fa-edit"></i> Sửa</button>
                                    <button class="btn btn-outline-danger px-1" @click="xoaDong(index)"><i class="fas fa-trash-alt"></i></button>
                                </div>
                            </td>
                        </tr>
                        <tr v-if="listHienThi.length === 0"><td colspan="6" class="text-center py-3 text-muted">Phiếu rỗng, vui lòng thêm sản phẩm!</td></tr>
                    </tbody>
                    <tfoot v-if="listHienThi.length > 0">
                        <tr class="table-light fw-bold">
                            <td colspan="3" class="text-end border-end-0">TỔNG CỘNG:</td>
                            <td class="text-center text-primary fs-6 border-start-0">{{ tongSoLuongPhieu }}</td>
                            <td class="text-end text-danger fs-6">{{ formatCurrency(tongTienPhieu) }}</td>
                            <td></td>
                        </tr>
                    </tfoot>
                </table>
            </div>

            <div class="mt-4 text-center">
                <button class="btn btn-warning px-5 py-2 fw-bold shadow-sm" @click="luuPhieuSua" :disabled="listHienThi.length === 0 || isSaving">
                    <span v-if="isSaving" class="spinner-border spinner-border-sm me-2 text-dark"></span>
                    <i v-else class="fas fa-save me-2 text-dark"></i> <span class="text-dark">LƯU THAY ĐỔI TOÀN BỘ PHIẾU</span>
                </button>
            </div>
        </div>
        <div v-else class="card-body text-center py-5"><div class="spinner-border text-warning"></div></div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/utils/axios';
import { useRouter, useRoute } from 'vue-router';

const router = useRouter();
const route = useRoute();
const soPhieu = route.params.id; // Truyền ID từ URL

const listSanPham = ref([]);
const listTrangThai = ref([]); 
const listDonVi = ref([]); 
const listKho = ref([]); 

const phieuNhap = ref({ ngayTaoPhieu: '', ghiChu: '' });
const tenKho = ref('');
const tenNCC = ref('');

const currentItem = ref({ maSP: '', donGia: 0, soLuong: 1, trangThai: 1 }); 
const listHienThi = ref([]);
const isSaving = ref(false);
const loadingInit = ref(true);

// --- TÌM KIẾM SẢN PHẨM ---
const searchProductText = ref("");
const filteredProducts = computed(() => {
    if (!searchProductText.value) return listSanPham.value;
    const kw = searchProductText.value.toLowerCase();
    return listSanPham.value.filter(sp => sp.tenSP.toLowerCase().includes(kw) || sp.maSP.toLowerCase().includes(kw));
});
const selectProduct = (maSP) => { currentItem.value.maSP = maSP; searchProductText.value = ""; };

// --- CHỈNH SỬA DÒNG ---
const editIndex = ref(-1);
const editItemData = ref({ donGia: 0, soLuong: 1, trangThai: 1 });

const batDauSuaDong = (index) => {
    editIndex.value = index;
    editItemData.value = { ...listHienThi.value[index] };
};
const luuSuaDong = (index) => {
    if(editItemData.value.soLuong <= 0) return alert("SL > 0");
    listHienThi.value[index] = { ...listHienThi.value[index], ...editItemData.value };
    editIndex.value = -1;
};
const xoaDong = (index) => { listHienThi.value.splice(index, 1); if(editIndex.value === index) editIndex.value = -1; };

// --- TÍNH TỔNG ---
const tongSoLuongPhieu = computed(() => listHienThi.value.reduce((s, i) => s + parseInt(i.soLuong||0), 0));
const tongTienPhieu = computed(() => listHienThi.value.reduce((s, i) => s + (parseFloat(i.donGia||0) * parseInt(i.soLuong||0)), 0));

// --- FORMAT ---
const formatForInput = (dateArray) => {
    if (!dateArray) return '';
    if (Array.isArray(dateArray)) {
        const [y, m, d, h, mn] = dateArray;
        const f = n => n < 10 ? '0'+n : n;
        return `${y}-${f(m)}-${f(d)}T${f(h||0)}:${f(mn||0)}`;
    }
    return new Date(dateArray).toISOString().slice(0, 16);
};
const getTenSP = (ma) => listSanPham.value.find(s => s.maSP === ma)?.tenSP || ma;
const getTenTrangThai = (id) => listTrangThai.value.find(t => t.maTrangThai === id)?.tenTrangThai || id;
const formatCurrency = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);

// --- LOAD DATA BAN ĐẦU ---
const loadData = async () => {
    try {
        const [k, s, d, tt, chiTietRes] = await Promise.all([
            api.get('/kho'), api.get('/san-pham/list'), api.get('/don-vi?size=1000'), api.get('/trang-thai'),
            api.get(`/kho/nhap/${soPhieu}`)
        ]);
        
        listKho.value = k.data; listSanPham.value = s.data; 
        listDonVi.value = d.data.content || d.data; listTrangThai.value = tt.data; 

        // Đổ dữ liệu phiếu cũ ra
        const p = chiTietRes.data;
        phieuNhap.value.ghiChu = p.ghiChu;
        phieuNhap.value.ngayTaoPhieu = formatForInput(p.ngayNhap);
        tenKho.value = p.khoNhap?.tenKho;
        tenNCC.value = p.nhaCungCap?.tenDonVi;

        // Gộp máy in thành dòng y hệt lúc tạo
        const map = new Map();
        p.danhSachChiTiet.forEach(ct => {
            const key = `${ct.sanPham.maSP}_${ct.mayIn.trangThai}_${ct.donGia}`;
            if (!map.has(key)) map.set(key, { maSP: ct.sanPham.maSP, trangThai: ct.mayIn.trangThai, donGia: ct.donGia, soLuong: 0 });
            map.get(key).soLuong += 1;
        });
        listHienThi.value = Array.from(map.values());
        
        loadingInit.value = false;
    } catch (e) { alert("Lỗi tải phiếu!"); router.push('/nhap-kho'); }
};

const themDong = () => {
    if(!currentItem.value.maSP) return alert("Chọn SP!");
    if(currentItem.value.soLuong <= 0) return alert("SL > 0");

    const dup = listHienThi.value.findIndex(i => i.maSP === currentItem.value.maSP && i.trangThai === currentItem.value.trangThai);
    if(dup !== -1) {
        listHienThi.value[dup].soLuong += currentItem.value.soLuong;
        listHienThi.value[dup].donGia = currentItem.value.donGia;
    } else {
        listHienThi.value.push({...currentItem.value});
    }
    currentItem.value.maSP = ''; currentItem.value.soLuong = 1;
};

const luuPhieuSua = async () => {
    if(editIndex.value !== -1) return alert("Lưu hoặc Hủy dòng đang sửa trước!");
    if(!confirm("Bạn chuẩn bị THAY THẾ TOÀN BỘ chi tiết phiếu này.\nTiếp tục?")) return;
    
    isSaving.value = true;
    try {
        await api.put(`/kho/nhap/toan-dien/${soPhieu}`, { 
            ghiChu: phieuNhap.value.ghiChu, 
            ngayTaoPhieu: phieuNhap.value.ngayTaoPhieu || null,
            chiTietPhieuNhap: listHienThi.value 
        });
        alert("Sửa phiếu thành công!"); 
        router.push('/nhap-kho');
    } catch (e) { 
        alert("Lỗi: " + (e.response?.data?.message || e.message)); 
    } finally { isSaving.value = false; }
};

onMounted(() => loadData());
</script>

<style scoped>
.dropdown-toggle::after { display: none !important; }
@media (min-width: 768px) { .btn-add-fix { height: 31px; display: flex; align-items: center; justify-content: center; } }
@media (max-width: 767px) {
    .header-title { font-size: 13px !important; }
    .back-btn { font-size: 11px !important; }
    .small-label { font-size: 10px !important; margin-bottom: 0 !important; }
    .form-select-sm, .form-control-sm { font-size: 12px !important; height: 30px !important; width: 100% !important; max-width: 100% !important; }
    .custom-col { width: 100%; margin-bottom: 5px; }
}
</style>