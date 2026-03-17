<template>
    <div class="modal-backdrop show"></div>
    <div class="modal d-block" tabindex="-1">
        <div class="modal-dialog custom-modal-size modal-dialog-scrollable">
            <div class="modal-content">
                <div class="modal-header bg-info text-white p-3">
                    <h4 class="modal-title fw-bold m-0">CHI TIẾT VÀ CHỈNH SỬA PHIẾU: {{ chiTiet?.soPhieu }}</h4>
                    <button type="button" class="btn-close btn-close-white" @click="$emit('close')" :disabled="isDeleting"></button>
                </div>
                
                <div class="modal-body p-4" v-if="chiTiet">
                    <div class="row mb-4 p-3 bg-light border rounded shadow-sm align-items-center" style="font-size: 1.1rem;">
                        <div class="col-md-3">
                            <label class="fw-bold text-muted mb-1 fs-6">Kho Nhập</label>
                            <div class="fw-bold text-primary fs-5">{{ chiTiet.khoNhap?.tenKho }}</div>
                        </div>
                        
                        <div class="col-md-2">
                            <label class="fw-bold text-muted mb-1 fs-6">Hình Thức Nhập</label>
                            <select class="form-select border-primary fw-bold" v-model="editThongTin.maHT">
                                <option v-for="ht in listHinhThuc" :key="ht.maHT" :value="ht.maHT">{{ ht.tenHT }}</option>
                            </select>
                        </div>

                        <div class="col-md-3">
                            <label class="fw-bold text-muted mb-1 fs-6">Ngày Nhập</label>
                            <input type="datetime-local" class="form-control border-primary fw-bold" v-model="editThongTin.ngayTaoPhieu">
                        </div>
                        <div class="col-md-2">
                            <label class="fw-bold text-muted mb-1 fs-6">Ghi Chú</label>
                            <input type="text" class="form-control border-primary fw-bold" v-model="editThongTin.ghiChu">
                        </div>
                        <div class="col-md-2 text-end">
                            <label class="fw-bold text-muted mb-1 fs-6">&nbsp;</label>
                            <button class="btn btn-primary fw-bold w-100" @click="luuThongTinChung" :disabled="isSavingInfo">
                                <span v-if="isSavingInfo">Đang lưu...</span>
                                <span v-else>Lưu Thông Tin</span>
                            </button>
                        </div>
                    </div>

                    <div class="d-flex justify-content-between align-items-center mt-4 mb-3">
                        <h4 class="text-primary m-0 fw-bold">Danh sách máy trong phiếu</h4>
                        <div class="d-flex align-items-center gap-4">
                            <div class="fs-5">Tổng số lượng: <span class="badge bg-primary fs-5">{{ chiTiet.tongSoLuong }}</span></div>
                            <div class="fs-5">Tổng thành tiền: <span class="text-danger fw-bold fs-5">{{ formatCurrency(chiTiet.tongTien) }}</span></div>
                            <button class="btn btn-danger fw-bold px-4 py-2 shadow-sm fs-6 ms-3" 
                                    :disabled="selectedItems.length === 0 || isDeleting" 
                                    @click="xoaNhieuDong">
                                <span v-if="isDeleting">Đang xử lý...</span>
                                <span v-else>Xóa Đã Chọn ({{ selectedItems.length }})</span>
                            </button>
                        </div>
                    </div>

                    <div class="bg-light p-2 mb-2 border rounded d-flex align-items-center gap-3">
                        <span class="fw-bold text-muted">Chọn nhanh máy theo Model để xóa:</span>
                        <div class="dropdown flex-grow-1" style="max-width: 400px;">
                            <button class="btn btn-outline-secondary btn-sm w-100 text-start d-flex justify-content-between align-items-center bg-white shadow-none" 
                                    type="button" data-bs-toggle="dropdown" aria-expanded="false" data-bs-auto-close="outside">
                                <span>-- Tick chọn Model cần xóa nhanh --</span>
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
                                    <th width="250px">Mã Máy (Hệ thống)</th>
                                    <th width="150px">Trạng Thái</th> 
                                    <th width="220px">Đơn Giá</th>
                                    <th width="220px">Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(item, index) in chiTiet.danhSachChiTiet" :key="item.maCTPN" 
                                    :class="{'table-warning': selectedItems.includes(item.maCTPN)}">
                                    
                                    <td>
                                        <input class="form-check-input cursor-pointer" type="checkbox" 
                                               style="width: 22px; height: 22px;"
                                               v-if="item.mayIn?.tonKho === true"
                                               :value="item.maCTPN" 
                                               v-model="selectedItems"
                                               @click="handleShiftClick($event, index, item)">
                                    </td>

                                    <td class="fw-bold">{{ index + 1 }}</td>
                                    
                                    <td class="fw-bold text-secondary">{{ item.sanPham?.maSP }}</td>
                                    <td class="text-start fw-bold text-primary fs-5">{{ item.sanPham?.tenSP }}</td>
                                    <td class="font-monospace fw-bold">{{ item.mayIn?.maMay }}</td>
                                    
                                    <td>
                                        <span v-if="item.mayIn?.tonKho === false" class="badge bg-secondary p-2 fs-6 w-100">Đã Xuất</span>
                                        <span v-else class="badge bg-success p-2 fs-6 w-100">Tồn Kho</span>
                                    </td>
                                    
                                    <td class="text-end">
                                        <div v-if="editingMaSP === item.sanPham?.maSP" class="input-group">
                                            <input type="number" class="form-control text-end border-warning fw-bold fs-5" v-model="editPrice" min="0">
                                        </div>
                                        <span v-else class="fw-bold text-danger fs-5">{{ formatCurrency(item.donGia) }}</span>
                                    </td>

                                    <td>
                                        <template v-if="item.mayIn?.tonKho === true">
                                            <div v-if="editingMaSP === item.sanPham?.maSP" class="d-flex justify-content-center gap-2">
                                                <button class="btn btn-success fw-bold px-3 fs-6" @click="luuGiaTheoSP(item.sanPham?.maSP)">Lưu Giá</button>
                                                <button class="btn btn-secondary fw-bold px-3 fs-6" @click="editingMaSP = null">Hủy</button>
                                            </div>
                                            
                                            <div v-else class="d-flex justify-content-center gap-2">
                                                <button class="btn btn-outline-warning fw-bold px-3 fs-6" @click="batDauSuaGia(item)" :disabled="isDeleting">
                                                    Sửa Giá
                                                </button>
                                                <button class="btn btn-outline-danger fw-bold px-4 fs-6" @click="xoaDong(item.maCTPN)" :disabled="isDeleting">
                                                    Xóa
                                                </button>
                                            </div>
                                        </template>
                                        <span v-else class="text-muted fw-bold fst-italic">Đã bán (Khóa)</span>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="card border-success shadow-sm">
                        <div class="card-header bg-success text-white py-3 fs-5 fw-bold">
                            Bổ sung thêm máy vào phiếu
                        </div>
                        <div class="card-body bg-light p-4">
                            <div class="row g-3 align-items-end">
                                <div class="col-12 col-md-5">
                                    <label class="form-label fw-bold">Chọn Sản Phẩm</label>
                                    <div class="dropdown">
                                        <button class="form-select form-select-lg fs-6 text-start" type="button" data-bs-toggle="dropdown" aria-expanded="false">
                                            <span class="text-truncate">
                                                {{ newItem.maSP ? getTenSP(newItem.maSP) : '-- Chọn Sản Phẩm (Gõ để tìm) --' }}
                                            </span>
                                        </button>
                                        <div class="dropdown-menu w-100 p-2 shadow" style="max-height: 350px; overflow-y: auto;">
                                            <input type="text" class="form-control form-control-sm mb-2 border-success" v-model="searchProductTextNew" placeholder="🔍 Nhập mã hoặc tên sản phẩm..." @click.stop>
                                            <div v-if="filteredProductsNew.length > 0">
                                                <button type="button" class="dropdown-item small py-2 border-bottom text-wrap hover-bg" v-for="sp in filteredProductsNew" :key="sp.maSP" @click="selectProductNew(sp.maSP)">
                                                    <strong class="text-primary">{{ sp.maSP }}</strong> - {{ sp.tenSP }}
                                                </button>
                                            </div>
                                            <div v-else class="text-center text-muted py-2 small">Không tìm thấy sản phẩm.</div>
                                        </div>
                                    </div>
                                </div>
                                <div class="col-6 col-md-3">
                                    <label class="form-label fw-bold">Đơn Giá</label>
                                    <input type="number" class="form-control form-control-lg fs-6" v-model="newItem.donGia" min="0">
                                </div>
                                <div class="col-6 col-md-2">
                                    <label class="form-label fw-bold">Số Lượng</label>
                                    <input type="number" class="form-control form-control-lg fs-6 text-center" v-model="newItem.soLuong" min="1">
                                </div>
                                <div class="col-12 col-md-2">
                                    <button class="btn btn-success btn-lg w-100 fw-bold fs-6" @click="themMoiVaoPhieu" :disabled="isDeleting">
                                        Lưu Bổ Sung
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="modal-body text-center p-5" v-else>
                    <div class="spinner-border text-info" style="width: 4rem; height: 4rem;"></div>
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
const listHinhThuc = ref([]); // Mới
const newItem = ref({ maSP: '', donGia: 0, soLuong: 1 });

const editThongTin = ref({ ngayTaoPhieu: '', ghiChu: '', maHT: null }); // Thêm maHT
const isSavingInfo = ref(false);

const selectedItems = ref([]);
let lastCheckedIndex = -1;
const isDeleting = ref(false);

const editingMaSP = ref(null);
const editPrice = ref(0);

const danhSachModelTrongPhieu = computed(() => {
    if (!chiTiet.value || !chiTiet.value.danhSachChiTiet) return [];
    const map = new Map();
    chiTiet.value.danhSachChiTiet.forEach(ct => {
        if (ct.mayIn?.tonKho === true) { 
            const ma = ct.sanPham?.maSP;
            if (ma) {
                if (!map.has(ma)) {
                    map.set(ma, { maSP: ma, tenSP: ct.sanPham?.tenSP, soLuong: 0 });
                }
                map.get(ma).soLuong++;
            }
        }
    });
    return Array.from(map.values());
});

const isModelSelected = (maSP) => {
    const listIDCuaModel = chiTiet.value.danhSachChiTiet
        .filter(ct => ct.sanPham?.maSP === maSP && ct.mayIn?.tonKho === true)
        .map(ct => ct.maCTPN);
        
    if (listIDCuaModel.length === 0) return false;
    return listIDCuaModel.every(id => selectedItems.value.includes(id));
};

const toggleModelSelection = (maSP, isChecked) => {
    const listIDCuaModel = chiTiet.value.danhSachChiTiet
        .filter(ct => ct.sanPham?.maSP === maSP && ct.mayIn?.tonKho === true)
        .map(ct => ct.maCTPN);

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
    return listSanPham.value.filter(sp => 
        sp.tenSP.toLowerCase().includes(keyword) || 
        sp.maSP.toLowerCase().includes(keyword)
    );
});

const selectProductNew = (maSP) => {
    newItem.value.maSP = maSP;
    searchProductTextNew.value = ""; 
};

const getTenSP = (ma) => listSanPham.value.find(s => s.maSP === ma)?.tenSP || ma;

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
    if(!editThongTin.value.maHT) return alert("Vui lòng chọn Hình thức Nhập!");
    isSavingInfo.value = true;
    try {
        await api.put(`/kho/nhap/${props.soPhieu}`, { 
            soPhieu: props.soPhieu,
            ghiChu: editThongTin.value.ghiChu,
            ngayTaoPhieu: editThongTin.value.ngayTaoPhieu,
            maHT: editThongTin.value.maHT // LƯU HÌNH THỨC
        });
        alert("Lưu thông tin thành công!");
        loadChiTiet();
        emit('update-success');
    } catch (e) {
        alert("Lỗi: " + (e.response?.data?.message || e.message));
    } finally {
        isSavingInfo.value = false;
    }
};

const batDauSuaGia = (item) => {
    editingMaSP.value = item.sanPham?.maSP;
    editPrice.value = item.donGia || 0;
};

const luuGiaTheoSP = async (maSP) => {
    if (editPrice.value < 0) return alert("Đơn giá không được âm!");
    try {
        await api.put(`/kho/nhap/${props.soPhieu}/san-pham/${maSP}/gia`, { donGia: editPrice.value });
        editingMaSP.value = null; 
        loadChiTiet(); 
        emit('update-success');
    } catch(e) {
        alert("Lỗi cập nhật giá: " + (e.response?.data?.message || e.message));
    }
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
        for (const maCTPN of selectedItems.value) {
            await api.delete(`/kho/nhap/chi-tiet/${maCTPN}`);
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

const xoaDong = async (maCTPN) => {
    if(!confirm("Xóa máy này khỏi hệ thống?")) return;
    try {
        await api.delete(`/kho/nhap/chi-tiet/${maCTPN}`);
        selectedItems.value = selectedItems.value.filter(id => id !== maCTPN);
        loadChiTiet(); 
        emit('update-success');
    } catch (e) { 
        alert("Lỗi xóa: " + (e.response?.data?.message || e.message)); 
    }
};

const loadChiTiet = async () => {
    try {
        const res = await api.get(`/kho/nhap/${props.soPhieu}`);
        chiTiet.value = res.data;
        editThongTin.value.ngayTaoPhieu = formatForInput(res.data.ngayNhap); 
        editThongTin.value.ghiChu = res.data.ghiChu;
        editThongTin.value.maHT = res.data.hinhThucNhap?.maHT || null; // GÁN HÌNH THỨC CŨ VÀO FORM SỬA
    } catch (e) { 
        alert("Lỗi tải chi tiết: " + (e.response?.data?.message || e.message));
    }
};

const loadSanPham = async () => {
    try {
        const [s, ht] = await Promise.all([ 
            api.get('/san-pham/list'),
            api.get('/hinh-thuc-nhap') // GỌI API LẤY DANH SÁCH HÌNH THỨC
        ]);
        listSanPham.value = s.data.content || s.data;
        listHinhThuc.value = ht.data; // LƯU VÀO BIẾN
    } catch (e) { console.error(e); }
};

const themMoiVaoPhieu = async () => {
    if (!newItem.value.maSP) return alert("Vui lòng chọn sản phẩm!");
    try {
        await api.post(`/kho/nhap/${props.soPhieu}/bo-sung`, newItem.value);
        alert("Bổ sung thành công!");
        newItem.value = { maSP: '', donGia: 0, soLuong: 1 };
        loadChiTiet();
        emit('update-success');
    } catch (e) { 
        alert("Lỗi thêm mới: " + (e.response?.data?.message || e.message)); 
    }
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
    loadSanPham(); 
});
</script>

<style scoped>
.modal-backdrop { opacity: 0.5; position: fixed; top: 0; left: 0; width: 100%; height: 100vh; background: #000; z-index: 1040; }
.modal { z-index: 1050; display: block; }
.cursor-pointer { cursor: pointer; }

/* Mở rộng chiều rộng tối đa của Modal cho thoáng */
.custom-modal-size {
    max-width: 95%;
}
.hover-bg:hover { background-color: #f8f9fa; }
/* Ẩn mũi tên mặc định của dropdown */
.dropdown-toggle::after {
    display: none !important;
}
</style>