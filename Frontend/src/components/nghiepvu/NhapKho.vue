<script setup>
import { ref, onMounted } from 'vue';
import api from '@/utils/axios'; // Dùng instance api đã cấu hình Token
import NhapKhoChiTiet from './NhapKhoChiTiet.vue'; 

// --- CẤU HÌNH API ---
// Backend: @RequestMapping("/api/kho") + @GetMapping("/nhap")
// Axios BaseURL: /api
// => Kết quả: /kho/nhap
const API_URL = '/kho/nhap'; 

const danhSachPhieu = ref([]);
const showModal = ref(false);
const showEditModal = ref(false); 
const selectedSoPhieu = ref(null);
const loading = ref(false);
const editItem = ref({ soPhieu: '', ghiChu: '' }); 

// 1. Lấy danh sách phiếu nhập
const layDanhSach = async () => {
    loading.value = true;
    try {
        const res = await api.get(API_URL);
        danhSachPhieu.value = res.data;
    } catch (e) { 
        console.error("Lỗi data:", e); 
        const msg = e.response?.data?.message || e.message;
        // Có thể hiện thông báo lỗi nếu cần
    } finally { loading.value = false; }
};

// Mở modal xem chi tiết
const moChiTiet = (soPhieu) => {
    selectedSoPhieu.value = soPhieu;
    showModal.value = true;
};

// Mở modal sửa ghi chú
const moModalSua = (item) => {
    editItem.value = { ...item };
    showEditModal.value = true;
};

// 2. Cập nhật phiếu nhập (PUT)
const luuCapNhat = async () => {
    try {
        // Backend nhận PhieuNhapDTO, gửi các trường cần thiết
        const payload = {
            soPhieu: editItem.value.soPhieu,
            ghiChu: editItem.value.ghiChu
        };

        // URL: /api/kho/nhap/{soPhieu}
        await api.put(`${API_URL}/${editItem.value.soPhieu}`, payload);
        
        alert("Cập nhật thành công!");
        showEditModal.value = false;
        layDanhSach(); 
    } catch (e) {
        alert("Lỗi cập nhật: " + (e.response?.data?.message || e.response?.data || e.message));
    }
};

// 3. Xóa phiếu nhập (DELETE)
const xoaPhieu = async (soPhieu) => {
    if(!confirm('Cảnh báo: Xóa phiếu này sẽ xóa toàn bộ máy trong kho. Tiếp tục?')) return;
    try {
        // URL: /api/kho/nhap/{soPhieu}
        await api.delete(`${API_URL}/${soPhieu}`);
        
        alert("Đã xóa thành công!");
        layDanhSach();
    } catch (e) { 
        alert("Lỗi xóa: " + (e.response?.data?.message || e.response?.data || e.message)); 
    }
};

// Helper: Tách chuỗi tóm tắt sản phẩm
const splitSummary = (str) => str ? str.split(', ') : [];

// Helper: Format ngày tháng
const formatDate = (dateArray) => {
    if (!dateArray) return '';
    if (Array.isArray(dateArray)) {
        const [year, month, day, hour, minute] = dateArray;
        const f = (n) => n < 10 ? '0' + n : n;
        return `${f(day)}/${f(month)}/${year} ${hour ? f(hour) + ':' + f(minute) : ''}`;
    }
    return new Date(dateArray).toLocaleString('vi-VN');
};

// Helper: Format tiền tệ
const formatCurrency = (v) => {
    if(!v) return '0 đ';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);
};

onMounted(() => layDanhSach());
</script>

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
                        <th>Nhà Cung Cấp</th> 
                        <th width="25%">Chi tiết</th>
                        <th>Hiện Trạng (Còn/Tổng)</th>
                        <th>Giá Trị Tồn</th>
                        <th width="150px">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="item in danhSachPhieu" :key="item.soPhieu">
                        <td class="fw-bold text-primary">{{ item.soPhieu }}</td>
                        <td>{{ formatDate(item.ngayNhap) }}</td>
                        <td>{{ item.tenKho }}</td>
                        
                        <td class="fw-bold">{{ item.tenKhachHang || '---' }}</td>

                        <td>
                            <span v-for="(part, idx) in splitSummary(item.tomTatSanPham)" :key="idx" 
                                  class="badge bg-info text-dark me-1 mb-1">
                                {{ part }}
                            </span>
                            <div v-if="item.ghiChu" class="small text-muted fst-italic mt-1">
                                <i class="fas fa-comment-alt me-1"></i>{{ item.ghiChu }}
                            </div>
                        </td>
                        
                        <td class="text-center">
                            <span class="fw-bold text-success fs-6">{{ item.soLuongConLai }}</span>
                            <span class="text-muted small"> / {{ item.tongSoLuongMay }}</span>
                            <div class="progress mt-1" style="height: 4px;">
                                <div class="progress-bar bg-success" role="progressbar" 
                                     :style="{width: (item.soLuongConLai / item.tongSoLuongMay * 100) + '%'}" 
                                     aria-valuenow="25" aria-valuemin="0" aria-valuemax="100"></div>
                            </div>
                        </td>

                        <td class="text-end">
                            <div class="fw-bold text-danger">{{ formatCurrency(item.tienConLai) }}</div>
                            <small class="text-muted text-decoration-line-through">{{ formatCurrency(item.tongTien) }}</small>
                        </td>

                        <td class="text-center">
                            <button class="btn btn-sm btn-outline-info me-1" @click="moChiTiet(item.soPhieu)" title="Xem chi tiết">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-warning me-1" @click="moModalSua(item)" title="Sửa ghi chú">
                                <i class="fas fa-edit"></i>
                            </button>
                            <button v-if="item.soLuongConLai === item.tongSoLuongMay" 
                                    class="btn btn-sm btn-outline-danger" 
                                    @click="xoaPhieu(item.soPhieu)" title="Xóa phiếu">
                                <i class="fas fa-trash-alt"></i>
                            </button>
                            <button v-else class="btn btn-sm btn-secondary" disabled title="Không thể xóa vì đã có hàng xuất bán">
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