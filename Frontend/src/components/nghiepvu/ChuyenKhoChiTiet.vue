<template>
    <div class="modal-backdrop show"></div>
    <div class="modal d-block" tabindex="-1">
        <div class="modal-dialog modal-xl">
            <div class="modal-content">
                <div class="modal-header bg-info text-white">
                    <h5 class="modal-title">Chi Tiết Phiếu Chuyển: {{ chiTiet?.soPhieu }}</h5>
                    <button type="button" class="btn-close" @click="$emit('close')"></button>
                </div>
                <div class="modal-body" v-if="chiTiet">
                    <div class="row mb-3 p-3 bg-light border rounded mx-1">
                        <div class="col-md-3"><strong>Ngày:</strong> {{ formatDate(chiTiet.ngayChuyen) }}</div>
                        <div class="col-md-3"><strong>Từ Kho:</strong> <span class="text-danger fw-bold">{{ chiTiet.khoDi?.tenKho }}</span></div>
                        <div class="col-md-3"><strong>Đến Kho:</strong> <span class="text-success fw-bold">{{ chiTiet.khoDen?.tenKho }}</span></div>
                        <div class="col-md-3"><strong>Ghi chú:</strong> {{ chiTiet.ghiChu }}</div>
                    </div>
                    <table class="table table-bordered text-center">
                        <thead class="table-secondary">
                            <tr><th>#</th><th>Mã SP</th><th>Sản Phẩm</th><th>Mã Máy</th><th>Trạng Thái</th></tr>
                        </thead>
                        <tbody>
                            <tr v-for="(item, idx) in chiTiet.danhSachChiTiet" :key="idx">
                                <td>{{ idx + 1 }}</td>
                                <td class="fw-bold text-secondary">{{ item.sanPham?.maSP }}</td>
                                <td class="text-start">{{ item.sanPham?.tenSP }}</td>
                                <td class="text-primary font-monospace">{{ item.mayIn?.maMay }}</td>
                                <td><span class="badge bg-success">Đã chuyển</span></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="modal-footer"><button class="btn btn-secondary" @click="$emit('close')">Đóng</button></div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/utils/axios';
const props = defineProps(['soPhieu']);
const chiTiet = ref(null);
const formatDate = (d) => { if(!d) return ''; if(Array.isArray(d)) return `${d[2]}/${d[1]}/${d[0]} ${d[3]}:${d[4]}`; return new Date(d).toLocaleString('vi-VN'); };

onMounted(async () => {
    try { const res = await api.get(`/kho/chuyen/${props.soPhieu}`); chiTiet.value = res.data; }
    catch(e) { alert("Lỗi tải chi tiết"); }
});
</script>
<style scoped>.modal-backdrop { opacity: 0.5; position: fixed; top: 0; left: 0; width: 100%; height: 100vh; background: #000; z-index: 1040; } .modal { z-index: 1050; display: block; }</style>