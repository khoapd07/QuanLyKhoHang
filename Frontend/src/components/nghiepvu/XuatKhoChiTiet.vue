<template>
    <div class="card border-warning">
        <div class="card-header bg-warning text-dark">
            <h5 class="mb-0">Tạo Phiếu Xuất Kho (Bán Hàng)</h5>
        </div>
        <div class="card-body">
            <div class="row mb-4">
                <div class="col-md-4">
                    <label class="form-label">Kho Xuất (*)</label>
                    <select class="form-select" v-model="phieuXuat.maKho">
                        <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label">Khách Hàng (*)</label>
                    <select class="form-select" v-model="phieuXuat.maDonVi">
                         <option v-for="kh in listKhachHang" :key="kh.maDonVi" :value="kh.maDonVi">{{ kh.tenDonVi }}</option>
                    </select>
                </div>
                <div class="col-md-4">
                    <label class="form-label">Ghi Chú</label>
                    <input type="text" class="form-control" v-model="phieuXuat.ghiChu" placeholder="VD: Xuất bán cho dự án A...">
                </div>
            </div>

            <hr>

            <div class="row g-3 bg-light p-3 rounded mb-3 border border-warning">
                <div class="col-md-12">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle"></i> 
                        Lưu ý: Chỉ nhập những Serial đang có trạng thái <strong>"Mới" (1)</strong> trong kho. 
                        Nếu nhập sai, hệ thống sẽ báo lỗi khi Lưu.
                    </div>
                </div>

                <div class="col-md-3">
                    <label class="form-label">Chọn Sản Phẩm</label>
                    <select class="form-select" v-model="currentItem.maSP">
                        <option v-for="sp in listSanPham" :key="sp.maSP" :value="sp.maSP">{{ sp.tenSP }}</option>
                    </select>
                </div>
                <div class="col-md-2">
                    <label class="form-label">Giá Bán</label>
                    <input type="number" class="form-control" v-model="currentItem.donGia">
                </div>
                <div class="col-md-2">
                    <label class="form-label">Số Lượng</label>
                    <input type="number" class="form-control" v-model="currentItem.soLuong">
                </div>
                <div class="col-md-5">
                    <label class="form-label">
                        Quét Mã / Nhập Serial Xuất
                        <small class="text-danger">(Đủ {{ currentItem.soLuong || 0 }} mã)</small>
                    </label>
                    <textarea class="form-control" rows="2" v-model="currentItem.rawSerials" 
                              placeholder="Quét mã vạch hoặc nhập tay..."></textarea>
                </div>
                <div class="col-md-12 text-end">
                    <button class="btn btn-warning" @click="themDongChiTiet">
                        <i class="fas fa-cart-plus"></i> Thêm vào phiếu xuất
                    </button>
                </div>
            </div>

            <table class="table table-bordered mt-3">
                <thead class="table-secondary">
                    <tr>
                        <th>Sản Phẩm</th>
                        <th>Giá Bán</th>
                        <th>SL</th>
                        <th>Serial Xuất</th>
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
                            <span class="badge bg-warning text-dark me-1" v-for="s in item.danhSachSeri" :key="s">{{ s }}</span>
                        </td>
                        <td>{{ formatCurrency(item.donGia * item.soLuong) }}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-danger" @click="xoaDong(index)">X</button>
                        </td>
                    </tr>
                </tbody>
            </table>

            <div class="mt-4 text-center">
                <button class="btn btn-secondary me-3" @click="$router.push('/xuat-kho')">Hủy bỏ</button>
                <button class="btn btn-primary" @click="luuPhieuXuat">
                    <i class="fas fa-check"></i> Hoàn thành Xuất Kho
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

// Dữ liệu danh mục
const listKho = ref([]);
const listDonVi = ref([]);
const listSanPham = ref([]);

// Model
const phieuXuat = ref({
    maKho: null,
    maDonVi: null,
    ghiChu: '',
    chiTietPhieuXuat: [] 
});

const currentItem = ref({
    maSP: '',
    donGia: 0,
    soLuong: 1,
    rawSerials: ''
});

const listChiTietHienThi = ref([]);
const listKhachHang = computed(() => listDonVi.value.filter(dv => dv.loaiDonVi === 2));

// Load data (Giống bên Nhập kho, có thể tách ra file JS chung nếu muốn)
const loadMasterData = async () => {
    try {
        // Mock data tạm thời
        listKho.value = [{maKho: 1, tenKho: 'Kho Tổng'}, {maKho: 2, tenKho: 'Kho CN'}];
        listDonVi.value = [{maDonVi: 'NCC-01', tenDonVi: 'Canon VN', loaiDonVi: 1}, {maDonVi: 'KH-01', tenDonVi: 'Nguyễn Tấn Dũng', loaiDonVi: 2}];
        listSanPham.value = [{maSP: 'SP-2900', tenSP: 'Canon 2900'}, {maSP: 'IP15', tenSP: 'iPhone 15'}];
        // Nếu có API thật thì mở comment dưới:
        /*
        const [k, d, s] = await Promise.all([
             axios.get('/api/kho'), axios.get('/api/don-vi'), axios.get('/api/san-pham')
        ]);
        listKho.value = k.data; listDonVi.value = d.data; listSanPham.value = s.data;
        */
    } catch (e) {
        console.error(e);
    }
};

const themDongChiTiet = () => {
    if (!currentItem.value.maSP) return alert("Chưa chọn sản phẩm");
    
    // Tách và chuẩn hóa Serial
    const serialArray = currentItem.value.rawSerials
        .split(/[\n,]+/).map(s => s.trim()).filter(s => s !== '');

    if (serialArray.length !== parseInt(currentItem.value.soLuong)) {
        return alert(`Sai số lượng: Bạn nhập SL ${currentItem.value.soLuong} nhưng cung cấp ${serialArray.length} mã.`);
    }

    listChiTietHienThi.value.push({
        maSP: currentItem.value.maSP,
        donGia: currentItem.value.donGia,
        soLuong: parseInt(currentItem.value.soLuong),
        danhSachSeri: serialArray
    });

    currentItem.value.rawSerials = '';
    currentItem.value.soLuong = 1;
};

const xoaDong = (index) => listChiTietHienThi.value.splice(index, 1);

const luuPhieuXuat = async () => {
    if (!phieuXuat.value.maKho || !phieuXuat.value.maDonVi) return alert("Thiếu thông tin chung");
    if (listChiTietHienThi.value.length === 0) return alert("Phiếu trống");

    const payload = {
        maKho: phieuXuat.value.maKho,
        maDonVi: phieuXuat.value.maDonVi,
        ghiChu: phieuXuat.value.ghiChu,
        chiTietPhieuXuat: listChiTietHienThi.value.map(item => ({
            maSP: item.maSP,
            soLuong: item.soLuong,
            donGia: item.donGia,
            danhSachSeri: item.danhSachSeri
        }))
    };

    try {
        await axios.post('/api/kho/xuat', payload);
        alert("Xuất kho thành công!");
        router.push('/xuat-kho');
    } catch (error) {
        // Backend sẽ check xem Serial đó có tồn tại và trang thái = 1 không
        // Nếu không, nó ném exception, ta hứng ở đây
        alert("Lỗi xuất kho: " + (error.response?.data?.message || error.response?.data || error.message));
    }
};

const getTenSP = (maSP) => {
    const sp = listSanPham.value.find(s => s.maSP === maSP);
    return sp ? sp.tenSP : maSP;
};
const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

onMounted(() => loadMasterData());
</script>