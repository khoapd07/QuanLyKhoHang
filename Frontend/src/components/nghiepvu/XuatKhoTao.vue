<template>
    <div class="card border-warning">
        <div class="card-header bg-warning text-dark d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Tạo Phiếu Xuất Kho</h5>
            <button class="btn btn-sm btn-dark" @click="$router.push('/xuat-kho')">
                <i class="fas fa-arrow-left"></i> Quay lại
            </button>
        </div>
        <div class="card-body">
            <div class="row mb-4">
                <div class="col-md-2">
                    <label class="form-label fw-bold">Kho Xuất (*)</label>
                    <select class="form-select" v-model="phieuXuat.maKho" @change="resetSelection" :disabled="!isAdmin">
                        <option :value="null" disabled>-- Chọn Kho --</option>
                        <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                    </select>
                </div>

                <div class="col-md-3">
                    <label class="form-label fw-bold">Khách Hàng (*)</label>
                    <div class="dropdown">
                        <button class="form-select text-start bg-white" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                            <span class="text-truncate">
                                {{ phieuXuat.maDonVi ? getTenKhach(phieuXuat.maDonVi) : '-- Chọn KH (Gõ để tìm) --' }}
                            </span>
                        </button>
                        <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 300px; overflow-y: auto;">
                            <input type="text" class="form-control mb-2 border-warning" v-model="searchKhachText" placeholder="🔍 Nhập tên KH..." @click.stop>
                            <div v-if="filteredKhachHang.length > 0">
                                <button type="button" class="dropdown-item py-2 border-bottom text-wrap hover-bg" 
                                        v-for="kh in filteredKhachHang" :key="kh.maDonVi" @click="selectKhachHang(kh.maDonVi)">
                                    {{ kh.tenDonVi }}
                                </button>
                            </div>
                            <div v-else class="text-center text-muted py-2">Không tìm thấy KH.</div>
                        </div>
                    </div>
                </div>

                <div class="col-md-2">
                    <label class="form-label fw-bold">Hình Thức (*)</label>
                    <select class="form-select border-warning" v-model="phieuXuat.maHT">
                        <option :value="null" disabled>-- Chọn Lý do --</option>
                        <option v-for="ht in listHinhThuc" :key="ht.maHT" :value="ht.maHT">{{ ht.tenHT }}</option>
                    </select>
                </div>

                <div class="col-md-2">
                    <label class="form-label fw-bold">Ngày Xuất (Tùy chỉnh)</label>
                    <input type="datetime-local" class="form-control" v-model="phieuXuat.ngayTaoPhieu">
                </div>

                <div class="col-md-3">
                    <label class="form-label fw-bold">Ghi Chú</label>
                    <input type="text" class="form-control" v-model="phieuXuat.ghiChu" placeholder="Lý do xuất...">
                </div>
            </div>

            <div class="card bg-light border-warning mb-3">
                <div class="card-body">
                    <div class="col-12 mb-3">
                        <div class="alert alert-info py-2 small shadow-sm">
                            <i class="fas fa-info-circle"></i> Chọn kho và sản phẩm để xem danh sách số Serial <b>đang tồn kho</b>.
                        </div>
                    </div>

                    <div class="row g-3 align-items-end">
                        <div class="col-md-4">
                            <label class="form-label fw-bold">1. Sản Phẩm</label>
                            <div class="dropdown">
                                <button class="form-select text-start bg-white" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                    <span class="text-truncate">
                                        {{ currentItem.maSP ? getTenSP(currentItem.maSP) : '-- Chọn SP (Gõ để tìm) --' }}
                                    </span>
                                </button>
                                <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 350px; overflow-y: auto;">
                                    <input type="text" class="form-control mb-2 border-warning" v-model="searchProductText" placeholder="🔍 Nhập mã hoặc tên SP..." @click.stop>
                                    <div v-if="filteredProducts.length > 0">
                                        <button type="button" class="dropdown-item py-2 border-bottom text-wrap hover-bg" 
                                                v-for="sp in filteredProducts" :key="sp.maSP" @click="selectProduct(sp.maSP)">
                                            <strong class="text-primary">{{ sp.maSP }}</strong> - {{ sp.tenSP }}
                                        </button>
                                    </div>
                                    <div v-else class="text-center text-muted py-2">Không tìm thấy.</div>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-2">
                            <label class="form-label fw-bold">Giá Bán</label>
                            <input type="number" class="form-control" v-model="currentItem.donGia">
                        </div>

                        <div class="col-md-4">
                            <label class="form-label fw-bold d-flex justify-content-between">
                                <span>2. Danh Sách Mã Máy <span class="badge bg-secondary">Tồn: {{ selectableSerials.length }}</span></span>
                                <small class="text-primary" v-if="selectedSerials.length > 0">Đang chọn: {{ selectedSerials.length }}</small>
                            </label>
                            
                            <div class="dropdown">
                                <button class="btn btn-outline-secondary w-100 text-start d-flex justify-content-between align-items-center bg-white" 
                                        type="button" data-bs-toggle="dropdown" 
                                        :disabled="!currentItem.maSP || !phieuXuat.maKho || selectableSerials.length === 0">
                                    <span class="text-truncate">
                                        {{ selectedSerials.length > 0 ? `Đã chọn ${selectedSerials.length} máy` : (currentItem.maSP ? '-- Chọn các máy cần xuất --' : '-- Vui lòng chọn SP trước --') }}
                                    </span>
                                    <i class="fas fa-chevron-down"></i>
                                </button>
                                
                                <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 400px; overflow-y: auto;">
                                    <input type="text" class="form-control mb-2" v-model="searchText" placeholder="🔍 Tìm serial (hoặc gõ Shift)...">
                                    
                                    <div class="d-flex align-items-center px-2 py-2 mb-2 bg-light rounded border" v-if="filteredSerials.length > 0">
                                        <input class="form-check-input m-0 me-2" type="checkbox" id="selectAllXuat" 
                                               :checked="isAllSelected" 
                                               @change="toggleSelectAll" style="cursor: pointer;">
                                        <label class="form-check-label fw-bold cursor-pointer w-100" for="selectAllXuat">
                                            Chọn tất cả ({{ filteredSerials.length }})
                                        </label>
                                    </div>
                                    
                                    <div v-if="filteredSerials.length > 0">
                                        <div class="d-flex align-items-center py-1 px-2 hover-bg rounded" v-for="(seri, index) in filteredSerials" :key="seri">
                                            <input class="form-check-input m-0 me-2" type="checkbox" 
                                                   :value="seri" :id="seri" 
                                                   v-model="selectedSerials"
                                                   @click="handleShiftClick($event, index)" style="cursor: pointer;">
                                            
                                            <label class="form-check-label w-100 cursor-pointer text-break" :for="seri">
                                                {{ seri }}
                                            </label>
                                        </div>
                                    </div>
                                    <div v-else class="text-center text-muted py-2 small">
                                        Không tìm thấy máy phù hợp.
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="col-md-2">
                            <button class="btn btn-warning w-100 fw-bold" @click="themDongChiTiet">
                                <i class="fas fa-plus-circle"></i> Thêm
                            </button>
                        </div>
                    </div>

                    <div class="mt-2 d-flex flex-wrap gap-1" v-if="selectedSerials.length > 0">
                        <span v-for="s in selectedSerials" :key="s" class="badge bg-primary">
                            {{ s }} <i class="fas fa-times ms-1 cursor-pointer" @click="removeSerial(s)"></i>
                        </span>
                        <span class="badge bg-danger cursor-pointer" @click="selectedSerials = []" v-if="selectedSerials.length > 5">
                            Xóa hết
                        </span>
                    </div>
                </div>
            </div>

            <table class="table table-bordered mt-3" v-if="listHienThi.length > 0">
                <thead class="table-secondary text-center">
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
                        <td class="fw-bold text-primary">{{ getTenSP(item.maSP) }}</td>
                        <td>{{ formatCurrency(item.donGia) }}</td>
                        <td class="fw-bold text-center">{{ item.soLuong }}</td>
                        <td>
                            <div class="d-flex flex-wrap gap-1" style="max-height: 80px; overflow-y: auto;">
                                <span class="badge bg-secondary" v-for="s in item.danhSachSeri" :key="s">{{ s }}</span>
                            </div>
                        </td>
                        <td class="text-end fw-bold">{{ formatCurrency(item.donGia * item.soLuong) }}</td>
                        <td class="text-center">
                            <button class="btn btn-sm btn-outline-danger" @click="listHienThi.splice(index, 1)">
                                <i class="fas fa-trash-alt"></i> Xóa
                            </button>
                        </td>
                    </tr>
                </tbody>
            </table>

            <div class="mt-4 text-center">
                <button class="btn btn-primary px-5 py-2 fw-bold" @click="luuPhieuXuat" :disabled="listHienThi.length === 0">
                    <i class="fas fa-save me-2"></i> Hoàn thành Xuất Kho
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
const listHinhThuc = ref([]);

const phieuXuat = ref({ maKho: null, maDonVi: null, maHT: null, ghiChu: '', ngayTaoPhieu: '', chiTietPhieuXuat: [] });
const currentItem = ref({ maSP: '', donGia: 0 });
const listHienThi = ref([]);

const availableSerials = ref([]); 
const selectedSerials = ref([]);  
const searchText = ref("");       
const isAdmin = ref(false);
let lastCheckedIndex = -1;

const searchKhachText = ref("");

const listKhachHang = computed(() => {
    if (!Array.isArray(listDonVi.value)) return [];
    return listDonVi.value.filter(dv => {
        const loai = (dv.loaiDonVi && typeof dv.loaiDonVi === 'object') ? dv.loaiDonVi.loaiDonVi : dv.loaiDonVi;
        return loai === 2;
    });
});

const filteredKhachHang = computed(() => {
    if(!searchKhachText.value) return listKhachHang.value;
    const kw = searchKhachText.value.toLowerCase();
    return listKhachHang.value.filter(kh => kh.tenDonVi.toLowerCase().includes(kw) || kh.maDonVi.toLowerCase().includes(kw));
});

const selectKhachHang = (ma) => { phieuXuat.value.maDonVi = ma; searchKhachText.value = ""; };
const getTenKhach = (ma) => listKhachHang.value.find(kh => kh.maDonVi === ma)?.tenDonVi || ma;

const searchProductText = ref("");

const filteredProducts = computed(() => {
    if (!searchProductText.value) return listSanPham.value;
    const kw = searchProductText.value.toLowerCase();
    return listSanPham.value.filter(sp => sp.tenSP.toLowerCase().includes(kw) || sp.maSP.toLowerCase().includes(kw));
});

const selectProduct = (maSP) => {
    currentItem.value.maSP = maSP;
    searchProductText.value = ""; 
    onChonSanPham();
};

const getTenSP = (maSP) => listSanPham.value.find(s => s.maSP === maSP)?.tenSP || maSP;


const usedSerials = computed(() => {
    return listHienThi.value.flatMap(item => item.danhSachSeri);
});

const selectableSerials = computed(() => {
    return availableSerials.value.filter(s => !usedSerials.value.includes(s));
});

const filteredSerials = computed(() => {
    if (!searchText.value) return selectableSerials.value;
    return selectableSerials.value.filter(s => s.toLowerCase().includes(searchText.value.toLowerCase()));
});

const isAllSelected = computed(() => {
    if (filteredSerials.value.length === 0) return false;
    return filteredSerials.value.every(s => selectedSerials.value.includes(s));
});

const toggleSelectAll = (e) => {
    const isChecked = e.target.checked;
    if (isChecked) {
        filteredSerials.value.forEach(s => {
            if (!selectedSerials.value.includes(s)) {
                selectedSerials.value.push(s);
            }
        });
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
            if (isChecking) {
                if (!selectedSerials.value.includes(serial)) selectedSerials.value.push(serial);
            } else {
                selectedSerials.value = selectedSerials.value.filter(s => s !== serial);
            }
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
    selectedSerials.value = [];
    availableSerials.value = [];
    searchText.value = "";
    lastCheckedIndex = -1;
    
    if (!phieuXuat.value.maKho) {
        alert("Vui lòng chọn Kho Xuất trước!");
        currentItem.value.maSP = ""; 
        return;
    }

    try {
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

const resetSelection = () => {
    currentItem.value.maSP = "";
    availableSerials.value = [];
    selectedSerials.value = [];
    listHienThi.value = []; 
    lastCheckedIndex = -1;
};

const removeSerial = (s) => {
    selectedSerials.value = selectedSerials.value.filter(item => item !== s);
};

const loadMasterData = async () => {
    try {
        const [k, d, s, ht] = await Promise.all([
             api.get('/kho'),      
             api.get('/don-vi?size=1000'), 
             api.get('/san-pham/list'), 
             api.get('/hinh-thuc-xuat')
        ]);
        
        listKho.value = getDataSafe(k);
        listDonVi.value = getDataSafe(d);
        listSanPham.value = getDataSafe(s);
        listHinhThuc.value = getDataSafe(ht);

        const role = localStorage.getItem('userRole');
        let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');
        
        if (!userMaKho) {
             const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
             userMaKho = userInfo.maKho;
        }

        if (role === 'ADMIN') {
             isAdmin.value = true;
        } else {
             isAdmin.value = false;
             if (userMaKho) {
                 phieuXuat.value.maKho = parseInt(userMaKho);
             } else {
                 if (listKho.value.length > 0) phieuXuat.value.maKho = listKho.value[0].maKho;
             }
        }

    } catch (e) { console.error(e); }
};

const themDongChiTiet = () => {
    if (!currentItem.value.maSP) return alert("Chưa chọn sản phẩm");
    if (selectedSerials.value.length === 0) return alert("Chưa chọn máy nào để xuất!");

    if (currentItem.value.donGia < 0) {
        alert("Đơn giá phải lớn hơn hoặc bằng 0");
        return;
    }

    listHienThi.value.push({
        maSP: currentItem.value.maSP,
        donGia: currentItem.value.donGia,
        soLuong: selectedSerials.value.length, 
        danhSachSeri: [...selectedSerials.value] 
    });

    selectedSerials.value = [];
    searchText.value = "";
    lastCheckedIndex = -1;
};

const luuPhieuXuat = async () => {
    if (!phieuXuat.value.maKho || !phieuXuat.value.maDonVi) return alert("Vui lòng chọn Kho và Khách hàng");
    if (!phieuXuat.value.maHT) return alert("Vui lòng chọn Hình thức xuất!");
    
    const payload = {
        maKho: phieuXuat.value.maKho,
        maDonVi: phieuXuat.value.maDonVi,
        maHT: phieuXuat.value.maHT,
        ghiChu: phieuXuat.value.ghiChu,
        ngayTaoPhieu: phieuXuat.value.ngayTaoPhieu || null,
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

const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

onMounted(() => loadMasterData());
</script>

<style scoped>
.dropdown-menu::-webkit-scrollbar { width: 6px; }
.dropdown-menu::-webkit-scrollbar-thumb { background-color: #ccc; border-radius: 4px; }
.cursor-pointer { cursor: pointer; }
.hover-bg:hover { background-color: #f8f9fa; }
.dropdown-toggle::after { display: none !important; }
</style>