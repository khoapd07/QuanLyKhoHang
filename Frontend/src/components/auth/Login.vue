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
import axios from 'axios';
import { useRouter } from 'vue-router';

// --- CẤU HÌNH ---
const API_LOGIN = 'http://localhost:8080/api/auth/login';
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
    // 1. Gọi API Login (Gửi JSON { tenTaiKhoan, password })
    const response = await axios.post(API_LOGIN, form);
    
    // 2. Lấy dữ liệu trả về (Token & User info)
    // Lưu ý: Backend trả về Map<String, Object> nên sẽ có các key như 'token', 'username', 'role'...
    const data = response.data;
    const token = data.token; 
    
    // 3. Lưu vào LocalStorage để dùng cho các request sau
    if (token) {
        localStorage.setItem('token', token);
        // Lưu thêm thông tin user nếu cần hiển thị (ví dụ tên, role)
        localStorage.setItem('user', JSON.stringify({
            username: data.username || form.tenTaiKhoan,
            role: data.role || 'USER'
        }));

        // 4. Chuyển hướng vào trang Dashboard
        router.push('/dashboard');
    } else {
        errorMessage.value = "Phản hồi không chứa Token!";
    }

  } catch (error) {
    console.error("Login Error:", error);
    if (error.response) {
       // Lỗi từ Backend trả về (400, 401...)
       errorMessage.value = error.response.data || "Sai tài khoản hoặc mật khẩu!";
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
</style>