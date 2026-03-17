<template>
  <div class="content-wrapper">
    <div class="content-header">
      <div class="container-fluid">
        <div class="row mb-2">
          <div class="col-sm-6">
            <h1 class="m-0 text-danger"><i class="fas fa-trash-alt"></i> Xóa Dữ Liệu Tồn Kho</h1>
          </div>
        </div>
      </div>
    </div>

    <section class="content">
      <div class="container-fluid">
        <div class="card shadow-sm border-danger">
          <div class="card-body p-4">
            
            <div class="alert alert-danger small shadow-sm">
              <i class="fas fa-exclamation-triangle fw-bold"></i> Cảnh báo: Hành động này sẽ xóa toàn bộ dữ liệu Danh Mục Tồn Kho của Năm và Kho được chọn. Không thể khôi phục!
            </div>
            
            <div class="row align-items-end mb-4 bg-light p-3 rounded border">
              <div class="col-md-4 mb-2 mb-md-0">
                <label class="form-label fw-bold small text-muted">Năm cần xóa</label>
                <input type="number" class="form-control shadow-none border-secondary" v-model="selectedYear" placeholder="Nhập năm..." />
              </div>

              <div class="col-md-4 mb-2 mb-md-0">
                <label class="form-label fw-bold small text-muted">Kho áp dụng</label>
                <select class="form-control shadow-none border-secondary" v-model="selectedKho" :disabled="!isAdmin">
                  <option value="" disabled>-- Vui lòng chọn kho --</option> 
                  <option v-for="k in listKho" :key="k.maKho" :value="k.maKho">{{ k.tenKho }}</option>
                </select>
              </div>
              

              <div class="col-md-4">
                <button type="button" class="btn btn-danger fw-bold w-100 shadow-sm" @click="deleteTonKho" :disabled="isDeleting">
                  <i class="fas" :class="isDeleting ? 'fa-spinner fa-spin' : 'fa-trash'"></i>
                  {{ isDeleting ? 'Đang xóa dữ liệu...' : 'Thực Hiện Xóa' }}
                </button>
              </div>
            </div>

            <div class="col-sm-6 text-right mb-3 px-0">
              <router-link to="/bao-cao-ton-dau-nam" class="btn btn-sm btn-secondary font-weight-bold">
                <i class="fas fa-arrow-left"></i> Quay Lại Trang Chốt Sổ
              </router-link>
            </div>

          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import api from '@/utils/axios';
import Swal from 'sweetalert2';

const listKho = ref([]);
const selectedKho = ref(''); 
const selectedYear = ref(new Date().getFullYear());
const isAdmin = ref(false);
const isDeleting = ref(false);

const setupPhanQuyen = async () => {
  const role = localStorage.getItem('userRole');
  let userMaKho = localStorage.getItem('maKho') || localStorage.getItem('userMaKho');
  if (!userMaKho) {
    try { userMaKho = JSON.parse(localStorage.getItem('userInfo') || '{}').maKho; } catch (e) {}
  }
  if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
    isAdmin.value = true;
    selectedKho.value = ''; 
  } else {
    isAdmin.value = false;
    selectedKho.value = userMaKho ? parseInt(userMaKho) : ''; 
  }
};

const loadKho = async () => {
  try {
    const res = await api.get('/kho');
    listKho.value = res.data;
  } catch (e) { console.error("Lỗi tải kho:", e); }
};

const deleteTonKho = async () => {
  if (!selectedYear.value || !selectedKho.value) {
    return Swal.fire('Lỗi', 'Vui lòng chọn đầy đủ Năm và Kho cần xóa!', 'error');
  }

  // VALIDATE MẬT KHẨU BẰNG SWEETALERT2
  const { value: password } = await Swal.fire({
    title: 'Xác thực bảo mật',
    html: `Bạn đang chuẩn bị <b>xóa sạch</b> dữ liệu Danh Mục Tồn Kho năm <b>${selectedYear.value}</b>.<br><br><span class="text-danger"><b>Cảnh báo:</b> Hành động này không thể khôi phục!</span><br>Vui lòng nhập mật khẩu tài khoản để xác nhận.`,
    input: 'password',
    inputPlaceholder: 'Nhập mật khẩu của bạn',
    inputAttributes: { autocapitalize: 'off', autocorrect: 'off' },
    icon: 'warning',
    showCancelButton: true,
    confirmButtonColor: '#d33',
    cancelButtonColor: '#6c757d',
    confirmButtonText: 'Đồng ý xóa',
    cancelButtonText: 'Hủy',
    inputValidator: (value) => {
      if (!value) return 'Mật khẩu không được để trống!'
    }
  });

  if (!password) return;

  isDeleting.value = true;
  try {
    let savedUsername = localStorage.getItem('hoTen') || localStorage.getItem('username') || localStorage.getItem('tenTaiKhoan') || '';
    try { const ui = JSON.parse(localStorage.getItem('userInfo') || '{}'); savedUsername = ui.hoTen || ui.username || ui.sub || savedUsername; } catch(e){}

    if (!savedUsername) {
      isDeleting.value = false;
      return Swal.fire('Lỗi phiên đăng nhập', 'Không tìm thấy tên tài khoản. Vui lòng đăng xuất và đăng nhập lại!', 'error');
    }

    const authRes = await api.post('/thong-ke/verify-password', { username: savedUsername, password: password });
    
    if (authRes.data.success) {
      // ĐÃ UPDATE SANG URL MỚI (/api/import/xoa-theo-nam)
      await api.delete('/import/xoa-theo-nam', {
        params: { nam: selectedYear.value, maKho: selectedKho.value }
      });
      Swal.fire('Thành công', 'Đã xóa dữ liệu tồn kho thành công!', 'success');
    } else {
      Swal.fire('Lỗi xác thực', 'Mật khẩu không chính xác. Không thể xóa!', 'error');
    }
  } catch (error) {
    Swal.fire('Lỗi', 'Lỗi khi xóa: ' + (error.response?.data?.message || error.message), 'error');
  } finally {
    isDeleting.value = false;
  }
};

onMounted(async () => {
  await setupPhanQuyen();
  await loadKho();
});
</script>