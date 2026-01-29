<template>
    <div class="card">
        <div class="card-header bg-success text-white d-flex justify-content-between">
            <h5 class="mb-0">Nhập Hàng Vào Kho (Auto Mã)</h5>
            <button class="btn btn-sm btn-light text-success fw-bold" @click="$router.push('/nhap-kho')">
                <i class="fas fa-arrow-left"></i> Quay lại
            </button>
        </div>
        <div class="card-body">
            <div class="row mb-4">
                <div class="col-md-6">
                    <label class="form-label">Chọn Kho Nhập (*)</label>
                    <select class="form-select" v-model="phieuNhap.maKho">
                        <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                    </select>
                </div>
                <div class="col-md-6">
                    <label class="form-label">Ghi Chú</label>
                    <input type="text" class="form-control" v-model="phieuNhap.ghiChu" placeholder="VD: Nhập hàng đợt 1...">
                </div>
            </div>

            <div class="row g-3 bg-light p-3 rounded mb-3 border">
                <div class="col-md-4">
                    <label class="form-label">Sản Phẩm</label>
                    <select class="form-select" v-model="currentItem.maSP">
                        <option value="" disabled>-- Chọn sản phẩm --</option>
                        <option v-for="sp in listSanPham" :key="sp.maSP" :value="sp.maSP">
                            {{ sp.tenSP }} ({{ sp.maSP }})
                        </option>
                    </select>
                </div>
                <div class="col-md-3">
                    <label class="form-label">Giá Nhập</label>
                    <input type="number" class="form-control" v-model="currentItem.donGia" min="0">
                </div>
                <div class="col-md-3">
                    <label class="form-label">Số Lượng</label>
                    <input type="number" class="form-control" v-model="currentItem.soLuong" min="1">
                </div>
                <div class="col-md-2 d-flex align-items-end">
                    <button class="btn btn-success w-100" @click="themDong">
                        <i class="fas fa-plus"></i> Thêm
                    </button>
                </div>
            </div>

            <table class="table table-bordered mt-3" v-if="listHienThi.length > 0">
                <thead class="table-secondary">
                    <tr>
                        <th>Sản Phẩm</th>
                        <th>Giá Nhập</th>
                        <th>Số Lượng</th>
                        <th>Thành Tiền</th>
                        <th>Xóa</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(item, index) in listHienThi" :key="index">
                        <td>{{ getTenSP(item.maSP) }}</td>
                        <td>{{ formatCurrency(item.donGia) }}</td>
                        <td class="text-center fw-bold">{{ item.soLuong }}</td>
                        <td>{{ formatCurrency(item.donGia * item.soLuong) }}</td>
                        <td>
                            <button class="btn btn-sm btn-danger" @click="listHienThi.splice(index, 1)">X</button>
                        </td>
                    </tr>
                </tbody>
            </table>

            <div class="mt-4 text-center">
                <button class="btn btn-primary px-5" @click="luuPhieu" :disabled="listHienThi.length === 0">
                    <i class="fas fa-save"></i> Lưu Phiếu Nhập
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const listKho = ref([]);
const listSanPham = ref([]);
const phieuNhap = ref({ maKho: null, ghiChu: '' });
const currentItem = ref({ maSP: '', donGia: 0, soLuong: 1 });
const listHienThi = ref([]);

const loadData = async () => {
    try {
        const [k, s] = await Promise.all([
            axios.get('/api/kho'),      // Cần có API này (KhoService)
            axios.get('/api/san-pham')  // Cần có API này (SanPhamService)
        ]);
        listKho.value = k.data;
        listSanPham.value = s.data;
    } catch (e) { 
        console.error("Lỗi load danh mục: ", e);
        // Dữ liệu giả để bạn test giao diện nếu chưa có API danh mục
        if(listKho.value.length === 0) listKho.value = [{maKho: 1, tenKho: 'Kho Tổng'}];
        if(listSanPham.value.length === 0) listSanPham.value = [{maSP: 'SP-2900', tenSP: 'Canon 2900'}, {maSP: 'IP15', tenSP: 'iPhone 15'}];
    }
};

const themDong = () => {
    if(!currentItem.value.maSP) return alert("Vui lòng chọn sản phẩm!");
    if(currentItem.value.soLuong <= 0) return alert("Số lượng phải lớn hơn 0");
    
    // Thêm vào danh sách
    listHienThi.value.push({...currentItem.value});
    
    // Reset form
    currentItem.value.soLuong = 1;
};

const luuPhieu = async () => {
    if(!phieuNhap.value.maKho) return alert("Vui lòng chọn Kho!");
    if(listHienThi.value.length === 0) return alert("Chưa có sản phẩm nào!");
    
    try {
        // Payload gọn nhẹ, không có seri, không có NCC
        await axios.post('/api/kho/nhap', {
            maKho: phieuNhap.value.maKho,
            ghiChu: phieuNhap.value.ghiChu,
            chiTietPhieuNhap: listHienThi.value
        });
        alert("Nhập kho thành công!");
        router.push('/nhap-kho');
    } catch (e) { 
        alert("Lỗi: " + (e.response?.data || e.message)); 
    }
};

const getTenSP = (ma) => listSanPham.value.find(s => s.maSP === ma)?.tenSP || ma;
const formatCurrency = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);

onMounted(() => loadData());
</script>