<template>
    <div class="card shadow-sm border-primary">
        <div class="card-header bg-primary text-white py-2 d-flex justify-content-between align-items-center">
            <h6 class="mb-0 fw-bold header-title"><i class="fas fa-download me-1"></i> NHẬP KHO MỚI</h6>
            <button class="btn btn-sm btn-light text-success fw-bold py-0 px-2 back-btn" @click="$router.push('/nhap-kho')">
                <i class="fas fa-arrow-left"></i> Quay lại
            </button>
        </div>
        <div class="card-body p-2">
            <div class="bg-light border rounded p-2 mb-3">
                <div class="row g-2">
                    <div class="col-12 col-md-2 custom-col">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">KHO NHẬP (*)</label>
                        <select class="form-select form-select-sm shadow-none" v-model="phieuNhap.maKho" :disabled="!isAdmin">
                            <option :value="null" disabled>-- Chọn Kho --</option>
                            <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                        </select>
                    </div>

                    <div class="col-12 col-md-2 custom-col">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">HÌNH THỨC (*)</label>
                        <select class="form-select form-select-sm shadow-none border-primary" v-model="phieuNhap.maHT">
                            <option :value="null" disabled>-- Chọn Lý do --</option>
                            <option v-for="ht in listHinhThuc" :key="ht.maHT" :value="ht.maHT">{{ ht.tenHT }}</option>
                        </select>
                    </div>

                    <div class="col-12 col-md-3 custom-col" v-if="!isNhapNoiBo">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">NHÀ CUNG CẤP (*)</label>
                        <div class="dropdown">
                            <button class="form-select form-select-sm shadow-none text-start" 
                                    type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                <span class="text-truncate">
                                    {{ phieuNhap.maDonVi ? getTenNCC(phieuNhap.maDonVi) : '-- Chọn NCC (Gõ để tìm) --' }}
                                </span>
                            </button>
                            <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 300px; overflow-y: auto;">
                                <input type="text" class="form-control form-control-sm mb-2 shadow-none border-success" 
                                       v-model="searchNccText" 
                                       placeholder="🔍 Nhập tên Nhà cung cấp..." 
                                       @click.stop>
                                <div v-if="filteredNcc.length > 0">
                                    <button type="button" class="dropdown-item small py-2 border-bottom text-wrap hover-bg" 
                                            v-for="ncc in filteredNcc" :key="ncc.maDonVi" 
                                            @click="selectNcc(ncc.maDonVi)">
                                        {{ ncc.tenDonVi }}
                                    </button>
                                </div>
                                <div v-else class="text-center text-muted py-2 small">Không tìm thấy.</div>
                            </div>
                        </div>
                    </div>

                    <div class="col-12 col-md-3 custom-col" v-else>
                        <label class="form-label small mb-0 fw-bold text-danger small-label">PHIẾU CHỜ NHẬP (*)</label>
                        <select class="form-select form-select-sm shadow-none border-danger" v-model="selectedPhieuXuatNoiBo" @change="onChonPhieuXuatNoiBo">
                            <option value="">-- Chọn Phiếu Xuất Nội Bộ --</option>
                            <option v-for="px in listPhieuXuatNoiBo" :key="px.soPhieu" :value="px.soPhieu">
                                {{ px.soPhieu }} (Từ: {{ px.tenKho }} - SL: {{ px.tongSoLuong }})
                            </option>
                        </select>
                    </div>

                    <div class="col-12 col-md-2 custom-col">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">NGÀY NHẬP</label>
                        <input type="datetime-local" class="form-control form-control-sm shadow-none" v-model="phieuNhap.ngayTaoPhieu">
                    </div>
                    <div class="col-12 col-md-3 custom-col">
                        <label class="form-label small mb-0 fw-bold text-muted small-label">GHI CHÚ</label>
                        <input type="text" class="form-control form-control-sm shadow-none" v-model="phieuNhap.ghiChu" placeholder="...">
                    </div>
                </div>
            </div>

            <div class="card border-success mb-3 shadow-sm" v-if="!isNhapNoiBo">
                <div class="card-body p-2 bg-white">
                    <div class="row g-2 align-items-end">
                        <div class="col-12 col-md-5">
                            <label class="form-label fw-bold small-label">SẢN PHẨM</label>
                            
                            <div class="dropdown">
                                <button class="form-select form-select-sm shadow-none text-start" 
                                        type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    <span class="text-truncate">
                                        {{ currentItem.maSP ? getTenSP(currentItem.maSP) : '-- Chọn sản phẩm (Gõ để tìm) --' }}
                                    </span>
                                </button>
                                <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 350px; overflow-y: auto;">
                                    <input type="text" class="form-control form-control-sm mb-2 shadow-none border-success" 
                                           v-model="searchProductText" 
                                           placeholder="🔍 Nhập mã hoặc tên sản phẩm..." 
                                           @click.stop>
                                    <div v-if="filteredProducts.length > 0">
                                        <button type="button" class="dropdown-item small py-2 border-bottom text-wrap hover-bg" 
                                                v-for="sp in filteredProducts" :key="sp.maSP" 
                                                @click="selectProduct(sp.maSP)">
                                            <strong class="text-primary">{{ sp.maSP }}</strong> - {{ sp.tenSP }}
                                        </button>
                                    </div>
                                    <div v-else class="text-center text-muted py-2 small">Không tìm thấy sản phẩm.</div>
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
                                <option v-for="tt in listTrangThai" :key="tt.maTrangThai" :value="tt.maTrangThai">
                                    {{ tt.tenTrangThai }}
                                </option>
                            </select>
                        </div>

                        <div class="col-12 col-md-1">
                            <button class="btn btn-success btn-sm w-100 fw-bold btn-add-fix" @click="themDong">
                                Thêm
                            </button>
                        </div>
                    </div>
                </div>
            </div>
            
            <div class="alert alert-warning mb-3 fw-bold shadow-sm" v-else>
                 <i class="fas fa-lock me-2"></i> Chế độ Nhập Nội Bộ: Không thể thêm tay. Sản phẩm và Số lượng sẽ được lấy trực tiếp từ Phiếu Xuất!
            </div>

            <div class="table-responsive border rounded bg-white" v-if="listHienThi.length > 0">
                <table class="table table-sm table-bordered align-middle mb-0 small" style="font-size: 13px;">
                    <thead class="table-light text-center">
                        <tr>
                            <th>Sản Phẩm</th>
                            <th width="150px">Trạng Thái</th>
                            <th width="120px">Giá</th>
                            <th width="60px">SL</th>
                            <th width="150px">Tiền</th>
                            <th width="50px" v-if="!isNhapNoiBo">#</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr v-for="(item, index) in listHienThi" :key="index">
                            <td class="text-break">{{ getTenSP(item.maSP) }}</td>
                            <td class="text-center">{{ getTenTrangThai(item.trangThai) }}</td>
                            <td class="text-end">{{ formatCurrency(item.donGia) }}</td>
                            <td class="text-center fw-bold">{{ item.soLuong }}</td>
                            <td class="text-end text-primary">{{ formatCurrency(item.donGia * item.soLuong) }}</td>
                            <td class="text-center p-0" v-if="!isNhapNoiBo">
                                <button class="btn btn-link btn-sm text-danger text-decoration-none fw-bold" @click="listHienThi.splice(index, 1)">
                                    Xóa
                                </button>
                            </td>
                        </tr>
                    </tbody>
                    <tfoot>
                        <tr class="table-light fw-bold">
                            <td colspan="3" class="text-end border-end-0">TỔNG CỘNG:</td>
                            <td class="text-center text-primary fs-6 border-start-0">{{ tongSoLuongPhieu }}</td>
                            <td class="text-end text-danger fs-6">{{ formatCurrency(tongTienPhieu) }}</td>
                            <td v-if="!isNhapNoiBo"></td>
                        </tr>
                    </tfoot>
                </table>
            </div>

            <div class="mt-3 text-center">
                <button class="btn btn-primary px-5 py-2 fw-bold shadow-sm" @click="luuPhieu" :disabled="listHienThi.length === 0 || isSaving">
                    <span v-if="isSaving" class="spinner-border spinner-border-sm me-2"></span>
                    LƯU PHIẾU
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
// [GIỮ NGUYÊN CÁC IMPORT CŨ]
import { ref, onMounted, computed, watch } from 'vue';
import api from '@/utils/axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const listKho = ref([]);
const listSanPham = ref([]);
const listDonVi = ref([]); 
const listTrangThai = ref([]); 
const listHinhThuc = ref([]);

const phieuNhap = ref({ maKho: null, maDonVi: '', maHT: null, ghiChu: '', ngayTaoPhieu: '' });
const currentItem = ref({ maSP: '', donGia: 0, soLuong: 1, trangThai: 1 }); 
const listHienThi = ref([]);
const isAdmin = ref(false);
const isSaving = ref(false);

// [CODE MỚI]: THEO DÕI HÌNH THỨC ĐỂ XỬ LÝ NỘI BỘ
const isNhapNoiBo = computed(() => {
    const ht = listHinhThuc.value.find(h => h.maHT === phieuNhap.value.maHT);
    return ht && ht.tenHT.toLowerCase().includes('nội bộ');
});

const listPhieuXuatNoiBo = ref([]);
const selectedPhieuXuatNoiBo = ref('');

const loadPhieuXuatNoiBoChoNhap = async (maKhoNhan) => {
    try {
        const res = await api.get('/kho/xuat/noi-bo/cho-nhap', { params: { maKhoNhan }});
        listPhieuXuatNoiBo.value = res.data;
    } catch(e) { console.error(e); }
};

watch(() => phieuNhap.value.maKho, (newMaKho) => {
    if (isNhapNoiBo.value && newMaKho) {
        loadPhieuXuatNoiBoChoNhap(newMaKho);
    }
});

watch(isNhapNoiBo, (newVal) => {
    if (newVal) {
        phieuNhap.value.maDonVi = null; 
        listHienThi.value = [];
        if (phieuNhap.value.maKho) loadPhieuXuatNoiBoChoNhap(phieuNhap.value.maKho);
    } else {
        selectedPhieuXuatNoiBo.value = '';
        listHienThi.value = [];
    }
});

const onChonPhieuXuatNoiBo = async () => {
    if (!selectedPhieuXuatNoiBo.value) return;
    try {
        const res = await api.get(`/kho/xuat/${selectedPhieuXuatNoiBo.value}`);
        const px = res.data;

        // Sinh ghi chú tự động (Cấm để trống)
        const d = phieuNhap.value.ngayTaoPhieu ? new Date(phieuNhap.value.ngayTaoPhieu) : new Date();
        const f = n => n < 10 ? '0'+n : n;
        phieuNhap.value.ghiChu = `Đã nhập ngày ${f(d.getDate())}/${f(d.getMonth()+1)}/${d.getFullYear()} - `;

        // Lấy chi tiết máy gom lại
        const map = new Map();
        px.danhSachChiTiet.forEach(ct => {
            const key = `${ct.sanPham.maSP}_${ct.donGia}`;
            if (!map.has(key)) {
                map.set(key, { maSP: ct.sanPham.maSP, donGia: ct.donGia, soLuong: 0, trangThai: 1 });
            }
            map.get(key).soLuong++;
        });
        listHienThi.value = Array.from(map.values());

    } catch(e) { console.error(e); }
};

// [PHẦN CÒN LẠI GIỮ NGUYÊN 100%]
const searchNccText = ref("");

const listNhaCungCap = computed(() => {
    if (!Array.isArray(listDonVi.value)) return [];
    return listDonVi.value.filter(dv => {
        const loai = (dv.loaiDonVi && typeof dv.loaiDonVi === 'object') ? dv.loaiDonVi.loaiDonVi : dv.loaiDonVi;
        return loai === 1; // Chỉ lấy loại 1 là Nhà Cung Cấp
    });
});

const filteredNcc = computed(() => {
    if (!searchNccText.value) return listNhaCungCap.value;
    const keyword = searchNccText.value.toLowerCase();
    return listNhaCungCap.value.filter(ncc => 
        ncc.tenDonVi.toLowerCase().includes(keyword) || 
        ncc.maDonVi.toLowerCase().includes(keyword)
    );
});

const selectNcc = (ma) => {
    phieuNhap.value.maDonVi = ma;
    searchNccText.value = ""; 
};

const getTenNCC = (ma) => listNhaCungCap.value.find(n => n.maDonVi === ma)?.tenDonVi || ma;

const searchProductText = ref("");

const filteredProducts = computed(() => {
    if (!searchProductText.value) return listSanPham.value;
    const keyword = searchProductText.value.toLowerCase();
    return listSanPham.value.filter(sp => 
        sp.tenSP.toLowerCase().includes(keyword) || 
        sp.maSP.toLowerCase().includes(keyword)
    );
});

const selectProduct = (maSP) => {
    currentItem.value.maSP = maSP;
    searchProductText.value = ""; 
};

const tongSoLuongPhieu = computed(() => listHienThi.value.reduce((sum, item) => sum + parseInt(item.soLuong || 0), 0));
const tongTienPhieu = computed(() => listHienThi.value.reduce((sum, item) => sum + (parseFloat(item.donGia || 0) * parseInt(item.soLuong || 0)), 0));

const getDataSafe = (res) => (res?.data?.content && Array.isArray(res.data.content)) ? res.data.content : (Array.isArray(res?.data) ? res.data : []);

const loadData = async () => {
    try {
        const [k, s, d, tt, ht] = await Promise.all([
            api.get('/kho'), 
            api.get('/san-pham/list'),
            api.get('/don-vi?size=1000'), 
            api.get('/trang-thai'),
            api.get('/hinh-thuc-nhap') 
        ]);
        
        listKho.value = getDataSafe(k);
        listSanPham.value = getDataSafe(s); 
        listDonVi.value = getDataSafe(d);
        listTrangThai.value = getDataSafe(tt); 
        listHinhThuc.value = getDataSafe(ht); 

        const role = localStorage.getItem('userRole');
        let userMaKho = localStorage.getItem('maKho') || (JSON.parse(localStorage.getItem('userInfo') || '{}').maKho);
        if (role === 'ADMIN') { isAdmin.value = true; } 
        else { isAdmin.value = false; if (userMaKho) phieuNhap.value.maKho = parseInt(userMaKho); else if (listKho.value.length > 0) phieuNhap.value.maKho = listKho.value[0].maKho; }
    } catch (e) { console.error(e); }
};

const themDong = () => {
    if(!currentItem.value.maSP) return alert("Chọn sản phẩm!");
    if(currentItem.value.soLuong <= 0) return alert("SL > 0");

    if(currentItem.value.donGia < 0) {
        alert("Đơn giá phải lớn hơn hoặc bằng 0");
        return; 
    }

    listHienThi.value.push({...currentItem.value});
    currentItem.value.soLuong = 1;
};

// CẬP NHẬT KIỂM TRA LƯU PHIẾU
const luuPhieu = async () => {
    if(!phieuNhap.value.maKho) return alert("Chọn Kho!");
    if(!isNhapNoiBo.value && !phieuNhap.value.maDonVi) return alert("Chọn NCC!");
    if(!phieuNhap.value.maHT) return alert("Chọn Hình Thức Nhập!");
    if(isNhapNoiBo.value && !selectedPhieuXuatNoiBo.value) return alert("Bạn chưa chọn phiếu xuất nội bộ nào!");
    if(isNhapNoiBo.value && (!phieuNhap.value.ghiChu || phieuNhap.value.ghiChu.trim() === '')) return alert("Không được để trống ghi chú khi nhập nội bộ!");

    if(listHienThi.value.length === 0) return alert("Chưa có SP!");
    
    isSaving.value = true;
    try {
        await api.post('/kho/nhap', { 
            maKho: phieuNhap.value.maKho, 
            maDonVi: phieuNhap.value.maDonVi, 
            maHT: phieuNhap.value.maHT, 
            ghiChu: phieuNhap.value.ghiChu, 
            ngayTaoPhieu: phieuNhap.value.ngayTaoPhieu || null,
            soPhieuXuatNoiBo: isNhapNoiBo.value ? selectedPhieuXuatNoiBo.value : null, // GỬI LÊN SERVER ĐỂ ĐÓNG PHIẾU
            chiTietPhieuNhap: listHienThi.value 
        });
        alert("Thành công!"); router.push('/nhap-kho');
    } catch (e) { 
        alert("Lỗi: " + (e.response?.data?.message || e.message)); 
    } finally {
        isSaving.value = false;
    }
};

const getTenSP = (ma) => listSanPham.value.find(s => s.maSP === ma)?.tenSP || ma;

const getTenTrangThai = (id) => {
    const tt = listTrangThai.value.find(t => t.maTrangThai === id);
    return tt ? tt.tenTrangThai : id;
};

const formatCurrency = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);

onMounted(() => loadData());
</script>

<style scoped>
.dropdown-menu::-webkit-scrollbar { width: 6px; }
.dropdown-menu::-webkit-scrollbar-thumb { background-color: #ccc; border-radius: 4px; }
.hover-bg:hover { background-color: #f8f9fa; }

/* Ẩn cái dấu mũi tên mặc định của dropdown bootstrap để nhìn giống thẻ select thật */
.dropdown-toggle::after {
    display: none !important;
}

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