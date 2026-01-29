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
                        <div class="col-md-3"><strong>Kho:</strong> {{ chiTiet.tenKho }}</div>
                        <div class="col-md-3"><strong>Tổng SL:</strong> <span class="badge bg-primary">{{ chiTiet.tongSoLuongMay }}</span></div>
                        <div class="col-md-3"><strong>Tổng Tiền:</strong> <span class="text-danger fw-bold">{{ formatCurrency(chiTiet.tongTien) }}</span></div>
                    </div>

                    <h6 class="text-primary px-1 mt-4">Danh sách máy (Trạng thái)</h6>
                    <table class="table table-bordered text-center align-middle shadow-sm">
                        <thead class="table-secondary">
                            <tr>
                                <th>#</th>
                                <th>Sản Phẩm</th>
                                <th>Mã Máy</th>
                                <th>Số Serial</th>
                                <th>Trạng Thái</th> <th>Hành động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(item, index) in chiTiet.danhSachChiTiet" :key="item.maCTPN">
                                <td>{{ index + 1 }}</td>
                                <td class="text-start">{{ item.sanPham?.tenSP }}</td>
                                <td class="text-primary font-monospace">{{ item.mayIn?.maMay }}</td>
                                <td>{{ item.mayIn?.soSeri }}</td>
                                
                                <td>
                                    <span v-if="item.mayIn?.trangThai === 1" class="badge bg-success">Tồn Kho</span>
                                    <span v-else-if="item.mayIn?.trangThai === 2" class="badge bg-danger">Đã Xuất Bán</span>
                                    <span v-else class="badge bg-secondary">Khác</span>
                                </td>
                                
                                <td>
                                    <button v-if="item.mayIn?.trangThai === 1" 
                                            class="btn btn-sm btn-outline-danger" 
                                            @click="xoaDong(item.maCTPN)" 
                                            title="Xóa máy này">
                                        <i class="fas fa-trash-alt"></i>
                                    </button>
                                    <span v-else class="text-muted small">🔒 Đã khóa</span>
                                </td>
                            </tr>
                        </tbody>
                    </table>

                    <hr class="my-4">
                    <div class="card border-success shadow-sm">
                        <div class="card-header bg-success text-white d-flex align-items-center py-2">
                            <i class="fas fa-plus-circle me-2"></i> 
                            <strong>Bổ sung máy vào phiếu này</strong>
                        </div>
                        <div class="card-body bg-light">
                            <div class="row g-3 align-items-end">
                                <div class="col-md-5">
                                    <label class="form-label small mb-1">Chọn Sản Phẩm</label>
                                    <select class="form-select" v-model="newItem.maSP">
                                        <option value="" disabled>-- Chọn SP --</option>
                                        <option v-for="sp in listSanPham" :key="sp.maSP" :value="sp.maSP">
                                            {{ sp.tenSP }} ({{ sp.maSP }})
                                        </option>
                                    </select>
                                </div>
                                <div class="col-md-3">
                                    <label class="form-label small mb-1">Đơn Giá</label>
                                    <input type="number" class="form-control" v-model="newItem.donGia" min="0">
                                </div>
                                <div class="col-md-2">
                                    <label class="form-label small mb-1">Số Lượng</label>
                                    <input type="number" class="form-control" v-model="newItem.soLuong" min="1">
                                </div>
                                <div class="col-md-2">
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
import axios from 'axios';

const props = defineProps(['soPhieu']);
const emit = defineEmits(['close', 'update-success']);

const chiTiet = ref(null);
const listSanPham = ref([]);
const newItem = ref({ maSP: '', donGia: 0, soLuong: 1 });

const loadChiTiet = async () => {
    try {
        const res = await axios.get(`/api/kho/nhap/${props.soPhieu}`);
        chiTiet.value = res.data;
    } catch (e) { console.error(e); }
};

const loadSanPham = async () => {
    try {
        const res = await axios.get('/api/san-pham');
        listSanPham.value = res.data;
    } catch (e) {}
};

const xoaDong = async (maCTPN) => {
    if(!confirm("Xóa vĩnh viễn máy này?")) return;
    try {
        await axios.delete(`/api/kho/nhap/chi-tiet/${maCTPN}`);
        alert("Đã xóa!");
        loadChiTiet(); 
        emit('update-success');
    } catch (e) { alert("Lỗi: " + (e.response?.data || e.message)); }
};

const themMoiVaoPhieu = async () => {
    try {
        await axios.post(`/api/kho/nhap/${props.soPhieu}/bo-sung`, newItem.value);
        alert("Thành công!");
        newItem.value = { maSP: '', donGia: 0, soLuong: 1 };
        loadChiTiet();
        emit('update-success');
    } catch (e) { alert("Lỗi: " + (e.response?.data || e.message)); }
};

const formatDate = (d) => {
    if(!d) return '';
    if(Array.isArray(d)) return `${d[2]}/${d[1]}/${d[0]}`;
    return new Date(d).toLocaleString('vi-VN');
};
const formatCurrency = (v) => new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v || 0);

onMounted(() => { loadChiTiet(); loadSanPham(); });
</script>

<style scoped>
.modal-backdrop { opacity: 0.5; position: fixed; top: 0; left: 0; width: 100%; height: 100vh; background: #000; z-index: 1040; }
.modal { z-index: 1050; display: block; }
</style>