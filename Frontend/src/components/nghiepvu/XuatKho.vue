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
                        <th>Khách Hàng</th> <th width="30%">Chi tiết (Sản phẩm x SL)</th>
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
                            <button class="btn btn-sm btn-outline-info me-1" @click="moChiTiet(item.soPhieu)">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-danger" @click="huyPhieu(item.soPhieu)">
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
import axios from 'axios';
import XuatKhoChiTiet from './XuatKhoChiTiet.vue';

const danhSachPhieu = ref([]);
const loading = ref(false);
const showModal = ref(false);
const selectedSoPhieu = ref(null);

const layDanhSach = async () => {
    loading.value = true;
    try {
        const res = await axios.get('/api/kho/xuat');
        danhSachPhieu.value = res.data;
    } catch (error) {
        console.error(error);
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
        await axios.delete(`/api/kho/xuat/${soPhieu}`);
        alert("Đã hủy phiếu và hoàn trả kho thành công!");
        layDanhSach();
    } catch (error) {
        alert("Lỗi: " + (error.response?.data || error.message));
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