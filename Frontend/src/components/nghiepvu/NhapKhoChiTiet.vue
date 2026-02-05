<template>
    <div class="modal-backdrop show"></div>
    <div class="modal d-block" tabindex="-1">
        <div class="modal-dialog modal-xl modal-dialog-scrollable">
            <div class="modal-content">
                <div class="modal-header bg-info text-white">
                    <h5 class="modal-title">Chi Tiết Phiếu: {{ chiTiet?.soPhieu }}</h5>
                    <button type="button" class="btn-close" @click="$emit('close')"></button>
                </div>
                
                <div class="modal-body" v-if="chiTiet">
                    <div class="row mb-3 p-3 bg-light border rounded mx-1 shadow-sm">
                        <div class="col-md-3"><strong>Ngày Nhập:</strong> {{ formatDate(chiTiet.ngayNhap) }}</div>
                        <div class="col-md-3"><strong>Kho Nhập:</strong> <span class="fw-bold text-primary">{{ chiTiet.khoNhap?.tenKho }}</span></div>
                        <div class="col-md-3"><strong>Tổng SL:</strong> <span class="badge bg-primary">{{ chiTiet.tongSoLuong }}</span></div>
                        <div class="col-md-3"><strong>Tổng Tiền:</strong> <span class="text-danger fw-bold">{{ formatCurrency(chiTiet.tongTien) }}</span></div>
                    </div>

                    <h6 class="text-primary px-1 mt-4">Danh sách máy (Trạng thái hiện tại)</h6>
                    
                    <div class="table-responsive d-none d-md-block">
                        <table class="table table-bordered text-center align-middle shadow-sm">
                            <thead class="table-secondary">
                                <tr>
                                    <th>#</th>
                                    <th>Mã SP</th> 
                                    <th>Sản Phẩm</th>
                                    <th>Mã Máy (System ID)</th>
                                    <th>Số Serial</th>
                                    <th>Trạng Thái</th> 
                                    <th>Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(item, index) in chiTiet.danhSachChiTiet" :key="item.maCTPN">
                                    <td>{{ index + 1 }}</td>
                                    <td class="fw-bold text-secondary">{{ item.sanPham?.maSP || '---' }}</td>
                                    <td class="text-start">{{ item.sanPham?.tenSP }}</td>
                                    <td class="text-primary font-monospace">{{ item.mayIn?.maMay }}</td>
                                    <td>{{ item.mayIn?.soSeri }}</td>
                                    <td>
                                        <span v-if="item.mayIn?.tonKho === false" class="badge bg-secondary">Đã Xuất Bán</span>
                                        <span v-else-if="item.mayIn?.kho?.maKho !== chiTiet.khoNhap?.maKho" class="badge bg-warning text-dark">
                                            <i class="fas fa-shipping-fast"></i> Đã chuyển: {{ item.mayIn?.kho?.tenKho }}
                                        </span>
                                        <span v-else class="badge bg-success">Tồn Tại Kho</span>
                                    </td>
                                    <td>
                                        <button v-if="item.mayIn?.tonKho === true" 
                                                class="btn btn-sm btn-outline-danger" 
                                                @click="xoaDong(item.maCTPN)" 
                                                title="Xóa máy này">
                                            <i class="fas fa-trash-alt"></i>
                                        </button>
                                        <span v-else class="text-muted small"><i class="fas fa-lock"></i> Đã khóa</span>
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="d-md-none">
                        <div v-for="(item, index) in chiTiet.danhSachChiTiet" :key="item.maCTPN" class="card mb-2 border shadow-sm">
                            <div class="card-body p-2">
                                <div class="d-flex justify-content-between align-items-start mb-2">
                                    <div class="text-truncate pe-2">
                                        <span class="badge bg-secondary me-1">{{ index + 1 }}</span>
                                        <span class="fw-bold text-primary">{{ item.sanPham?.maSP }}</span> - 
                                        <span class="fw-bold">{{ item.sanPham?.tenSP }}</span>
                                    </div>
                                    <div>
                                        <button v-if="item.mayIn?.tonKho === true" 
                                                class="btn btn-sm btn-outline-danger py-0 px-2" 
                                                @click="xoaDong(item.maCTPN)">
                                            Xóa
                                        </button>
                                        <i v-else class="fas fa-lock text-muted"></i>
                                    </div>
                                </div>

                                <div class="bg-light p-2 rounded mb-2 small font-monospace d-flex justify-content-between">
                                    <span class="text-primary">{{ item.mayIn?.maMay }}</span>
                                    <span class="text-dark fw-bold">{{ item.mayIn?.soSeri }}</span>
                                </div>

                                <div>
                                    <span v-if="item.mayIn?.tonKho === false" class="badge bg-secondary w-100">Đã Xuất Bán</span>
                                    <span v-else-if="item.mayIn?.kho?.maKho !== chiTiet.khoNhap?.maKho" class="badge bg-warning text-dark w-100 text-wrap text-start">
                                        <i class="fas fa-shipping-fast"></i> Chuyển: {{ item.mayIn?.kho?.tenKho }}
                                    </span>
                                    <span v-else class="badge bg-success w-100">Tồn Tại Kho</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <hr class="my-4">
                    
                    <div class="card border-success shadow-sm">
                        <div class="card-header bg-success text-white d-flex align-items-center py-2">
                            <i class="fas fa-plus-circle me-2"></i> 
                            <strong>Bổ sung máy vào phiếu này</strong>
                        </div>
                        <div class="card-body bg-light">
                            <div class="row g-3 align-items-end">
                                <div class="col-12 col-md-5">
                                    <label class="form-label small mb-1">Chọn Sản Phẩm</label>
                                    <select class="form-select" v-model="newItem.maSP">
                                        <option value="" disabled>-- Chọn SP --</option>
                                        <option v-for="sp in listSanPham" :key="sp.maSP" :value="sp.maSP">
                                            {{ sp.tenSP }} ({{ sp.maSP }})
                                        </option>
                                    </select>
                                </div>
                                <div class="col-6 col-md-3">
                                    <label class="form-label small mb-1">Đơn Giá</label>
                                    <input type="number" class="form-control" v-model="newItem.donGia" min="0">
                                </div>
                                <div class="col-6 col-md-2">
                                    <label class="form-label small mb-1">Số Lượng</label>
                                    <input type="number" class="form-control" v-model="newItem.soLuong" min="1">
                                </div>
                                <div class="col-12 col-md-2">
                                    <button class="btn btn-success w-100 fw-bold" @click="themMoiVaoPhieu">
                                        <i class="fas fa-save"></i> Lưu
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                
                <div class="modal-body text-center" v-else>
                    <div class="spinner-border text-info"></div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" @click="$emit('close')">Đóng</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/utils/axios'; 

const props = defineProps(['soPhieu']);
const emit = defineEmits(['close', 'update-success']);

const chiTiet = ref(null);
const listSanPham = ref([]);
const newItem = ref({ maSP: '', donGia: 0, soLuong: 1 });

// Hàm load chi tiết phiếu
const loadChiTiet = async () => {
    try {
        const res = await api.get(`/kho/nhap/${props.soPhieu}`);
        chiTiet.value = res.data;
    } catch (e) { 
        console.error(e); 
        alert("Lỗi tải chi tiết: " + (e.response?.data?.message || e.message));
    }
};

// Hàm load sản phẩm (Có xử lý an toàn dữ liệu Page/List)
const loadSanPham = async () => {
    try {
        const res = await api.get('/san-pham');
        if (res.data && res.data.content && Array.isArray(res.data.content)) {
            listSanPham.value = res.data.content;
        } else {
            listSanPham.value = res.data;
        }
    } catch (e) { console.error(e); }
};

const xoaDong = async (maCTPN) => {
    if(!confirm("CẢNH BÁO: Xóa dòng này sẽ xóa máy khỏi hệ thống.\nNếu máy đã chuyển kho, lịch sử chuyển cũng bị xóa.\nTiếp tục?")) return;
    try {
        await api.delete(`/kho/nhap/chi-tiet/${maCTPN}`);
        alert("Đã xóa thành công!");
        loadChiTiet(); 
        emit('update-success');
    } catch (e) { 
        const msg = e.response?.data?.message || e.response?.data || e.message;
        alert("Lỗi xóa: " + msg); 
    }
};

const themMoiVaoPhieu = async () => {
    if (!newItem.value.maSP) {
        alert("Vui lòng chọn sản phẩm!");
        return;
    }
    try {
        await api.post(`/kho/nhap/${props.soPhieu}/bo-sung`, newItem.value);
        alert("Thêm mới thành công!");
        newItem.value = { maSP: '', donGia: 0, soLuong: 1 };
        loadChiTiet();
        emit('update-success');
    } catch (e) { 
        const msg = e.response?.data?.message || e.response?.data || e.message;
        alert("Lỗi thêm mới: " + msg); 
    }
};

const formatDate = (d) => {
    if(!d) return '';
    if(Array.isArray(d)) {
        const [year, month, day, hour, minute] = d;
        const f = (n) => n < 10 ? '0' + n : n;
        return `${f(day)}/${f(month)}/${year} ${hour ? f(hour) + ':' + f(minute) : ''}`;
    }
    return new Date(d).toLocaleString('vi-VN');
};

const formatCurrency = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0);

onMounted(() => { 
    loadChiTiet(); 
    loadSanPham(); 
});
</script>

<style scoped>
.modal-backdrop { opacity: 0.5; position: fixed; top: 0; left: 0; width: 100%; height: 100vh; background: #000; z-index: 1040; }
.modal { z-index: 1050; display: block; }
</style>