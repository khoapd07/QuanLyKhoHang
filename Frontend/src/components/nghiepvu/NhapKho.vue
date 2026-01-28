<template>
    <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Quản lý Nhập Kho</h5>
            <router-link to="/nhap-kho/tao-moi" class="btn btn-primary btn-sm">
                <i class="fas fa-plus"></i> Tạo Phiếu Nhập
            </router-link>
        </div>
        <div class="card-body">
            <table class="table table-bordered table-hover">
                <thead class="table-light">
                    <tr>
                        <th>Số Phiếu</th>
                        <th>Ngày Nhập</th>
                        <th>Nhà Cung Cấp</th>
                        <th>Kho Nhập</th>
                        <th>Ghi Chú</th>
                        <th class="text-center">Thao tác</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="item in danhSachPhieu" :key="item.soPhieu">
                        <td>{{ item.soPhieu }}</td>
                        <td>{{ formatDate(item.ngayNhap) }}</td>
                        <td>{{ item.nhaCungCap?.tenDonVi || 'N/A' }}</td>
                        <td>{{ item.khoNhap?.tenKho || 'N/A' }}</td>
                        <td>{{ item.ghiChu }}</td>
                        <td class="text-center">
                            <button class="btn btn-info btn-sm me-2" @click="xemChiTiet(item.soPhieu)">
                                <i class="fas fa-eye"></i>
                            </button>
                            <button class="btn btn-danger btn-sm" @click="xoaPhieu(item.soPhieu)">
                                <i class="fas fa-trash"></i>
                            </button>
                        </td>
                    </tr>
                    <tr v-if="danhSachPhieu.length === 0">
                        <td colspan="6" class="text-center">Chưa có dữ liệu phiếu nhập</td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const danhSachPhieu = ref([]);
const router = useRouter();

// Gọi API lấy danh sách
const layDanhSach = async () => {
    try {
        const res = await axios.get('/api/kho/nhap'); // Config base URL trong axios rồi nhé
        danhSachPhieu.value = res.data;
    } catch (error) {
        console.error(error);
        alert("Lỗi tải dữ liệu!");
    }
};

const xoaPhieu = async (soPhieu) => {
    if(!confirm("Bạn có chắc chắn muốn xóa phiếu nhập này? Hành động này sẽ xóa cả các máy đã nhập!")) return;
    try {
        await axios.delete(`/api/kho/nhap/${soPhieu}`);
        alert("Đã xóa thành công!");
        layDanhSach();
    } catch (error) {
        alert("Không thể xóa: " + (error.response?.data || error.message));
    }
};

const xemChiTiet = (soPhieu) => {
    // Chuyển hướng sang trang chi tiết (bạn cần config route nhận param id)
    // Ở router config: path: 'nhap-kho/chi-tiet/:id'
    // Nhưng tạm thời ta dùng chung form tao-moi và xử lý logic sau
    alert("Chức năng xem chi tiết đang phát triển (Backend trả về JSON chi tiết rồi, chỉ cần bind lên Form)");
};

const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleString('vi-VN');
};

onMounted(() => {
    layDanhSach();
});
</script>