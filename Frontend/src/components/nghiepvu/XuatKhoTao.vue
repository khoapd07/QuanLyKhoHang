<template>
    <div class="card border-warning">
        <div class="card-header bg-warning text-dark d-flex justify-content-between">
            <h5 class="mb-0">Tạo Phiếu Xuất Kho</h5>
            <button class="btn btn-sm btn-dark" @click="$router.push('/xuat-kho')">
                <i class="fas fa-arrow-left"></i> Quay lại
            </button>
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
                    <input type="text" class="form-control" v-model="phieuXuat.ghiChu" placeholder="Lý do xuất...">
                </div>
            </div>

            <div class="row g-3 bg-light p-3 rounded mb-3 border border-warning">
                <div class="col-12">
                    <div class="alert alert-info py-2 small">
                        <i class="fas fa-info-circle"></i> Nhập đúng số Serial của các máy đang có trạng thái <b>Mới</b> trong kho.
                    </div>
                </div>
                <div class="col-md-3">
                    <label class="form-label">Sản Phẩm</label>
                    <select class="form-select" v-model="currentItem.maSP">
                        <option value="" disabled>-- Chọn SP --</option>
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
                        Nhập Serial Xuất 
                        <span v-if="itemCount > 0" :class="{'text-success': isCountValid, 'text-danger': !isCountValid}">
                            (Đã nhập: {{ itemCount }} / {{ currentItem.soLuong }})
                        </span>
                    </label>
                    <textarea class="form-control" rows="2" v-model="currentItem.rawSerials" 
                              placeholder="Nhập serial, mỗi mã cách nhau bởi dấu phẩy hoặc xuống dòng"></textarea>
                </div>
                <div class="col-md-12 text-end">
                    <button class="btn btn-warning" @click="themDongChiTiet">
                        <i class="fas fa-plus-circle"></i> Thêm vào phiếu
                    </button>
                </div>
            </div>

            <table class="table table-bordered mt-3" v-if="listHienThi.length > 0">
                <thead class="table-secondary">
                    <tr>
                        <th>Sản Phẩm</th>
                        <th>Giá Bán</th>
                        <th>SL</th>
                        <th>Danh Sách Serial</th>
                        <th>Thành Tiền</th>
                        <th>Xóa</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(item, index) in listHienThi" :key="index">
                        <td>{{ getTenSP(item.maSP) }}</td>
                        <td>{{ formatCurrency(item.donGia) }}</td>
                        <td class="fw-bold text-center">{{ item.soLuong }}</td>
                        <td>
                            <span class="badge bg-secondary me-1" v-for="s in item.danhSachSeri" :key="s">{{ s }}</span>
                        </td>
                        <td>{{ formatCurrency(item.donGia * item.soLuong) }}</td>
                        <td>
                            <button class="btn btn-sm btn-outline-danger" @click="listHienThi.splice(index, 1)">X</button>
                        </td>
                    </tr>
                </tbody>
            </table>

            <div class="mt-4 text-center">
                <button class="btn btn-primary px-5" @click="luuPhieuXuat" :disabled="listHienThi.length === 0">
                    <i class="fas fa-save"></i> Hoàn thành Xuất Kho
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
const listKho = ref([]);
const listDonVi = ref([]);
const listSanPham = ref([]);

const phieuXuat = ref({ maKho: null, maDonVi: null, ghiChu: '', chiTietPhieuXuat: [] });
const currentItem = ref({ maSP: '', donGia: 0, soLuong: 1, rawSerials: '' });
const listHienThi = ref([]);

// Filter lấy khách hàng (LoaiDonVi = 2)
const listKhachHang = computed(() => listDonVi.value.filter(dv => dv.loaiDonVi === 2));

// Tính toán số lượng serial đang nhập realtime
const itemCount = computed(() => {
    if(!currentItem.value.rawSerials) return 0;
    return currentItem.value.rawSerials.split(/[\n,]+/).map(s => s.trim()).filter(s => s !== '').length;
});
const isCountValid = computed(() => itemCount.value === parseInt(currentItem.value.soLuong));

const loadMasterData = async () => {
    try {
        const [k, d, s] = await Promise.all([
             axios.get('/api/kho'), axios.get('/api/don-vi'), axios.get('/api/san-pham')
        ]);
        listKho.value = k.data; listDonVi.value = d.data; listSanPham.value = s.data;
    } catch (e) {
        // Fallback data nếu API chưa có
        if(listKho.value.length === 0) listKho.value = [{maKho: 1, tenKho: 'Kho Tổng'}];
        if(listDonVi.value.length === 0) listDonVi.value = [{maDonVi: 'KH-LE', tenDonVi: 'Khách Lẻ', loaiDonVi: 2}];
        if(listSanPham.value.length === 0) listSanPham.value = [{maSP: 'SP-2900', tenSP: 'Canon 2900'}];
    }
};

const themDongChiTiet = () => {
    if (!currentItem.value.maSP) return alert("Chưa chọn sản phẩm");
    if (!isCountValid.value) return alert(`Số lượng nhập (${currentItem.value.soLuong}) không khớp với số mã Serial cung cấp (${itemCount.value}).`);

    const serialArray = currentItem.value.rawSerials.split(/[\n,]+/).map(s => s.trim()).filter(s => s !== '');

    listHienThi.value.push({
        maSP: currentItem.value.maSP,
        donGia: currentItem.value.donGia,
        soLuong: parseInt(currentItem.value.soLuong),
        danhSachSeri: serialArray
    });

    // Reset form item
    currentItem.value.rawSerials = '';
    currentItem.value.soLuong = 1;
};

const luuPhieuXuat = async () => {
    if (!phieuXuat.value.maKho || !phieuXuat.value.maDonVi) return alert("Vui lòng chọn Kho và Khách hàng");
    
    const payload = {
        maKho: phieuXuat.value.maKho,
        maDonVi: phieuXuat.value.maDonVi,
        ghiChu: phieuXuat.value.ghiChu,
        chiTietPhieuXuat: listHienThi.value
    };

    try {
        await axios.post('/api/kho/xuat', payload);
        alert("Xuất kho thành công!");
        router.push('/xuat-kho');
    } catch (error) {
        alert("Lỗi: " + (error.response?.data?.message || error.response?.data || error.message));
    }
};

const getTenSP = (maSP) => listSanPham.value.find(s => s.maSP === maSP)?.tenSP || maSP;
const formatCurrency = (val) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);

onMounted(() => loadMasterData());
</script>