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
                <div class="col-md-4">
                    <label class="form-label">Chọn Kho Nhập (*)</label>
                    <select class="form-select" v-model="phieuNhap.maKho" :disabled="!isAdmin">
                        <option :value="null" disabled>-- Chọn Kho --</option>
                        <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                    </select>
                </div>
                
                <div class="col-md-4">
                    <label class="form-label">Nhà Cung Cấp (*)</label>
                    <select class="form-select" v-model="phieuNhap.maDonVi">
                        <option value="" disabled>-- Chọn NCC --</option>
                        <option v-for="ncc in listNhaCungCap" :key="ncc.maDonVi" :value="ncc.maDonVi">
                            {{ ncc.tenDonVi }}
                        </option>
                    </select>
                </div>

                <div class="col-md-4">
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
                <div class="col-md-2">
                    <label class="form-label">Giá Nhập</label>
                    <input type="number" class="form-control" v-model="currentItem.donGia" min="0">
                </div>
                <div class="col-md-2">
                    <label class="form-label">Số Lượng</label>
                    <input type="number" class="form-control" v-model="currentItem.soLuong" min="1">
                </div>
                
                <div class="col-md-2">
                    <label class="form-label">Trạng Thái</label>
                    <select class="form-select" v-model="currentItem.trangThai">
                        <option :value="1">Mới (New)</option>
                        <option :value="2">Like New</option>
                        <option :value="6">Nhập Khẩu</option>
                    </select>
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
                        <th>Trạng Thái</th>
                        <th>Giá Nhập</th>
                        <th>Số Lượng</th>
                        <th>Thành Tiền</th>
                        <th>Xóa</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="(item, index) in listHienThi" :key="index">
                        <td>{{ getTenSP(item.maSP) }}</td>
                        
                        <td>
                            <span v-if="item.trangThai === 1" class="badge bg-success">Mới</span>
                            <span v-else-if="item.trangThai === 2" class="badge bg-info text-dark">Like New</span>
                            <span v-else class="badge bg-secondary">Khác</span>
                        </td>

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
import { ref, onMounted, computed } from 'vue';
import api from '@/utils/axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const listKho = ref([]);
const listSanPham = ref([]);
const listDonVi = ref([]); 

const phieuNhap = ref({ maKho: null, maDonVi: '', ghiChu: '' });
const currentItem = ref({ maSP: '', donGia: 0, soLuong: 1, trangThai: 1 });
const listHienThi = ref([]);

// [SỬA 2]: Thêm biến isAdmin
const isAdmin = ref(false);

const getDataSafe = (response) => {
    if (!response || !response.data) return [];
    if (response.data.content && Array.isArray(response.data.content)) {
        return response.data.content;
    }
    if (Array.isArray(response.data)) {
        return response.data;
    }
    return [];
};

const listNhaCungCap = computed(() => {
    if (!Array.isArray(listDonVi.value)) return [];
    return listDonVi.value.filter(dv => {
        const loai = (dv.loaiDonVi && typeof dv.loaiDonVi === 'object') 
                     ? dv.loaiDonVi.loaiDonVi 
                     : dv.loaiDonVi;
        return loai === 1;
    });
});

const loadData = async () => {
    try {
        const [k, s, d] = await Promise.all([
            api.get('/kho'),      
            api.get('/san-pham'), 
            api.get('/don-vi')    
        ]);
        
        listKho.value = getDataSafe(k);
        listSanPham.value = getDataSafe(s);
        listDonVi.value = getDataSafe(d);

        // [SỬA 3]: Xử lý phân quyền chọn kho
        const role = localStorage.getItem('userRole');
        
        // Lấy maKho từ localStorage
        let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');
        if (!userMaKho) {
             const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
             userMaKho = userInfo.maKho;
        }

        if (role === 'ADMIN') {
             isAdmin.value = true;
             // Admin để mặc định là null để tự chọn
        } else {
             // STAFF
             isAdmin.value = false;
             if (userMaKho) {
                 // Staff bị ép chọn kho của mình
                 phieuNhap.value.maKho = parseInt(userMaKho);
             } else {
                 // Fallback: Nếu lỗi không tìm thấy kho, chọn kho đầu tiên hoặc để trống
                 if (listKho.value.length > 0) {
                     phieuNhap.value.maKho = listKho.value[0].maKho;
                 }
             }
        }

    } catch (e) { 
        console.error("Lỗi load danh mục: ", e);
    }
};

const themDong = () => {
    if(!currentItem.value.maSP) return alert("Vui lòng chọn sản phẩm!");
    if(currentItem.value.soLuong <= 0) return alert("Số lượng phải lớn hơn 0");
    listHienThi.value.push({...currentItem.value});
    currentItem.value.soLuong = 1;
};

const luuPhieu = async () => {
    if(!phieuNhap.value.maKho) return alert("Vui lòng chọn Kho!");
    if(!phieuNhap.value.maDonVi) return alert("Vui lòng chọn Nhà Cung Cấp!");
    if(listHienThi.value.length === 0) return alert("Chưa có sản phẩm nào!");
    
    try {
        await api.post('/kho/nhap', {
            maKho: phieuNhap.value.maKho,
            maDonVi: phieuNhap.value.maDonVi, 
            ghiChu: phieuNhap.value.ghiChu,
            chiTietPhieuNhap: listHienThi.value
        });
        
        alert("Nhập kho thành công!");
        router.push('/nhap-kho');
    } catch (e) { 
        const msg = e.response?.data?.message || e.response?.data || e.message;
        alert("Lỗi: " + msg); 
    }
};

const getTenSP = (ma) => listSanPham.value.find(s => s.maSP === ma)?.tenSP || ma;
const formatCurrency = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);

onMounted(() => loadData());
</script>