<template>
    <div class="modal-backdrop show"></div>
    <div class="modal d-block" tabindex="-1">
        <div class="modal-dialog modal-xl modal-dialog-scrollable modal-fullscreen-sm-down">
            <div class="modal-content">
                <div class="modal-header bg-dark text-white p-2">
                    <h6 class="modal-title mb-0" style="font-size: 14px;"><i class="fas fa-info-circle me-1"></i> {{ chiTiet?.soPhieu }}</h6>
                    <button type="button" class="btn-close btn-close-white small" @click="$emit('close')"></button>
                </div>
                
                <div class="modal-body p-2" v-if="chiTiet">
                    <div class="bg-light border rounded p-2 mb-3 small" style="font-size: 12px;">
                        <div class="row g-1">
                            <div class="col-6 col-md-3">📅 Ngày: <strong>{{ formatDate(chiTiet.ngayXuat) }}</strong></div>
                            <div class="col-6 col-md-3 text-truncate">👤 Khách: <strong>{{ chiTiet.khachHang?.tenDonVi }}</strong></div>
                            <div class="col-6 col-md-3">💰 Tổng: <strong class="text-danger">{{ formatCurrency(chiTiet.tongTien) }}</strong></div>
                            <div class="col-6 col-md-3 text-truncate">📝 Note: {{ chiTiet.ghiChu }}</div>
                        </div>
                    </div>

                    <h6 class="text-warning small fw-bold bg-dark p-1 rounded ps-2 mb-2">DANH SÁCH MÁY ĐÃ XUẤT</h6>
                    
                    <div class="table-responsive d-none d-md-block" style="max-height: 400px;">
                        <table class="table table-bordered text-center align-middle mb-0 small">
                            <thead class="table-secondary sticky-top">
                                <tr>
                                    <th width="40px">#</th>
                                    <th>Sản Phẩm</th>
                                    <th>Số Serial / Mã Máy</th>
                                    <th>Giá Bán</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr v-for="(item, index) in chiTiet.danhSachChiTiet" :key="index">
                                    <td>{{ index + 1 }}</td>
                                    <td class="text-start fw-bold">{{ item.sanPham?.tenSP }}</td>
                                    <td class="text-primary font-monospace">{{ item.mayIn?.soSeri || item.mayIn?.maMay }}</td>
                                    <td class="text-end">{{ formatCurrency(item.donGia) }}</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>

                    <div class="d-md-none border rounded px-2" style="max-height: 60vh; overflow-y: auto;">
                        <div v-for="(item, index) in chiTiet.danhSachChiTiet" :key="index" class="border-bottom py-2">
                            <div class="d-flex justify-content-between align-items-start mb-1">
                                <div>
                                    <span class="badge bg-secondary me-1" style="font-size: 10px;">{{ index + 1 }}</span>
                                    <span class="fw-bold text-dark" style="font-size: 12px;">{{ item.sanPham?.tenSP }}</span>
                                </div>
                                <span class="text-danger fw-bold" style="font-size: 12px;">{{ formatCurrency(item.donGia) }}</span>
                            </div>
                            <div class="small text-muted ms-1" style="font-size: 11px;">
                                Mã Máy: <span class="text-primary font-monospace">{{ item.mayIn?.soSeri || item.mayIn?.maMay }}</span>
                            </div>
                        </div>
                        <div v-if="chiTiet.danhSachChiTiet.length === 0" class="text-center py-3 small text-muted">Chưa có dữ liệu.</div>
                    </div>
                </div>
                
                <div class="modal-body text-center" v-else>
                    <div class="spinner-border spinner-border-sm text-warning"></div>
                </div>

                <div class="modal-footer p-1 bg-light">
                    <button type="button" class="btn btn-secondary btn-sm w-100" @click="$emit('close')">Đóng</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/utils/axios';

const props = defineProps(['soPhieu']);
const emit = defineEmits(['close']);
const chiTiet = ref(null);

onMounted(async () => {
try {
 const res = await api.get(`/kho/xuat/${props.soPhieu}`);
chiTiet.value = res.data;
} catch (e) {
console.error(e);
 emit('close');
}
});

const formatDate = (dateArray) => {
if (!dateArray) return '';
if (Array.isArray(dateArray)) {
const [year, month, day, hour, minute] = dateArray;
const f = (n) => n < 10 ? '0' + n : n;
return `${f(day)}/${f(month)}/${year} ${hour ? f(hour) + ':' + f(minute) : ''}`;
}
 return new Date(dateArray).toLocaleString('vi-VN');
};

const formatCurrency = (v) => {
if(!v) return '0 đ';
return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);
};
</script>

<style scoped>
.modal-backdrop { opacity: 0.5; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background-color: #000; z-index: 1040; }
.modal { z-index: 1050; display: block; }

@media (max-width: 768px) {
    .modal-fullscreen-sm-down { width: 100vw; max-width: none; height: 100%; margin: 0; }
    .modal-fullscreen-sm-down .modal-content { height: 100%; border: 0; border-radius: 0; }
}
</style>