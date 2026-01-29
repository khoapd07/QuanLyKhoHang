<template>
    <div class="modal-backdrop show"></div>
    <div class="modal d-block" tabindex="-1">
        <div class="modal-dialog modal-xl modal-dialog-scrollable">
            <div class="modal-content">
                <div class="modal-header bg-dark text-white">
                    <h5 class="modal-title">Chi Tiết Phiếu Xuất: {{ chiTiet?.soPhieu }}</h5>
                    <button type="button" class="btn-close btn-close-white" @click="$emit('close')"></button>
                </div>
                
                <div class="modal-body" v-if="chiTiet">
                    <div class="row mb-3 p-3 bg-light border rounded mx-1">
                        <div class="col-md-3"><strong>Ngày Xuất:</strong> {{ formatDate(chiTiet.ngayXuat) }}</div>
                        
                        <div class="col-md-3"><strong>Khách Hàng:</strong> {{ chiTiet.khachHang?.tenDonVi || '---' }}</div>
                        
                        <div class="col-md-3"><strong>Tổng Tiền:</strong> <span class="text-danger fw-bold">{{ formatCurrency(chiTiet.tongTien) }}</span></div>
                        <div class="col-md-3"><strong>Ghi Chú:</strong> {{ chiTiet.ghiChu }}</div>
                    </div>

                    <h6 class="text-warning px-1 mt-3 bg-dark p-2 rounded">Danh sách máy đã xuất</h6>
                    <table class="table table-bordered text-center align-middle">
                        <thead class="table-secondary">
                            <tr>
                                <th>#</th>
                                <th>Sản Phẩm</th>
                                <th>Số Serial / Mã Máy</th>
                                <th>Giá Bán</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr v-for="(item, index) in chiTiet.danhSachChiTiet" :key="index">
                                <td>{{ index + 1 }}</td>
                                <td class="text-start fw-bold">{{ item.sanPham?.tenSP }}</td>
                                <td class="text-primary font-monospace">
                                    {{ item.mayIn?.soSeri || item.mayIn?.maMay }}
                                </td>
                                <td class="text-end">{{ formatCurrency(item.donGia) }}</td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                
                <div class="modal-body text-center" v-else>
                    <div class="spinner-border text-warning"></div>
                    <p>Đang tải chi tiết...</p>
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
const emit = defineEmits(['close']);
const chiTiet = ref(null);

onMounted(async () => {
    try {
        const res = await axios.get(`/api/kho/xuat/${props.soPhieu}`);
        chiTiet.value = res.data;
    } catch (e) {
        alert("Không tải được chi tiết");
        emit('close');
    }
});

const formatDate = (dateArray) => {
    if (!dateArray) return '';
    if (Array.isArray(dateArray)) return `${dateArray[2]}/${dateArray[1]}/${dateArray[0]} ${dateArray[3]}:${dateArray[4]}`;
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
</style>