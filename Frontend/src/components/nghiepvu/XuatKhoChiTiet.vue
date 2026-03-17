<template>
    <div class="modal-backdrop show"></div>
    <div class="modal d-block" tabindex="-1">
        <div class="modal-dialog custom-modal-size modal-dialog-scrollable">
            <div class="modal-content">
                <div class="modal-header bg-warning text-dark p-3">
                    <h4 class="modal-title fw-bold m-0">CHI TIẾT VÀ CHỈNH SỬA PHIẾU XUẤT: {{ chiTiet?.soPhieu }}</h4>
                    <button type="button" class="btn-close" @click="$emit('close')" :disabled="isDeleting"></button>
                </div>
                
                <div class="modal-body p-4" v-if="chiTiet">
                    <div class="row mb-4 p-3 bg-light border rounded shadow-sm align-items-center" style="font-size: 1.1rem;">
                        <div class="col-md-2">
                            <label class="fw-bold text-muted mb-1 fs-6">Khách Hàng</label>
                            <div class="fw-bold text-primary fs-5 text-truncate" :title="chiTiet.khachHang?.tenDonVi">{{ chiTiet.khachHang?.tenDonVi }}</div>
                        </div>
                        
                        <div class="col-md-2">
                            <label class="fw-bold text-muted mb-1 fs-6">Hình Thức</label>
                            <select class="form-select border-warning fw-bold" v-model="editThongTin.maHT" :disabled="isNoiBoGoc">
                                <option v-for="ht in listHinhThuc" :key="ht.maHT" :value="ht.maHT" 
                                        :disabled="!isNoiBoGoc && ht.tenHT.toLowerCase().includes('nội bộ')">
                                    {{ ht.tenHT }}
                                </option>
                            </select>
                        </div>
                        
                        <div class="col-md-3">
                            <label class="fw-bold text-muted mb-1 fs-6">Ngày Xuất</label>
                            <input type="datetime-local" class="form-control border-warning fw-bold" v-model="editThongTin.ngayTaoPhieu">
                        </div>
                        <div class="col-md-3">
                            <label class="fw-bold text-muted mb-1 fs-6">Ghi Chú</label>
                            <input type="text" class="form-control border-warning fw-bold" v-model="editThongTin.ghiChu" 
                                   :disabled="isNoiBoGoc" :placeholder="isNoiBoGoc ? 'Bị khóa đối với xuất nội bộ' : ''">
                        </div>
                        <div class="col-md-2 text-end">
                            <label class="fw-bold text-muted mb-1 fs-6">&nbsp;</label>
                            <button class="btn btn-warning fw-bold w-100" @click="luuThongTinChung" :disabled="isSavingInfo">
                                <span v-if="isSavingInfo">Đang lưu...</span>
                                <span v-else>Lưu Thông Tin</span>
                            </button>
                        </div>
                    </div>

                    <div class="d-flex justify-content-between align-items-center mt-4 mb-3">
                        <h4 class="text-warning text-dark m-0 fw-bold">Danh sách máy đã xuất</h4>
                        <div class="d-flex align-items-center gap-4">
                            <div class="fs-5">Tổng số lượng: <span class="badge bg-warning text-dark fs-5">{{ chiTiet.tongSoLuong }}</span></div>
                            <div class="fs-5">Tổng thành tiền: <span class="text-danger fw-bold fs-5">{{ formatCurrency(chiTiet.tongTien) }}</span></div>
                            <button class="btn btn-danger fw-bold px-4 py-2 shadow-sm fs-6 ms-3" 
                                    :disabled="selectedItems.length === 0 || isDeleting" 
                                    @click="xoaNhieuDong">
                                <span v-if="isDeleting">Đang xử lý...</span>
                                <span v-else>Thu Hồi Các Máy Đã Chọn ({{ selectedItems.length }})</span>
                            </button>
                        </div>
                    </div>

                    <div class="bg-light p-2 mb-2 border rounded d-flex align-items-center gap-3">
                        <span class="fw-bold text-muted">Chọn nhanh máy theo Model để thu hồi:</span>
                        <div class="dropdown flex-grow-1" style="max-width: 400px;">
                            <button class="btn btn-outline-secondary btn-sm w-100 text-start d-flex justify-content-between align-items-center bg-white shadow-none" 
                                    type="button" data-bs-toggle="dropdown" aria-expanded="false" data-bs-auto-close="outside">
                                <span>-- Tick chọn Model cần thu hồi nhanh --</span>
                                <i class="fas fa-filter"></i>
                            </button>
                            <ul class="dropdown-menu w-100 p-2 shadow" style="max-height: 250px; overflow-y: auto;" @click.stop>
                                <li v-for="sp in danhSachModelTrongPhieu" :key="sp.maSP" class="dropdown-item py-2 border-bottom d-flex align-items-center hover-bg">
                                    <input class="form-check-input cursor-pointer me-2 m-0" type="checkbox" 
                                           style="width: 18px; height: 18px;"
                                           :id="'chk_model_' + sp.maSP"
                                           :checked="isModelSelected(sp.maSP)"
                                           @change="toggleModelSelection(sp.maSP, $event.target.checked)">
                                    <label class="d-flex justify-content-between align-items-center w-100 cursor-pointer mb-0" :for="'chk_model_' + sp.maSP">
                                        <span class="fw-bold text-primary">{{ sp.tenSP }}</span>
                                        <span class="badge bg-secondary rounded-pill">{{ sp.soLuong }} máy</span>
                                    </label>
                                </li>
                            </ul>
                        </div>
                    </div>
                    
                    <div class="table-responsive border rounded mb-5">
                        <table class="table table-bordered table-hover text-center align-middle mb-0" style="font-size: 1.1rem;">
                            <thead class="table-secondary">
                                <tr>
                                    <th width="60px">
                                        <input class="form-check-input cursor-pointer" type="checkbox" 
                                               style="width: 22px; height: 22px;"
                                               :checked="isAllSelected" 
                                               @change="toggleSelectAll" 
                                               title="Chọn tất cả">
                                    </th>
                                    <th width="70px">#</th>
                                    <th width="150px">Mã SP</th>
                                    <th>Sản Phẩm (Model)</th>
                                    <th width="250px">Mã Máy (Hệ thống) / Serial</th>
                                    <th width="220px">Giá Bán</th>
                                    <th width="220px">Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(item, index) in chiTiet.danhSachChiTiet" :key="item.maCTPX" 
                                    :class="{'table-warning': selectedItems.includes(item.maCTPX)}">
                                    
                                    <td>
                                        <input class="form-check-input cursor-pointer" type="checkbox" 
                                               style="width: 22px; height: 22px;"
                                               :value="item.maCTPX" 
                                               v-model="selectedItems"
                                               @click="handleShiftClick($event, index, item)">
                                    </td>

                                    <td class="fw-bold">{{ index + 1 }}</td>
                                    
                                    <td class="fw-bold text-secondary">{{ item.sanPham?.maSP }}</td>
                                    <td class="text-start fw-bold text-primary fs-5">{{ item.sanPham?.tenSP }}</td>
                                    <td class="font-monospace fw-bold">{{ item.mayIn?.soSeri || item.mayIn?.maMay }}</td>
                                    
                                    <td class="text-end">
                                        <div v-if="editingMaSP === item.sanPham?.maSP" class="input-group">
                                            <input type="number" class="form-control text-end border-warning fw-bold fs-5" v-model="editPrice" min="0">
                                        </div>
                                        <span v-else class="fw-bold text-danger fs-5">{{ formatCurrency(item.donGia) }}</span>
                                    </td>

                                    <td>
                                        <div v-if="editingMaSP === item.sanPham?.maSP" class="d-flex justify-content-center gap-2">
                                            <button class="btn btn-success fw-bold px-3 fs-6" @click="luuGiaTheoSP(item.sanPham?.maSP)">Lưu Giá</button>
                                            <button class="btn btn-secondary fw-bold px-3 fs-6" @click="editingMaSP = null">Hủy</button>
                                        </div>
                                        
                                        <div v-else class="d-flex justify-content-center gap-2">
                                            <button class="btn btn-outline-warning fw-bold px-3 fs-6" @click="batDauSuaGia(item)" :disabled="isDeleting">
                                                Sửa Giá
                                            </button>
                                            <button class="btn btn-outline-danger fw-bold px-4 fs-6" @click="xoaDong(item.maCTPX)" :disabled="isDeleting" title="Thu hồi máy này về kho">
                                                Thu Hồi
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                                <tr v-if="chiTiet.danhSachChiTiet.length === 0"><td colspan="7" class="py-3 text-muted">Phiếu rỗng.</td></tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="card border-warning shadow-sm">
                        <div class="card-header bg-warning text-dark py-3 fs-5 fw-bold">
                            Bổ sung xuất thêm máy
                        </div>
                        <div class="card-body bg-light p-4">
                            <div class="row g-3 align-items-end">
                                <div class="col-12 col-md-4">
                                    <label class="form-label fw-bold">1. Chọn Sản Phẩm</label>
                                    <div class="dropdown">
                                        <button class="form-select form-select-lg fs-6 text-start" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            <span class="text-truncate">
                                                {{ newItem.maSP ? getTenSP(newItem.maSP) : '-- Chọn Sản Phẩm (Gõ để tìm) --' }}
                                            </span>
                                        </button>
                                        <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 350px; overflow-y: auto;">
                                            <input type="text" class="form-control form-control-sm mb-2 border-warning" v-model="searchProductTextNew" placeholder="🔍 Nhập mã hoặc tên sản phẩm..." @click.stop>
                                            <div v-if="filteredProductsNew.length > 0">
                                                <button type="button" class="dropdown-item small py-2 border-bottom text-wrap hover-bg" v-for="sp in filteredProductsNew" :key="sp.maSP" @click="selectProductNew(sp.maSP)">
                                                    <strong class="text-primary">{{ sp.maSP }}</strong> - {{ sp.tenSP }}
                                                </button>
                                            </div>
                                            <div v-else class="text-center text-muted py-2 small">Không tìm thấy sản phẩm.</div>
                                        </div>
                                    </div>
                                </div>
                                
                                <div class="col-12 col-md-4">
                                    <label class="form-label fw-bold d-flex justify-content-between">
                                        <span>2. Chọn Máy (Serial) <span class="badge bg-secondary">Kho: {{ selectableSerials.length }}</span></span>
                                        <small class="text-danger" v-if="selectedSerials.length > 0">Đã chọn: {{ selectedSerials.length }}</small>
                                    </label>
                                    <div class="dropdown">
                                        <button class="btn btn-outline-secondary w-100 text-start d-flex justify-content-between align-items-center bg-white form-select-lg fs-6 shadow-none" type="button" data-bs-toggle="dropdown" :disabled="!newItem.maSP || selectableSerials.length === 0">
                                            <span class="text-truncate">{{ selectedSerials.length > 0 ? `Đã chọn ${selectedSerials.length} máy` : (newItem.maSP ? '-- Click chọn các máy --' : '-- Chọn SP trước --') }}</span>
                                        </button>
                                        <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 300px; overflow-y: auto;" @click.stop>
                                            <input type="text" class="form-control mb-2" v-model="searchSerialText" placeholder="🔍 Tìm serial (hoặc gõ Shift)...">
                                            <div class="d-flex align-items-center px-2 py-2 mb-2 bg-light rounded border" v-if="filteredSerials.length > 0">
                                                <input class="form-check-input m-0 me-2 cursor-pointer" type="checkbox" id="selectAllNewXuat" :checked="isAllSerialSelected" @change="toggleSelectAllSerial">
                                                <label class="form-check-label fw-bold cursor-pointer w-100" for="selectAllNewXuat">Chọn tất cả ({{ filteredSerials.length }})</label>
                                            </div>
                                            <div v-if="filteredSerials.length > 0">
                                                <div class="d-flex align-items-center py-2 px-2 hover-bg border-bottom" v-for="(seri, index) in filteredSerials" :key="seri">
                                                    <input class="form-check-input m-0 me-2 cursor-pointer" type="checkbox" :value="seri" :id="'n_'+seri" v-model="selectedSerials" @click="handleShiftClickSerial($event, index)">
                                                    <label class="form-check-label w-100 cursor-pointer font-monospace" :for="'n_'+seri">{{ seri }}</label>
                                                </div>
                                            </div>
                                            <div v-else class="text-center text-muted py-2 small">Hết máy trong kho.</div>
                                        </div>
                                    </div>
                                </div>

                                <div class="col-6 col-md-2">
                                    <label class="form-label fw-bold">Đơn Giá Bán</label>
                                    <input type="number" class="form-control form-control-lg fs-6" v-model="newItem.donGia" min="0">
                                </div>
                                <div class="col-12 col-md-2">
                                    <button class="btn btn-warning btn-lg w-100 fw-bold fs-6" @click="themMoiVaoPhieu" :disabled="isDeleting">Lưu Bổ Sung</button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="modal-body text-center p-5" v-else>
                    <div class="spinner-border text-warning" style="width: 4rem; height: 4rem;"></div>
                </div>

                <div class="modal-footer p-3 bg-light">
                    <button type="button" class="btn btn-secondary btn-lg fw-bold px-5 fs-6" @click="$emit('close')" :disabled="isDeleting">ĐÓNG</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import api from '@/utils/axios'; 

const props = defineProps(['soPhieu']);
const emit = defineEmits(['close', 'update-success']);

const chiTiet = ref(null);
const listSanPham = ref([]);
const listHinhThuc = ref([]);
const newItem = ref({ maSP: '', donGia: 0 });

const editThongTin = ref({ ngayTaoPhieu: '', ghiChu: '', maHT: null });
const isSavingInfo = ref(false);

const selectedItems = ref([]);
let lastCheckedIndex = -1;
const isDeleting = ref(false);

const editingMaSP = ref(null);
const editPrice = ref(0);

const availableSerials = ref([]); 
const selectedSerials = ref([]);  
const searchSerialText = ref(""); 
let lastCheckedIndexSerial = -1;

// --- CHECK XEM CÓ PHẢI NỘI BỘ KHÔNG ĐỂ KHÓA INPUT ---
const isNoiBoGoc = computed(() => {
    if (!chiTiet.value || !chiTiet.value.hinhThucXuat || !chiTiet.value.hinhThucXuat.tenHT) return false;
    return chiTiet.value.hinhThucXuat.tenHT.toLowerCase().includes('nội bộ');
});

const danhSachModelTrongPhieu = computed(() => {
    if (!chiTiet.value || !chiTiet.value.danhSachChiTiet) return [];
    const map = new Map();
    chiTiet.value.danhSachChiTiet.forEach(ct => {
        const ma = ct.sanPham?.maSP;
        if (ma) {
            if (!map.has(ma)) map.set(ma, { maSP: ma, tenSP: ct.sanPham?.tenSP, soLuong: 0 });
            map.get(ma).soLuong++;
        }
    });
    return Array.from(map.values());
});

const isModelSelected = (maSP) => {
    const listIDCuaModel = chiTiet.value.danhSachChiTiet.filter(ct => ct.sanPham?.maSP === maSP).map(ct => ct.maCTPX);
    if (listIDCuaModel.length === 0) return false;
    return listIDCuaModel.every(id => selectedItems.value.includes(id));
};

const toggleModelSelection = (maSP, isChecked) => {
    const listIDCuaModel = chiTiet.value.danhSachChiTiet.filter(ct => ct.sanPham?.maSP === maSP).map(ct => ct.maCTPX);
    if (isChecked) {
        const newSelected = new Set([...selectedItems.value, ...listIDCuaModel]);
        selectedItems.value = Array.from(newSelected);
    } else {
        selectedItems.value = selectedItems.value.filter(id => !listIDCuaModel.includes(id));
    }
};

const searchProductTextNew = ref("");
const filteredProductsNew = computed(() => {
    if (!searchProductTextNew.value) return listSanPham.value;
    const keyword = searchProductTextNew.value.toLowerCase();
    return listSanPham.value.filter(sp => sp.tenSP.toLowerCase().includes(keyword) || sp.maSP.toLowerCase().includes(keyword));
});

const selectProductNew = (maSP) => {
    newItem.value.maSP = maSP;
    searchProductTextNew.value = ""; 
    onChonSanPham();
};

const getTenSP = (maSP) => listSanPham.value.find(s => s.maSP === maSP)?.tenSP || maSP;

const formatForInput = (dateArray) => {
    if (!dateArray) return '';
    if (Array.isArray(dateArray)) {
        const [y, m, d, h, mn] = dateArray;
        const f = n => n < 10 ? '0'+n : n;
        return `${y}-${f(m)}-${f(d)}T${f(h||0)}:${f(mn||0)}`;
    }
    return new Date(dateArray).toISOString().slice(0, 16);
};

const luuThongTinChung = async () => {
    if(!editThongTin.value.ngayTaoPhieu) return alert("Vui lòng chọn ngày!");
    if(!editThongTin.value.maHT) return alert("Vui lòng chọn Hình thức xuất!");
    isSavingInfo.value = true;
    try {
        await api.put(`/kho/xuat/${props.soPhieu}`, { 
            soPhieu: props.soPhieu,
            ghiChu: editThongTin.value.ghiChu,
            ngayTaoPhieu: editThongTin.value.ngayTaoPhieu,
            maHT: editThongTin.value.maHT 
        });
        alert("Lưu thông tin thành công!");
        loadChiTiet();
        emit('update-success');
    } catch (e) { alert("Lỗi: " + (e.response?.data?.message || e.message)); } 
    finally { isSavingInfo.value = false; }
};

const batDauSuaGia = (item) => {
    editingMaSP.value = item.sanPham?.maSP;
    editPrice.value = item.donGia || 0;
};

const luuGiaTheoSP = async (maSP) => {
    if (editPrice.value < 0) return alert("Đơn giá không được âm!");
    try {
        await api.put(`/kho/xuat/${props.soPhieu}/san-pham/${maSP}/gia`, { donGia: editPrice.value });
        editingMaSP.value = null; 
        loadChiTiet(); 
        emit('update-success');
    } catch(e) { alert("Lỗi cập nhật giá: " + (e.response?.data?.message || e.message)); }
};

const deletableItems = computed(() => {
    if (!chiTiet.value || !chiTiet.value.danhSachChiTiet) return [];
    return chiTiet.value.danhSachChiTiet.filter(i => i.mayIn?.tonKho === true);
});

const isAllSelected = computed(() => {
    if (deletableItems.value.length === 0) return false;
    return deletableItems.value.every(i => selectedItems.value.includes(i.maCTPN));
});

const toggleSelectAll = (e) => {
    if (e.target.checked) selectedItems.value = deletableItems.value.map(i => i.maCTPN);
    else selectedItems.value = [];
};

const handleShiftClick = (event, index, item) => {
    if (item.mayIn?.tonKho === false) return; 
    if (event.shiftKey && lastCheckedIndex !== -1) {
        const start = Math.min(lastCheckedIndex, index);
        const end = Math.max(lastCheckedIndex, index);
        const subList = chiTiet.value.danhSachChiTiet.slice(start, end + 1);
        const isChecking = event.target.checked;
        subList.forEach(row => {
            if (row.mayIn?.tonKho === false) return; 
            if (isChecking) {
                if (!selectedItems.value.includes(row.maCTPN)) selectedItems.value.push(row.maCTPN);
            } else {
                selectedItems.value = selectedItems.value.filter(id => id !== row.maCTPN);
            }
        });
    }
    lastCheckedIndex = index;
};

const xoaNhieuDong = async () => {
    if (selectedItems.value.length === 0) return;
    if (!confirm(`Bạn chuẩn bị xóa vĩnh viễn ${selectedItems.value.length} máy khỏi kho.\nTiếp tục?`)) return;

    isDeleting.value = true;
    let successCount = 0;
    try {
        for (const maCTPX of selectedItems.value) {
            await api.delete(`/kho/xuat/chi-tiet/${maCTPX}`);
            successCount++;
        }
    } catch (e) {
        alert(`Bị gián đoạn. Đã xóa ${successCount}/${selectedItems.value.length} máy. Lỗi: ` + (e.response?.data?.message || e.message));
    } finally {
        isDeleting.value = false;
        selectedItems.value = [];
        lastCheckedIndex = -1;
        loadChiTiet(); 
        emit('update-success');
    }
};

const xoaDong = async (maCTPX) => {
    if(!confirm("Thu hồi máy này về kho?")) return;
    try {
        await api.delete(`/kho/xuat/chi-tiet/${maCTPX}`);
        selectedItems.value = selectedItems.value.filter(id => id !== maCTPX);
        loadChiTiet(); 
        emit('update-success');
    } catch (e) { alert("Lỗi xóa: " + (e.response?.data?.message || e.message)); }
};

const selectableSerials = computed(() => availableSerials.value);
const filteredSerials = computed(() => {
    if (!searchSerialText.value) return selectableSerials.value;
    return selectableSerials.value.filter(s => s.toLowerCase().includes(searchSerialText.value.toLowerCase()));
});
const isAllSerialSelected = computed(() => {
    if (filteredSerials.value.length === 0) return false;
    return filteredSerials.value.every(s => selectedSerials.value.includes(s));
});

const toggleSelectAllSerial = (e) => {
    if (e.target.checked) filteredSerials.value.forEach(s => { if (!selectedSerials.value.includes(s)) selectedSerials.value.push(s); });
    else selectedSerials.value = selectedSerials.value.filter(s => !filteredSerials.value.includes(s));
};

const handleShiftClickSerial = (event, index) => {
    if (event.shiftKey && lastCheckedIndexSerial !== -1) {
        const start = Math.min(lastCheckedIndexSerial, index);
        const end = Math.max(lastCheckedIndexSerial, index);
        const subList = filteredSerials.value.slice(start, end + 1);
        const isChecking = event.target.checked;
        subList.forEach(serial => {
            if (isChecking) { if (!selectedSerials.value.includes(serial)) selectedSerials.value.push(serial); } 
            else { selectedSerials.value = selectedSerials.value.filter(s => s !== serial); }
        });
    }
    lastCheckedIndexSerial = index;
};

const onChonSanPham = async () => {
    selectedSerials.value = []; availableSerials.value = []; searchSerialText.value = ""; lastCheckedIndexSerial = -1;
    if (!chiTiet.value.khoXuat?.maKho) return;
    try {
        const res = await api.get('/kho/may-in/kha-dung', {
            params: { maSP: newItem.value.maSP, maKho: chiTiet.value.khoXuat.maKho }
        });
        availableSerials.value = res.data;
    } catch (e) { alert("Không tải được danh sách máy tồn kho!"); }
};

const themMoiVaoPhieu = async () => {
    if (!newItem.value.maSP) return alert("Vui lòng chọn sản phẩm!");
    if (selectedSerials.value.length === 0) return alert("Chưa chọn Serial nào để xuất!");
    try {
        await api.post(`/kho/xuat/${props.soPhieu}/bo-sung`, {
            maSP: newItem.value.maSP,
            donGia: newItem.value.donGia,
            danhSachSeri: selectedSerials.value
        });
        alert("Bổ sung thành công!");
        newItem.value = { maSP: '', donGia: 0 };
        selectedSerials.value = [];
        loadChiTiet();
        emit('update-success');
    } catch (e) { alert("Lỗi thêm mới: " + (e.response?.data?.message || e.message)); }
};

const loadChiTiet = async () => {
    try {
        const res = await api.get(`/kho/xuat/${props.soPhieu}`);
        chiTiet.value = res.data;
        editThongTin.value.ngayTaoPhieu = formatForInput(res.data.ngayXuat); 
        editThongTin.value.ghiChu = res.data.ghiChu;
        editThongTin.value.maHT = res.data.hinhThucXuat?.maHT || null; 
    } catch (e) { alert("Lỗi tải chi tiết: " + (e.response?.data?.message || e.message)); }
};

const loadMasterData = async () => {
    try {
        const [s, ht] = await Promise.all([ api.get('/san-pham/list'), api.get('/hinh-thuc-xuat') ]);
        listSanPham.value = s.data.content || s.data;
        listHinhThuc.value = ht.data; 
    } catch (e) { console.error(e); }
};

const formatDate = (d) => {
    if(!d) return '';
    if(Array.isArray(d)) {
        const [year, month, day, hour, minute] = d;
        const f = (n) => n < 10 ? '0' + n : n;
        return `${f(day)}/${f(month)}/${year} ${hour ? f(hour) + ':' + f(minute) : ''}`;
    }
    return new Date(d).toLocaleString('vi-VN');
};

const formatCurrency = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0);

onMounted(() => { 
    loadChiTiet(); 
    loadMasterData(); 
});
</script>

<style scoped>
.modal-backdrop { opacity: 0.5; position: fixed; top: 0; left: 0; width: 100%; height: 100vh; background: #000; z-index: 1040; }
.modal { z-index: 1050; display: block; }
.cursor-pointer { cursor: pointer; }
.custom-modal-size { max-width: 95%; }
.hover-bg:hover { background-color: #f8f9fa; }
.dropdown-toggle::after { display: none !important; }
</style>