<template>
    <div class="card shadow-sm border-warning">
        <div class="card-header bg-warning text-dark d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Quản lý Xuất Kho (Bán Hàng)</h5>
            <router-link to="/xuat-kho/tao-moi" class="btn btn-dark btn-sm fw-bold">
                <i class="fas fa-plus"></i> Tạo Phiếu Xuất
            </router-link>
        </div>
        <div class="card-body">
            <div v-if="loading" class="text-center py-3">
                <div class="spinner-border text-warning" role="status"></div>
                <p>Đang tải dữ liệu...</p>
            </div>

            <table v-else class="table table-hover table-bordered align-middle">
                <thead class="table-light text-center">
                    <tr>
                        <th>Số Phiếu</th>
                        <th>Ngày Xuất</th>
                        <th>Kho Xuất</th>
                        <th>Khách Hàng</th>
                        <th width="30%">Chi tiết (Sản phẩm x SL)</th>
                        <th>Tổng SL</th>
                        <th>Tổng Tiền</th>
                        <th width="120px">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="item in danhSachPhieu" :key="item.soPhieu">
                        <td class="fw-bold text-primary">{{ item.soPhieu }}</td>
                        <td>{{ formatDate(item.ngayXuat) }}</td>
                        <td>{{ item.tenKho }}</td>
                        <td class="fw-bold">{{ item.tenKhachHang || '---' }}</td>

                        <td>
                            <span v-for="(part, idx) in splitSummary(item.tomTatSanPham)" :key="idx" 
                                  class="badge bg-warning text-dark me-1 mb-1">
                                {{ part }}
                            </span>
                            <div v-if="item.ghiChu" class="small text-muted fst-italic mt-1">
                                ({{ item.ghiChu }})
                            </div>
                        </td>
                        <td class="text-center fw-bold">{{ item.tongSoLuong }}</td>
                        <td class="text-end text-danger fw-bold">{{ formatCurrency(item.tongTien) }}</td>
                        
                        <td class="text-center">
                            <button class="btn btn-sm btn-outline-info me-1" @click="moChiTiet(item.soPhieu)" title="Xem chi tiết">
                                <i class="fas fa-eye"></i>
                            </button>

                            <span v-if="isYearLocked(item.ngayXuat)" class="text-muted ms-1" title="Đã chốt sổ năm này">
                                <i class="fas fa-lock"></i>
                            </span>
                            
                            <button v-else class="btn btn-sm btn-outline-danger" @click="huyPhieu(item.soPhieu)">
                                <i class="fas fa-undo"></i>
                            </button>
                        </td>
                    </tr>
                    <tr v-if="danhSachPhieu.length === 0">
                        <td colspan="8" class="text-center text-muted">Chưa có phiếu xuất nào.</td>
                    </tr>
                </tbody>
            </table>
        </div>

        <XuatKhoChiTiet v-if="showModal" :soPhieu="selectedSoPhieu" @close="showModal = false" />
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
// [QUAN TRỌNG] Dùng api thay axios thường
import api from '@/utils/axios'; 
import XuatKhoChiTiet from './XuatKhoChiTiet.vue';

// CẤU HÌNH API: Khớp với KhoController -> /api/kho/xuat
const API_URL = '/kho/xuat'; 

const danhSachPhieu = ref([]);
const loading = ref(false);
const showModal = ref(false);
const selectedSoPhieu = ref(null);

const layDanhSach = async () => {
    loading.value = true;
    try {
        const res = await api.get(API_URL);
        danhSachPhieu.value = res.data;
    } catch (error) {
        console.error(error);
        // Có thể alert lỗi nếu cần
    } finally {
        loading.value = false;
    }
};

const moChiTiet = (soPhieu) => {
    selectedSoPhieu.value = soPhieu;
    showModal.value = true;
};

const huyPhieu = async (soPhieu) => {
    if(!confirm(`CẢNH BÁO: Hủy phiếu ${soPhieu} sẽ hoàn trả toàn bộ máy về trạng thái Tồn Kho (Có thể bán lại). Tiếp tục?`)) return;
    try {
        // DELETE /api/kho/xuat/{soPhieu}
        await api.delete(`${API_URL}/${soPhieu}`);
        alert("Đã hủy phiếu và hoàn trả kho thành công!");
        layDanhSach();
    } catch (error) {
        // Hiển thị lỗi từ backend (VD: Năm đã chốt sổ...)
        const msg = error.response?.data?.message || error.response?.data || error.message;
        alert("Lỗi: " + msg);
    }
};

// [LOGIC MỚI] Kiểm tra xem năm của phiếu có bị khóa không
// Giả định: Các năm trước năm hiện tại đều đã được chốt sổ.
const isYearLocked = (dateInput) => {
    if (!dateInput) return false;
    
    let year = 0;
    // Xử lý cả dạng mảng [yyyy, mm, dd...] và chuỗi ISO
    if (Array.isArray(dateInput)) {
        year = dateInput[0];
    } else {
        year = new Date(dateInput).getFullYear();
    }

    const currentYear = new Date().getFullYear();
    // Nếu năm phiếu < năm hiện tại => Coi như đã chốt sổ => Khóa
    return year < currentYear;
};

const splitSummary = (str) => str ? str.split(', ') : [];

const formatDate = (dateArray) => {
    if (!dateArray) return '';
    if (Array.isArray(dateArray)) {
        const [year, month, day, hour, minute] = dateArray;
        // Thêm số 0 nếu < 10
        const f = (n) => n < 10 ? '0' + n : n;
        return `${f(day)}/${f(month)}/${year} ${hour ? f(hour) + ':' + f(minute) : ''}`;
    }
    return new Date(dateArray).toLocaleString('vi-VN');
};

const formatCurrency = (v) => {
    if(!v) return '0 đ';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);
};

onMounted(() => layDanhSach());
</script>