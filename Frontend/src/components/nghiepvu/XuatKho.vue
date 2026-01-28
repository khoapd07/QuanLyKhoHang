<template>
    <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center bg-warning text-dark">
            <h5 class="mb-0">Quản lý Xuất Kho</h5>
            <router-link to="/xuat-kho/tao-moi" class="btn btn-dark btn-sm">
                <i class="fas fa-plus"></i> Tạo Phiếu Xuất
            </router-link>
        </div>
        <div class="card-body">
            <table class="table table-bordered table-hover">
                <thead class="table-light">
                    <tr>
                        <th>Số Phiếu</th>
                        <th>Ngày Xuất</th>
                        <th>Khách Hàng</th>
                        <th>Kho Xuất</th>
                        <th>Ghi Chú</th>
                        <th class="text-center">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="item in danhSachPhieu" :key="item.soPhieu">
                        <td>{{ item.soPhieu }}</td>
                        <td>{{ formatDate(item.ngayXuat) }}</td>
                        <td>{{ item.khachHang?.tenDonVi || 'N/A' }}</td>
                        <td>{{ item.khoXuat?.tenKho || 'N/A' }}</td>
                        <td>{{ item.ghiChu }}</td>
                        <td class="text-center">
                            <button class="btn btn-danger btn-sm" @click="huyPhieuXuat(item.soPhieu)">
                                <i class="fas fa-undo"></i> Hủy & Trả hàng
                            </button>
                        </td>
                    </tr>
                    <tr v-if="danhSachPhieu.length === 0">
                        <td colspan="6" class="text-center">Chưa có dữ liệu phiếu xuất</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';

const danhSachPhieu = ref([]);

const layDanhSach = async () => {
    try {
        const res = await axios.get('/api/kho/xuat');
        danhSachPhieu.value = res.data;
    } catch (error) {
        alert("Lỗi tải dữ liệu xuất kho");
    }
};

const huyPhieuXuat = async (soPhieu) => {
    if(!confirm(`Cảnh báo: Hủy phiếu ${soPhieu} sẽ trả lại toàn bộ máy trong phiếu này về trạng thái Tồn Kho. Tiếp tục?`)) return;
    try {
        await axios.delete(`/api/kho/xuat/${soPhieu}`);
        alert("Đã hủy phiếu và hoàn trả kho thành công!");
        layDanhSach();
    } catch (error) {
        alert("Lỗi: " + (error.response?.data || error.message));
    }
};

const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleString('vi-VN');
};

onMounted(() => {
    layDanhSach();
});
</script>