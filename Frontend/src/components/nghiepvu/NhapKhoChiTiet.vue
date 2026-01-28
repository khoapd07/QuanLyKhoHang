<template>
    <div class="card">
        <div class="card-header bg-primary text-white">
            <h5 class="mb-0">Tạo Phiếu Nhập Kho Mới</h5>
        </div>
        <div class="card-body">
            <div class="row mb-4">
                <div class="col-md-4">
                    <label class="form-label">Kho Nhập (*)</label>
                    <select class="form-select" v-model="phieuNhap.maKho">
                        <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label">Nhà Cung Cấp (*)</label>
                    <select class="form-select" v-model="phieuNhap.maDonVi">
                         <option v-for="ncc in listNCC" :key="ncc.maDonVi" :value="ncc.maDonVi">{{ ncc.tenDonVi }}</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label">Ghi Chú</label>
                    <input type="text" class="form-control" v-model="phieuNhap.ghiChu" placeholder="Nhập lý do nhập...">
                </div>
            </div>

            <hr>

            <div class="row g-3 align-items-end bg-light p-3 rounded mb-3 border">
                <div class="col-md-3">
                    <label class="form-label">Chọn Sản Phẩm</label>
                    <select class="form-select" v-model="currentItem.maSP">
                        <option v-for="sp in listSanPham" :key="sp.maSP" :value="sp.maSP">
                            {{ sp.tenSP }} ({{ sp.maSP }})
                        </option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label">Đơn Giá Nhập</label>
                    <input type="number" class="form-control" v-model="currentItem.donGia">
                </div>
                <div class="col-md-2">
                    <label class="form-label">Số Lượng</label>
                    <input type="number" class="form-control" v-model="currentItem.soLuong">
                </div>
                <div class="col-md-5">
                    <label class="form-label">
                        Nhập Serial/IMEI 
                        <small class="text-danger">(Mỗi dòng 1 mã, phải đủ {{ currentItem.soLuong || 0 }} mã)</small>
                    </label>
                    <textarea class="form-control" rows="2" v-model="currentItem.rawSerials" 
                              placeholder="VD: SN001, SN002..."></textarea>
                </div>
                <div class="col-md-12 text-end">
                    <button class="btn btn-success" @click="themDongChiTiet">
                        <i class="fas fa-plus-circle"></i> Thêm vào phiếu
                    </button>
                </div>
            </div>

            <table class="table table-bordered mt-3">
                <thead class="table-secondary">
                    <tr>
                        <th>Sản Phẩm</th>
                        <th>Đơn Giá</th>
                        <th>Số Lượng</th>
                        <th>Danh Sách Serial</th>
                        <th>Thành Tiền</th>
                        <th>Xóa</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(item, index) in listChiTietHienThi" :key="index">
                        <td>{{ getTenSP(item.maSP) }}</td>
                        <td>{{ formatCurrency(item.donGia) }}</td>
                        <td>{{ item.soLuong }}</td>
                        <td>
                            <span class="badge bg-info text-dark me-1" v-for="s in item.danhSachSeri" :key="s">{{ s }}</span>
                        </td>
                        <td>{{ formatCurrency(item.donGia * item.soLuong) }}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-danger" @click="xoaDong(index)">X</button>
                        </td>
                    </tr>
                </tbody>
            </table>

            <div class="mt-4 text-center">
                <button class="btn btn-secondary me-3" @click="$router.push('/nhap-kho')">Hủy bỏ</button>
                <button class="btn btn-primary" @click="luuPhieuNhap">
                    <i class="fas fa-save"></i> Lưu Phiếu Nhập
                </button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();

// Data nguồn
const listKho = ref([]);
const listDonVi = ref([]);
const listSanPham = ref([]);

// Model dữ liệu
const phieuNhap = ref({
    maKho: null,
    maDonVi: null,
    ghiChu: '',
    chiTietPhieuNhap: [] // Đây là mảng sẽ gửi về server
});

// Model cho dòng đang nhập liệu hiện tại
const currentItem = ref({
    maSP: '',
    donGia: 0,
    soLuong: 1,
    rawSerials: '' // Chuỗi thô người dùng nhập
});

// List tạm để hiển thị ra table
const listChiTietHienThi = ref([]);

// Computed để lọc NCC
const listNCC = computed(() => listDonVi.value.filter(dv => dv.loaiDonVi === 1));

// Load dữ liệu danh mục
const loadMasterData = async () => {
    try {
        const [khoRes, dvRes, spRes] = await Promise.all([
            axios.get('/api/kho'), // API lấy danh sách kho (bạn cần có API này trong KhoService)
            axios.get('/api/don-vi'), // Giả định bạn có API này
            axios.get('/api/san-pham') // Giả định bạn có API này
        ]);
        
        // Mock data nếu chưa có API danh mục (để bạn test giao diện trước)
        // Xóa đoạn này khi có API thật
        if(!khoRes.data) listKho.value = [{maKho: 1, tenKho: 'Kho Tổng'}, {maKho: 2, tenKho: 'Kho CN'}];
        else listKho.value = khoRes.data;

        // Mock đơn vị
        listDonVi.value = [{maDonVi: 'NCC-01', tenDonVi: 'Canon VN', loaiDonVi: 1}, {maDonVi: 'KH-01', tenDonVi: 'Khách lẻ', loaiDonVi: 2}];
        
        // Mock sản phẩm
        listSanPham.value = [{maSP: 'SP-2900', tenSP: 'Canon 2900'}, {maSP: 'IP15', tenSP: 'iPhone 15'}];

    } catch (e) {
        console.log("Dùng dữ liệu mẫu do chưa gọi được API danh mục");
        listKho.value = [{maKho: 1, tenKho: 'Kho Tổng'}];
        listDonVi.value = [{maDonVi: 'NCC-01', tenDonVi: 'Canon VN', loaiDonVi: 1}];
        listSanPham.value = [{maSP: 'SP-2900', tenSP: 'Canon 2900'}, {maSP: 'IP15', tenSP: 'iPhone 15'}];
    }
};

// Hàm thêm dòng vào bảng tạm
const themDongChiTiet = () => {
    if (!currentItem.value.maSP) return alert("Vui lòng chọn sản phẩm");
    if (currentItem.value.soLuong <= 0) return alert("Số lượng phải lớn hơn 0");

    // Xử lý chuỗi Serial: Tách dấu phẩy, dấu xuống dòng, khoảng trắng
    const serialArray = currentItem.value.rawSerials
        .split(/[\n,]+/) // Tách bằng xuống dòng hoặc dấu phẩy
        .map(s => s.trim()) // Xóa khoảng trắng thừa
        .filter(s => s !== ''); // Bỏ chuỗi rỗng

    // Validate: Số lượng serial phải khớp với số lượng nhập
    if (serialArray.length !== parseInt(currentItem.value.soLuong)) {
        return alert(`Lỗi: Bạn nhập số lượng là ${currentItem.value.soLuong} nhưng chỉ cung cấp ${serialArray.length} mã Serial.`);
    }

    // Check trùng serial trong chính đợt nhập này (Client side check)
    const allCurrentSerials = listChiTietHienThi.value.flatMap(item => item.danhSachSeri);
    const duplicates = serialArray.filter(s => allCurrentSerials.includes(s));
    if(duplicates.length > 0) {
        return alert(`Lỗi: Các mã serial sau đã có trong phiếu này rồi: ${duplicates.join(', ')}`);
    }

    // Push vào list hiển thị
    listChiTietHienThi.value.push({
        maSP: currentItem.value.maSP,
        donGia: currentItem.value.donGia,
        soLuong: parseInt(currentItem.value.soLuong),
        danhSachSeri: serialArray,
        ghiChu: ''
    });

    // Reset form nhập
    currentItem.value.rawSerials = '';
    currentItem.value.soLuong = 1;
};

const xoaDong = (index) => {
    listChiTietHienThi.value.splice(index, 1);
};

const luuPhieuNhap = async () => {
    if (!phieuNhap.value.maKho || !phieuNhap.value.maDonVi) {
        return alert("Vui lòng chọn Kho và Nhà cung cấp");
    }
    if (listChiTietHienThi.value.length === 0) {
        return alert("Phiếu chưa có sản phẩm nào");
    }

    // Chuẩn bị payload gửi lên Server (Mapping đúng với PhieuNhapDTO)
    const payload = {
        maKho: phieuNhap.value.maKho,
        maDonVi: phieuNhap.value.maDonVi,
        ghiChu: phieuNhap.value.ghiChu,
        chiTietPhieuNhap: listChiTietHienThi.value.map(item => ({
            maSP: item.maSP,
            soLuong: item.soLuong,
            donGia: item.donGia,
            danhSachSeri: item.danhSachSeri,
            ghiChu: item.ghiChu
        }))
    };

    try {
        await axios.post('/api/kho/nhap', payload);
        alert("Thêm phiếu nhập thành công!");
        router.push('/nhap-kho');
    } catch (error) {
        // Backend trả lỗi (VD: trùng serial)
        alert("Lỗi: " + (error.response?.data?.message || error.response?.data || error.message));
    }
};

// Helper
const getTenSP = (maSP) => {
    const sp = listSanPham.value.find(s => s.maSP === maSP);
    return sp ? sp.tenSP : maSP;
};
const formatCurrency = (value) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
};

onMounted(() => {
    loadMasterData();
});
</script>