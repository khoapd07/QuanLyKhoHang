<template>
    <div class="card shadow-sm">
        <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Quản lý Nhập Kho</h5>
            <router-link to="/nhap-kho/tao-moi" class="btn btn-light btn-sm fw-bold">
                <i class="fas fa-plus"></i> Tạo Phiếu Mới
            </router-link>
        </div>
        <div class="card-body">
            <div v-if="loading" class="text-center py-3">
                <div class="spinner-border text-primary" role="status"></div>
                <p>Đang tải dữ liệu...</p>
            </div>

            <table v-else class="table table-hover table-bordered align-middle">
                <thead class="table-light text-center">
                    <tr>
                        <th>Số Phiếu</th>
                        <th>Ngày Nhập</th>
                        <th>Kho</th>
                        <th>Hãng SX</th>
                        <th width="30%">Chi tiết</th>
                        <th>Tổng SL</th>
                        <th>Tổng Tiền</th>
                        <th width="150px">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="item in danhSachPhieu" :key="item.soPhieu">
                        <td class="fw-bold text-primary">{{ item.soPhieu }}</td>
                        <td>{{ formatDate(item.ngayNhap) }}</td>
                        <td>{{ item.tenKho }}</td>
                        <td class="fw-bold text-success">{{ item.danhSachHang }}</td>
                        <td>
                            <span v-for="(part, idx) in splitSummary(item.tomTatSanPham)" :key="idx" 
                                  class="badge bg-info text-dark me-1 mb-1">
                                {{ part }}
                            </span>
                            <div v-if="item.ghiChu" class="small text-muted fst-italic mt-1">
                                <i class="fas fa-comment-alt me-1"></i>{{ item.ghiChu }}
                            </div>
                        </td>
                        <td class="text-center fw-bold">{{ item.tongSoLuongMay }}</td>
                        <td class="text-end text-danger fw-bold">{{ formatCurrency(item.tongTien) }}</td>
                        <td class="text-center">
                            <button class="btn btn-sm btn-outline-info me-1" @click="moChiTiet(item.soPhieu)" title="Xem chi tiết">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-warning me-1" @click="moModalSua(item)" title="Sửa ghi chú">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-danger" @click="xoaPhieu(item.soPhieu)" title="Xóa phiếu">
                                <i class="fas fa-trash-alt"></i>
                            </button>
                        </td>
                    </tr>
                    <tr v-if="danhSachPhieu.length === 0">
                        <td colspan="8" class="text-center text-muted">Chưa có dữ liệu nhập kho</td>
                    </tr>
                </tbody>
            </table>
        </div>
        
        <NhapKhoChiTiet 
            v-if="showModal" 
            :soPhieu="selectedSoPhieu" 
            @close="showModal = false"
            @update-success="layDanhSach" 
        />

        <div v-if="showEditModal" class="modal d-block" style="background: rgba(0,0,0,0.5)">
            <div class="modal-dialog">
                <div class="modal-content">
                    <div class="modal-header bg-warning">
                        <h5 class="modal-title">Cập nhật Phiếu: {{ editItem.soPhieu }}</h5>
                        <button type="button" class="btn-close" @click="showEditModal = false"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label class="form-label">Ghi Chú</label>
                            <textarea class="form-control" rows="3" v-model="editItem.ghiChu"></textarea>
                        </div>
                        <div class="alert alert-warning small">
                            <i class="fas fa-exclamation-triangle"></i> Lưu ý: Để sửa sản phẩm hoặc số lượng, vui lòng bấm vào nút "Xem chi tiết" (icon con mắt).
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button class="btn btn-secondary" @click="showEditModal = false">Hủy</button>
                        <button class="btn btn-primary" @click="luuCapNhat">Lưu thay đổi</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import NhapKhoChiTiet from './NhapKhoChiTiet.vue'; 

const danhSachPhieu = ref([]);
const showModal = ref(false);
const showEditModal = ref(false); 
const selectedSoPhieu = ref(null);
const loading = ref(false);
const editItem = ref({ soPhieu: '', ghiChu: '' }); 

const layDanhSach = async () => {
    loading.value = true;
    try {
        // Hàm này sẽ được gọi lại ngay khi Modal con báo 'update-success'
        const res = await axios.get('/api/kho/nhap');
        danhSachPhieu.value = res.data;
    } catch (e) { console.error("Lỗi data:", e); } 
    finally { loading.value = false; }
};

const moChiTiet = (soPhieu) => {
    selectedSoPhieu.value = soPhieu;
    showModal.value = true;
};

// --- LOGIC SỬA GHI CHÚ ---
const moModalSua = (item) => {
    editItem.value = { ...item };
    showEditModal.value = true;
};

const luuCapNhat = async () => {
    try {
        await axios.put(`/api/kho/nhap/${editItem.value.soPhieu}`, {
            ghiChu: editItem.value.ghiChu
        });
        alert("Cập nhật thành công!");
        showEditModal.value = false;
        layDanhSach(); 
    } catch (e) {
        alert("Lỗi cập nhật: " + (e.response?.data || e.message));
    }
};

const xoaPhieu = async (soPhieu) => {
    if(!confirm('Cảnh báo: Xóa phiếu này sẽ xóa toàn bộ máy trong kho. Tiếp tục?')) return;
    try {
        await axios.delete(`/api/kho/nhap/${soPhieu}`);
        alert("Đã xóa thành công!");
        layDanhSach();
    } catch (e) { 
        alert("Lỗi xóa: " + (e.response?.data || e.message)); 
    }
};

const splitSummary = (str) => str ? str.split(', ') : [];

const formatDate = (dateArray) => {
    if (!dateArray) return '';
    if (Array.isArray(dateArray)) {
        const [year, month, day, hour, minute] = dateArray;
        return `${day}/${month}/${year} ${hour}:${minute}`;
    }
    return new Date(dateArray).toLocaleString('vi-VN');
};

const formatCurrency = (v) => {
    if(!v) return '0 đ';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);
};

onMounted(() => layDanhSach());
</script>