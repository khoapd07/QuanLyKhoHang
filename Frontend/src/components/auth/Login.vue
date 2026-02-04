<template>
  <div class="login-container d-flex justify-content-center align-items-center min-vh-100 bg-light">
    <div class="card shadow-lg border-0" style="width: 400px; border-radius: 12px;">
      <div class="card-body p-5">
        <div class="text-center mb-4">
          <h3 class="text-primary fw-bold text-uppercase">Kho Máy In</h3>
          <p class="text-muted small">Đăng nhập hệ thống quản lý</p>
        </div>

        <form @submit.prevent="handleLogin">
          <div class="mb-3">
            <label class="form-label fw-semibold text-secondary">Tài khoản</label>
            <div class="input-group">
              <span class="input-group-text bg-white text-primary border-end-0"><i class="bi bi-person-fill"></i></span>
              <input 
                v-model="form.tenTaiKhoan" 
                type="text" 
                class="form-control border-start-0 ps-0" 
                placeholder="Nhập tên đăng nhập..." 
                required 
                autofocus
              >
            </div>
          </div>

          <div class="mb-4">
            <label class="form-label fw-semibold text-secondary">Mật khẩu</label>
            <div class="input-group">
              <span class="input-group-text bg-white text-primary border-end-0"><i class="bi bi-lock-fill"></i></span>
              <input 
                v-model="form.password" 
                type="password" 
                class="form-control border-start-0 ps-0" 
                placeholder="Nhập mật khẩu..." 
                required
              >
            </div>
          </div>

          <div v-if="errorMessage" class="alert alert-danger d-flex align-items-center py-2 mb-3 small" role="alert">
            <i class="bi bi-exclamation-triangle-fill me-2"></i>
            <div>{{ errorMessage }}</div>
          </div>

          <button type="submit" class="btn btn-primary w-100 py-2 fw-bold text-uppercase shadow-sm" :disabled="isLoading">
            <span v-if="isLoading" class="spinner-border spinner-border-sm me-2"></span>
            <span v-else>Đăng Nhập</span>
          </button>
        </form>

        <div class="text-center mt-4">
          <small class="text-muted">Phiên bản 1.0 &copy; 2026</small>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
// 1. Import instance axios đã cấu hình
import api from '@/utils/axios'; 

const router = useRouter();

// --- STATE ---
const isLoading = ref(false);
const errorMessage = ref('');

const form = reactive({
  tenTaiKhoan: '',
  password: ''
});

// --- HÀM XỬ LÝ ---
const handleLogin = async () => {
  isLoading.value = true;
  errorMessage.value = '';

  try {
    // 2. Sử dụng api instance thay vì axios thường
    // Lưu ý: Đường dẫn này nối tiếp vào baseURL trong file utils/axios.js
    // Ví dụ: baseURL là 'http://localhost:8080/api' thì url gọi sẽ là 'http://localhost:8080/api/auth/login'
    const response = await api.post('/auth/login', form);
    
    // Một số cấu hình axios interceptor trả về data trực tiếp (bỏ qua .data). 
    // Nếu file utils của bạn trả về response gốc thì dùng dòng dưới:
    const data = response.data; 
    // Nếu utils của bạn đã return response.data thì dùng: const data = response;

    if (data.token) {
        // --- LƯU THÔNG TIN ---
        localStorage.setItem('token', data.token);
        localStorage.setItem('userRole', data.role); 
        localStorage.setItem('hoTen', data.hoTen);
        
        
        localStorage.setItem('userInfo', JSON.stringify({
            username: data.tenTaiKhoan,
            role: data.role,
            hoTen: data.hoTen
        }));

        localStorage.setItem('maKho', data.maKho);

        // 3. Chuyển hướng (Không cần set header thủ công nữa vì utils/axios nên xử lý việc này)
        router.push('/dashboard'); 
    } else {
        errorMessage.value = "Lỗi: Server không trả về Token!";
    }

  } catch (error) {
    console.error("Login Error:", error);
    if (error.response) {
       // Lỗi từ backend (401, 400...)
       errorMessage.value = error.response.data?.message || "Sai tên đăng nhập hoặc mật khẩu!";
    } else {
       errorMessage.value = "Không thể kết nối đến Server!";
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.form-control:focus {
  box-shadow: none;
  border-color: #0d6efd;
}
.input-group-text {
  border-color: #ced4da;
}
.login-container {
    background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}
</style>